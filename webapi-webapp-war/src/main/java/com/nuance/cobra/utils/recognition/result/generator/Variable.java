package com.nuance.cobra.utils.recognition.result.generator;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Variable implements Comparable<Variable> {
    @JsonProperty("id")
    private String id;
    @JsonProperty("value")
    private String value;

    public Variable() {
        //empty method
    }

    public Variable(String id, String value) {
        this.id = id;
        this.value = value;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public int compareTo(Variable v) {
        int compare = id.compareTo(v.id);
        if (compare == 0) {
            compare = value.compareTo(v.value);
        }
        return compare;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Variable && compareTo((Variable)o) == 0;
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        result.append("Variable[");
        result.append("id=" + id + ",");
        result.append("value=" + value);
        result.append("]");
        return result.toString();
    }

    @Override
    public int hashCode() {
        int result = id == null ? 0 : id.hashCode();
        result ^= value == null ? 0 : value.hashCode();
        return result;
    }

}
