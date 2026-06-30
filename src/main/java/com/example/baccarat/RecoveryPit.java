package com.example.baccarat;

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

    public void start(double loss, Risk risk) {

        this.totalLoss = Math.max(loss, 0.01);
        this.risk = risk;
        this.currentBox = 1;
        this.currentProgress = 0;

        this.boxTarget = Math.ceil(this.totalLoss / risk.getBoxes());
    }

    public void applyWin(double amount) {

        currentProgress += amount;

        while (currentProgress >= boxTarget && currentBox < risk.getBoxes()) {
            currentProgress -= boxTarget;
            currentBox++;
        }
    }

    public void applyLoss(double amount) {

        currentProgress -= amount;

        while (currentProgress < 0 && currentBox > 1) {
            currentBox--;
            currentProgress += boxTarget;
        }

        if (currentBox == 1 && currentProgress < 0) {
            currentProgress = 0;
        }
    }

    public boolean isComplete() {
        return currentBox >= risk.getBoxes() && currentProgress >= boxTarget;
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
        return (currentBox - 1) * boxTarget + currentProgress;
    }
}