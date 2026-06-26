package com.nuance.ncs.nmsp.engine.pdx;

import com.nuance.ncs.nmsp.pdx.NmspDictionary;

public class PdxQueryResult {
    private final NmspDictionary data;
    private final boolean isFinal;

    public PdxQueryResult(NmspDictionary data, boolean isFinal) {
        this.data = data;
        this.isFinal = isFinal;
    }

    public NmspDictionary getData() { return data; }
    public boolean isFinal() { return isFinal; }
}
