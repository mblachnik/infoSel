/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.prules.exceptions;

/**
 * @author Marcin
 */
public class IncorrectAttributeException extends RuntimeException {

    public IncorrectAttributeException() {
    }

    public IncorrectAttributeException(String message) {
        super(message);
    }

    public IncorrectAttributeException(String message, Throwable cause) {
        super(message, cause);
    }

    public IncorrectAttributeException(Throwable cause) {
        super(cause);
    }

    public IncorrectAttributeException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }


    @Override
    public String getMessage() {
        String msg = "Incorrect attribute: ";
        msg += super.getMessage(); //To change body of generated methods, choose Tools | Templates.
        return msg;
    }
}
