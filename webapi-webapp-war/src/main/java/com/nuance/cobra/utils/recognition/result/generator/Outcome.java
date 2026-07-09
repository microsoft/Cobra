package com.nuance.cobra.utils.recognition.result.generator;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class Outcome implements Serializable{
    private static final long serialVersionUID = -6310058155424887628L;
    public static final String AGENT_TIMEOUT = "AGENT_TIMEOUT";
    public static final String ROUTING_TIMEOUT = "ROUTING_TIMEOUT"; // for testing only

    @JsonProperty("intent")
    private Intent intent;
    @JsonProperty("variables")
    private Variable[] variables;
    @JsonProperty("globals")
    private List<String> globals;

    public Intent getIntent() {
        return intent;
    }

    public void setIntent(Intent intent) {
        this.intent = intent;
    }

    public Variable[] getVariables() {
        return variables;
    }

    public void setVariables(Variable[] variables) {
        this.variables = variables;
    }

    public List<String> getGlobals() {
        return globals;
    }

    public void setGlobals(List<String> globals) {
        this.globals = globals;
    }

    public Variable getVariableFromId(String id) {
        for (Variable variable : variables) {
            if (variable.getId().equals(id)) {
                return variable;
            }
        }
        return null;
    }
    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        result.append("Outcome[");
        result.append("intent=" + intent);
        if (variables != null)
            result.append(", variables=" + Arrays.toString(variables));
        if (globals != null)
            result.append(", globals=" + Arrays.toString(globals.toArray(new String[] {})));
        result.append("]");
        return result.toString();
    }
    @Override
    public boolean equals(Object o) {
        return o instanceof Outcome &&
               Objects.deepEquals(this.intent, ((Outcome)o).intent) &&
               Objects.deepEquals(this.variables, ((Outcome)o).variables) &&
               Objects.deepEquals(this.globals, ((Outcome)o).globals);
    }

    // for testing only
    public static Outcome createRoutingTimeoutOutcome() {
        Outcome outcome = new Outcome();
        List<String> globals = new ArrayList<>();
        globals.add(ROUTING_TIMEOUT);
        outcome.setGlobals(globals);
        return outcome;
    }

    @Override
    public int hashCode() {
        int result = intent == null ? 0 : intent.hashCode();
        result ^= variables == null ? 0 : Arrays.hashCode(variables);
        result ^= globals == null ? 0 : globals.hashCode();
        return result;
    }


    public static Outcome createAgentTimeoutOutcome() {
        Outcome outcome = new Outcome();
        List<String> globals = new ArrayList<>();
        globals.add(AGENT_TIMEOUT);
        outcome.setGlobals(globals);
        return outcome;
    }

    @JsonIgnore
    public boolean isAgentTimeout() {
        return globals != null && globals.contains(AGENT_TIMEOUT);
    }
}
