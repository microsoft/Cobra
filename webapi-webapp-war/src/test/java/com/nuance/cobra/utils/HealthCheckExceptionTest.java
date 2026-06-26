package com.nuance.cobra.utils;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class HealthCheckExceptionTest {

    @Test
    public void getMessage() {
        HealthCheckException exceptionOne = new HealthCheckException("Message Test");
        assertEquals("Message Test", exceptionOne.getDisplayMessage());

        HealthCheckException exceptionTwo = new HealthCheckException("Second Message Test", new Exception());
        assertEquals( "Second Message Test", exceptionTwo.getDisplayMessage());
    }
}
