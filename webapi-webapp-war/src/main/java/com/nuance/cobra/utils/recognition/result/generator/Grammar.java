package com.nuance.cobra.utils.recognition.result.generator;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Grammar {
    @JsonProperty("id")
    private String id;

    @JsonProperty("ref")
    private String ref;

    public Grammar(String id, String ref) {
        this.id = id;
        this.ref = ref;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getRef() {
        return ref;
    }

    public void setRef(String ref) {
        this.ref = ref;
    }
}
