package com.nuance.plugin.kryptonws.utils;

import org.junit.Test;

import static org.junit.Assert.*;

public class KryptonWsTimeDesignationTest
{

    @Test
    public void getSecondsFromTimeDesignation()
    {
        assertEquals(1, KryptonWsTimeDesignation.getSecondsFromTimeDesignation("1s"));
        assertEquals(1, KryptonWsTimeDesignation.getSecondsFromTimeDesignation("1000ms"));
        assertEquals(-1, KryptonWsTimeDesignation.getSecondsFromTimeDesignation("yolo"));
    }

    @Test
    public void getMillisecondsFromTimeDesignation()
    {
        assertEquals(1000, KryptonWsTimeDesignation.getMillisecondsFromTimeDesignation("1000ms"));
        assertEquals(1000, KryptonWsTimeDesignation.getMillisecondsFromTimeDesignation("1s"));
        assertEquals(-1, KryptonWsTimeDesignation.getMillisecondsFromTimeDesignation("yolo"));
    }
}