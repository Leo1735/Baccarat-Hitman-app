package com.example.baccarat;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RecoveryPitTest {
    @Test
    void mediumRecoveryUsesScaledStakeAndPartiallyClearsBox() {
        RecoveryPit pit = new RecoveryPit();
        pit.start(39.0, RecoveryPit.Risk.MEDIUM);

        assertEquals(2.0, pit.getRecommendedStake(), 0.000001);

        pit.applyWin(pit.getRecommendedStake());
        assertEquals(1.9, pit.getActiveBoxes().get(0), 0.000001);
        assertEquals(2.0, pit.getRecommendedStake(), 0.000001);
        assertEquals(10, pit.getTotalBoxes());

        pit.applyWin(pit.getRecommendedStake());
        assertEquals(3.9, pit.getActiveBoxes().get(0), 0.000001);
        assertEquals(9, pit.getTotalBoxes());
    }

    @Test
    void lossAddsTheRecommendedStakeToEveryBox() {
        RecoveryPit pit = new RecoveryPit();
        pit.start(39.0, RecoveryPit.Risk.MEDIUM);

        pit.applyLoss(pit.getRecommendedStake());

        assertEquals(5.9, pit.getActiveBoxes().get(0), 0.000001);
    }
}
