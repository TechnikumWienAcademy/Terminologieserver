/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.fhdo.terminologie.ws.administration._import;

import de.fhdo.terminologie.db.hibernate.CodeSystem;
import static de.fhdo.terminologie.ws.administration._import.AbstractImport.LOGGER;
import de.fhdo.terminologie.ws.administration.exceptions.ImportParameterValidationException;

/**
 *
 * @author puraner
 */
public abstract class CodeSystemImport extends AbstractImport
{
    protected CodeSystem codesystem;
    
    public CodeSystemImport()
    {
        super();
    }
    
    /**
     * Calls the super.validateParameters
     * @throws ImportParameterValidationException 
     */
    @Override
    protected void validateParameters() throws ImportParameterValidationException{
        try{
            super.validateParameters();
        }
        catch(ImportParameterValidationException ex){
            throw ex;
        }

        if (this.codesystem == null){
            throw new ImportParameterValidationException("Codesystem must not be null.");
        }
    }
    
    public CodeSystem getCodeSystem()
    {
        if(this.codesystem != null)
        {
            return this.codesystem;
        }
        else
        {
            return new CodeSystem();
        }
    }
    
    public void setCodeSystem(CodeSystem cs)
    {
        this.codesystem = cs;
    }
}
