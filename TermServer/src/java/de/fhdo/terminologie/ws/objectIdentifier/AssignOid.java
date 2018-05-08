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
package de.fhdo.terminologie.ws.objectIdentifier;

import de.fhdo.terminologie.db.HibernateUtil;
import de.fhdo.terminologie.db.hibernate.CodeSystemVersion;
import de.fhdo.terminologie.db.hibernate.ValueSetVersion;
import de.fhdo.terminologie.helper.LoginHelper;
import de.fhdo.terminologie.ws.objectIdentifier.types.AssignOidRequestType;
import de.fhdo.terminologie.ws.objectIdentifier.types.AssignOidResponseType;
import de.fhdo.terminologie.ws.types.LoginInfoType;
import de.fhdo.terminologie.ws.types.OidInformation;
import de.fhdo.terminologie.ws.types.ReturnType;

/**
 *
 * @author Philipp Urbauer
 */
public class AssignOid
{

  private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();

  public AssignOidResponseType AssignOid(AssignOidRequestType parameter)
  {
    
    if (logger.isInfoEnabled())
      logger.info("====== AssignOid gestartet ======");

    // Return-Informationen anlegen
    AssignOidResponseType response = new AssignOidResponseType();
    response.setReturnInfos(new ReturnType());
    
    
    /*boolean loggedIn = false;
    LoginInfoType loginInfoType = null;
    if (parameter != null && parameter.getLogin() != null)
    {
        loginInfoType = LoginHelper.getInstance().getLoginInfos(parameter.getLogin());
        loggedIn = loginInfoType != null;
    }

    if (loggedIn == false)
    {
        // Benutzer muss für diesen Webservice eingeloggt sein
        response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.WARN);
        response.getReturnInfos().setStatus(ReturnType.Status.OK);
        response.getReturnInfos().setMessage("Sie müssen mit Administrationsrechten am Terminologieserver angemeldet sein, um diesen Service nutzen zu können.");
        return response;
    }*/

    // Parameter prüfen
    if (validateParameter(parameter, response) == false)
    {
      return response; // Fehler bei den Parametern
    }
    // Hibernate-Block, Session öffnen
    org.hibernate.Session hb_session = null;
    
    try
    {      
        hb_session = HibernateUtil.getSessionFactory().openSession();
        hb_session.getTransaction().begin();
        OidInformation oi = parameter.getOidInformation();
        if(oi.getVersionType().equals("CodeSystemVersion")){
        
            CodeSystemVersion csv = (CodeSystemVersion)hb_session.get(CodeSystemVersion.class, oi.getVersionId());
            csv.setOid(oi.getOid());
            
            hb_session.update(csv);
            
            response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.INFO);
            response.getReturnInfos().setStatus(ReturnType.Status.OK);
            response.getReturnInfos().setMessage("OID wurde der CodeSystemVersion erfolgreich zugewiesen!");
        }else if(oi.getVersionType().equals("ValueSetVersion")){
        
            ValueSetVersion vsv = (ValueSetVersion)hb_session.get(ValueSetVersion.class, oi.getVersionId());
            vsv.setOid(oi.getOid());
            
            hb_session.update(vsv);
            
            response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.INFO);
            response.getReturnInfos().setStatus(ReturnType.Status.OK);
            response.getReturnInfos().setMessage("OID wurde der ValueSetVersion erfolgreich zugewiesen!");
        }else{
        
            response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.INFO);
            response.getReturnInfos().setStatus(ReturnType.Status.OK);
            response.getReturnInfos().setMessage("Es wurde keine OID gespeichert!");
        }
        
        hb_session.getTransaction().commit();
    }
    catch (Exception e)
    {
      // Fehlermeldung an den Aufrufer weiterleiten
      response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.ERROR);
      response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
      response.getReturnInfos().setMessage("Fehler bei 'CreateConcept', Hibernate: " + e.getLocalizedMessage());
      
      logger.error("Fehler bei 'AssignOid', Hibernate: " + e.getLocalizedMessage());
    }
    finally
    {
        if(hb_session != null)
            hb_session.close();
    }
    
    return response;
  }

  /**
   * Prüft die Parameter anhand der Cross-Reference
   *
   * @param Request
   * @param Response
   * @return false, wenn fehlerhafte Parameter enthalten sind
   */
  private boolean validateParameter(AssignOidRequestType Request, AssignOidResponseType Response)
  {
        boolean erfolg = true;

        OidInformation oi = Request.getOidInformation();
        if (oi == null)
        {
          Response.getReturnInfos().setMessage("OidInformation darf nicht NULL sein!");
          erfolg = false;
        }
        else
        {
          if(oi.getOid() == null || oi.getOid().length() == 0){
              Response.getReturnInfos().setMessage("OID muss angegeben werden!");
              erfolg = false;
          }else{

              if(!(oi.getVersionType().equals("CodeSystemVersion") || oi.getVersionType().equals("ValueSetVersion"))){

                  Response.getReturnInfos().setMessage("VersionType darf nur die festgelegten Werte haben!");
                  erfolg = false;
              }else{

                  if(oi.getVersionId() <= 0){
                    Response.getReturnInfos().setMessage("VersionId darf nicht NULL, 0 oder negativ sein!");
                    erfolg = false;
                  }
              }
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
