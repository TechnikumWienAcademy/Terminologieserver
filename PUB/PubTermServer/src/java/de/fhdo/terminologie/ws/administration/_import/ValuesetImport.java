/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.fhdo.terminologie.ws.administration._import;

import de.fhdo.terminologie.db.hibernate.ValueSet;
import static de.fhdo.terminologie.ws.administration._import.AbstractImport.LOGGER;
import de.fhdo.terminologie.ws.administration.exceptions.ImportParameterValidationException;

/**
 *
 * @author puraner
 */
public class ValuesetImport extends AbstractImport
{
    protected ValueSet _valueset;
    
    public ValuesetImport()
    {
        super();
    }
    
    @Override
    protected void validateParameters() throws ImportParameterValidationException
    {
        LOGGER.info("validateParameters started");

        try
        {
            super.validateParameters();
        }
        catch(ImportParameterValidationException ex)
        {
            throw ex;
        }

        if (this._valueset == null)
        {
            throw new ImportParameterValidationException("Codesystem must not be null.");
        }
    }

    public ValueSet getValueset()
    {
        return _valueset;
    }

    public void setValueset(ValueSet _valueset)
    {
        this._valueset = _valueset;
    }
    
    
}
