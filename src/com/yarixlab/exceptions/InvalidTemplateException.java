package com.yarixlab.exceptions;

import javax.servlet.ServletException;

public class InvalidTemplateException extends ServletException {
    public InvalidTemplateException (String message) {
        super(message);
    }
}
