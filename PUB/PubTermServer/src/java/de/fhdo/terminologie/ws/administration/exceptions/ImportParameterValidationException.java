/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.fhdo.terminologie.ws.administration.exceptions;

/**
 *
 * @author puraner
 */
public class ImportParameterValidationException extends Exception
{
    public ImportParameterValidationException(){}
    
    public ImportParameterValidationException(String message)
    {
        super(message);
    }
}