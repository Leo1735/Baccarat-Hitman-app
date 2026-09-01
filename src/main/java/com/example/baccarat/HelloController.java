package com.example.baccarat;

import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.css.PseudoClass;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.BorderPane;
import javafx.scene.shape.SVGPath;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/** Coordinates the JavaFX screens and delegates betting state to BaccaratSystem. */
public class HelloController {



    @FXML private Label  lblPlayerNextBet, lblPlayerRecovered, lblPlayerProfit, lblPlayerBank, lblPlayerUnit, lblPlayerStageDetail, txtPlayerOutput, lblPointsProfit;
    @FXML private Button txtPlayerBetAmount, txtBankerBetAmount;
    @FXML private Button btnPlayerUndo, btnBankerUndo;

    @FXML private Label lblBankerStage, lblBankerNextBet, lblBankerRecovered, lblBankerProfit, lblBankerBank, lblBankerUnit, txtBankerOutput, lblBankerPointsProfit;



    @FXML private Label lblWinPct, lblLossPct, lblPlayerWinPct, lblPlayerLossPct, lblBankerWinPct, lblBankerLossPct,
            playerWinStreak, playerLossStreak, bankerWinStreak, bankerLossStreak, winStreak, lossStreak;
    @FXML private PieChart pieChart, pieChartPlayer, pieChartBanker;


    @FXML private Label lblTotalProfit, lblTotalBank, lblTotalProfitBank, lblTotalBankBank;
    @FXML private VBox settingsPane;
    @FXML private Button btnSaveSettings, btnSaveApp;
    @FXML private ChoiceBox<String> planChoice;
    @FXML private TextField txtBaseUnit, txtBank;

    @FXML private Separator sepPlayerProfit, sepPlayerBank, sepBankerProfit, sepBankerBank;


    @FXML private VBox loadingOverlay;
    @FXML private Label lblFirstBetRate, lblSecondBetRate, lblStage1Rate, lblStage2Rate, lblStage3Rate, lblWinStreaks, lblLossStreaks;


    private BaccaratSystem playerSystem;
    private BaccaratSystem bankerSystem;


    @FXML private TabPane tabPane;
    @FXML private VBox statsPane;


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


    private double stopLoss = 0.0;
    private double stopProfit = 0.0;


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


    private double lastStopLoss;
    private double lastStopProfit;

    @FXML
    private Label lblCurrentPlan;

    private boolean systemLockedTriggered = false;



    private boolean stoppedForProfit = false;

    private boolean bankWasManuallySet = false;

    @FXML private Label lblBankerPointValue, lblBankerPointsBank, lblBankerStopLoss, lblBankerStopProfit, lblStopProfitValue, lblStopLossValue, lblBankerStopLossValue, lblBankerStopProfitValue;


    private final File playerFile = new File("baccarat_player.dat");
    private final File bankerFile = new File("baccarat_banker.dat");
    private final File settingsFile = new File("baccarat_settings.dat");

    @FXML private TableView<CompoundingDay> compoundingTable;
    @FXML private TableColumn<CompoundingDay, Integer> colDay;
    @FXML private TableColumn<CompoundingDay, Double> colOpening;
    @FXML private TableColumn<CompoundingDay, Double> colProfitLoss;
    @FXML private TableColumn<CompoundingDay, Double> colToGoal;
    @FXML private TableColumn<CompoundingDay, Double> colDailyGoal;

    private double dailyRate = 0.0234;
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
    @FXML private Label lblFullTotalProfit;

    private static final PseudoClass POSITIVE = PseudoClass.getPseudoClass("positive");
    public enum TrackingMode {
        SEPARATE,
        COMBINED
    }
    private TrackingMode trackingMode = TrackingMode.SEPARATE;
    private boolean showCompoundingHelp = false;
    @FXML
    private StackPane compoundingHelpOverlay;
    private Stage recoveryStage;
    private Stage compoundingStage;
    private GridPane recoveryWindowGrid;
    private TableView<CompoundingDay> compoundingWindowTable;
    private Button recoveryWinButton;
    private Button recoveryLossButton;


    @FXML
    public void initialize() {

        playerSystem = new BaccaratSystem();
        bankerSystem = new BaccaratSystem();
        configureUndoButtons();

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




        File compFile = new File("compounding.dat");

        if (compFile.exists()) {
            try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(compFile))) {
                loadCompoundingData(in);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }




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

