/* 
 * CTS2 based Terminology Server and Terminology Browser
 * Copyright (C) 2013 FH Dortmund: Peter Haas, Robert Muetzner
 * government-funded by the Ministry of Health of Germany
 * government-funded by the Ministry of Health, Equalities, Care and Ageing of North Rhine-Westphalia and the European Union
 * 
 * This program is free software: you can redistribute it and/or modify it 
 * under the terms of the GNU General Public License as published by the 
 * Free Software Foundation, either version 3 of the License, or (at your 
 * option) any later version.
 *  
 * This program is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY 
 * or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License 
 * for more details.
 *  
 * You should have received a copy of the GNU General Public License 
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package de.fhdo.terminologie.ws.authoring;

import de.fhdo.terminologie.db.HibernateUtil;
import de.fhdo.terminologie.db.hibernate.ValueSet;
import de.fhdo.terminologie.db.hibernate.ValueSetVersion;
import de.fhdo.terminologie.helper.LastChangeHelper;
import de.fhdo.terminologie.helper.LoginHelper;
import de.fhdo.terminologie.ws.authoring.types.UpdateValueSetStatusRequestType;
import de.fhdo.terminologie.ws.authoring.types.UpdateValueSetStatusResponseType;
import de.fhdo.terminologie.ws.types.ReturnType;
import java.util.List;
import java.util.Set;

/**
 * TODO Javadoc
 * TODO english translation
 * @author Mathias Aschhoff
 */
public class UpdateValueSetStatus
{

  final private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();

