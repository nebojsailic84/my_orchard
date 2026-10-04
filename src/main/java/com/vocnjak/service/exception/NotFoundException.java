package com.vocnjak.service.exception;

public class NotFoundException extends RuntimeException implements CodedException {

    private final String code;
    private final Object[] args;

    public NotFoundException(String code, Object... args) {
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
