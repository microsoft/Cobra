package com.nuance.cobra.api.request;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class RequestBasicTest {

    @Test
    public void testName() {
        String name = "kaleb";
        RequestBasic.CommandBasic RCB = new RequestBasic().new CommandBasic();
        RCB.setName(name);
        assertEquals(RCB.getName(), name);
    }
    @Test
    public void testId() {
        String id = "1001";
        RequestBasic.CommandBasic RCB = new RequestBasic().new CommandBasic();
        RCB.setId(id);
        assertEquals(RCB.getId(), id);
    }

}