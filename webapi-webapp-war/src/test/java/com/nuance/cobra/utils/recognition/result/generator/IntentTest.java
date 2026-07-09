package com.nuance.cobra.utils.recognition.result.generator;

import org.junit.Test;

import static org.junit.Assert.*;

public class IntentTest {
    private Intent intent = new Intent();
    private String id = "Expected Intent ID";
    private String value = "Expected Intent Value";

    @Test
    public void allMethodsTest(){
        Intent intent2 = new Intent(id, value);

        intent.setId(id);
        intent.setValue(value);

        assertEquals(id, intent.getId());
        assertEquals(value, intent.getValue());

        assertTrue(intent.equals(intent2));

        String expectedOutput = "Intent[id=Expected Intent ID,value=Expected Intent Value]";

        assertEquals(expectedOutput, intent.toString());

        Intent intent3 = new Intent();

        String expectedNullOutput = "Intent[id=null,value=null]";

        assertEquals(expectedNullOutput, intent3.toString());

        assertNotEquals(intent2.hashCode(), intent3.hashCode());

        int hash = 1;
        int prime = 31;
        hash = prime * hash + (id.hashCode());
        hash = prime * hash + (value.hashCode());

        assertEquals(hash, intent2.hashCode());
        assertEquals((prime*prime), intent3.hashCode());
    }
}