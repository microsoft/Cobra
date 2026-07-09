package com.nuance.plugin.kryptonws.utils.exceptions;

public class InvalidIdException extends Exception {

    public InvalidIdException() {
        super("ID should not be shorter than 1 character.");
    }
}
