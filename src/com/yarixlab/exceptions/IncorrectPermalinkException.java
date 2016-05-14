package com.yarixlab.exceptions;

import javax.servlet.ServletException;

public class IncorrectPermalinkException extends ServletException {

    public IncorrectPermalinkException(String message) {
        super(message);
    }
}
