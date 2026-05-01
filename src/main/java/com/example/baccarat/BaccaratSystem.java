package com.example.baccarat;

import javafx.beans.property.*;

import java.io.*;
import java.util.Stack;

public class BaccaratSystem {

    // --- Inner StageData class ---
    public static class StageData implements Serializable {
        public double[] sequence;
        public double bank;
        public double recoveryGoal;
        public int currentIndex;
        public double recovered;

        public StageData(double[] seq, double b, double goal) {
            sequence = seq.clone();
            bank = b;
            recoveryGoal = goal;
            currentIndex = 0;
            recovered = 0;
        }

        public double getCurrentBet() {
            if (currentIndex >= sequence.length) return sequence[sequence.length - 1];
            return sequence[currentIndex];
        }

        public void nextBet() { currentIndex = Math.min(currentIndex + 1, sequence.length - 1); }
        public void prevBet() { currentIndex = Math.max(currentIndex - 1, 0); }
        public void reset() { currentIndex = 0; recovered = 0; }
        public void resetIndex() { currentIndex = 0; }
        public void resetRecoveryGoal() { recoveryGoal = 0.00; }
    }

    // --- Bet type ---
    public enum BetType { PLAYER, BANKER }
    private BetType betType = BetType.PLAYER;

    // --- JavaFX Properties ---
    private final IntegerProperty currentStage = new SimpleIntegerProperty(1);
    private final DoubleProperty nextBet = new SimpleDoubleProperty(1.0);
    private final DoubleProperty recoveredThisStage = new SimpleDoubleProperty(0.0);
    private final DoubleProperty profit = new SimpleDoubleProperty(0.0);
    private final DoubleProperty bank = new SimpleDoubleProperty(65.0);
    private final DoubleProperty baseUnit = new SimpleDoubleProperty(1.0);
    private final StringProperty statusMessage = new SimpleStringProperty("Kevin's 1000-1 Plan");

    // --- System state ---
    private StageData[] stages;
    private int stageIndex = 0;
    private int totalWins = 0;
    private int totalLosses = 0;
    private int[] winsPerStage = new int[3];

    // --- History for undo ---
    private final Stack<SystemSnapshot> history = new Stack<>();

    private int currentWinStreak = 0;
    private int currentLoseStreak = 0;

    // --- Double-up plan state ---
    private boolean useDoubleUpPlan = false;       // true when the double-up staking plan is loaded
    private boolean waitingDoubleAttempt = false;  // true when we've won a base bet and are awaiting the doubled follow-up bet
    private double doubleBaseUnits = 0.0;          // stores the base units that will be doubled for the follow-up bet

    // --- Extended stats ---
    private int firstBetWins = 0, firstBetLosses = 0;
    private int secondBetWins = 0, secondBetLosses = 0;
    private int[] stageWins = new int[3];
    private int[] stageLosses = new int[3];
    private int maxWinStreak = 0;
    private int maxLoseStreak = 0;

    private boolean systemLocked = false;
    // --- Constructor (Default = 1000-1 Plan) ---
    public BaccaratSystem() {
        load500to1Plan();
    }

    // --- 1000-1 Plan ---
    public void load1000to1Plan() {
        useDoubleUpPlan = false;
        stages = new StageData[]{
                new StageData(new double[]{1, 2, 4, 8}, 15, 0),
                new StageData(new double[]{4, 8, 10}, 22, 15),
                new StageData(new double[]{6, 10, 12}, 28, 22)
        };
        bank.set(65);  // Example bank size for 1000–1 plan
        statusMessage.set("1000-1");
        resetStats();
        updateUIState();
    }

    // --- 500–1 plan ---
    public void load500to1Plan() {
        useDoubleUpPlan = false;
        stages = new StageData[]{
                new StageData(new double[]{1, 2, 4}, 7, 0),
                new StageData(new double[]{2, 4, 6}, 12, 7),
                new StageData(new double[]{4, 6, 10}, 20, 12)
        };
        bank.set(39);
        statusMessage.set(" 500-1");
        resetStats();
        updateUIState();
    }

