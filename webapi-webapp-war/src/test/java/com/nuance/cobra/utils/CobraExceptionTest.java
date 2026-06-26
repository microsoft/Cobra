package com.nuance.cobra.utils;

import com.nuance.cobra.api.response.ResponseCode;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;

public class CobraExceptionTest {

    @Test
    public void testCloseConnection() throws Exception {
        CobraException CE = new CobraException(ResponseCode.createResponseErrorForNimService(1000));
        assertNotNull(CE);
    }

}