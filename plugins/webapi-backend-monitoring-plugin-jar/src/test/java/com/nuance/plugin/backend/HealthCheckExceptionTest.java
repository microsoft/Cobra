package com.nuance.plugin.backend;

import org.junit.Test;

import static org.junit.Assert.*;

public class HealthCheckExceptionTest {

    @Test
    public void getMessage() {
        HealthCheckException exceptionOne = new HealthCheckException("Message Test");
        assertEquals("Message Test", exceptionOne.getMessage());

        HealthCheckException exceptionTwo = new HealthCheckException("Second Message Test", new Exception());
        assertEquals( "Second Message Test", exceptionTwo.getMessage());
    }
}