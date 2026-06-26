package com.nuance.cobra.utils.recognition.result.generator;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class GrammarTest {
    private Grammar grammar;
    private String id = "Expected Grammar ID";
    private String ref = "Expected Grammar ref";

    @Test
    public void getterSetterTest(){
        grammar = new Grammar(id, ref);

        assertEquals("Expected Grammar ID", grammar.getId());
        assertEquals("Expected Grammar ref", grammar.getRef());

        grammar.setId("Expected ID");
        grammar.setRef("Expected Ref");

        assertEquals("Expected ID", grammar.getId());
        assertEquals("Expected Ref", grammar.getRef());
    }
}