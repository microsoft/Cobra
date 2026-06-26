package com.nuance.ncs.nmsp.engine.pdx;

public class PdxQueryError {
    private final int error;
    private final String description;

    public PdxQueryError(int error, String description) {
        this.error = error;
        this.description = description;
    }

    public int getError() { return error; }
    public String getDescription() { return description; }
}
