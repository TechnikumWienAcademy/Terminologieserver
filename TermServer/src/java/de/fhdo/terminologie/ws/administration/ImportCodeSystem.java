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
package de.fhdo.terminologie.ws.administration;

import de.fhdo.terminologie.helper.LoginHelper;
import de.fhdo.terminologie.ws.administration._import.ImportCSSVS;
import de.fhdo.terminologie.ws.administration._import.ImportCSV_ELGA;
import de.fhdo.terminologie.ws.administration._import.ImportCSV_FHDo;
import de.fhdo.terminologie.ws.administration._import.ImportClaml;
import de.fhdo.terminologie.ws.administration._import.ImportICDBMGAT;
import de.fhdo.terminologie.ws.administration._import.ImportKAL;
import de.fhdo.terminologie.ws.administration._import.ImportKBV;
import de.fhdo.terminologie.ws.administration._import.ImportLOINC;
import de.fhdo.terminologie.ws.administration._import.ImportLeiKatAt;
import de.fhdo.terminologie.ws.administration.types.ImportCodeSystemRequestType;
import de.fhdo.terminologie.ws.administration.types.ImportCodeSystemResponseType;
import de.fhdo.terminologie.ws.types.LoginInfoType;
import de.fhdo.terminologie.ws.types.ReturnType;

/**
 * 28.03.2012, Mï¿½tzner: Hinzufï¿½gen vom LOINC-Import 27.02.2013, Mï¿½tzner: KBV
 * Keytabs hinzugefügt
 *
 * @author Robert Mützner (robert.muetzner@fh-dortmund.de)
 */
public class ImportCodeSystem{
    private static final org.apache.log4j.Logger LOGGER = de.fhdo.logging.Logger4j.getInstance().getLogger();

    public ImportCodeSystemResponseType ImportCodeSystem(ImportCodeSystemRequestType parameter){
        LOGGER.info("+++++ ImportCodeSystem gestartet +++++");

    // Return-Informationen anlegen
    ImportCodeSystemResponseType response = new ImportCodeSystemResponseType();
    response.setReturnInfos(new ReturnType());
    
//    if (StaticStatus.importRunning)
//    {
//      // Fehlermeldung ausgeben (Import kann nur 1x laufen)
//      response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.WARN);
//      response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
//      response.getReturnInfos().setMessage("Ein Import läuft bereits. Warten Sie darauf, bis dieser beendet ist und versuchen Sie es anschließend erneut.");
//      return response;
//    }

    // Parameter prï¿½fen
    if (validateParameter(parameter, response) == false)
    {
      return response; // Fehler bei den Parametern
    }

    // Login-Informationen auswerten (gilt fï¿½r jeden Webservice)
    boolean loggedIn = false;
    LoginInfoType loginInfoType = null;
    
    if (parameter != null && parameter.getLogin() != null)
    {
      loginInfoType = LoginHelper.getInstance().getLoginInfos(parameter.getLogin());
      loggedIn = loginInfoType != null;

      if (loggedIn)
      {

        if (loginInfoType!=null && loginInfoType.getTermUser().isIsAdmin())
        {
          loggedIn = true;
        }
        else
        {
          loggedIn = false;
        }
      }
    }    
    
    LOGGER.debug("Eingeloggt: " + loggedIn);

    if (loggedIn == false)
    {
      response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.WARN);
      response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
      response.getReturnInfos().setMessage("Für diesen Dienst müssen Sie am Terminologieserver angemeldet sein!");
      return response;
    }

    long formatId = parameter.getImportInfos().getFormatId();

