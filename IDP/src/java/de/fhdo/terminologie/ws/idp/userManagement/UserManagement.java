/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.fhdo.terminologie.ws.idp.userManagement;

import de.fhdo.exceptions.WebServiceException;
import de.fhdo.terminologie.ws.idp.authorizationIDP.types.DeleteUserRequestType;
import de.fhdo.terminologie.ws.idp.authorizationIDP.types.DeleteUserResponseType;
import de.fhdo.terminologie.ws.idp.authorizationIDP.types.GetCollaborationUserRequestType;
import de.fhdo.terminologie.ws.idp.authorizationIDP.types.GetCollaborationUserResponseType;
import de.fhdo.terminologie.ws.idp.authorizationIDP.types.GetTermUserRequestType;
import de.fhdo.terminologie.ws.idp.authorizationIDP.types.GetTermUserResponseType;
import de.fhdo.terminologie.ws.idp.authorizationIDP.types.ReturnType;
import de.fhdo.terminologie.ws.idp.authorizationIDP.types.ReturnType.Status;
import de.fhdo.terminologie.ws.idp.authorizationIDP.types.SaveCollaborationUserRequestType;
import de.fhdo.terminologie.ws.idp.authorizationIDP.types.SaveCollaborationUserResponseType;
import de.fhdo.terminologie.ws.idp.authorizationIDP.types.SaveTermUserRequestType;
import de.fhdo.terminologie.ws.idp.authorizationIDP.types.SaveTermUserResponseType;
import javax.jws.WebService;
import javax.jws.WebMethod;
import javax.jws.WebParam;

/**
 *
 * @author puraner
 */
@WebService(serviceName = "UserManagement")
public class UserManagement
{    
    private static final org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();
    /**
     * This is a sample web service operation
     * @param parameter
     * @return 
     */
    @WebMethod(operationName = "getUserList")
    public GetTermUserResponseType getUserList(@WebParam(name = "parameter") GetTermUserRequestType parameter)
    {
        logger.info("UserManagement Service - getUserList");
        try
        {
            GetTermUserList userList = new GetTermUserList();
            return userList.getUserList(parameter);
        }
        catch (WebServiceException e)
        {
            logger.error(e);
            GetTermUserResponseType resp = new GetTermUserResponseType();
            resp.setReturnInfos(new ReturnType());
            resp.getReturnInfos().setStatus(Status.FAILURE);
            resp.getReturnInfos().setMessage(e.getLocalizedMessage());
            resp.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.ERROR);
            return resp;
        }
    }

    /**
     * Web service operation
     * @param parameter
     * @return 
     */
    @WebMethod(operationName = "saveTermUser")
    public SaveTermUserResponseType saveTermUser(@WebParam(name = "parameter") SaveTermUserRequestType parameter)
    {
        logger.info("UserManagement Service - saveTermUser");
        try
        {
            SaveTermUser updateTermuser = new SaveTermUser();
            return updateTermuser.saveTermUser(parameter);
        }
        catch (WebServiceException e)
        {
            logger.error(e);
            SaveTermUserResponseType resp = new SaveTermUserResponseType();
            resp.setReturnInfos(new ReturnType());
            resp.getReturnInfos().setStatus(Status.FAILURE);
            resp.getReturnInfos().setMessage(e.getLocalizedMessage());
            resp.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.ERROR);
            return resp;
        }
    }

    /**
     * Web service operation
     * @param parameter
     * @return 
     */
    @WebMethod(operationName = "getCollaborationUserList")
    public GetCollaborationUserResponseType getCollaborationUserList(@WebParam(name = "parameter") GetCollaborationUserRequestType parameter)
    {
        logger.info("UserManagement Service - getCollaborationUserList");
        try
        {
            GetCollaborationUserList userList = new GetCollaborationUserList();
            return userList.getUserList(parameter);
        }
        catch (WebServiceException e)
        {
            logger.error(e);
            GetCollaborationUserResponseType resp = new GetCollaborationUserResponseType();
            resp.setReturnInfos(new ReturnType());
            resp.getReturnInfos().setStatus(Status.FAILURE);
            resp.getReturnInfos().setMessage(e.getLocalizedMessage());
            resp.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.ERROR);
            return resp;
        }
    }

    /**
     * Web service operation
     * @param parameter
     * @return 
     */
    @WebMethod(operationName = "saveCollaborationUser")
    public SaveCollaborationUserResponseType saveCollaborationUser(@WebParam(name = "parameter") SaveCollaborationUserRequestType parameter)
    {
        logger.info("UserManagement Service - saveCollaborationUser");
        try
        {
            SaveCollaborationUser saveCollabUser = new SaveCollaborationUser();
            return saveCollabUser.saveCollaborationUser(parameter);
        }
        catch (WebServiceException e)
        {
            logger.error(e);
            SaveCollaborationUserResponseType resp = new SaveCollaborationUserResponseType();
            resp.setReturnInfos(new ReturnType());
            resp.getReturnInfos().setStatus(Status.FAILURE);
            resp.getReturnInfos().setMessage(e.getLocalizedMessage());
            resp.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.ERROR);
            return resp;
        }
    }

    /**
     * Web service operation
     * @param parameter
     * @return 
     */
    @WebMethod(operationName = "deleteUser")
    public DeleteUserResponseType deleteUser(@WebParam(name = "parameter") DeleteUserRequestType parameter)
    {
        logger.info("UserManagement Service - deleteUser");
        try
        {
            DeleteUser deleteUser = new DeleteUser();
            return deleteUser.deleteUser(parameter);
        }
        catch (WebServiceException e)
        {
            logger.error(e);
            DeleteUserResponseType resp = new DeleteUserResponseType();
            resp.setReturnInfos(new ReturnType());
            resp.getReturnInfos().setStatus(Status.FAILURE);
            resp.getReturnInfos().setMessage(e.getLocalizedMessage());
            resp.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.ERROR);
            return resp;
        }
    }
}
