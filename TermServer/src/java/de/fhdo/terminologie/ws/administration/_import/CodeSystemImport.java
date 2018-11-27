/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.fhdo.terminologie.ws.administration._import;

import de.fhdo.terminologie.db.hibernate.CodeSystem;
import static de.fhdo.terminologie.ws.administration._import.AbstractImport.logger;
import de.fhdo.terminologie.ws.administration.exceptions.ImportParameterValidationException;

/**
 *
 * @author puraner
 */
public abstract class CodeSystemImport extends AbstractImport
{
    protected CodeSystem _codesystem;
    
    public CodeSystemImport()
    {
        super();
    }
    
    @Override
    protected void validateParameters() throws ImportParameterValidationException
    {
        logger.info("validateParameters-function started");

        try
        {
            super.validateParameters();
        }
        catch(ImportParameterValidationException ex)
        {
            throw ex;
        }

        if (this._codesystem == null)
        {
            throw new ImportParameterValidationException("Codesystem must not be null.");
        }
        
        //3.2.20
        logger.debug("validateParameters-function finished");
    }
    
    public CodeSystem getCodeSystem()
    {
        if(this._codesystem != null)
        {
            return this._codesystem;
        }
        else
        {
            return new CodeSystem();
        }
    }
    
    public void setCodeSystem(CodeSystem cs)
    {
        this._codesystem = cs;
    }
}