    // --- Double-up staking plan ---
    public void loadDoubleUpPlan() {
        // sequences as requested: stage1 = 1,1,1 ; stage2 = 2,2,3 ; stage3 = 4,5,7
        useDoubleUpPlan = true;
        stages = new StageData[]{
                new StageData(new double[]{1, 1, 1}, 26, 0),
                new StageData(new double[]{2, 2, 3}, 26, 0),
                new StageData(new double[]{4, 5, 7}, 26, 0)
        };
        baseUnit.set(1.0);
        bank.set(26);
        stageIndex = 0;
        waitingDoubleAttempt = false;
        doubleBaseUnits = 0.0;
        statusMessage.set("Double-Up ");
        resetStats();
        updateUIState();
    }

    private void resetStats() {
        stageIndex = 0;
        totalWins = 0;
        totalLosses = 0;
        winsPerStage = new int[3];
        profit.set(0);
        currentWinStreak = 0;
        currentLoseStreak = 0;
        waitingDoubleAttempt = false;
        doubleBaseUnits = 0.0;
        systemLocked = false;
    }
    public int shouldMoveShoe() {
        // Trigger on a win
        if (currentWinStreak >= 1) {
            return 1;
        }

        // Trigger on every 2 consecutive losses
        if (currentLoseStreak > 0 && currentLoseStreak % 2 == 0) {
            return 1;
        }
        if(currentLoseStreak == 0 && currentWinStreak ==0){
            return 0;
        }
        return 2;
        // 0 = not played, 1 = move shoe, 2 stay shoe
    }


    public void setBetType(BetType type) { this.betType = type; }

    // --- Record win/loss ---
    public void recordWin() {
        if (systemLocked) return;
        saveSnapshot();

        StageData stage = stages[stageIndex];

        // determine bet units used (consider if we're awaiting a doubled attempt)
        double betUnits;
        if (useDoubleUpPlan && waitingDoubleAttempt) {
            // doubled attempt should be 2 * doubleBaseUnits
            betUnits = doubleBaseUnits * 2.0;
        } else {
            betUnits = stage.getCurrentBet();
        }

        double betMoney = betUnits * baseUnit.get();
        if (betType == BetType.BANKER) betMoney *= 0.95;

        // common stats
        totalWins++;
        currentWinStreak++;

        maxWinStreak = Math.max(maxWinStreak, currentWinStreak);

        winsPerStage[stageIndex]++;
        stageWins[stageIndex]++;

        // track specific bet wins only when it's a sequence-indexed bet (not the doubled follow-up)
        if (!waitingDoubleAttempt) {
            if (stage.currentIndex == 0) firstBetWins++;
            else if (stage.currentIndex == 1) secondBetWins++;
        }

        // apply profit/bank changes
        profit.set(profit.get() + betMoney);
        bank.set(bank.get() + betMoney);

        // recovery tracking
        stage.recovered += betMoney;
        if (stageIndex == 0) stage.recovered = 0;

        statusMessage.set(String.format("WIN! +%.2f", betMoney));

        if (useDoubleUpPlan) {
            // Double-up plan behaviour
            if (waitingDoubleAttempt) {
                // This was the doubled follow-up and it WON -> full reset to Stage 1 (as requested)
                waitingDoubleAttempt = false;
                doubleBaseUnits = 0.0;
                stageIndex = 0;
                for (StageData s : stages) s.resetIndex();
                statusMessage.set("Double-up WON. \nReset to Stage 1");
                currentLoseStreak = 0;
            } else {
                // Regular sequence bet WON -> initiate the doubled follow-up attempt
                waitingDoubleAttempt = true;
                doubleBaseUnits = stage.getCurrentBet(); // remember base units to double next
                statusMessage.set(String.format("Double up Attempt", doubleBaseUnits * baseUnit.get()));
                // note: do NOT advance the sequence index here; the next bet (double) is separate
            }
            // After win/double-check, we reset index for UI to show base sequence index (unless waitingDouble)
            // keep stage.currentIndex unchanged for doubled attempt (so UI can reflect correct flow)
        } else {
            // Default behaviour: reset index on win
            currentLoseStreak = 0;
            stages[stageIndex].resetIndex();
            checkRecovery();
        }

        updateUIState();
    }


