package com.nuance.cobra.client;

import com.nuance.cobra.test.CobraTest;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;

public class UserResetClientsTest extends CobraTest {

    @Test
    public void test_createClient() {
        UserResetClients client = UserResetClients.fromString("mix");
        assertNotNull(client);
    }

    @Test(expected = NullPointerException.class)
    public void test_createClient_empty() {
        UserResetClients client = UserResetClients.fromString(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void test_createClient_invalid() {
        UserResetClients client = UserResetClients.fromString("yahoo");
    }
}
