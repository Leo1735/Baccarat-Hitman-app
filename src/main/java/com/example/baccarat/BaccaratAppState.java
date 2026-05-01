package com.example.baccarat;

import java.io.Serializable;
public class BaccaratAppState implements Serializable {
    private static final long serialVersionUID = 1L;

    private final BaccaratSystem.BetType betType;

    private final BaccaratSystem.StageData[] stages;
    private final int stageIndex;
    private final int totalWins;
    private final int totalLosses;
    private final int[] winsPerStage;
    private final double profit;
    private final double bank;


    public BaccaratAppState(BaccaratSystem system) {
        this.betType = system.getBetTypeForSave();

        this.stages = system.getStagesForSave();
        this.stageIndex = system.getStageIndexForSave();
        this.totalWins = system.getTotalWinsForSave();
        this.totalLosses = system.getTotalLossesForSave();
        this.winsPerStage = system.getWinsPerStageForSave().clone();
        this.profit = system.getProfitForSave();
        this.bank = system.getBankForSave();
    }

    public void restore(BaccaratSystem system) {
        system.setBetTypeFromLoad(betType);

        system.setStagesFromLoad(stages);
        system.setStageIndexFromLoad(stageIndex);
        system.setTotalWinsFromLoad(totalWins);
        system.setTotalLossesFromLoad(totalLosses);
        system.setWinsPerStageFromLoad(winsPerStage.clone());
        system.setProfitFromLoad(profit);
        system.setBankFromLoad(bank);
    }
}
