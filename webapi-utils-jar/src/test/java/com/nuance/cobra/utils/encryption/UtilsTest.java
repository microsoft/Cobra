package com.nuance.cobra.utils.encryption;

import org.junit.Test;

import static org.junit.Assert.*;

public class UtilsTest
{
    @Test
    public void getRandomIndexTest()
    {
        int size = 10;
        for (int i = 0; i < 100; i++)
        {
            int index = Utils.getRandomIndex(size);
            if (index < 0 || index > size - 1)
            {
                fail();
            }
        }
    }

    @Test
    public void getRandomString()
    {
        String rc = Utils.getRandomString(10);
        assertEquals(10, rc.length());
        assertTrue(rc.matches("[A-Za-z0-9]*"));
    }
}