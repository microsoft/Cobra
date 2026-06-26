package com.nuance.cobra.utils.recognition.result.generator;

import org.junit.Test;

import static org.junit.Assert.*;

public class VariableTest {
    private String id = "testId";
    private String value = "testValue";

    private Variable v = new Variable(id, value);

    @Test
    public void testGettersSetters(){

        assertEquals("testId", v.getId());
        assertEquals("testValue", v.getValue());

        v.setId("Expected ID");
        v.setValue("Expected Value");

        assertEquals("Expected ID", v.getId());
        assertEquals("Expected Value", v.getValue());

        Variable v3 = new Variable();

        assertNull(v3.getValue());
        assertNull(v3.getId());
    }

    @Test
    public void toStringTest(){
        String expectedToString = "Variable[id=testId,value=testValue]";
        assertEquals(expectedToString, v.toString());
    }

    @Test
    public void equals(){
        Variable v2 = new Variable(v.getId(), v.getValue());
        assertTrue(v.equals(v2));
    }

    @Test
    public void hashCodeTest(){
        int hash = v.getId().hashCode();
        hash ^= v.getValue().hashCode();

        Variable v2 = new Variable();

        int hash2 = 0;
        hash2 ^= 0;

        assertEquals(hash, v.hashCode());
        assertEquals(hash2, v2.hashCode());
    }

}