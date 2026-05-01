package com.example.baccarat;

import java.io.Serializable;

public class CompoundingDay implements Serializable {

    private static final long serialVersionUID = 1L;

    private int day;
    private double opening;
    private double profitLoss;
    private double toGoal;

    public CompoundingDay(int day, double opening) {
        this.day = day;
        this.opening = opening;
        this.profitLoss = 0;
        this.toGoal = 0;
    }

    public int getDay() { return day; }
    public double getOpening() { return opening; }
    public double getProfitLoss() { return profitLoss; }
    public double getToGoal() { return toGoal; }

    public void setOpening(double opening) { this.opening = opening; }
    public void setProfitLoss(double profitLoss) { this.profitLoss = profitLoss; }
    public void setToGoal(double toGoal) { this.toGoal = toGoal; }
}