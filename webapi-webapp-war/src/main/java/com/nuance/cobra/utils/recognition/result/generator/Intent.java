package com.nuance.cobra.utils.recognition.result.generator;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Intent {
    @JsonProperty("id")
    private String id;
    @JsonProperty("value")
    private String value;

    public Intent() {
        //empty method
    }

    public Intent(String id, String value) {
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
    public String toString() {
        StringBuilder result = new StringBuilder();
        result.append("Intent[");
        result.append("id=" + id + ",");
        result.append("value=" + value);
        result.append("]");
        return result.toString();
    }
    @Override
    public boolean equals(Object o) {
        return o instanceof Intent &&
               //this.id.equals(((Intent)o).id) && // id is merely to accomodate external systems
               this.value.equals(((Intent)o).value);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + ((value == null) ? 0 : value.hashCode());
        return result;
    }

}
