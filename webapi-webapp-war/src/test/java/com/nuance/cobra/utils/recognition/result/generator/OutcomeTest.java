package com.nuance.cobra.utils.recognition.result.generator;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

public class OutcomeTest {
    private Variable v = new Variable("Expected Variable ID", "Expected Variable Value");
    private Variable[] variables = {v};
    private Intent intent = new Intent("Expected Intent ID", "Expected Intent Value");
    private List<String> globals = new ArrayList<>();
    private Outcome outcome = new Outcome();

    @Before
    public void setUp(){
        outcome.setGlobals(globals);
        outcome.setIntent(intent);
        outcome.setVariables(variables);
    }


    @Test
    public void gettersAndSetters(){
        assertEquals(intent, outcome.getIntent());
        assertEquals(variables[0], outcome.getVariables()[0]);
        assertEquals(globals, outcome.getGlobals());
        assertEquals(v, outcome.getVariableFromId("Expected Variable ID"));
        assertNull(outcome.getVariableFromId("Non-existing Variable ID"));
    }

    @Test
    public void toStringTest(){
        String outcomeToStringExpected = "Outcome[intent=Intent[id=Expected Intent ID," +
                "value=Expected Intent Value], variables=[Variable[id=Expected Variable ID," +
                "value=Expected Variable Value]], globals=[]]";
        assertEquals(outcomeToStringExpected, outcome.toString());
    }

    @Test
    public void equalsTest(){
        Outcome outcome2 = new Outcome();
        outcome2.setIntent(intent);
        outcome2.setVariables(variables);
        outcome2.setGlobals(globals);

        assertTrue(outcome.equals(outcome2));

        outcome2.setGlobals(null);

        assertFalse(outcome.equals(outcome2));
    }

    @Test
    public void timeoutOutcomeTests(){
        Outcome outcome2 = new Outcome();
        List<String> globals = new ArrayList<>();
        globals.add("ROUTING_TIMEOUT");
        outcome2.setGlobals(globals);

        assertEquals(outcome2, Outcome.createRoutingTimeoutOutcome());
        assertNotEquals(null, Outcome.createRoutingTimeoutOutcome());

        assertEquals(outcome2.toString(), Outcome.createRoutingTimeoutOutcome().toString());

        globals.remove("ROUTING_TIMEOUT");
        globals.add("AGENT_TIMEOUT");
        outcome2.setGlobals(globals);

        assertEquals(outcome2, Outcome.createAgentTimeoutOutcome());
        assertNotEquals(null, Outcome.createAgentTimeoutOutcome());
    }

    @Test
    public void hashCodeTest(){
        int hash = intent.hashCode();
        hash ^= Arrays.hashCode(variables);
        hash ^= globals.hashCode();

        assertEquals(hash, outcome.hashCode());
    }

    @Test
    public void isAgentTimeoutTest(){
        assertFalse(outcome.isAgentTimeout());

        List<String> globals = new ArrayList<>();
        globals.add("AGENT TIMEOUT");
        outcome.setGlobals(globals);

        assertFalse(outcome.isAgentTimeout());

        globals.remove("AGENT TIMEOUT");
        globals.add("AGENT_TIMEOUT");

        assertTrue(outcome.isAgentTimeout());
    }
}