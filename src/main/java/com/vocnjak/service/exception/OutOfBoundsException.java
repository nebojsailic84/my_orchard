package com.vocnjak.service.exception;

public class OutOfBoundsException extends RuntimeException implements CodedException {

    private final String code;
    private final Object[] args;

    public OutOfBoundsException(String code, Object... args) {
        super(code);
        this.code = code;
        this.args = args;
    }

    public String getCode() {
        return code;
    }

    public Object[] getArgs() {
        return args;
    }
}
