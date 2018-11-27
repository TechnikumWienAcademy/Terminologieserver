/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.fhdo.terminologie.ws.administration._import;

import de.fhdo.logging.Logger4j;
import de.fhdo.terminologie.ws.administration.ImportStatus;
import de.fhdo.terminologie.ws.administration.StaticStatusList;
import de.fhdo.terminologie.ws.administration.exceptions.ImportParameterValidationException;
import de.fhdo.terminologie.ws.types.FilecontentListEntry;
import de.fhdo.terminologie.ws.types.ImportType;
import de.fhdo.terminologie.ws.types.LoginType;
import java.util.List;
import org.apache.log4j.Logger;

/**
 *
 * @author puraner
 */
public abstract class AbstractImport
{
    private Long importId;
    private LoginType loginType;
    private ImportType importType;
    protected ImportStatus _status;
    protected static final Logger logger = Logger4j.getInstance().getLogger();
    protected org.hibernate.Session hb_session;
    protected int _aktCount;
    protected byte[] _fileContent;
    protected List<FilecontentListEntry> _fileContentList;

    public AbstractImport()
    {
        this._status = new ImportStatus();
    }
    
    public Long getImportId()
    {
        return importId;
    }

    public void setImportId(Long importId)
    {
        this.importId = importId;
    }

    public LoginType getLoginType()
    {
        return loginType;
    }

    public void setLoginType(LoginType loginType)
    {
        this.loginType = loginType;
    }

    public ImportType getImportType()
    {
        return importType;
    }

    public void setImportType(ImportType importType)
    {
        this.importType = importType;
    }
    
    protected void validateParameters() throws ImportParameterValidationException
    {
        //3.2.20
        logger.debug("validateParameters-function started");
        if(this.importId == null)
        {
            throw new ImportParameterValidationException("ImportId must not be null.");
        }
        
        if(this.loginType == null)
        {
            throw new ImportParameterValidationException("LoginType must not be null.");
        }
        
        if(this.importType == null)
        {
            throw new ImportParameterValidationException("ImportType must not be null.");
        }
        
        if ((this._fileContent == null) && (this._fileContentList == null))
        {
            throw new ImportParameterValidationException("Either Filecontent or FileContentList have to be set.");
        }
        //3.2.20
        logger.debug("validateParamters-function finished");
    }
    
    protected void setTotalCountInStatusList(int totalCount, Long importId)
    {
        StaticStatusList.getStatus(importId).setImportTotal(totalCount);
    }
    
    protected void setCurrentTaskInStatusList(String currentTask, Long importId)
    {
        StaticStatusList.getStatus(importId).setCurrentTask(currentTask);
    }
    
    protected void setCurrentCountInStatusList(int currentCount, Long importId)
    {
        StaticStatusList.getStatus(importId).setImportCount(currentCount);
    }
    
    protected void closeHibernateSession()
    {
        if (this.hb_session != null)
        {
            this.hb_session.close();
        }
    }
    
    protected void rollbackHibernateTransaction()
    {
        if (this.hb_session != null)
        {
            this.hb_session.getTransaction().rollback();
        }
    }
    
    protected int getTotalCountInStatusList(Long importId)
    {
        return StaticStatusList.getStatus(importId).getImportTotal();
    }
}
