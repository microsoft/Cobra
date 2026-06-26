package com.nuance.ncs.nmsp.engine.pdx;

public class PdxQueryRetry {
    private final String name;
    private final int cause;
    private final String prompt;

    public PdxQueryRetry(String name, int cause, String prompt) {
        this.name = name;
        this.cause = cause;
        this.prompt = prompt;
    }

    public String getName() { return name; }
    public int getCause() { return cause; }
    public String getPrompt() { return prompt; }
}
