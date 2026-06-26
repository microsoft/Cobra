package com.nuance.cobra.doc;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class DocGeneratorExceptionTest {

    @Test
    public void testDocGeneratorException() {

        DocGeneratorException e = new DocGeneratorException("message");
        assertEquals("message", e.getMessage());

        Exception cause = new Exception("cause");
        e = new DocGeneratorException("message", cause);
        assertEquals("cause", e.getCause().getMessage());
    }
}