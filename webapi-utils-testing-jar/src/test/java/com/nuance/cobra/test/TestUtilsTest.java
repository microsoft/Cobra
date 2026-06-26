package com.nuance.cobra.test;

import org.junit.Test;

import static org.junit.Assert.*;

public class TestUtilsTest
{
    @Test
    public void inject()
            throws NoSuchFieldException, IllegalAccessException
    {
        String s1 = "hello";
        TestUtils.inject(s1, String.class, "hash", 234);
        int a = (Integer) TestUtils.getFieldValue(String.class, s1, "hash");
        assertEquals(234, a);
    }

    @Test(expected = NoSuchFieldException.class)
    public void noSuchField()
            throws NoSuchFieldException, IllegalAccessException
    {
        StringBuffer s1 = new StringBuffer("hello");
        TestUtils.getFieldValue(String.class, s1, "NoSuchField");
    }

    @Test(expected = IllegalAccessException.class)
    public void inject2()
            throws NoSuchFieldException, IllegalAccessException
    {
        StringBuffer s1 = new StringBuffer("hello");
        TestUtils.inject(StringBuffer.class, "serialVersionUID", 3388685877147921108L);
    }


}