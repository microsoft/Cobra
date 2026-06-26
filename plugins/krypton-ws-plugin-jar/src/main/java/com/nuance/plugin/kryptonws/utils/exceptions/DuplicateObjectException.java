package com.nuance.plugin.kryptonws.utils.exceptions;

public class DuplicateObjectException extends Exception{

    private String extra;

    public DuplicateObjectException(String duplicateObject) {
        super(String.format("Duplicate object %s.", duplicateObject));
    }

    public DuplicateObjectException setExtra(String extra) {
        this.extra = extra;
        return this;
    }

    public String getExtra() {
        return this.extra;
    }
}