  public UpdateValueSetStatusResponseType updateValueSetStatus(UpdateValueSetStatusRequestType parameter)
  {
    if (logger.isInfoEnabled())
    {
      logger.info("====== UpdateValueSetStatus started ======");
    }

    UpdateValueSetStatusResponseType response = new UpdateValueSetStatusResponseType();
    response.setReturnInfos(new ReturnType());

    //Checking parameters
    if (validateParameter(parameter, response) == false)
    {
      return response; // Parameter check failed
    }

    // Check login
    //3.2.17 added second check
    if (parameter != null)
    {
      if (LoginHelper.getInstance().doLogin(parameter.getLogin(), response.getReturnInfos(), true) == false)
        return response;
    }

    try
    {
      ValueSet vs = parameter.getValueSet();
      ValueSetVersion vsv = null;

      if (vs.getValueSetVersions() != null && vs.getValueSetVersions().size() > 0)
        vsv = (ValueSetVersion) vs.getValueSetVersions().toArray()[0];

      // Opening hibernate session
      org.hibernate.Session hb_session = HibernateUtil.getSessionFactory().openSession();
      hb_session.getTransaction().begin();
      
      try
      {
        boolean changedVSStatus = false;
        // Reading VS and VSV from DB, changing status and saving them again
        ValueSet vs_db = null;
        
        if(vs.getId() != null){
           vs_db = (ValueSet) hb_session.get(ValueSet.class, vs.getId());

            if (vs.getStatus() != null)
            {
              vs_db.setStatus(vs.getStatus());
              hb_session.update(vs_db);
            }
        }else{
        
            String hql = "select distinct vsv from ValueSetVersion vsv join fetch vsv.valueSet vs WHERE vsv.versionId=" + vsv.getVersionId();
            org.hibernate.Query q = hb_session.createQuery(hql);
            List vsv_list = q.list();
            if(!vsv_list.isEmpty()){
                vs_db = ((ValueSetVersion)vsv_list.get(0)).getValueSet();
            }
        }
        
        if (vsv != null)
        {
          ValueSetVersion vsv_db = (ValueSetVersion) hb_session.get(ValueSetVersion.class, vsv.getVersionId());
          
          if(vsv.getStatus() != null){
            vsv_db.setStatus(vsv.getStatus());
            hb_session.update(vsv_db);
          }
					
          //Matthias: uncommented based on Request from ELGA
          //ChangePreviousVersionStatus(vsv_db, hb_session, vsv.getStatus());

          // prüfen, ob ValueSet-Version die letzte Version ist, dann auch VS-Status ändern (!)
          String hql = "select distinct vsv from ValueSetVersion vsv where vsv.previousVersionId=" + vsv_db.getVersionId();
          org.hibernate.Query q = hb_session.createQuery(hql);
          List vsv_list = q.list();
          if (vsv_list == null || vsv_list.size() == 0)
          {
            // ValueSetVersion ist letzte Version, jetzt also auch ValueSet-Status ändern
            vs_db.setStatus(vsv.getStatus());
            hb_session.update(vs_db);
            changedVSStatus = true;
          }
          LastChangeHelper.updateLastChangeDate(false, vsv_db.getVersionId(),hb_session);
        }
        hb_session.getTransaction().commit();

        response.getReturnInfos().setCount(1);
        response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.INFO);
        response.getReturnInfos().setStatus(ReturnType.Status.OK);
        
        response.getReturnInfos().setMessage("Status erfolgreich aktualisiert.");
        if(changedVSStatus)
          response.getReturnInfos().setMessage(response.getReturnInfos().getMessage() + "\nStatus der ValueSet-Version wurde ebenfalls aktualisiert, da alle ValueSet-Versionen von der Ã„nderung betroffen waren.");
      }
      catch (Exception e)
      {
          if(!hb_session.getTransaction().wasRolledBack())
             hb_session.getTransaction().rollback();
        // Fehlermeldung an den Aufrufer weiterleiten
        response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.ERROR);
        response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
        response.getReturnInfos().setMessage("Fehler bei 'UpdateValueSetStatus': " + e.getLocalizedMessage());

        logger.error("Fehler bei 'UpdateValueSetStatus' a: " + e.getLocalizedMessage());
      }
      finally
      {
          if(hb_session.isOpen())
            hb_session.close();
      }
    }
    catch (Exception e)
    {
      // Fehlermeldung an den Aufrufer weiterleiten
      response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.ERROR);
      response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
      response.getReturnInfos().setMessage("Fehler bei 'UpdateValueSetStatus': " + e.getLocalizedMessage());

      logger.error("Fehler bei 'UpdateValueSetStatus': b" + e.getLocalizedMessage());
    }

    // Alles OK
    response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.INFO);
    response.getReturnInfos().setStatus(ReturnType.Status.OK);
    response.getReturnInfos().setMessage("Status erfolgreich geändert");
    
    return response;
  }

  private void ChangePreviousVersionStatus(ValueSetVersion vsv, org.hibernate.Session hb_session, int new_status)
  {
    if (vsv != null && vsv.getPreviousVersionId() != null && vsv.getPreviousVersionId() > 0)
    {
      ValueSetVersion vsv_db = (ValueSetVersion) hb_session.get(ValueSetVersion.class, vsv.getPreviousVersionId());
      vsv_db.setStatus(new_status);
      hb_session.update(vsv_db);

      ChangePreviousVersionStatus(vsv_db, hb_session, new_status);
    }
  }

  private boolean validateParameter(UpdateValueSetStatusRequestType Request, UpdateValueSetStatusResponseType Response)
  {
    boolean erfolg = true;

    ValueSet vs = Request.getValueSet();
    if (vs == null)
    {
      Response.getReturnInfos().setMessage("ValueSet darf nicht NULL sein!");
      erfolg = false;
    }

    Set<ValueSetVersion> vsvSet = vs.getValueSetVersions();
    if (vsvSet.size() > 1)
    {
      Response.getReturnInfos().setMessage(
              "Die ValueSet-Version-Liste darf maximal einen Eintrag haben!");
      erfolg = false;
    }
    else if (vsvSet.size() == 1)
    {
      ValueSetVersion vsv = (ValueSetVersion) vs.getValueSetVersions().toArray()[0];

      if (vsv.getVersionId() == null || vsv.getVersionId() == 0)
      {
        Response.getReturnInfos().setMessage(
                "Es muss eine ID für die ValueSet-Version angegeben werden!");
        erfolg = false;
      }

      if (vsv.getStatus() == null)
      {
        Response.getReturnInfos().setMessage(
                "Es muss ein Status für die ValueSet-Version angegeben werden!");
        erfolg = false;
      }
    }

    if (erfolg == false)
    {
      Response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.WARN);
      Response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
    }
    return erfolg;
  }
}