    public void recordLoss() {

        if (systemLocked) return;
        saveSnapshot();

        StageData stage = stages[stageIndex];

        // determine bet units used (consider if we're awaiting a doubled attempt)
        double betUnits;
        if (useDoubleUpPlan && waitingDoubleAttempt) {
            betUnits = doubleBaseUnits * 2.0;
        } else {
            betUnits = stage.getCurrentBet();
        }

        double betMoney = betUnits * baseUnit.get();

        totalLosses++;
        currentLoseStreak++;
        currentWinStreak = 0;
        maxLoseStreak = Math.max(maxLoseStreak, currentLoseStreak);

        stageLosses[stageIndex]++;

        // track specific bet losses only when it's a sequence-indexed bet (not the doubled follow-up)
        if (!waitingDoubleAttempt) {
            if (stage.currentIndex == 0) firstBetLosses++;
            else if (stage.currentIndex == 1) secondBetLosses++;
        }

        if(waitingDoubleAttempt) {
            statusMessage.set(String.format("Double-up Lost. \nLOSS!  -%.2f", betMoney ));
        }

        profit.set(profit.get() - betMoney);
        bank.set(bank.get() - betMoney);
        statusMessage.set(String.format("LOSS!  -%.2f",betMoney ));

        stage.recovered -= betMoney;
        if (stage.recovered < 0) stage.recovered = 0;


        if (useDoubleUpPlan) {
            // Double-up plan behaviour on loss
            if (waitingDoubleAttempt) {
                // The doubled follow-up lost -> cancel waitingDoubleAttempt and advance in sequence
                waitingDoubleAttempt = false;
                doubleBaseUnits = 0.0;

                // advance to next bet in sequence (because doubled follow-up failed)
                if (stage.currentIndex >= stage.sequence.length - 1) {
                    nextStage();
                } else {
                    stage.nextBet();
                }
            } else {
                // Not a doubled attempt: regular sequence loss -> advance to next bet in sequence
                if (stage.currentIndex >= stage.sequence.length - 1) {
                    nextStage();
                } else {
                    stage.nextBet();
                }
            }
        } else {
            // Default behaviour
            if (stage.currentIndex >= stage.sequence.length - 1) {
                nextStage();

            } else {
                stage.nextBet();
            }
        }

        updateUIState();
    }


    // --- Undo last action ---
    public void undo() {
        if (!history.isEmpty()) {
            SystemSnapshot snapshot = history.pop();
            restoreSnapshot(snapshot);
        }
    }

    private void saveSnapshot() {
        history.push(new SystemSnapshot(this));
    }

    // --- Stage management ---
    private void nextStage() {
        StageData stage = stages[stageIndex];
        double betUnits;
        if (useDoubleUpPlan && waitingDoubleAttempt) {
            betUnits = doubleBaseUnits * 2.0;
        } else {
            betUnits = stage.getCurrentBet();
        }

        double betMoney = betUnits * baseUnit.get();
        if (stageIndex < stages.length - 1) {
            stageIndex++;
            stages[stageIndex].reset();
            statusMessage.set(String.format("LOSS! -%.2f\nMoving to Stage %d", betMoney, stageIndex + 1));
        } else {
            statusMessage.set("All stages \nfinished! \nRecovery pit");
            systemLocked = true;
        }
    }

    private void checkRecovery() {
        StageData stage = stages[stageIndex];
        double goal = stage.recoveryGoal * baseUnit.get();

        if (stageIndex > 0 && stage.recovered >= goal) {
            stageIndex--;
            stages[stageIndex].reset();
            statusMessage.set("Recovery complete!\nStage " + (stageIndex + 1));
        }
    }

    // --- Reset entire system (1000–1 default) ---
    public void resetSystem() {
        resetStats();
    }

