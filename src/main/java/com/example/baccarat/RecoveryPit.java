package com.example.baccarat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Implements the hybrid recovery queue from the supplied calculator specification. */
public class RecoveryPit implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    public enum Risk {
        HIGH(6), MEDIUM(10), LOW(14);

        private final int boxes;
        Risk(int boxes) { this.boxes = boxes; }
        public int getBoxes() { return boxes; }
    }

    public record LossResult(boolean safetyValveTriggered, int boxesAdded, int totalBoxes) { }

    private static final int MIN_POINTS = 20;
    private static final int MAX_POINTS = 60;
    private final List<Double> activeBoxes = new ArrayList<>();
    private double initialLoss;
    private double recoveredLiability;
    private double recommendedStake;
    private double pointTarget = MIN_POINTS;
    private double maxStake = 50.0;

    public void start(double loss, Risk risk) {
        start(loss, risk, pointTarget, maxStake);
    }

    public void start(double loss, Risk risk, double pointTarget, double maxStake) {
        initialLoss = Math.max(loss, 0.0);
        this.pointTarget = clamp(pointTarget, MIN_POINTS, MAX_POINTS);
        this.maxStake = Math.max(maxStake, 0.01);
        recoveredLiability = 0.0;
        activeBoxes.clear();
        int count = risk == null ? Risk.MEDIUM.getBoxes() : risk.getBoxes();
        if (initialLoss <= 0) return;
        double share = initialLoss / count;
        for (int i = 0; i < count; i++) activeBoxes.add(share);
        refreshRecommendedStake();
    }

    /** A winning hand reduces the first active box; only a fully cleared box disperses. */
    public void applyWin(double stake) {
        if (activeBoxes.isEmpty()) return;
        double amount = Math.max(stake, 0.0);
        double remainingInBox = activeBoxes.get(0);
        double cleared = Math.min(amount, remainingInBox);
        recoveredLiability += cleared;

        if (remainingInBox - cleared <= 0.0000001) {
            activeBoxes.remove(0);
            refreshRecommendedStake();
        } else {
            activeBoxes.set(0, remainingInBox - cleared);
        }
    }

    /** Redistributes a lost wager, then expands the queue if the stake cap is breached. */
    public LossResult applyLoss(double amount) {
        if (activeBoxes.isEmpty()) return new LossResult(false, 0, 0);
        double burden = Math.max(amount, 0.0) / activeBoxes.size();
        for (int i = 0; i < activeBoxes.size(); i++) {
            activeBoxes.set(i, activeBoxes.get(i) + burden);
        }
        refreshRecommendedStake();
        if (getRecommendedStake() <= maxStake + 0.0000001) {
            return new LossResult(false, 0, activeBoxes.size());
        }
        double remaining = getRemainingDrawdown();
        int required = Math.max(activeBoxes.size(), (int) Math.ceil(remaining / (maxStake * (pointTarget / 10.0))));
        int added = required - activeBoxes.size();
        if (added > 0) {
            activeBoxes.clear();
            double share = remaining / required;
            for (int i = 0; i < required; i++) activeBoxes.add(share);
            refreshRecommendedStake();
        }
        return new LossResult(added > 0, added, activeBoxes.size());
    }

    public boolean isComplete() { return activeBoxes.isEmpty(); }
    public double getBoxTarget() { return getRecommendedStake(); }
    public double getRecommendedStake() { return activeBoxes.isEmpty() ? 0.0 : recommendedStake; }
    public int getCurrentBox() { return activeBoxes.isEmpty() ? 0 : 1; }
    public int getTotalBoxes() { return activeBoxes.size(); }
    public double getCurrentProgress() { return 0.0; }
    public double getRemainingInBox() { return activeBoxes.isEmpty() ? 0.0 : activeBoxes.get(0); }
    public double getTotalLoss() { return initialLoss; }
    public double getRemainingDrawdown() { return activeBoxes.stream().mapToDouble(Double::doubleValue).sum(); }
    /** Total liability cleared by recovery wins, including redistributed losses. */
    public double getTotalRecovered() { return recoveredLiability; }
    public double getPointTarget() { return pointTarget; }
    public double getMaxStake() { return maxStake; }
    public List<Double> getActiveBoxes() { return Collections.unmodifiableList(activeBoxes); }

    private void refreshRecommendedStake() {
        if (activeBoxes.isEmpty()) {
            recommendedStake = 0.0;
            return;
        }
        double rawStake = activeBoxes.get(0) / (pointTarget / 10.0);
        recommendedStake = Math.ceil(rawStake * 10.0 - 0.0000001) / 10.0;
    }

    private static double clamp(double value, double min, double max) {
        if (!Double.isFinite(value)) return min;
        return Math.max(min, Math.min(max, value));
    }
}