        if (compoundingData.isEmpty()) {
            startNewDay();

        } else {
            startNewDay();
        }

        playerSystem.setBetType(BaccaratSystem.BetType.PLAYER);
        bankerSystem.setBetType(BaccaratSystem.BetType.BANKER);


        planChoice.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {

            if (newVal == null) return;

            applyPlan(newVal);
        });



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
        }
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

    /** Uses a vector icon so the undo arrow is independent of installed fonts. */
    private void configureUndoButtons() {
        configureUndoButton(btnPlayerUndo);
        configureUndoButton(btnBankerUndo);
    }

    private void configureUndoButton(Button button) {
        if (button == null) return;
        SVGPath arrow = new SVGPath();
        arrow.setContent("M 10 2 L 3 8 L 10 14 L 10 10 L 15 10 C 18 10 20 12 20 15 C 20 9 18 6 14 6 L 10 6 Z");
        arrow.setStyle("-fx-fill: #dcdcdc;");
        button.setGraphic(arrow);
        button.setTooltip(new Tooltip("Undo last result"));
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


        stopProfit = Math.max(totalProfit, 0);


        stopLoss = Math.min(totalProfit, 0);

        updateStopLabels();
    }


    private boolean suppressOutputRefresh = false;




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
            stopProfit += betAmount;
            stopLoss   += betAmount;
        } else {
            if (playerSystem.isSystemLocked()) return;
            if (bankerSystem.isSystemLocked()) return;
            stopProfit -= betAmount;
            stopLoss   -= betAmount;
        }

        updateStopLabels();
    }





    private void updateStopLabels() {


        if (lblStopProfit != null)
            lblStopProfit.setText(String.format("$%.0f", stopProfit));
        if (lblStopLoss != null)
            lblStopLoss.setText(String.format("$%.0f", stopLoss));

        if (lblBankerStopProfit != null)
            lblBankerStopProfit.setText(String.format("$%.0f", stopProfit));
        if (lblBankerStopLoss != null)
            lblBankerStopLoss.setText(String.format("$%.0f", stopLoss));


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

    @FXML
    private void handleSaveSettings() {
        runWithLoading(() -> {


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


            if (txtPointValue != null && !txtPointValue.getText().trim().isEmpty()) {
                this.pointValue = parseDouble(txtPointValue.getText(), this.pointValue);
            }



            if (txtStopLoss != null && !txtStopLoss.getText().trim().isEmpty()) {
                this.initialStopLoss = parseDouble(txtStopLoss.getText(), this.initialStopLoss);
                this.stopLoss = 0.0;
            }

            if (txtStopProfit != null && !txtStopProfit.getText().trim().isEmpty()) {
                this.initialStopProfit = parseDouble(txtStopProfit.getText(), this.initialStopProfit);
                this.stopProfit = 0.0;
            }


            BaccaratSystem.saveControllerSettings(settingsFile, pointValue, initialStopLoss, initialStopProfit, stopLoss, stopProfit, planChoice.getValue());


            if (txtBaseUnit != null && !txtBaseUnit.getText().trim().isEmpty()) txtBaseUnit.clear();
            if (txtBank != null && !txtBank.getText().trim().isEmpty()) txtBank.clear();
            if (txtPointValue != null && !txtPointValue.getText().trim().isEmpty()) txtPointValue.clear();
            if (txtStopLoss != null && !txtStopLoss.getText().trim().isEmpty()) txtStopLoss.clear();
            if (txtStopProfit != null && !txtStopProfit.getText().trim().isEmpty()) txtStopProfit.clear();


            updateStopLabels();
            updateUI();
            updateFieldPrompts();
        });
    }




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




        if (lblBankerPointValue != null)
            lblBankerPointValue.setText(String.format("💲%.2f", pointValue));

        if (lblBankerPointsBank != null) {
            double totalBank = playerSystem.getBank() + bankerSystem.getBank();
            int pointsBank = (int) Math.floor(totalBank / pointValue);
            lblBankerPointsBank.setText("  (" + String.valueOf(pointsBank) + ")");
        }



        if (lblStopLossValue != null)

            lblStopLossValue.setText("Stop Loss  ("+ String.format("$%.0f", initialStopLoss)+ ")");
        if (lblStopProfitValue != null)
            lblStopProfitValue.setText("Stop Profit  ("+String.format("$%.0f", initialStopProfit)+ ")");

        if (lblBankerStopLossValue != null)
            lblBankerStopLossValue.setText("Stop Loss  ("+ String.format("$%.0f", initialStopLoss)+ ")");
        if (lblBankerStopProfitValue != null)
            lblBankerStopProfitValue.setText("Stop Profit  ("+String.format("$%.0f", initialStopProfit)+ ")");


        if (!systemLockedTriggered) {


            if (initialStopProfit > 0 && stopProfit >= initialStopProfit) {
                systemLockedTriggered = true;
                stoppedForProfit = true;

                playerSystem.setSystemLocked(true);
                bankerSystem.setSystemLocked(true);



            }


            else if (initialStopLoss > 0 && Math.abs(stopLoss) >= initialStopLoss) {
                systemLockedTriggered = true;
                stoppedForProfit = false;

                playerSystem.setSystemLocked(true);
                bankerSystem.setSystemLocked(true);


            }
        }

    }


    private void updateLockedOutputs() {



        String msg = stoppedForProfit
                ? "🎉 Well done! Reset to continue."
                : "All systems are complete.\nProceed to recovery.";

        if (playerSystem.isSystemLocked()) {
            txtPlayerOutput.setText(msg);
        }

        if (bankerSystem.isSystemLocked()) {
            txtBankerOutput.setText(msg);
        }
    }

    @FXML
    private void handleResetBoth() {
        runWithLoading(() -> {
            playerSystem.resetSystem();
            bankerSystem.resetSystem();
            applyPlan("500-1");
            resetCompounding();



            systemLockedTriggered = false;
            stoppedForProfit = false;
            stopLoss = 0.0;
            stopProfit = 0.0;

            updateUI();
        });
    }

    private void hideAllOverlays() {
        if (recoveryPitOverlay != null) recoveryPitOverlay.setVisible(false);
        if (compoundingOverlay != null) compoundingOverlay.setVisible(false);
        if (settingsPane != null) settingsPane.setVisible(false);
        if (statsPane != null) statsPane.setVisible(false);
    }

    private void makeWindowDraggable(Stage stage, Region titleBar) {
        final double[] offset = new double[2];
        titleBar.setOnMousePressed(event -> {
            offset[0] = event.getSceneX();
            offset[1] = event.getSceneY();
        });
        titleBar.setOnMouseDragged(event -> {
            stage.setX(event.getScreenX() - offset[0]);
            stage.setY(event.getScreenY() - offset[1]);
        });
    }


    private double getRecoveryStake() {
        boolean bankerTab = tabPane != null && tabPane.getSelectionModel().getSelectedIndex() == 1;
        if (bankerTab && trackingMode != TrackingMode.COMBINED) return bankerSystem.getNextBet();
        return playerSystem.getNextBet();
    }

    private void buildRecoveryGrid(int totalBoxes) {
        GridPane grid = recoveryWindowGrid != null ? recoveryWindowGrid : recoveryGrid;
        grid.getChildren().clear();
        grid.getColumnConstraints().clear();
        grid.getRowConstraints().clear();

        int cols = (int) Math.ceil(Math.sqrt(totalBoxes));
        int rows = (int) Math.ceil((double) totalBoxes / cols);
        for (int col = 0; col < cols; col++) {
            javafx.scene.layout.ColumnConstraints constraint = new javafx.scene.layout.ColumnConstraints();
            constraint.setMinWidth(64);
            constraint.setPercentWidth(100.0 / cols);
            constraint.setHgrow(Priority.ALWAYS);
            grid.getColumnConstraints().add(constraint);
        }
        for (int row = 0; row < rows; row++) {
            javafx.scene.layout.RowConstraints constraint = new javafx.scene.layout.RowConstraints();
            constraint.setMinHeight(48);
            constraint.setPercentHeight(100.0 / rows);
            constraint.setVgrow(Priority.ALWAYS);
            grid.getRowConstraints().add(constraint);
        }

        for (int i = 0; i < totalBoxes; i++) {

            Label box = new Label(String.valueOf(i + 1));

            box.setMinSize(0, 0);
            box.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
            box.setAlignment(Pos.CENTER);

            box.setStyle("-fx-background-color: #2a2a3a; -fx-text-fill: white;");

            int row = i / cols;
            int col = i % cols;

            grid.add(box, col, row);
        }
    }
    @FXML
    private void openRecoveryPit() {
        if (recoveryStage != null && recoveryStage.isShowing()) {
            recoveryStage.requestFocus();
            return;
        }
        createRecoveryWindow();
        double loss = Math.abs(playerSystem.getProfit() + bankerSystem.getProfit());
        RecoveryPit.Risk risk = cmbRecoveryRisk.getValue();
        recoveryPit.start(loss, risk);
        buildRecoveryGrid(risk.getBoxes());
        updateRecoveryUI();
        recoveryStage.show();
    }

    private void createRecoveryWindow() {
        recoveryStage = new Stage(StageStyle.UNDECORATED);
        recoveryStage.setTitle("Recovery Pit");
        recoveryStage.setMinWidth(360);
        recoveryStage.setMinHeight(440);
        recoveryStage.setResizable(true);
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #181822; -fx-padding: 12;");

        VBox top = new VBox(12);
        HBox header = new HBox();
        header.setStyle("-fx-background-color: rgba(255,255,255,0.08); -fx-padding: 8;");
        Label title = new Label("Recovery Pit");
        title.setStyle("-fx-font-size: 20px; -fx-text-fill: white; -fx-font-weight: bold;");
        Region spacer = new Region(); HBox.setHgrow(spacer, Priority.ALWAYS);
        Button close = new Button("X"); close.setOnAction(e -> recoveryStage.close());
        header.getChildren().addAll(title, spacer, close);
        Label loss = new Label(); loss.setStyle("-fx-text-fill: white;"); configureRecoveryInfoLabel(loss); lblRecoveryLoss = loss;
        Label stake = new Label(); stake.setStyle("-fx-text-fill: white;"); configureRecoveryInfoLabel(stake); lblRecoveryStake = stake;
        Label progress = new Label(); progress.setStyle("-fx-text-fill: white;"); configureRecoveryInfoLabel(progress); lblBoxProgress = progress;
        ComboBox<RecoveryPit.Risk> risk = new ComboBox<>(FXCollections.observableArrayList(RecoveryPit.Risk.values()));
        risk.setValue(cmbRecoveryRisk.getValue()); cmbRecoveryRisk = risk;
        risk.setOnAction(e -> { recoveryPit.start(recoveryPit.getTotalLoss(), risk.getValue()); buildRecoveryGrid(risk.getValue().getBoxes()); updateRecoveryUI(); });
        top.getChildren().addAll(header, loss, stake, risk, progress);

        recoveryWindowGrid = new GridPane();
        recoveryWindowGrid.setHgap(8);
        recoveryWindowGrid.setVgap(8);
        recoveryWindowGrid.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        ScrollPane gridScroll = new ScrollPane(recoveryWindowGrid);
        gridScroll.setFitToWidth(true);
        gridScroll.setFitToHeight(true);
        gridScroll.setPannable(true);
        gridScroll.setStyle("-fx-background: #181822; -fx-background-color: transparent;");
        Button win = new Button("WIN"); win.setMaxWidth(Double.MAX_VALUE); win.getStyleClass().add("recovery-win"); win.setOnAction(e -> onRecoveryWin());
        Button lossButton = new Button("LOSS"); lossButton.setMaxWidth(Double.MAX_VALUE); lossButton.getStyleClass().add("recovery-loss"); lossButton.setOnAction(e -> onRecoveryLoss());
        recoveryWinButton = win;
        recoveryLossButton = lossButton;
        HBox actions = new HBox(12, win, lossButton); HBox.setHgrow(win, Priority.ALWAYS); HBox.setHgrow(lossButton, Priority.ALWAYS);
        root.setTop(top);
        root.setCenter(gridScroll);
        root.setBottom(actions);
        BorderPane.setMargin(gridScroll, new javafx.geometry.Insets(12, 0, 12, 0));
        Scene scene = new Scene(root, 440, 560);
        scene.getStylesheets().add(getClass().getResource("/com/example/baccarat/styles.css").toExternalForm());
        recoveryStage.setScene(scene);
        makeWindowDraggable(recoveryStage, header);
        MainApp.installResizeHandles(recoveryStage, scene);
        recoveryStage.setOnHidden(e -> {
            recoveryWindowGrid = null;
            recoveryWinButton = null;
            recoveryLossButton = null;
        });
    }

    /** Allows the fixed recovery summary to remain visible in narrow windows. */
    private void configureRecoveryInfoLabel(Label label) {
        label.setMinWidth(0);
        label.setMaxWidth(Double.MAX_VALUE);
        label.setWrapText(true);
    }
    private void updateRecoveryUI() {
        double remaining = Math.max(0, recoveryPit.getTotalBoxes() * recoveryPit.getBoxTarget()
                - recoveryPit.getTotalRecovered());
        if (remaining > 0) {
            lblRecoveryLoss.setText(String.format(
                    "Loss: £%.2f · %d x boxes, £%.2f each · Remaining: £%.2f",
                    recoveryPit.getTotalLoss(), recoveryPit.getTotalBoxes(), recoveryPit.getBoxTarget(), remaining));
        } else {
            lblRecoveryLoss.setText(String.format(
                    "Recovery complete: £0.00 · %d x boxes, £%.2f each",
                    recoveryPit.getTotalBoxes(), recoveryPit.getBoxTarget()));
        }


        lblRecoveryLoss.setStyle(remaining > 0
                ? "-fx-text-fill:#e74c3c; -fx-font-weight:bold;"
                : "-fx-text-fill:#2ecc71; -fx-font-weight:bold;");

        lblBoxProgress.setText(
                "Box " + recoveryPit.getCurrentBox() +
                        " / " + recoveryPit.getTotalBoxes()
        );

        double target = recoveryPit.getBoxTarget();

        GridPane grid = recoveryWindowGrid != null ? recoveryWindowGrid : recoveryGrid;
        for (Node node : grid.getChildren()) {

            if (!(node instanceof Label box)) continue;

            Integer row = GridPane.getRowIndex(box);
            Integer col = GridPane.getColumnIndex(box);

            int r = row == null ? 0 : row;
            int c = col == null ? 0 : col;

            int cols = (int) Math.ceil(Math.sqrt(recoveryPit.getTotalBoxes()));
            int index = r * cols + c;

            int boxNumber = index + 1;


            if (boxNumber < recoveryPit.getCurrentBox()) {
                box.setText("✓");
                box.setStyle("-fx-background-color:#2ecc71; -fx-text-fill:black;");
            }


            else if (boxNumber == recoveryPit.getCurrentBox()) {

                double progress = recoveryPit.getCurrentProgress();

                box.setText(String.format("£%.2f / £%.2f", progress, target));

                box.setStyle("-fx-background-color:#f1c40f; -fx-text-fill:black;");
            }


            else {
                box.setText(String.format("£%.2f", target));
                box.setStyle("-fx-background-color:#2a2a3a; -fx-text-fill:white;");
            }
        }
        double stake = getRecoveryStake();
        lblRecoveryStake.setText(String.format("Stake: £%.2f", stake));
    }
    @FXML
    private void onRecoveryWin() {
        recordRecoveryResult(true);
    }

    @FXML
    private void onRecoveryLoss() {
        recordRecoveryResult(false);
    }


    private void recordRecoveryResult(boolean win) {
        if (recoveryWinButton == null || recoveryLossButton == null
                || recoveryWinButton.isDisable() || recoveryLossButton.isDisable()) return;
        showRecoveryActionFeedback(win);
        runWithLoading(() -> {
            double before = playerSystem.getBank() + bankerSystem.getBank();
            playerSystem.setSystemLocked(false); bankerSystem.setSystemLocked(false);
            boolean bankerTab = tabPane.getSelectionModel().getSelectedIndex() == 1;
            if (bankerTab) {
                if (win && trackingMode == TrackingMode.COMBINED) playerSystem.recordCombinedWin(0.95);
                else if (win) bankerSystem.recordWin();
                else if (trackingMode == TrackingMode.COMBINED) playerSystem.recordLoss();
                else bankerSystem.recordLoss();
            } else if (win) playerSystem.recordWin(); else playerSystem.recordLoss();
            double change = Math.abs(playerSystem.getBank() + bankerSystem.getBank() - before);
            if (win) recoveryPit.applyWin(change); else recoveryPit.applyLoss(change);
            if (recoveryPit.isComplete()) {
                systemLockedTriggered = false; stoppedForProfit = false;
                updateRecoveryUI();


                PauseTransition closeDelay = new PauseTransition(Duration.millis(450));
                closeDelay.setOnFinished(e -> {
                    if (recoveryStage != null) recoveryStage.close();
                });
                closeDelay.play();
            } else {
                playerSystem.setSystemLocked(true); bankerSystem.setSystemLocked(true);
                updateRecoveryUI();
                enableRecoveryButtons();
            }
        });
    }

    private void showRecoveryActionFeedback(boolean win) {
        recoveryWinButton.setDisable(true);
        recoveryLossButton.setDisable(true);
        recoveryWinButton.setText(win ? "WIN ✓" : "WIN");
        recoveryLossButton.setText(win ? "LOSS" : "LOSS ✓");
    }

    private void enableRecoveryButtons() {
        if (recoveryWinButton != null) recoveryWinButton.setDisable(false);
        if (recoveryLossButton != null) recoveryLossButton.setDisable(false);
    }

    private void updateRecoveryPitButton() {

        boolean playerLocked = playerSystem.isSystemLocked();
        boolean bankerLocked = bankerSystem.isSystemLocked();

        if (btnRecoveryPit != null)
            btnRecoveryPit.setVisible(playerLocked);

        if (btnRecoveryPitBank != null)
            btnRecoveryPitBank.setVisible(bankerLocked);


        if (lblPlayerRecovered != null)
            lblPlayerRecovered.setVisible(!playerLocked);

        if (lblBankerRecovered != null)
            lblBankerRecovered.setVisible(!bankerLocked);
    }


    @FXML
    private void openCompoundingPlan() {
        if (compoundingStage != null && compoundingStage.isShowing()) {
            compoundingStage.requestFocus();
            return;
        }
        createCompoundingWindow();
        updateCurrentDay();
        updateCompoundingLabels();
        compoundingStage.show();
    }

    private void createCompoundingWindow() {
        compoundingStage = new Stage(StageStyle.UNDECORATED);
        compoundingStage.setTitle("Hit Man Compounding");
        compoundingStage.setMinWidth(640);
        compoundingStage.setMinHeight(420);
        compoundingStage.setResizable(true);
        VBox root = new VBox(12);
        root.setStyle("-fx-background-color: #181822; -fx-padding: 12;");
        root.setFillWidth(true);
        HBox header = new HBox();
        header.setStyle("-fx-background-color: rgba(255,255,255,0.08); -fx-padding: 8;");
        header.setMaxWidth(Double.MAX_VALUE);
        Label title = new Label("Hit Man Compounding"); title.setStyle("-fx-text-fill:white; -fx-font-size:18px; -fx-font-weight:bold;");
        title.setMinWidth(0);
        Region spacer = new Region(); HBox.setHgrow(spacer, Priority.ALWAYS);
        Button close = new Button("X"); close.setOnAction(e -> compoundingStage.close());
        header.getChildren().addAll(title, spacer, close);
        TextField goalField = new TextField(String.format("%.2f", goal));
        goalField.setMinWidth(80);
        goalField.setPrefWidth(100);
        goalField.setMaxWidth(110);
        goalField.textProperty().addListener((obs, oldValue, newValue) -> {
            goal = parseDouble(newValue, goal);


            if (txtGoal != null && !txtGoal.getText().equals(newValue)) txtGoal.setText(newValue);
            updateCurrentDay();
            updateCompoundingLabels();
        });
        Label goalLabel = new Label("Goal (£)"); goalLabel.setStyle("-fx-text-fill:white;");
        Label progress = new Label(); progress.setStyle("-fx-text-fill:white; -fx-font-weight:bold;");
        progress.setMinWidth(0);
        progress.setMaxWidth(Double.MAX_VALUE);
        progress.setWrapText(true);
        lblFullTotalProfit = progress;
        // Keep goal controls on one line and the progress text on its own full-width row so it remains visible when resizing
        HBox left = new HBox(12, goalLabel, goalField);
        left.setAlignment(Pos.CENTER_LEFT);
        left.setFillHeight(true);
        left.setMaxWidth(Region.USE_COMPUTED_SIZE);
        VBox summary = new VBox(6, left, progress);
        summary.setFillWidth(true);
        VBox.setVgrow(progress, Priority.NEVER);
        progress.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(left, Priority.NEVER);
        TableView<CompoundingDay> table = new TableView<>();
        TableColumn<CompoundingDay, Integer> day = new TableColumn<>("Day");
        TableColumn<CompoundingDay, Double> opening = moneyColumn("Opening (£)", "opening");
        TableColumn<CompoundingDay, Double> dailyGoal = percentColumn("Daily target (%)", "dailyGoal");
        TableColumn<CompoundingDay, Double> profit = moneyColumn("Profit / loss (£)", "profitLoss");
        day.setCellValueFactory(new PropertyValueFactory<>("day"));
        day.setMinWidth(55); day.setPrefWidth(65);
        opening.setMinWidth(120); opening.setPrefWidth(135);
        dailyGoal.setMinWidth(140); dailyGoal.setPrefWidth(165);
        profit.setMinWidth(130); profit.setPrefWidth(155);
        table.getColumns().addAll(day, opening, dailyGoal, profit);
        table.setId("compoundingTableWindow");
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setItems(compoundingData); compoundingWindowTable = table;
        VBox.setVgrow(table, Priority.ALWAYS);
        root.getChildren().addAll(header, summary, table);
        Scene scene = new Scene(root, 700, 540);
        scene.getStylesheets().add(getClass().getResource("/com/example/baccarat/styles.css").toExternalForm());
        compoundingStage.setScene(scene);
        makeWindowDraggable(compoundingStage, header);
        MainApp.installResizeHandles(compoundingStage, scene);
        compoundingStage.setOnHidden(e -> compoundingWindowTable = null);
    }

    private TableColumn<CompoundingDay, Double> moneyColumn(String title, String property) {
        TableColumn<CompoundingDay, Double> column = new TableColumn<>(title);
        column.setCellValueFactory(new PropertyValueFactory<>(property));
        column.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(Double value, boolean empty) {
                super.updateItem(value, empty);
                if (empty || value == null) { setText(null); setStyle(""); return; }
                setText(String.format("£%.2f", value));
                setStyle(value > 0 ? "-fx-text-fill:#2ecc71;" : value < 0 ? "-fx-text-fill:#e74c3c;" : "-fx-text-fill:white;");
            }
        });
        return column;
    }

    private TableColumn<CompoundingDay, Double> percentColumn(String title, String property) {
        TableColumn<CompoundingDay, Double> column = new TableColumn<>(title);
        column.setCellValueFactory(new PropertyValueFactory<>(property));
        column.setCellFactory(col -> new TableCell<CompoundingDay, Double>() {
            @Override protected void updateItem(Double value, boolean empty) {
                super.updateItem(value, empty);
                if (empty || value == null) { setText(null); setStyle(""); return; }

                CompoundingDay row = getTableView().getItems().get(getIndex());
                double opening = row.getOpening();
                double targetBalance = opening * (1 + value / 100.0);

                setText(String.format("%.2f%% (£%.2f)", value, targetBalance));
                setStyle(value > 0 ? "-fx-text-fill:#2ecc71;" : value < 0 ? "-fx-text-fill:#e74c3c;" : "-fx-text-fill:white;");
            }
        });
        return column;
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
        colDay.setCellValueFactory(new PropertyValueFactory<>("day"));
        colOpening.setCellValueFactory(new PropertyValueFactory<>("opening"));
        colProfitLoss.setCellValueFactory(new PropertyValueFactory<>("profitLoss"));
        colDailyGoal.setCellValueFactory(new PropertyValueFactory<>("dailyGoal"));

        colProfitLoss.setCellFactory(col -> new TableCell<CompoundingDay, Double>() {
            @Override
            protected void updateItem(Double value, boolean empty) {
                super.updateItem(value, empty);
                if (empty || value == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(String.format("%.2f", value));
                    if (value > 0) {
                        setStyle("-fx-text-fill: green; -fx-font-weight: bold;");
                    } else if (value == 0) {
                        setStyle("-fx-text-fill: white; -fx-font-weight: bold;");
                    } else {
                        setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
                    }
                }
            }
        });

        colDailyGoal.setCellFactory(col -> new TableCell<CompoundingDay, Double>() {
            @Override
            protected void updateItem(Double value, boolean empty) {
                super.updateItem(value, empty);

                setText(null);
                setStyle("");

                if (empty || value == null) {
                    return;
                }

                CompoundingDay row = getTableView().getItems().get(getIndex());

                double opening = row.getOpening();
                double targetBalance = opening * (1 + value / 100.0);

                setText(String.format("%.2f%% (£%.2f)", value, targetBalance));

                double actual = row.getOpening() + row.getProfitLoss();
                double threshold = targetBalance;

                if (actual >= threshold) {
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
        double target = start + goal;
        dailyRate = calculateDailyRate(start, target, compoundingDays);

        double totalBank = playerSystem.getBank() + bankerSystem.getBank();
        double dayProfit = totalBank - current.getOpening();
        current.setProfitLoss(dayProfit);

        double overallProfit = totalBank - start;
        current.setToGoal(goal - overallProfit);

        double dailyGoal = dailyRate * 100.0;
        current.setDailyGoal(dailyGoal);

        compoundingTable.refresh();
        if (compoundingWindowTable != null) compoundingWindowTable.refresh();
    }
    public void startNewDay() {

        double totalBank = playerSystem.getBank() + bankerSystem.getBank();

        CompoundingDay newDay = new CompoundingDay(compoundingData.size() + 1, totalBank);

        compoundingData.add(newDay);
    }
    private void completeDay() {

        updateCurrentDay();


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


        compoundingData.add(new CompoundingDay(1, totalBank));

        compoundingTable.refresh();
        if (compoundingWindowTable != null) compoundingWindowTable.refresh();

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

        if (txtGoal != null && !txtGoal.getText().isEmpty()) {
            goal = parseDouble(txtGoal.getText(), goal);
        }

        if (compoundingData.isEmpty()) return;

        double startingBank = compoundingData.get(0).getOpening();
        double profit = totalBank - startingBank;
        double remaining = goal - profit;



        if (lblFullTotalProfit != null) {
            lblFullTotalProfit.setText(String.format(
                    "Progress:\n £%.2f (£%.2f remaining)",
                    totalBank, remaining));
            setLabelColor(lblFullTotalProfit, remaining <= 0 ? 1 : (profit < 0 ? -1 : 0));
        }
    }

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
        updateStopLabels();
        updatePointStatsDisplay();




        updateRecoveryPitButton();

        updateCurrentDay();
        updateCompoundingLabels();
        updateFieldPrompts();
        updateLockedOutputs();
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

        String msg;

        if (playerSystem.isSystemLocked()) {
            msg = "$" +playerSystem.getBank()+" Lost \nAll systems are complete.\nProceed to recovery.";
        } else {
            msg = playerSystem.getStatusMessage();

            if (playerSystem.shouldMoveShoe() == 1) {
                msg = "Move To New Shoe \n" + msg;
            } else if (playerSystem.shouldMoveShoe() == 2) {
                msg = "Stay In Shoe \n" + msg;
            }
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

        String msg;

        if (system.isSystemLocked()) {
            msg = "$" +bankerSystem.getBank()+" Lost \nAll systems are complete.\nProceed to recovery.";
        } else {
            msg = system.getStatusMessage();

            if (system.shouldMoveShoe() == 1) {
                msg = "Move To New Shoe\n" + msg;
            } else if (system.shouldMoveShoe() == 2) {
                msg = "Stay In Shoe\n" + msg;
            }
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


    private void updateStats() {

        java.util.function.BiFunction<Double, Integer, Boolean> dummy = (d, i) -> true;

        java.util.function.BiFunction<Double, Double, Double> safeAverage = (a, b) -> {

            boolean aValid = !Double.isNaN(a) && a > 0;
            boolean bValid = !Double.isNaN(b) && b > 0;
            if (aValid && bValid) return (a + b) / 2.0;
            if (aValid) return a;
            if (bValid) return b;
            return 0.0;
        };


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
        if (player != null) player.setStyle("-fx-text-fill: #2196F3; -fx-font-weight: bold;");
        if (banker != null) banker.setStyle("-fx-text-fill: #F44336; -fx-font-weight: bold;");
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