    if (formatId == ImportCodeSystemRequestType.IMPORT_CLAML_ID)
    {
      try
      {
        //StaticStatusList.getStatus(parameter.getCodeSystem().getId()).importRunning = true;
        ImportClaml importClaml = new ImportClaml(parameter);

        response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.INFO);
        response.getReturnInfos().setStatus(ReturnType.Status.OK);
        response.setCodeSystem(importClaml.getCodeSystem());
        
        long id = 0;
        if(parameter.getCodeSystem().getId() != null)
        {
            id = parameter.getCodeSystem().getId();
        }
        ImportStatus status = StaticStatusList.getStatus(id);
        if (status != null && status.cancel)
          response.getReturnInfos().setMessage("Import abgebrochen.");
        else
          response.getReturnInfos().setMessage("Import abgeschlossen.");
        response.getReturnInfos().setCount(importClaml.getCountImported());
      }
      catch (Exception e)
      {
        response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.WARN);
        response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
        response.getReturnInfos().setMessage("Fehler beim Import: " + e.getLocalizedMessage());
      }
    }
    else if (formatId == ImportCodeSystemRequestType.IMPORT_CSV_ID)
    {
      try
      {
        String s = "";
        int countImported = 0;
        
        
          ImportCSV_ELGA importCSV = new ImportCSV_ELGA(parameter);

          s = importCSV.importCSV(response);
          countImported = importCSV.getCountImported();
        

        if (s.length() == 0)
        {
          response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.INFO);
          response.getReturnInfos().setStatus(ReturnType.Status.OK);
          response.getReturnInfos().setCount(countImported);
        }
        else
        {
          response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.WARN);
          response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
          response.getReturnInfos().setMessage("Fehler beim Import: " + s);
        }
      }
      catch (Exception e)
      {
        response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.WARN);
        response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
        response.getReturnInfos().setMessage("Fehler beim Import: " + e.getLocalizedMessage());

        e.printStackTrace();
      }
    }
    else if (formatId == ImportCodeSystemRequestType.IMPORT_LOINC_ID)
    {
      try
      {
        ImportLOINC importLOINC = new ImportLOINC(parameter);

        String s = importLOINC.importLOINC(response);

        if (s.length() == 0)
        {
          response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.INFO);
          response.getReturnInfos().setStatus(ReturnType.Status.OK);
          response.getReturnInfos().setCount(importLOINC.getCountImported());
        }
        else
        {
          response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.WARN);
          response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
          response.getReturnInfos().setMessage("Fehler beim Import: " + s);
        }
        LOGGER.warn("LOINC Import-Ende");
      }
      catch (Exception e)
      {
        response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.WARN);
        response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
        response.getReturnInfos().setMessage("Fehler beim Import: " + e.getLocalizedMessage());

        e.printStackTrace();
      }
    }
    else if (formatId == ImportCodeSystemRequestType.IMPORT_LOINC_RELATIONS_ID)
    {
      try
      {
        ImportLOINC importLOINC = new ImportLOINC(parameter);

        String s = importLOINC.importLOINC_Associations(response);

        if (s.length() == 0)
        {
          response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.INFO);
          response.getReturnInfos().setStatus(ReturnType.Status.OK);
          response.getReturnInfos().setCount(importLOINC.getCountImported());
        }
        else
        {
          response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.WARN);
          response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
          response.getReturnInfos().setMessage("Fehler beim Import: " + s);
        }
        LOGGER.warn("LOINC-Associations Import-Ende");
      }
      catch (Exception e)
      {
        response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.WARN);
        response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
        response.getReturnInfos().setMessage("Fehler beim Import: " + e.getLocalizedMessage());

        e.printStackTrace();
      }
    }
    else if (formatId == ImportCodeSystemRequestType.IMPORT_KBV_KEYTABS_ID)
    {
      try
      {
        //StaticStatus.importRunning = true;
        ImportKBV importKBV = new ImportKBV(parameter);

        String s = importKBV.importXML(response);

        if (s.length() == 0)
        {
          response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.INFO);
          response.getReturnInfos().setStatus(ReturnType.Status.OK);
          response.getReturnInfos().setCount(importKBV.getCountImported());
          //response.getReturnInfos().setMessage("Import abgeschlossen.");
        }
        else
        {
          response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.WARN);
          response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
          response.getReturnInfos().setMessage("Fehler beim Import: " + s);
        }
      }
      catch (Exception e)
      {
        response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.WARN);
        response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
        response.getReturnInfos().setMessage("Fehler beim Import: " + e.getLocalizedMessage());

        e.printStackTrace();
      }
    }
    else if (formatId == ImportCodeSystemRequestType.IMPORT_SVS_ID)
    {

      try
      {
        ImportCSSVS importCS_SVS = new ImportCSSVS(parameter);
        String s = importCS_SVS.importSVS(response);

        if (s.length() == 0)
        {
          response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.INFO);
          response.getReturnInfos().setStatus(ReturnType.Status.OK);
          response.getReturnInfos().setCount(importCS_SVS.getCountImported());
          //response.getReturnInfos().setMessage("Import abgeschlossen.");
        }
        else
        {
          response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.WARN);
          response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
          response.getReturnInfos().setMessage("Fehler beim Import: " + s);
        }
      }
      catch (Exception e)
      {
        response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.WARN);
        response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
        response.getReturnInfos().setMessage("Fehler beim Import: " + e.getLocalizedMessage());

        e.printStackTrace();
      }

    }
    else if (formatId == ImportCodeSystemRequestType.IMPORT_LeiKat_ID)
    {

      try
      {
        ImportLeiKatAt importLeiKatAt = new ImportLeiKatAt(parameter);
        String s = importLeiKatAt.importLeiKatAt(response);

        if (s.length() == 0)
        {
          response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.INFO);
          response.getReturnInfos().setStatus(ReturnType.Status.OK);
          response.getReturnInfos().setCount(importLeiKatAt.getCountImported());
          //response.getReturnInfos().setMessage("Import abgeschlossen.");
        }
        else
        {
          response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.WARN);
          response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
          response.getReturnInfos().setMessage("Fehler beim Import: " + s);
        }
      }
      catch (Exception e)
      {
        response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.WARN);
        response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
        response.getReturnInfos().setMessage("Fehler beim Import: " + e.getLocalizedMessage());

        e.printStackTrace();
      }

    }
    else if (formatId == ImportCodeSystemRequestType.IMPORT_KAL_ID)
    {

      try
      {
        ImportKAL importKAL = new ImportKAL(parameter);
        String s = importKAL.importKAL(response);

        if (s.length() == 0)
        {
          response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.INFO);
          response.getReturnInfos().setStatus(ReturnType.Status.OK);
          response.getReturnInfos().setCount(importKAL.getCountImported());
          //response.getReturnInfos().setMessage("Import abgeschlossen.");
        }
        else
        {
          response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.WARN);
          response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
          response.getReturnInfos().setMessage("Fehler beim Import: " + s);
        }
      }
      catch (Exception e)
      {
        response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.WARN);
        response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
        response.getReturnInfos().setMessage("Fehler beim Import: " + e.getLocalizedMessage());

        e.printStackTrace();
      }

    }
    else if (formatId == ImportCodeSystemRequestType.IMPORT_ICD_BMG_ID)
    {

      try
      {
        ImportICDBMGAT importICDBMGAT = new ImportICDBMGAT(parameter);
        String s = importICDBMGAT.importICDBMGAT(response);

        if (s.length() == 0)
        {
          response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.INFO);
          response.getReturnInfos().setStatus(ReturnType.Status.OK);
          response.getReturnInfos().setCount(importICDBMGAT.getCountImported());
          //response.getReturnInfos().setMessage("Import abgeschlossen.");
        }
        else
        {
          response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.WARN);
          response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
          response.getReturnInfos().setMessage("Fehler beim Import: " + s);
        }
      }
      catch (Exception e)
      {
        response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.WARN);
        response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
        response.getReturnInfos().setMessage("Fehler beim Import: " + e.getLocalizedMessage());

        e.printStackTrace();
      }

    }
    else
    {
      response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.WARN);
      response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
      response.getReturnInfos().setMessage("Das Import-Format mit folgender ID ist unbekannt: " + formatId + "\n" + ImportCodeSystemRequestType.getPossibleFormats());
    }

    if(parameter.getCodeSystem().getId() != null)
    {
        if(StaticStatusList.getStatus(parameter.getCodeSystem().getId()) != null)
            StaticStatusList.getStatus(parameter.getCodeSystem().getId()).importRunning = false;
    }
    else
    {
        if(StaticStatusList.getStatus(0L) != null)
            StaticStatusList.getStatus(0L).importRunning = false;
    }
    return response;
  }

  /**
   * Prï¿½ft die Parameter anhand der Cross-Reference
   *
   * @param Request
   * @param Response
   * @return false, wenn fehlerhafte Parameter enthalten sind
   */
  private boolean validateParameter(ImportCodeSystemRequestType Request,
          ImportCodeSystemResponseType Response)
  {
    boolean erfolg = true;

    if (Request.getImportInfos() == null)
    {
      Response.getReturnInfos().setMessage("ImportInfos darf nicht NULL sein!");
      erfolg = false;
    }
    else
    {
      /*if (Request.getImportInfos().getFilecontent() == null)
       {
       Response.getReturnInfos().setMessage("Sie mï¿½ssen eine Datei anhängen (filecontent)!");
       erfolg = false;
       }
       else*/ if (Request.getImportInfos().getFormatId() == null || Request.getImportInfos().getFormatId() == 0)
      {
        // TODO auf gï¿½ltiges Format prï¿½fen
        Response.getReturnInfos().setMessage("Sie mï¿½ssen ein Import-Format angeben!");
        erfolg = false;
      }
    }

    if (Request.getLogin() == null || Request.getLogin().getSessionID() == null || Request.getLogin().getSessionID().length() == 0)
    {
      Response.getReturnInfos().setMessage("Login darf nicht NULL sein und es muss eine Session-ID angegeben sein!");
      erfolg = false;
    }

    if (erfolg == false)
    {
      Response.getReturnInfos().setOverallErrorCategory(ReturnType.OverallErrorCategory.WARN);
      Response.getReturnInfos().setStatus(ReturnType.Status.FAILURE);
    }

    return erfolg;
  }
}
