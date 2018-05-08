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
package de.fhdo.terminologie.ws.administration._import;

import de.fhdo.logging.Logger4j;
import de.fhdo.terminologie.ws.administration.types.ImportCodeSystemRequestType;
import de.fhdo.terminologie.ws.administration.types.ImportCodeSystemResponseType;
import de.fhdo.terminologie.ws.authoring.CreateCodeSystem;
import de.fhdo.terminologie.ws.authoring.types.CreateCodeSystemRequestType;
import de.fhdo.terminologie.ws.authoring.types.CreateCodeSystemResponseType;
import de.fhdo.terminologie.ws.types.ReturnType;
import org.apache.log4j.Logger;

/**
 *
 * @author Robert Mützner (robert.muetzner@fh-dortmund.de)
 */
public class ImportCreateCodeSystem
{

  private static Logger logger = Logger4j.getInstance().getLogger();

  public static boolean createCodeSystem(org.hibernate.Session hb_session, ImportCodeSystemRequestType parameter, ImportCodeSystemResponseType response)
  {
    boolean erfolg = true;
    
    try
    {
      if (logger.isDebugEnabled())
        logger.debug("createCodeSystem: " + parameter.getCodeSystem().getName());

      // TODO zunächst prüfen, ob CodeSystem bereits existiert
      CreateCodeSystemRequestType request = new CreateCodeSystemRequestType();
      request.setCodeSystem(parameter.getCodeSystem());
      request.setLogin(parameter.getLogin());

      //Code System erstellen
      CreateCodeSystem ccs = new CreateCodeSystem();
      CreateCodeSystemResponseType resp = ccs.CreateCodeSystem(request, hb_session);

      if (resp.getReturnInfos().getStatus() != ReturnType.Status.OK)
      {
        return false;
      }
      parameter.setCodeSystem(resp.getCodeSystem());
      response.setCodeSystem(resp.getCodeSystem());

      logger.debug("Neue CodeSystem-ID: " + resp.getCodeSystem().getId());
    }
    catch (Exception e)
    {
      erfolg = false;
    }
    //logger.debug("Neue CodeSystemVersion-ID: " + ((CodeSystemVersion) resp.getCodeSystem().getCodeSystemVersions().toArray()[0]).getVersionId());
    return erfolg;
  }
}
