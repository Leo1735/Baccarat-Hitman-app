package com.example.baccarat;

/** Maintains the dynamically recalculated recovery plan independently of the UI. */
public class RecoveryPit {

    public enum Risk {
        HIGH(6), MEDIUM(10), LOW(14);

        private final int boxes;

        Risk(int boxes) {
            this.boxes = boxes;
        }

        public int getBoxes() {
            return boxes;
        }
    }

    private double totalLoss;
    private Risk risk;
    private int currentBox;
    private double currentProgress;
    private double boxTarget;
    private double recoveredAmount;

    public void start(double loss, Risk risk) {

        this.totalLoss = Math.max(loss, 0.01);
        this.risk = risk;
        this.currentBox = 1;
        this.currentProgress = 0;
        this.recoveredAmount = 0;
        recalculateBoxTarget();
    }

    public void applyWin(double amount) {

        recoveredAmount += Math.max(amount, 0);
        updateBoxProgress();
    }

    public void applyLoss(double amount) {



        totalLoss += Math.max(amount, 0);
        recalculateBoxTarget();
        updateBoxProgress();
    }

    public boolean isComplete() {
        return recoveredAmount >= risk.getBoxes() * boxTarget;
    }

    public double getBoxTarget() {
        return boxTarget;
    }

    public int getCurrentBox() {
        return currentBox;
    }

    public int getTotalBoxes() {
        return risk.getBoxes();
    }

    public double getCurrentProgress() {
        return currentProgress;
    }

    public double getRemainingInBox() {
        return Math.max(0, boxTarget - currentProgress);
    }

    public double getTotalLoss() {
        return totalLoss;
    }

    public double getProgressPercent() {
        return currentProgress / boxTarget;
    }

    public double getTotalRecovered() {
        return recoveredAmount;
    }

    private void recalculateBoxTarget() {


        boxTarget = Math.ceil(totalLoss / risk.getBoxes());
    }

    private void updateBoxProgress() {
        int completedBoxes = (int) Math.floor(recoveredAmount / boxTarget);
        if (completedBoxes >= risk.getBoxes()) {
            currentBox = risk.getBoxes();
            currentProgress = boxTarget;
            return;
        }
        currentBox = completedBoxes + 1;
        currentProgress = recoveredAmount - completedBoxes * boxTarget;
    }
}
