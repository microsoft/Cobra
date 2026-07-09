package com.nuance.cobra.session;

import org.junit.Test;

public class AuthTypeAndDataTest
{
    @Test(expected = IllegalStateException.class)
    public void AuthTypeAndDataNoArgCnstr()
    {
        new AuthTypeAndData();
    }
}