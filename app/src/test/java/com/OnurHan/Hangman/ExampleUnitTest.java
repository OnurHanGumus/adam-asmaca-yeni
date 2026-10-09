package com.OnurHan.Hangman;

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

    @Test
    public void testCategoryManagerProgression() {
        assertEquals(15, CategoryManager.CATEGORY_KEYS.length);
        assertEquals("all", CategoryManager.CATEGORY_KEYS[0]);
        assertEquals("body_parts", CategoryManager.CATEGORY_KEYS[1]);
        assertEquals("weather", CategoryManager.CATEGORY_KEYS[14]);

        // Level 1: category index 1 (body_parts), word index 0
        assertEquals("body_parts", CategoryManager.getMainGameCategoryForLevel(1));
        assertEquals(0, CategoryManager.getMainGameIndexForLevel(1));

        // Level 14: category index 14 (weather), word index 0
        assertEquals("weather", CategoryManager.getMainGameCategoryForLevel(14));
        assertEquals(0, CategoryManager.getMainGameIndexForLevel(14));

        // Level 15: cycle repeats -> category index 1 (body_parts), word index 1
        assertEquals("body_parts", CategoryManager.getMainGameCategoryForLevel(15));
        assertEquals(1, CategoryManager.getMainGameIndexForLevel(15));
    }

    @Test
    public void testSoundManagerMistakeStrokeProgression() {
        // Progression order: 1-long, 2-short, 3-long, 4-short, 5-short, 6-long
        assertTrue(SoundManager.isLongStroke(1));
        assertFalse(SoundManager.isLongStroke(2));
        assertTrue(SoundManager.isLongStroke(3));
        assertFalse(SoundManager.isLongStroke(4));
        assertFalse(SoundManager.isLongStroke(5));
        assertTrue(SoundManager.isLongStroke(6));
    }
}