/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.fhdo.terminologie.ws.administration._import;

import de.fhdo.terminologie.ws.administration.exceptions.ImportException;
import de.fhdo.terminologie.ws.administration.exceptions.ImportParameterValidationException;
import de.fhdo.terminologie.ws.administration.types.ImportCodeSystemRequestType;

/**
 *
 * @author puraner
 */
public interface ICodeSystemImport
{
    void setImportData(ImportCodeSystemRequestType request);
    void startImport() throws ImportException, ImportParameterValidationException;
}
