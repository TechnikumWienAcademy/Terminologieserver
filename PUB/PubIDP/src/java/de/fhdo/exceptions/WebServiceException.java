/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.fhdo.exceptions;

/**
 *
 * @author puraner
 */
public class WebServiceException extends Exception
{
    public WebServiceException(){}
    
    public WebServiceException(String message)
    {
        super(message);
    }
}
