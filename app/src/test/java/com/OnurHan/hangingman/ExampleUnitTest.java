package com.OnurHan.hangingman;

import org.junit.Test;

import static org.junit.Assert.*;

public class ExampleUnitTest {

    @Test
    public void addition_isCorrect() {
        assertEquals(4, 2 + 2);
    }

    @Test
    public void testLifeManagerTimeFormatting() {
        assertEquals("20:00", LifeManager.formatRemainingTime(20 * 60 * 1000L));
        assertEquals("00:00", LifeManager.formatRemainingTime(0L));
        assertEquals("04:35", LifeManager.formatRemainingTime((4 * 60 + 35) * 1000L));
        assertEquals("00:59", LifeManager.formatRemainingTime(59 * 1000L));
    }

    @Test
    public void testCurrencyConstants() {
        assertEquals(50, CurrencyManager.INITIAL_COINS);
        assertEquals(15, CurrencyManager.HINT_COST);
        assertEquals(10, CurrencyManager.REVEAL_LETTER_COST);
        assertEquals(15, CurrencyManager.LEVEL_WIN_REWARD);
        assertEquals(10, CurrencyManager.REFILL_LIFE_COST);
    }

    @Test
    public void testGameModeValues() {
        assertEquals(GameMode.PRACTICE, GameMode.valueOf("PRACTICE"));
        assertEquals(GameMode.MAIN_GAME, GameMode.valueOf("MAIN_GAME"));
    }
}