    public void saveApp(File file) {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(file))) {
            // Save the contents of each StageData
            StageData[] saveStages = new StageData[stages.length];
            for (int i = 0; i < stages.length; i++) {
                StageData s = stages[i];
                StageData copy = new StageData(s.sequence.clone(), s.bank, s.recoveryGoal);
                copy.currentIndex = s.currentIndex;
                copy.recovered = s.recovered;
                saveStages[i] = copy;
            }

            out.writeObject(saveStages);              // Stages with sequence, bank, recoveryGoal, currentIndex, recovered
            out.writeInt(stageIndex);                 // Current stage index
            out.writeInt(totalWins);                  // Total wins
            out.writeInt(totalLosses);                // Total losses
            out.writeObject(winsPerStage);            // Wins per stage
            out.writeDouble(profit.get());            // Profit
            out.writeDouble(bank.get());              // Bank
            out.writeObject(betType);                 // Bet type (PLAYER/BANKER)

            // --- Persist double-up state ---
            out.writeBoolean(useDoubleUpPlan);
            out.writeBoolean(waitingDoubleAttempt);
            out.writeDouble(doubleBaseUnits);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }




    public void loadApp(File file) {
        if (!file.exists()) return;

        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(file))) {
            StageData[] loadedStages = (StageData[]) in.readObject();
            int loadedStageIndex = in.readInt();
            int loadedTotalWins = in.readInt();
            int loadedTotalLosses = in.readInt();
            int[] loadedWinsPerStage = (int[]) in.readObject();
            double loadedProfit = in.readDouble();
            double loadedBank = in.readDouble();
            BetType loadedBetType = (BetType) in.readObject();

            // Read new double-up state (if present in file)
            boolean loadedUseDoubleUp = false;
            boolean loadedWaitingDouble = false;
            double loadedDoubleBase = 0.0;

            // The file might be from an older version that didn't write these fields.
            // Attempt to read them; if EOF or class mismatch occurs, fallback to defaults.
            try {
                loadedUseDoubleUp = in.readBoolean();
                loadedWaitingDouble = in.readBoolean();
                loadedDoubleBase = in.readDouble();
            } catch (EOFException eof) {
                // older save file format — keep defaults (don't crash)
            }

            // Update existing StageData objects (do NOT replace the array object)
            for (int i = 0; i < stages.length; i++) {
                stages[i].sequence = loadedStages[i].sequence.clone();
                stages[i].bank = loadedStages[i].bank;
                stages[i].recoveryGoal = loadedStages[i].recoveryGoal;
                stages[i].currentIndex = Math.min(loadedStages[i].currentIndex, stages[i].sequence.length - 1);
                stages[i].recovered = loadedStages[i].recovered;
            }

            // Restore system state
            this.stageIndex = Math.min(loadedStageIndex, stages.length - 1);
            this.totalWins = loadedTotalWins;
            this.totalLosses = loadedTotalLosses;
            this.winsPerStage = loadedWinsPerStage.clone();
            this.profit.set(loadedProfit);
            this.bank.set(loadedBank);
            this.betType = loadedBetType;

            // Restore double-up state (or keep defaults)
            this.useDoubleUpPlan = loadedUseDoubleUp;
            this.waitingDoubleAttempt = loadedWaitingDouble;
            this.doubleBaseUnits = loadedDoubleBase;

            // Ensure current stage index is valid and update UI
            if (stageIndex < 0) stageIndex = 0;
            if (stageIndex >= stages.length) stageIndex = stages.length - 1;

            updateUIState();

        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }


    // Static helper to save both systems in one file
    public static void saveBothSettings(File file, BaccaratSystem player, BaccaratSystem banker) {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(file))) {
            // Player
            out.writeDouble(player.baseUnit.get());
            out.writeDouble(player.bank.get());

            // Banker
            out.writeDouble(banker.baseUnit.get());
            out.writeDouble(banker.bank.get());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Static helper to load both systems from one file
    public static void loadBothSettings(File file, BaccaratSystem player, BaccaratSystem banker) {
        if (!file.exists()) return;
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(file))) {
            // Player
            double playerUnit = in.readDouble();
            double playerBank = in.readDouble();
            player.setBaseUnitDirect(playerUnit);
            player.setBankDirect(playerBank);

            // Banker
            double bankerUnit = in.readDouble();
            double bankerBank = in.readDouble();
            banker.setBaseUnitDirect(bankerUnit);
            banker.setBankDirect(bankerBank);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private String ordinalNo(int num) {
        if (num <= 0) {
            return Integer.toString(num);
        }

        int lastTwo = num % 100;
        int last = num % 10;

        String suffix;

        if (lastTwo >= 11 && lastTwo <= 13) {
            suffix = "TH";
        } else {
            switch (last) {
                case 1:  suffix = "ST"; break;
                case 2:  suffix = "ND"; break;
                case 3:  suffix = "RD"; break;
                default: suffix = "TH";
            }
        }

        return num + suffix;
    }



    // --- UI Update ---
    private void updateUIState() {
        StageData stage = stages[stageIndex];
        currentStage.set(stageIndex + 1);

        // If using double-up plan and we're waiting a doubled attempt, show doubled units * baseUnit
        double nextUnits;
        if (useDoubleUpPlan && waitingDoubleAttempt) {
            nextUnits = doubleBaseUnits * 2.0;
        } else {
            nextUnits = stage.getCurrentBet();
        }
        nextBet.set(nextUnits * baseUnit.get());

        double remaining = stage.recoveryGoal * baseUnit.get() - stage.recovered;
        if (remaining < 0) remaining = 0;
        recoveredThisStage.set(remaining);
    }


    // --- Getters ---
    public int getStageIndex() { return stageIndex; }
    public double getNextBet() {
        // This mirrors updateUIState's calculation
        StageData stage = stages[stageIndex];
        if (useDoubleUpPlan && waitingDoubleAttempt) {
            return doubleBaseUnits * 2.0 * baseUnit.get();
        } else {
            return stages[stageIndex].getCurrentBet() * baseUnit.get();
        }
    }
    public double getRecoveredThisStage() { return recoveredThisStage.get(); }
    public double getProfit() { return profit.get(); }
    public double getBank() { return bank.get(); }
    public double getBaseUnit() { return baseUnit.get(); }
    public String getStatusMessage() { return statusMessage.get(); }
    public int getTotalWins() { return totalWins; }
    public int getTotalLosses() { return totalLosses; }
    public int[] getWinsPerStage() { return winsPerStage; }

    // --- Setters for controller ---
    public void setBaseUnitDirect(double unit) { baseUnit.set(unit); }
    public void setBankDirect(double b) { bank.set(b); }

    // --- Undo snapshot ---
    private static class SystemSnapshot implements Serializable {
        final StageData[] stagesSnapshot;
        final int stageIndex;
        final int totalWins;
        final int totalLosses;
        final int[] winsPerStage;
        final double profit;
        final double bank;

        // double-up plan fields to restore waiting state
        final boolean waitingDoubleAttempt;
        final double doubleBaseUnits;
        final boolean useDoubleUpPlan;

        public SystemSnapshot(BaccaratSystem system) {
            stagesSnapshot = new StageData[system.stages.length];
            for (int i = 0; i < system.stages.length; i++) {
                StageData s = system.stages[i];
                StageData copy = new StageData(s.sequence.clone(), s.bank, s.recoveryGoal);
                copy.currentIndex = s.currentIndex;
                copy.recovered = s.recovered;
                stagesSnapshot[i] = copy;
            }
            stageIndex = system.stageIndex;
            totalWins = system.totalWins;
            totalLosses = system.totalLosses;
            winsPerStage = system.winsPerStage.clone();
            profit = system.profit.get();
            bank = system.bank.get();

            waitingDoubleAttempt = system.waitingDoubleAttempt;
            doubleBaseUnits = system.doubleBaseUnits;
            useDoubleUpPlan = system.useDoubleUpPlan;
        }
    }

    private void restoreSnapshot(SystemSnapshot snapshot) {
        for (int i = 0; i < stages.length; i++) stages[i] = snapshot.stagesSnapshot[i];
        stageIndex = snapshot.stageIndex;
        totalWins = snapshot.totalWins;
        totalLosses = snapshot.totalLosses;
        winsPerStage = snapshot.winsPerStage.clone();
        profit.set(snapshot.profit);
        bank.set(snapshot.bank);


        this.waitingDoubleAttempt = snapshot.waitingDoubleAttempt;
        this.doubleBaseUnits = snapshot.doubleBaseUnits;
        this.useDoubleUpPlan = snapshot.useDoubleUpPlan;

        updateUIState();
    }


    // --- Package-private getters for saving ---
    BaccaratSystem.StageData[] getStagesForSave() { return stages; }
    int getStageIndexForSave() { return stageIndex; }
    int getTotalWinsForSave() { return totalWins; }
    int getTotalLossesForSave() { return totalLosses; }
    int[] getWinsPerStageForSave() { return winsPerStage; }
    double getProfitForSave() { return profit.get(); }
    double getBankForSave() { return bank.get(); }
    BaccaratSystem.BetType getBetTypeForSave() { return betType; }

    // --- Package-private setters for loading ---
    void setStagesFromLoad(BaccaratSystem.StageData[] loadedStages) { this.stages = loadedStages; }
    void setStageIndexFromLoad(int idx) { this.stageIndex = idx; updateUIState(); }
    void setTotalWinsFromLoad(int wins) { this.totalWins = wins; }
    void setTotalLossesFromLoad(int losses) { this.totalLosses = losses; }
    void setWinsPerStageFromLoad(int[] arr) { this.winsPerStage = arr; }
    void setProfitFromLoad(double p) { profit.set(p); }
    void setBankFromLoad(double b) { bank.set(b); }
    void setBetTypeFromLoad(BaccaratSystem.BetType t) { this.betType = t; }
    public int getWinStreak() {
        return currentWinStreak;
    }

    public int getLossStreak() {
        return currentLoseStreak;
    }

    public double getFirstBetWinRate() {
        int total = firstBetWins + firstBetLosses;
        return total > 0 ? (firstBetWins * 100.0 / total) : 0.0;
    }

    public double getSecondBetWinRate() {
        int total = secondBetWins + secondBetLosses;
        return total > 0 ? (secondBetWins * 100.0 / total) : 0.0;
    }

    public double getStageWinRate(int stage) {
        if (stage < 0 || stage >= stageWins.length) return 0;
        int total = stageWins[stage] + stageLosses[stage];
        return total > 0 ? (stageWins[stage] * 100.0 / total) : 0.0;
    }

    public int getMaxWinStreak() { return maxWinStreak; }
    public int getMaxLoseStreak() { return maxLoseStreak; }

    // --- Count getters for better averaging ---
    public int getFirstBetCount() {
        return firstBetWins + firstBetLosses;
    }

    public int getSecondBetCount() {
        return secondBetWins + secondBetLosses;
    }

    public int getStageCount(int stage) {
        if (stage < 0 || stage >= stageWins.length) return 0;
        return stageWins[stage] + stageLosses[stage];
    }
    public static void saveControllerSettings(File file, double pointValue, double initialStopLoss  , double initialStopProfit,double stopLoss, double stopProfit) {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(file))) {
            out.writeDouble(pointValue);
            out.writeDouble(initialStopLoss);
            out.writeDouble(initialStopProfit);
            out.writeDouble(stopLoss);
            out.writeDouble(stopProfit);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static double[] loadControllerSettings(File file) {
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(file))) {
            double pv = in.readDouble();
            double isl = in.readDouble();
            double isp = in.readDouble();
            double sl = in.readDouble();
            double sp = in.readDouble();
            return new double[]{pv,isl,isp, sl, sp};
        } catch (Exception e) {
            return null; // fallback to defaults
        }
    }
    public int getCurrentBetIndex() {
        return stages[stageIndex].currentIndex;
    }
    public void setStatusMessage(String message) {statusMessage.set(message);}

    public boolean isSystemLocked() {
        return systemLocked;
    }
    public void setSystemLocked(boolean locked){
        systemLocked = locked;
    }
}



