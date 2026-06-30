package com.example.baccarat;

import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.css.PseudoClass;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class HelloController {


    // === Player Tab UI ===
    @FXML private Label  lblPlayerNextBet, lblPlayerRecovered, lblPlayerProfit, lblPlayerBank, lblPlayerUnit, lblPlayerStageDetail, txtPlayerOutput, lblPointsProfit;
    @FXML private Button txtPlayerBetAmount, txtBankerBetAmount;
    // === Banker Tab UI ===
    @FXML private Label lblBankerStage, lblBankerNextBet, lblBankerRecovered, lblBankerProfit, lblBankerBank, lblBankerUnit, txtBankerOutput, lblBankerPointsProfit;


    // === Charts / Stats labels ===
    @FXML private Label lblWinPct, lblLossPct, lblPlayerWinPct, lblPlayerLossPct, lblBankerWinPct, lblBankerLossPct,
            playerWinStreak, playerLossStreak, bankerWinStreak, bankerLossStreak, winStreak, lossStreak;
    @FXML private PieChart pieChart, pieChartPlayer, pieChartBanker;

    // === Settings Pane (unified) ===
    @FXML private Label lblTotalProfit, lblTotalBank, lblTotalProfitBank, lblTotalBankBank;
    @FXML private VBox settingsPane;
    @FXML private Button btnSaveSettings, btnSaveApp;
    @FXML private ChoiceBox<String> planChoice;
    @FXML private TextField txtBaseUnit, txtBank;

    @FXML private Separator sepPlayerProfit, sepPlayerBank, sepBankerProfit, sepBankerBank;

    // === Loading Overlay ===
    @FXML private VBox loadingOverlay;
    @FXML private Label lblFirstBetRate, lblSecondBetRate, lblStage1Rate, lblStage2Rate, lblStage3Rate, lblWinStreaks, lblLossStreaks;

    // Systems
    private BaccaratSystem playerSystem;
    private BaccaratSystem bankerSystem;

    // Tabs / stats pane
    @FXML private TabPane tabPane;
    @FXML private VBox statsPane;

    // Stats labels for breakdown
    @FXML private Label lblFirstBetTotal, lblFirstBetPlayer, lblFirstBetBanker;
    @FXML private Label lblSecondBetTotal, lblSecondBetPlayer, lblSecondBetBanker;
    @FXML private Label lblStage1Total, lblStage1Player, lblStage1Banker;
    @FXML private Label lblStage2Total, lblStage2Player, lblStage2Banker;
    @FXML private Label lblStage3Total, lblStage3Player, lblStage3Banker;
    @FXML private Label lblWinStreakTotal, lblWinStreakPlayer, lblWinStreakBanker;
    @FXML private Label lblLossStreakTotal, lblLossStreakPlayer, lblLossStreakBanker;

    @FXML private Label lblPointValue, lblPointsBank, lblStopLoss, lblStopProfit;
    @FXML private TextField txtPointValue, txtStopLoss, txtStopProfit;

    private double pointValue = 1.0;

    // RUNNING counters (these start from 0 and move as wins/losses occur)
    private double stopLoss = 0.0;
    private double stopProfit = 0.0;

    // BASELINE values (what user sets / what should appear in the "Value" labels)
    private double initialStopLoss = 0.0;
    private double initialStopProfit = 0.0;

    @FXML private Button btnRecoveryPit;
    @FXML private Button btnRecoveryPitBank;
    @FXML private Button btnCompounding;
    @FXML private VBox recoveryPitOverlay;
    @FXML private VBox compoundingOverlay;
    @FXML private VBox exitOverlay;
    @FXML private Button exitSaveBtn;
    @FXML private Button exitNoSaveBtn;
    @FXML private Button exitCancelBtn;

    // Save stop limits before each win/loss so undo can restore (operates on running counters only)
    private double lastStopLoss;
    private double lastStopProfit;

    @FXML
    private Label lblCurrentPlan;

    private boolean systemLockedTriggered = false;

    private boolean bankWasManuallySet = false;

    @FXML private Label lblBankerPointValue, lblBankerPointsBank, lblBankerStopLoss, lblBankerStopProfit, lblStopProfitValue, lblStopLossValue, lblBankerStopLossValue, lblBankerStopProfitValue;

    // Save file locations
    private final File playerFile = new File("baccarat_player.dat");
    private final File bankerFile = new File("baccarat_banker.dat");
    private final File settingsFile = new File("baccarat_settings.dat");
    // === COMPOUNDING SYSTEM ===
    @FXML private TableView<CompoundingDay> compoundingTable;
    @FXML private TableColumn<CompoundingDay, Integer> colDay;
    @FXML private TableColumn<CompoundingDay, Double> colOpening;
    @FXML private TableColumn<CompoundingDay, Double> colProfitLoss;
    @FXML private TableColumn<CompoundingDay, Double> colToGoal;
    @FXML private TableColumn<CompoundingDay, Double> colDailyGoal;

    private double dailyRate = 0.0234; // 2.34%
    @FXML
    private ComboBox<Integer> cmbDays;
    private int compoundingDays = 30;

    @FXML
    private Label lblRecoveryStake;
    @FXML private GridPane recoveryGrid;
    @FXML private ComboBox<RecoveryPit.Risk> cmbRecoveryRisk;
    @FXML private Label lblRecoveryLoss;
    @FXML private Label lblBoxProgress;

    private RecoveryPit recoveryPit = new RecoveryPit();
    private ObservableList<CompoundingDay> compoundingData = FXCollections.observableArrayList();
    private double setBank = 78;
    private double goal = 100;
    @FXML private TextField txtGoal;
    @FXML private Label lblStartBalance;
    @FXML private Label lblCurrentBalance;
    @FXML private Label lblFullTotalProfit;
    // pseudo-class constant for separators
    private static final PseudoClass POSITIVE = PseudoClass.getPseudoClass("positive");
    public enum TrackingMode {
        SEPARATE,
        COMBINED
    }
    private TrackingMode trackingMode = TrackingMode.SEPARATE;
    private boolean showCompoundingHelp = false;
    @FXML
    private StackPane compoundingHelpOverlay;


    @FXML
    public void initialize() {

        playerSystem = new BaccaratSystem();
        bankerSystem = new BaccaratSystem();

        planChoice.setItems(FXCollections.observableArrayList(
                "500-1",
                "1000-1",
                "Double Up",
                "500-1 P+B",
                "1000-1 P+B",
                "Double Up P+B"
        ));
        boolean loaded = false;

        buildEmptyCompoundingGrid();

        // =========================
        // LOAD COMPOUNDING DATA
        // =========================
        File compFile = new File("compounding.dat");

        if (compFile.exists()) {
            try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(compFile))) {
                loadCompoundingData(in);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // =========================
        // LOAD SYSTEM STATE
        // =========================
        if (playerFile.exists() && bankerFile.exists()) {

            try (ObjectInputStream in =
                         new ObjectInputStream(new FileInputStream(settingsFile))) {

                pointValue = in.readDouble();
                initialStopLoss = in.readDouble();
                initialStopProfit = in.readDouble();
                stopLoss = in.readDouble();
                stopProfit = in.readDouble();

                String plan = in.readUTF();

                planChoice.setValue(plan);
                applyPlan(plan);

            } catch (Exception e) {
                e.printStackTrace();
            }

            playerSystem.loadApp(playerFile);
            bankerSystem.loadApp(bankerFile);


            settingsFile.delete();
            playerFile.delete();
            bankerFile.delete();
            compFile.delete();

            loaded = true;
        }


        if (!loaded) {
            playerSystem.resetSystem();
            bankerSystem.resetSystem();

            initialStopLoss = 0.0;
            initialStopProfit = 0.0;
            stopLoss = 0.0;
            stopProfit = 0.0;
        }

        boolean firstRun = compoundingData.isEmpty();
        showCompoundingHelp = firstRun;
        // =========================
        if (compoundingData.isEmpty()) {
            startNewDay(); // first ever run → Day 1

        } else {
            startNewDay(); // new session → next day
        }

        playerSystem.setBetType(BaccaratSystem.BetType.PLAYER);
        bankerSystem.setBetType(BaccaratSystem.BetType.BANKER);


        planChoice.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {

            if (newVal == null) return;

            applyPlan(newVal);
        });
        // =========================
        // EXIT HANDLING
        // =========================
        exitSaveBtn.setOnAction(e -> {

            playerSystem.saveApp(playerFile);
            bankerSystem.saveApp(bankerFile);





            try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream("compounding.dat"))) {
                saveCompoundingData(out);
            } catch (Exception f) {
                f.printStackTrace();
            }

            BaccaratSystem.saveControllerSettings(
                    settingsFile,
                    pointValue,
                    initialStopLoss,
                    initialStopProfit,
                    stopLoss,
                    stopProfit,
                    planChoice.getValue()
            );

            Platform.exit();
        });

        exitNoSaveBtn.setOnAction(e -> Platform.exit());

        exitCancelBtn.setOnAction(e -> exitOverlay.setVisible(false));


        if (settingsPane != null) settingsPane.setVisible(false);

        bindPlayerUI();
        bindBankerUI();
        updatePieChart();
        updateTotals();
        updateStats();
        updateStopLabels();
        updatePointStatsDisplay();

        if (tabPane != null) {
            tabPane.getSelectionModel().selectedItemProperty()
                    .addListener((obs, oldTab, newTab) -> runWithLoading(this::updateUI));
        }

        if (btnRecoveryPit != null) btnRecoveryPit.setVisible(false);

        if (btnCompounding != null) {
            btnCompounding.setOnAction(e -> openCompoundingPlan());
        }

        updateFieldPrompts();

        if (txtGoal != null) {
            txtGoal.textProperty().addListener((obs, oldVal, newVal) -> {
                goal = parseDouble(newVal, goal);
                updateCurrentDay();
                updateCompoundingLabels();
            });
        }
        if (txtGoal != null && goal > 0) {
            txtGoal.setText(String.valueOf((int) goal));
        }
        cmbDays.getItems().addAll(7, 15, 30);
        if (compoundingDays > 0) {
            cmbDays.setValue(compoundingDays);
        } else {
            cmbDays.setValue(15);
        } // default
        cmbDays.setOnAction(e -> {
            compoundingDays = cmbDays.getValue();
            updateCurrentDay();
        });

        cmbRecoveryRisk.getItems().addAll(
                RecoveryPit.Risk.HIGH,
                RecoveryPit.Risk.MEDIUM,
                RecoveryPit.Risk.LOW
        );

        cmbRecoveryRisk.setValue(RecoveryPit.Risk.MEDIUM);

        cmbRecoveryRisk.setOnAction(e -> {
            RecoveryPit.Risk risk = cmbRecoveryRisk.getValue();

            recoveryPit.start(recoveryPit.getTotalLoss(), risk);

            buildRecoveryGrid(risk.getBoxes());
            updateRecoveryUI();
        });
    }
    private boolean isCombinedTrackingPlan(String plan) {
        return plan != null && plan.endsWith("P+B");
    }
    private void applyPlan(String newVal) {

        if (newVal == null) return;

        switch (newVal) {

            case "500-1":
            case "500-1 P+B":
                playerSystem.load500to1Plan();
                bankerSystem.load500to1Plan();
                break;

            case "1000-1":
            case "1000-1 P+B":
                playerSystem.load1000to1Plan();
                bankerSystem.load1000to1Plan();
                break;

            case "Double Up":
            case "Double Up P+B":
                playerSystem.loadDoubleUpPlan();
                bankerSystem.loadDoubleUpPlan();
                break;
        }

        trackingMode =
                isCombinedTrackingPlan(newVal)
                        ? TrackingMode.COMBINED
                        : TrackingMode.SEPARATE;
        if (trackingMode == TrackingMode.COMBINED) {
            playerSystem.setBankDirect(playerSystem.getBank() * 2);
            bankerSystem.setBankDirect(0);
        }
        lblCurrentPlan.setText(newVal);
    }
    @FXML
    private void handlePlayerWin() {
        runWithLoading(() -> {

            if (trackingMode == TrackingMode.COMBINED) {

                playerSystem.recordWin();


            } else {

                playerSystem.recordWin();
            }

            updateStopLimitsFromSystem();
            updateUI();
        });
    }

    @FXML
    private void handlePlayerLoss() {
        runWithLoading(() -> {

            if (trackingMode == TrackingMode.COMBINED) {

                playerSystem.recordLoss();


            } else {

                playerSystem.recordLoss();
            }

            updateStopLimitsFromSystem();
            updateUI();
        });
    }

    @FXML
    private void handleBankerWin() {
        runWithLoading(() -> {

            if (trackingMode == TrackingMode.COMBINED) {

                playerSystem.recordCombinedWin(0.95);


            } else {

                bankerSystem.recordWin();
            }

            updateStopLimitsFromSystem();
            updateUI();
        });
    }

    @FXML
    private void handleBankerLoss() {
        runWithLoading(() -> {

            if (trackingMode == TrackingMode.COMBINED) {

                playerSystem.recordLoss();


            } else {

                bankerSystem.recordLoss();
            }

            updateStopLimitsFromSystem();
            updateUI();
        });
    }

    @FXML
    private void handlePlayerUndo() {
        runWithLoading(() -> {

            if (trackingMode == TrackingMode.COMBINED) {

                playerSystem.undo();


                txtPlayerOutput.setText("");
                txtBankerOutput.setText("");

                playerSystem.setStatusMessage("");
                bankerSystem.setStatusMessage("");

            } else {

                playerSystem.undo();

                txtPlayerOutput.setText("");
                playerSystem.setStatusMessage("");
            }

            updateStopLimitsFromSystem();
            updateUI();
        });
    }

    @FXML
    private void handleBankerUndo() {
        runWithLoading(() -> {

            if (trackingMode == TrackingMode.COMBINED) {

                playerSystem.undo();


                txtPlayerOutput.setText("");
                txtBankerOutput.setText("");

                playerSystem.setStatusMessage("");
                bankerSystem.setStatusMessage("");

            } else {

                bankerSystem.undo();

                txtBankerOutput.setText("");
                bankerSystem.setStatusMessage("");
            }

            updateStopLimitsFromSystem();
            updateUI();
        });
    }

    private void updateStopLimitsFromSystem() {

        double totalProfit = playerSystem.getProfit() + bankerSystem.getProfit();

        // Stop profit shows positive profit only
        stopProfit = Math.max(totalProfit, 0);

        // Stop loss shows negative profit only
        stopLoss = Math.min(totalProfit, 0);

        updateStopLabels();
    }

    // when true the next updateUI() will clear the player/banker output fields
    private boolean suppressOutputRefresh = false;



    /**
     * Restore RUNNING counters (stopLoss/stopProfit) to previously saved running values.
     * Do NOT overwrite the baseline initialStopLoss/initialStopProfit here - they represent what the user set.
     */
    private void restoreStopLimits() {
        stopLoss = lastStopLoss;
        stopProfit = lastStopProfit;

        updateStopLabels();
    }


    @FXML private void handleBankerReset() { runWithLoading(bankerSystem::resetSystem); }
    private void updateStopLimitsAfterResult(double betAmount, boolean win) {
        if (betAmount <= 0) return;

        if (win) {
            if (playerSystem.isSystemLocked()) return;
            if (bankerSystem.isSystemLocked()) return;
            stopProfit += betAmount;  // running profit counter moves upward from 0
            stopLoss   += betAmount;  // running loss counter moves upward toward 0 from negative
        } else {
            if (playerSystem.isSystemLocked()) return;
            if (bankerSystem.isSystemLocked()) return;
            stopProfit -= betAmount;  // running profit counter moves downward toward 0
            stopLoss   -= betAmount;  // running loss counter goes deeper negative
        }

        updateStopLabels();
    }





    private void updateStopLabels() {

        // Running counters (these change as wins/losses occur)
        if (lblStopProfit != null)
            lblStopProfit.setText(String.format("$%.0f", stopProfit));
        if (lblStopLoss != null)
            lblStopLoss.setText(String.format("$%.0f", stopLoss));

        if (lblBankerStopProfit != null)
            lblBankerStopProfit.setText(String.format("$%.0f", stopProfit));
        if (lblBankerStopLoss != null)
            lblBankerStopLoss.setText(String.format("$%.0f", stopLoss));

        // Baseline (original set values shown in the "Value" labels)
        if (lblStopProfitValue != null)
            lblStopProfitValue.setText(String.format("$%.0f", initialStopProfit));
        if (lblStopLossValue != null)
            lblStopLossValue.setText(String.format("$%.0f", initialStopLoss));

        if (lblBankerStopProfitValue != null)
            lblBankerStopProfitValue.setText(String.format("$%.0f", initialStopProfit));
        if (lblBankerStopLossValue != null)
            lblBankerStopLossValue.setText(String.format("$%.0f", initialStopLoss));
    }


    private void updateFieldPrompts() {

        if (txtBaseUnit != null) {
            txtBaseUnit.setPromptText("Stake: " + playerSystem.getBaseUnit());
        }

        if (txtBank != null) {
            double totalBank = playerSystem.getBank() + bankerSystem.getBank();
            txtBank.setPromptText("Bank: " + totalBank);
        }

        if (txtPointValue != null) {
            txtPointValue.setPromptText("Point Value: " + pointValue);
        }

        if (txtStopLoss != null) {
            txtStopLoss.setPromptText("Stop Loss: " + initialStopLoss);
        }

        if (txtStopProfit != null) {
            txtStopProfit.setPromptText("Stop Profit: " + initialStopProfit);
        }
    }
    /**
     * Save current settings to both systems and persist to file.
     * Clears the temporary text fields afterward.
     */
    @FXML
    private void handleSaveSettings() {
        runWithLoading(() -> {

            // Only update fields that have input
            if (txtBaseUnit != null && !txtBaseUnit.getText().trim().isEmpty()) {
                double baseUnit = parseDouble(txtBaseUnit.getText(), playerSystem.getBaseUnit());
                playerSystem.setBaseUnitDirect(baseUnit);
                bankerSystem.setBaseUnitDirect(baseUnit);
            }

            if (txtBank != null && !txtBank.getText().trim().isEmpty()) {

                double totalBank = parseDouble(txtBank.getText(), playerSystem.getBank());
                setBank = totalBank;

                if (trackingMode == TrackingMode.COMBINED) {

                    playerSystem.setBankDirect(totalBank);
                    bankerSystem.setBankDirect(0);

                } else {

                    double half = totalBank / 2.0;

                    playerSystem.setBankDirect(half);
                    bankerSystem.setBankDirect(half);
                }

                bankWasManuallySet = true;
            }

            // Point system settings (if present)
            if (txtPointValue != null && !txtPointValue.getText().trim().isEmpty()) {
                this.pointValue = parseDouble(txtPointValue.getText(), this.pointValue);
            }

            // If the user provides stop-loss/stop-profit, store them as the BASELINE values,
            // and reset the running counters so they start from 0 immediately.
            if (txtStopLoss != null && !txtStopLoss.getText().trim().isEmpty()) {
                this.initialStopLoss = parseDouble(txtStopLoss.getText(), this.initialStopLoss);
                this.stopLoss = 0.0; // reset running counter
            }

            if (txtStopProfit != null && !txtStopProfit.getText().trim().isEmpty()) {
                this.initialStopProfit = parseDouble(txtStopProfit.getText(), this.initialStopProfit);
                this.stopProfit = 0.0; // reset running counter
            }

            // Save controller-level settings (point value and baseline stop values)
            BaccaratSystem.saveControllerSettings(settingsFile, pointValue, initialStopLoss, initialStopProfit, stopLoss, stopProfit, planChoice.getValue());

            // Clear only fields that had input
            if (txtBaseUnit != null && !txtBaseUnit.getText().trim().isEmpty()) txtBaseUnit.clear();
            if (txtBank != null && !txtBank.getText().trim().isEmpty()) txtBank.clear();
            if (txtPointValue != null && !txtPointValue.getText().trim().isEmpty()) txtPointValue.clear();
            if (txtStopLoss != null && !txtStopLoss.getText().trim().isEmpty()) txtStopLoss.clear();
            if (txtStopProfit != null && !txtStopProfit.getText().trim().isEmpty()) txtStopProfit.clear();

            // Update display
            updateStopLabels();
            updateUI();
            updateFieldPrompts();
        });
    }



    /**
     * Apply the values currently typed in the unified settings fields
     * to both player and banker systems.
     */
    @FXML
    private void handleSettingsValuesChange() {
        double baseUnit = txtBaseUnit != null ? parseDouble(txtBaseUnit.getText(), 1.0) : 1.0;
        double bank = txtBank != null ? parseDouble(txtBank.getText(), 65.0) : 65.0;

        playerSystem.setBaseUnitDirect(baseUnit);
        bankerSystem.setBaseUnitDirect(baseUnit);
        playerSystem.setBankDirect(bank);
        bankerSystem.setBankDirect(bank);

        updateUI();
        updatePointStatsDisplay();
    }

    public void showExitOverlay() {

        exitOverlay.toFront();
        exitOverlay.setVisible(true);
    }


    private void updatePointStatsDisplay() {

        if (lblPointValue != null)
            lblPointValue.setText(String.format("💲%.2f", pointValue));

        if (lblPointsBank != null) {
            double totalBank = playerSystem.getBank() + bankerSystem.getBank();
            int pointsBank = (int) Math.floor(totalBank / pointValue);
            lblPointsBank.setText("  (" + String.valueOf(pointsBank) + ")");
        }

        if (lblPointsProfit != null ) {
            double totalProfit = playerSystem.getProfit() + bankerSystem.getProfit();
            int pointsProfit = (int) Math.floor(totalProfit / pointValue);
            lblPointsProfit.setText(String.valueOf(pointsProfit));
            setLabelColor(lblPointsProfit, pointsProfit);
        }

        if (lblBankerPointsProfit != null ) {
            double totalProfit = playerSystem.getProfit() + bankerSystem.getProfit();
            int pointsProfit = (int) Math.floor(totalProfit / pointValue);
            lblBankerPointsProfit.setText(String.valueOf(pointsProfit));
            setLabelColor(lblBankerPointsProfit, pointsProfit);
        }
        if (stopProfit > 0){
            lblStopLoss.setText(("$0"));
            lblBankerStopLoss.setText(("$0"));
            if (lblStopProfit != null)
                setLabelColor(lblStopProfit, stopProfit);
            lblStopProfit.setText(String.format("$%.0f", stopProfit));

            if (lblBankerStopProfit != null)
                lblBankerStopProfit.setText(String.format("$%.0f", stopProfit));
            setLabelColor(lblBankerStopProfit, stopProfit);
            setLabelColor(lblBankerStopLoss, 0);
            setLabelColor(lblStopLoss, 0);
        }else {
            lblStopProfit.setText(("$0"));
            lblBankerStopProfit.setText(("$0"));
            if (lblStopLoss != null)
                lblStopLoss.setText(String.format("$%.0f", stopLoss));
            setLabelColor(lblStopLoss, stopLoss);
            if (lblBankerStopLoss != null)
                setLabelColor(lblBankerStopLoss, stopLoss);
            lblBankerStopLoss.setText(String.format("$%.0f", stopLoss));
            setLabelColor(lblBankerStopProfit, 0);
            setLabelColor(lblStopProfit, 0);
        }
        // Running counters (changing)



        if (lblBankerPointValue != null)
            lblBankerPointValue.setText(String.format("💲%.2f", pointValue));

        if (lblBankerPointsBank != null) {
            double totalBank = playerSystem.getBank() + bankerSystem.getBank();
            int pointsBank = (int) Math.floor(totalBank / pointValue);
            lblBankerPointsBank.setText("  (" + String.valueOf(pointsBank) + ")");
        }


        // Baseline values (VALUE labels)
        if (lblStopLossValue != null)

            lblStopLossValue.setText("Stop Loss  ("+ String.format("$%.0f", initialStopLoss)+ ")");
        if (lblStopProfitValue != null)
            lblStopProfitValue.setText("Stop Profit  ("+String.format("$%.0f", initialStopProfit)+ ")");

        if (lblBankerStopLossValue != null)
            lblBankerStopLossValue.setText("Stop Loss  ("+ String.format("$%.0f", initialStopLoss)+ ")");
        if (lblBankerStopProfitValue != null)
            lblBankerStopProfitValue.setText("Stop Profit  ("+String.format("$%.0f", initialStopProfit)+ ")");

// Check stop conditions (only once)
        if (!systemLockedTriggered) {

            // Stop Profit reached
            if (initialStopProfit > 0 && stopProfit >= initialStopProfit) {
                systemLockedTriggered = true;

                playerSystem.setSystemLocked(true);
                bankerSystem.setSystemLocked(true);
                
                txtPlayerOutput.setText(" Stop Profit reached!\nReset to continue.");
                txtBankerOutput.setText(" Stop Profit reached!\nReset to continue.");


            }

            // Stop Loss reached
            else if (initialStopLoss > 0 && Math.abs(stopLoss) >= initialStopLoss) {
                systemLockedTriggered = true;

                playerSystem.setSystemLocked(true);
                bankerSystem.setSystemLocked(true);

                txtPlayerOutput.setText("️ Stop Loss reached!\nReset to continue.");
                txtBankerOutput.setText("️ Stop Loss reached!\nReset to continue.");

            }
        }

    }



    /**
     * Reset both systems to their default plan/state.
     */
    @FXML
    private void handleResetBoth() {
        runWithLoading(() -> {
            playerSystem.resetSystem();
            bankerSystem.resetSystem();
            resetCompounding();
            applyPlan("500-1");
            updateUI();
        });
    }

    private void hideAllOverlays() {
        if (recoveryPitOverlay != null) recoveryPitOverlay.setVisible(false);
        if (compoundingOverlay != null) compoundingOverlay.setVisible(false);
        if (settingsPane != null) settingsPane.setVisible(false);
        if (statsPane != null) statsPane.setVisible(false);
    }


        private double getRecoveryStake() {
            double baseStake = playerSystem.getBaseUnit();
            return baseStake * 1.5;
        }

    private void buildRecoveryGrid(int totalBoxes) {

        recoveryGrid.getChildren().clear();

        int cols = (int) Math.ceil(Math.sqrt(totalBoxes));

        for (int i = 0; i < totalBoxes; i++) {

            Label box = new Label(String.valueOf(i + 1));

            box.setMinSize(60, 60);
            box.setAlignment(Pos.CENTER);

            box.setStyle("-fx-background-color: #2a2a3a; -fx-text-fill: white;");

            int row = i / cols;
            int col = i % cols;

            recoveryGrid.add(box, col, row);
        }
    }
    @FXML
    private void openRecoveryPit() {
        runWithLoading(() -> {
            if (recoveryPitOverlay == null) return;

            boolean show = !recoveryPitOverlay.isVisible();
            recoveryPitOverlay.setVisible(show);

            if (show) {
                recoveryPitOverlay.toFront();

                double loss = Math.abs(playerSystem.getBank() + bankerSystem.getBank());

                RecoveryPit.Risk risk = cmbRecoveryRisk.getValue();

                recoveryPit.start(loss, risk);

                buildRecoveryGrid(risk.getBoxes());

                updateRecoveryUI();
            }
        });
    }
    private void updateRecoveryUI() {

        lblRecoveryLoss.setText(String.format("Loss: £%.2f", recoveryPit.getTotalLoss()));

        lblBoxProgress.setText(
                "Box " + recoveryPit.getCurrentBox() +
                        " / " + recoveryPit.getTotalBoxes()
        );

        double target = recoveryPit.getBoxTarget();

        for (Node node : recoveryGrid.getChildren()) {

            if (!(node instanceof Label box)) continue;

            Integer row = GridPane.getRowIndex(box);
            Integer col = GridPane.getColumnIndex(box);

            int r = row == null ? 0 : row;
            int c = col == null ? 0 : col;

            int cols = (int) Math.ceil(Math.sqrt(recoveryPit.getTotalBoxes()));
            int index = r * cols + c;

            int boxNumber = index + 1;

            // COMPLETED
            if (boxNumber < recoveryPit.getCurrentBox()) {
                box.setText("✓");
                box.setStyle("-fx-background-color:#2ecc71; -fx-text-fill:black;");
            }

            // CURRENT BOX
            else if (boxNumber == recoveryPit.getCurrentBox()) {

                double progress = recoveryPit.getCurrentProgress();

                box.setText(String.format("%.0f / %.0f", progress, target));

                box.setStyle("-fx-background-color:#f1c40f; -fx-text-fill:black;");
            }

            // FUTURE
            else {
                box.setText(String.format("%.0f", target));
                box.setStyle("-fx-background-color:#2a2a3a; -fx-text-fill:white;");
            }
        }
        double stake = getRecoveryStake();
        lblRecoveryStake.setText(String.format("Stake: £%.2f", stake));
    }
    @FXML
    private void onRecoveryWin() {

        double stake = getRecoveryStake();

        recoveryPit.applyWin(stake);

        updateRecoveryUI();

        if (recoveryPit.isComplete()) {
            playerSystem.setSystemLocked(false);
            bankerSystem.setSystemLocked(false);
            recoveryPitOverlay.setVisible(false);

        }
    }

    @FXML
    private void onRecoveryLoss() {

        double stake = getRecoveryStake();

        recoveryPit.applyLoss(stake);

        updateRecoveryUI();


    }

    private void updateRecoveryPitButton() {

        boolean playerLocked = playerSystem.isSystemLocked();
        boolean bankerLocked = bankerSystem.isSystemLocked();

        if (btnRecoveryPit != null)
            btnRecoveryPit.setVisible(playerLocked);

        if (btnRecoveryPitBank != null)
            btnRecoveryPitBank.setVisible(bankerLocked);

        // 🔥 Hide "to recover" labels when locked
        if (lblPlayerRecovered != null)
            lblPlayerRecovered.setVisible(!playerLocked);

        if (lblBankerRecovered != null)
            lblBankerRecovered.setVisible(!bankerLocked);
    }


    @FXML
    private void openCompoundingPlan() {

        boolean show = !compoundingOverlay.isVisible();

        compoundingOverlay.setVisible(show);

        if (show) {

            compoundingOverlay.toFront();

            if (showCompoundingHelp) {

                compoundingHelpOverlay.setVisible(true);
                compoundingHelpOverlay.toFront();
            }

        } else {

            compoundingHelpOverlay.setVisible(false);
        }
    }

    @FXML
    private void closeCompoundingHelp() {

        compoundingHelpOverlay.setVisible(false);

        showCompoundingHelp = false;
    }
    private void buildEmptyCompoundingGrid() {
        compoundingTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        colDay.setResizable(false);
        colOpening.setResizable(false);
        colProfitLoss.setResizable(false);
        colToGoal.setResizable(false);
        colDay.setCellValueFactory(new PropertyValueFactory<>("day"));
        colOpening.setCellValueFactory(new PropertyValueFactory<>("opening"));
        colProfitLoss.setCellValueFactory(new PropertyValueFactory<>("profitLoss"));
        colToGoal.setCellValueFactory(new PropertyValueFactory<>("toGoal"));
        colDailyGoal.setCellValueFactory(new PropertyValueFactory<>("dailyGoal"));

        colProfitLoss.setCellFactory(col -> new TableCell<CompoundingDay, Double>() {
            @Override
            protected void updateItem(Double value, boolean empty) {
                super.updateItem(value, empty);
                if (empty || value == null) {
                    setText(null);
                } else {
                    setText(String.format("%.2f", value));
                }
            }
        });

        colToGoal.setCellFactory(col -> new TableCell<CompoundingDay, Double>() {
            @Override
            protected void updateItem(Double value, boolean empty) {
                super.updateItem(value, empty);
                if (empty || value == null) {
                    setText(null);
                } else {
                    setText(String.format("%.2f", value));
                }
            }
        });
        colDailyGoal.setCellFactory(col -> new TableCell<CompoundingDay, Double>() {
            @Override
            protected void updateItem(Double value, boolean empty) {
                super.updateItem(value, empty);

                // 🔥 ALWAYS reset first
                setText(null);
                setStyle("");

                if (empty || value == null) {
                    return;
                }

                setText(String.format("%.2f", value));

                CompoundingDay row = getTableView().getItems().get(getIndex());

                double actual = row.getOpening() + row.getProfitLoss();

                if (actual >= value) {
                    setStyle("-fx-text-fill: #2ecc71;");
                }
            }
        });
        compoundingTable.setItems(compoundingData);
    }
    public void updateCurrentDay() {

        if (compoundingData.isEmpty()) return;

        CompoundingDay current = compoundingData.get(compoundingData.size() - 1);

        if (bankWasManuallySet) {
            current.setOpening(setBank);
            bankWasManuallySet = false;
        }
        double start = compoundingData.get(0).getOpening();
        dailyRate = calculateDailyRate(start, goal, compoundingDays);

        double totalBank = playerSystem.getBank() + bankerSystem.getBank();
        double profit = totalBank - current.getOpening();

        current.setProfitLoss(profit);
        current.setToGoal(goal - totalBank);


        double dailyGoal = current.getOpening() * (1 + dailyRate);
        current.setDailyGoal(dailyGoal);

        compoundingTable.refresh();
    }
    public void startNewDay() {

        double totalBank = playerSystem.getBank() + bankerSystem.getBank();

        CompoundingDay newDay = new CompoundingDay(compoundingData.size() + 1, totalBank);

        compoundingData.add(newDay);
    }
    private void completeDay() {

        updateCurrentDay(); // finalize last day

        // Next session starts fresh day
        startNewDay();
    }

    private double calculateDailyRate(double start, double goal, int days) {
        return Math.pow(goal / start, 1.0 / days) - 1;
    }
    private void saveCompoundingData(ObjectOutputStream out) throws IOException {

        out.writeDouble(goal);
        out.writeInt(compoundingDays);
        out.writeObject(new ArrayList<>(compoundingData));
    }
    private void resetCompounding() {

        compoundingData.clear();

        dailyRate = 0.0;

        bankWasManuallySet = false;

        double totalBank = playerSystem.getBank() + bankerSystem.getBank();

        setBank = totalBank;

        // Start again from Day 1
        compoundingData.add(new CompoundingDay(1, totalBank));

        compoundingTable.refresh();

        updateCompoundingLabels();

    }
    private void loadCompoundingData(ObjectInputStream in) throws IOException, ClassNotFoundException {

        goal = in.readDouble();
        compoundingDays = in.readInt();
        List<CompoundingDay> loaded = (List<CompoundingDay>) in.readObject();

        compoundingData.clear();
        compoundingData.addAll(loaded);
    }

    private void updateCompoundingLabels() {

        double totalBank = playerSystem.getBank() + bankerSystem.getBank();

        // Set goal from textbox
        if (txtGoal != null && !txtGoal.getText().isEmpty()) {
            goal = parseDouble(txtGoal.getText(), goal);
        }



        // Start balance = first day opening
        if (!compoundingData.isEmpty()) {
            CompoundingDay startDay = compoundingData.get(0);
            double start = startDay.getOpening();
            lblStartBalance.setText(String.format("Start:\n $%.2f", start));
            setLabelColor(lblStartBalance, 0);
        }

        // Current balance
        lblCurrentBalance.setText(String.format("Current:\n $%.2f", totalBank));
        setLabelColor(lblCurrentBalance, 0);

        // Total profit
        if (!compoundingData.isEmpty()) {
            double start = setBank;
            double profit = totalBank - start;
            lblFullTotalProfit.setText(String.format("Profit:\n $%.2f", profit));
            setLabelColor(lblFullTotalProfit, profit);
        }
    }
    /**
     * Save application-level data (systems) to separate files.
     */
    @FXML
    private void handleSaveApp() {
        runWithLoading(() -> {
            playerSystem.saveApp(playerFile);
            bankerSystem.saveApp(bankerFile);
        });
    }

    @FXML
    private void handleExit() {

        Platform.exit();
    }


    /**
     * Toggle settings pane visibility. When closing, auto-save any values
     * typed into the unified settings fields.
     */
    @FXML
    private void toggleSettings() {
        runWithLoading(() -> {

            if (settingsPane == null) return;
            boolean show = !settingsPane.isVisible();
            settingsPane.setVisible(show);
            if (show) {
                settingsPane.toFront();
            } else {

                handleSaveSettings();

            }
        });
    }

    @FXML
    private void toggleHome() {
        runWithLoading(() -> {

            if (settingsPane != null && settingsPane.isVisible()) {
                // Only save if fields have text

                handleSaveSettings();

                settingsPane.setVisible(false);
            }
            if (statsPane != null) statsPane.setVisible(false);
        });
    }

    @FXML
    private void toggleStats() {
        runWithLoading(() -> {

            if (statsPane == null) return;
            statsPane.setVisible(!statsPane.isVisible());
            if (statsPane.isVisible()) statsPane.toFront();
        });
    }



    // === Utility / UI update methods ===

    private double parseDouble(String text, double fallback) {
        try {
            if (text == null) return fallback;
            return Math.max(Double.parseDouble(text.trim()), 1.0);
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    private void updateUI() {
        bindPlayerUI();
        bindBankerUI();
        updatePieChart();
        updateTotals();
        updateStats();
        updateStopLimitsFromSystem();
        // ensure stop labels updated (both baseline and running)
        updateRecoveryPitButton();
        updateStopLabels();
        updatePointStatsDisplay();
        updateCurrentDay();
        updateCompoundingLabels();
        updateFieldPrompts();
    }

    private void updateTotals() {

        double totalProfit;
        double totalBank;

        if (trackingMode == TrackingMode.COMBINED) {

            totalProfit = playerSystem.getProfit();
            totalBank = playerSystem.getBank();

        } else {

            totalProfit = playerSystem.getProfit() + bankerSystem.getProfit();
            totalBank = playerSystem.getBank() + bankerSystem.getBank();
        }

        lblTotalProfit.setText(String.format("💲%.2f", totalProfit));
        lblTotalProfitBank.setText(String.format("💲%.2f", totalProfit));

        lblTotalBank.setText(String.format("💲%.2f", totalBank));
        lblTotalBankBank.setText(String.format("💲%.2f", totalBank));

        setLabelColor(lblTotalProfit, totalProfit);
        setLabelColor(lblTotalProfitBank, totalProfit);

        setLabelColor(lblTotalBank, totalProfit);
        setLabelColor(lblTotalBankBank, totalProfit);

        updateSeparatorStyle(sepPlayerProfit, playerSystem.getProfit());
        updateSeparatorStyle(sepPlayerBank, playerSystem.getProfit());

        updateSeparatorStyle(
                sepBankerProfit,
                trackingMode == TrackingMode.COMBINED
                        ? playerSystem.getProfit()
                        : bankerSystem.getProfit()
        );

        updateSeparatorStyle(
                sepBankerBank,
                trackingMode == TrackingMode.COMBINED
                        ? playerSystem.getProfit()
                        : bankerSystem.getProfit()
        );
    }

    private void bindPlayerUI() {
        lblPlayerNextBet.setText(String.format("💲%.2f", playerSystem.getNextBet()));
        setRecoveredLabel(lblPlayerRecovered, playerSystem.getRecoveredThisStage());
        lblPlayerProfit.setText(String.format("💲%.2f", playerSystem.getProfit()));
        lblPlayerBank.setText(String.format("💲%.2f", playerSystem.getBank()));
        String msg = playerSystem.getStatusMessage();
        if (playerSystem.shouldMoveShoe() == 1) {
            msg = "Move To New Shoe \n" + msg;
        }else if (playerSystem.shouldMoveShoe() == 2) {
            msg = "Stay In Shoe \n" + msg;
        }else if (playerSystem.isSystemLocked() ) {

        }

        txtPlayerOutput.setText(msg);
        lblPlayerUnit.setText(String.format("💲%.2f", playerSystem.getBaseUnit()));
        setLabelColor(lblPlayerProfit, playerSystem.getProfit());
        setLabelColor(lblPlayerBank, playerSystem.getProfit());
        int stage = playerSystem.getStageIndex() + 1;
        int bet    = playerSystem.getCurrentBetIndex() + 1;
        lblPlayerStageDetail.setText("     " + stage + " (" + bet + ")");
        String msg1 = String.format("$%.2f", playerSystem.getNextBet());
        msg1 = "PLAYER" + "\n" + msg1;

        txtPlayerBetAmount.setText(msg1);
    }

    private void bindBankerUI() {

        BaccaratSystem system =
                trackingMode == TrackingMode.COMBINED
                        ? playerSystem
                        : bankerSystem;

        int stage = system.getStageIndex() + 1;
        int bet = system.getCurrentBetIndex() + 1;

        lblBankerStage.setText("     " + stage + " (" + bet + ")");

        lblBankerNextBet.setText(
                String.format("💲%.2f", system.getNextBet())
        );

        setRecoveredLabel(
                lblBankerRecovered,
                system.getRecoveredThisStage()
        );

        lblBankerProfit.setText(
                String.format("💲%.2f", system.getProfit())
        );

        lblBankerBank.setText(
                String.format("💲%.2f", system.getBank())
        );

        setLabelColor(lblBankerProfit, system.getProfit());
        setLabelColor(lblBankerBank, system.getProfit());

        String msg = system.getStatusMessage();

        if (system.shouldMoveShoe() == 1) {
            msg = "Move To New Shoe\n" + msg;
        } else if (system.shouldMoveShoe() == 2) {
            msg = "Stay In Shoe\n" + msg;
        }

        txtBankerOutput.setText(msg);

        lblBankerUnit.setText(
                String.format("💲%.2f", system.getBaseUnit())
        );

        txtBankerBetAmount.setText(
                "BANKER\n" +
                        String.format("$%.2f", system.getNextBet())
        );
    }

    /**
     * Shows remaining recovery in white 0.00 when nothing to recover, or as negative red amount while recovering.
     */
    private void setRecoveredLabel(Label label, double remainingToRecover) {
        double eps = 1e-9;
        if (remainingToRecover <= eps) {
            label.setText("💲0.00");
            label.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");
        } else {
            label.setText(String.format("-💲%.2f", remainingToRecover));
            label.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
        }
    }

    /**
     * Update pie charts and percentage labels.
     */
    public void updatePieChart() {
        int totalWins = playerSystem.getTotalWins() + bankerSystem.getTotalWins();
        int totalLosses = playerSystem.getTotalLosses() + bankerSystem.getTotalLosses();
        int total = totalWins + totalLosses;

        pieChart.setData(FXCollections.observableArrayList(
                new PieChart.Data("Win", totalWins),
                new PieChart.Data("Lose", totalLosses)
        ));

        pieChartPlayer.setData(FXCollections.observableArrayList(
                new PieChart.Data("Win", playerSystem.getTotalWins()),
                new PieChart.Data("Lose", playerSystem.getTotalLosses())
        ));

        pieChartBanker.setData(FXCollections.observableArrayList(
                new PieChart.Data("Win", bankerSystem.getTotalWins()),
                new PieChart.Data("Lose", bankerSystem.getTotalLosses())
        ));

        lblWinPct.setText(total > 0 ? String.format("%.1f%%", totalWins * 100.0 / total) : "0%");
        lblLossPct.setText(total > 0 ? String.format("%.1f%%", totalLosses * 100.0 / total) : "0%");
        lblPlayerWinPct.setText(playerSystem.getTotalWins() + playerSystem.getTotalLosses() > 0
                ? String.format("%.1f%%", playerSystem.getTotalWins() * 100.0 /
                (playerSystem.getTotalWins() + playerSystem.getTotalLosses())) : "0%");
        lblPlayerLossPct.setText(playerSystem.getTotalWins() + playerSystem.getTotalLosses() > 0
                ? String.format("%.1f%%", playerSystem.getTotalLosses() * 100.0 /
                (playerSystem.getTotalWins() + playerSystem.getTotalLosses())) : "0%");
        lblBankerWinPct.setText(bankerSystem.getTotalWins() + bankerSystem.getTotalLosses() > 0
                ? String.format("%.1f%%", bankerSystem.getTotalWins() * 100.0 /
                (bankerSystem.getTotalWins() + bankerSystem.getTotalLosses())) : "0%");
        lblBankerLossPct.setText(bankerSystem.getTotalWins() + bankerSystem.getTotalLosses() > 0
                ? String.format("%.1f%%", bankerSystem.getTotalLosses() * 100.0 /
                (bankerSystem.getTotalWins() + bankerSystem.getTotalLosses())) : "0%");

        Platform.runLater(() -> {
            stylePieChart(pieChart);
            stylePieChart(pieChartPlayer);
            stylePieChart(pieChartBanker);
        });
    }

    private void stylePieChart(PieChart chart) {
        for (PieChart.Data d : chart.getData()) {
            String color = d.getName().equals("Win") ? "green" : "red";
            d.getNode().setStyle("-fx-pie-color: " + color + ";");
        }
    }

    private void setLabelColor(Label label, double value) {
        if (label == null) return;
        if (value > 0)
            label.setStyle("-fx-text-fill: green; -fx-font-weight: bold;");
        else if (value == 0)
            label.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");
        else
            label.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
    }

    private void updateSeparatorStyle(Separator separator, double value) {
        if (separator == null) return;
        Platform.runLater(() -> separator.pseudoClassStateChanged(POSITIVE, value > 0));
    }

    /**
     * Update all stats display. Uses counts to compute "combined total" while ignoring sides with zero trials.
     */
    private void updateStats() {
        // Helper: return average of rates ignoring sides with count == 0.
        java.util.function.BiFunction<Double, Integer, Boolean> dummy = (d, i) -> true; // placeholder no-op

        java.util.function.BiFunction<Double, Double, Double> safeAverage = (a, b) -> {
            // This version is retained for fallback but we'll compute below using counts.
            boolean aValid = !Double.isNaN(a) && a > 0;
            boolean bValid = !Double.isNaN(b) && b > 0;
            if (aValid && bValid) return (a + b) / 2.0;
            if (aValid) return a;
            if (bValid) return b;
            return 0.0;
        };

        // First Bet (use counts to decide)
        double firstTotal;
        int pFirstCount = playerSystem.getFirstBetCount();
        int bFirstCount = bankerSystem.getFirstBetCount();
        if (pFirstCount > 0 && bFirstCount > 0) {
            firstTotal = (playerSystem.getFirstBetWinRate() + bankerSystem.getFirstBetWinRate()) / 2.0;
        } else if (pFirstCount > 0) {
            firstTotal = playerSystem.getFirstBetWinRate();
        } else if (bFirstCount > 0) {
            firstTotal = bankerSystem.getFirstBetWinRate();
        } else {
            firstTotal = 0.0;
        }
        updateStatsDisplay(lblFirstBetTotal, lblFirstBetPlayer, lblFirstBetBanker,
                firstTotal,
                playerSystem.getFirstBetWinRate(),
                bankerSystem.getFirstBetWinRate());

        // Second Bet
        double secondTotal;
        int pSecondCount = playerSystem.getSecondBetCount();
        int bSecondCount = bankerSystem.getSecondBetCount();
        if (pSecondCount > 0 && bSecondCount > 0) {
            secondTotal = (playerSystem.getSecondBetWinRate() + bankerSystem.getSecondBetWinRate()) / 2.0;
        } else if (pSecondCount > 0) {
            secondTotal = playerSystem.getSecondBetWinRate();
        } else if (bSecondCount > 0) {
            secondTotal = bankerSystem.getSecondBetWinRate();
        } else {
            secondTotal = 0.0;
        }
        updateStatsDisplay(lblSecondBetTotal, lblSecondBetPlayer, lblSecondBetBanker,
                secondTotal,
                playerSystem.getSecondBetWinRate(),
                bankerSystem.getSecondBetWinRate());

        // Stage 1
        double stage1Total;
        int pStage1 = playerSystem.getStageCount(0);
        int bStage1 = bankerSystem.getStageCount(0);
        if (pStage1 > 0 && bStage1 > 0) {
            stage1Total = (playerSystem.getStageWinRate(0) + bankerSystem.getStageWinRate(0)) / 2.0;
        } else if (pStage1 > 0) {
            stage1Total = playerSystem.getStageWinRate(0);
        } else if (bStage1 > 0) {
            stage1Total = bankerSystem.getStageWinRate(0);
        } else {
            stage1Total = 0.0;
        }
        updateStatsDisplay(lblStage1Total, lblStage1Player, lblStage1Banker,
                stage1Total,
                playerSystem.getStageWinRate(0),
                bankerSystem.getStageWinRate(0));

        // Stage 2
        double stage2Total;
        int pStage2 = playerSystem.getStageCount(1);
        int bStage2 = bankerSystem.getStageCount(1);
        if (pStage2 > 0 && bStage2 > 0) {
            stage2Total = (playerSystem.getStageWinRate(1) + bankerSystem.getStageWinRate(1)) / 2.0;
        } else if (pStage2 > 0) {
            stage2Total = playerSystem.getStageWinRate(1);
        } else if (bStage2 > 0) {
            stage2Total = bankerSystem.getStageWinRate(1);
        } else {
            stage2Total = 0.0;
        }
        updateStatsDisplay(lblStage2Total, lblStage2Player, lblStage2Banker,
                stage2Total,
                playerSystem.getStageWinRate(1),
                bankerSystem.getStageWinRate(1));

        // Stage 3
        double stage3Total;
        int pStage3 = playerSystem.getStageCount(2);
        int bStage3 = bankerSystem.getStageCount(2);
        if (pStage3 > 0 && bStage3 > 0) {
            stage3Total = (playerSystem.getStageWinRate(2) + bankerSystem.getStageWinRate(2)) / 2.0;
        } else if (pStage3 > 0) {
            stage3Total = playerSystem.getStageWinRate(2);
        } else if (bStage3 > 0) {
            stage3Total = bankerSystem.getStageWinRate(2);
        } else {
            stage3Total = 0.0;
        }
        updateStatsDisplay(lblStage3Total, lblStage3Player, lblStage3Banker,
                stage3Total,
                playerSystem.getStageWinRate(2),
                bankerSystem.getStageWinRate(2));

        // Streaks (sum for total)
        updateStatsStreakDisplay(lblWinStreakTotal, lblWinStreakPlayer, lblWinStreakBanker,
                playerSystem.getMaxWinStreak() + bankerSystem.getMaxWinStreak(),
                playerSystem.getMaxWinStreak(),
                bankerSystem.getMaxWinStreak());

        updateStatsStreakDisplay(lblLossStreakTotal, lblLossStreakPlayer, lblLossStreakBanker,
                playerSystem.getMaxLoseStreak() + bankerSystem.getMaxLoseStreak(),
                playerSystem.getMaxLoseStreak(),
                bankerSystem.getMaxLoseStreak());
    }

    private void updateStatsDisplay(Label total, Label player, Label banker,
                                    double totalVal, double playerVal, double bankerVal) {
        if (total != null) total.setText(String.format("%.1f%%", totalVal));
        if (player != null) player.setText(String.format("%.1f%%", playerVal));
        if (banker != null) banker.setText(String.format("%.1f%%", bankerVal));

        if (total != null) total.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");
        if (player != null) player.setStyle("-fx-text-fill: #2196F3; -fx-font-weight: bold;");  // blue
        if (banker != null) banker.setStyle("-fx-text-fill: #F44336; -fx-font-weight: bold;"); // red
    }

    private void updateStatsStreakDisplay(Label total, Label player, Label banker,
                                          int totalVal, int playerVal, int bankerVal) {
        if (total != null) total.setText(String.valueOf(totalVal));
        if (player != null) player.setText(String.valueOf(playerVal));
        if (banker != null) banker.setText(String.valueOf(bankerVal));

        if (total != null) total.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");
        if (player != null) player.setStyle("-fx-text-fill: #2196F3; -fx-font-weight: bold;");
        if (banker != null) banker.setStyle("-fx-text-fill: #F44336; -fx-font-weight: bold;");
    }

    /**
     * Runs an action while showing the loading overlay briefly, then updates UI.
     */
    private void runWithLoading(Runnable action) {
        if (loadingOverlay != null) {
            loadingOverlay.toFront();
            loadingOverlay.setVisible(true);
        }

        PauseTransition pause = new PauseTransition(Duration.seconds(0.25));
        pause.setOnFinished(e -> {
            try {
                action.run();
            } finally {
                updateUI();
                if (loadingOverlay != null) loadingOverlay.setVisible(false);
            }
        });
        pause.play();
    }

}
