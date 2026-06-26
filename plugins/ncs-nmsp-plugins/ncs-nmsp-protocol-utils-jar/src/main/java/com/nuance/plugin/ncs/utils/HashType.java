package com.nuance.plugin.ncs.utils;

public enum HashType {
    HASH_TYPE_NONE("none"),
    HASH_TYPE_SHA256("sha256"),
    HASH_TYPE_NCS("ncsHash");

    private final String value;

    HashType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static HashType fromValue(String value) {
        for (HashType hashType : HashType.values()) {
            if (hashType.getValue().equals(value)) {
                return hashType;
            }
        }
        throw new IllegalArgumentException("Invalid hash type: " + value);
    }
}


