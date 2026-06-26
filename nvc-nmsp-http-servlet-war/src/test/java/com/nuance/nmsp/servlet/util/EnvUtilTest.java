package com.nuance.nmsp.servlet.util;

import org.junit.Test;

public class EnvUtilTest
{
    @Test(expected = IllegalStateException.class)
    public void noConstuct()
    {
        new EnvUtil();
    }
}