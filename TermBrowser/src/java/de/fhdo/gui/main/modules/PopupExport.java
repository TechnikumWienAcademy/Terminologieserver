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
package de.fhdo.gui.main.modules;

import de.fhdo.helper.WebServiceHelper;
import de.fhdo.terminologie.ws.administration.ExportCodeSystemContentRequestType;
import de.fhdo.terminologie.ws.administration.ExportCodeSystemContentResponse.Return;
import de.fhdo.terminologie.ws.administration.ExportParameterType;
import de.fhdo.terminologie.ws.administration.ExportType;
import de.fhdo.terminologie.ws.administration.ExportValueSetContentRequestType;
import de.fhdo.terminologie.ws.administration.ExportValueSetContentResponse;
import de.fhdo.terminologie.ws.administration.LoginType;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Filedownload;
import org.zkoss.zul.Label;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Row;
import org.zkoss.zul.Window;
import types.termserver.fhdo.de.CodeSystem;
import types.termserver.fhdo.de.CodeSystemVersion;
import types.termserver.fhdo.de.ValueSet;
import types.termserver.fhdo.de.ValueSetVersion;

/**
 *
 * @author Becker
 */
public class PopupExport extends GenericForwardComposer
{

  private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();
  private CodeSystemVersion csv;
  private ValueSetVersion vsv;
  private Window window;
  private Combobox cboxFormat;
  private Label lProgress;
  private Row rowProgress;
  private Row rowHinweis;
  SimpleDateFormat sdfFilename = new SimpleDateFormat("yyyyMMdd");

  @Override
  public void doAfterCompose(Component comp) throws Exception
  {
    super.doAfterCompose(comp);
    window = (Window) comp;

    if (arg.get("CSV") != null)
      csv = (CodeSystemVersion) arg.get("CSV");

    if (arg.get("VSV") != null)
      vsv = (ValueSetVersion) arg.get("VSV");

    if (csv != null)
    {
      /* formatId: 193 = ClaML, 194 = CSV, 195 = SVS*/
      cboxFormat.appendItem("CSV");
      cboxFormat.appendItem("ClaML");  // TODO Werte aus Domain lesen
      cboxFormat.appendItem("SVS");
      
      cboxFormat.setSelectedIndex(0);

      //logger.debug("CSV: " + csv.getVersionId() + ", " + csv.getName());
    }
    else if (vsv != null)
    {
      cboxFormat.appendItem("CSV");
      cboxFormat.appendItem("SVS");
      cboxFormat.setSelectedIndex(0);
    }
  }

  /* formatId: 193 = ClaML, 194 = CSV */
  public void export(long formatId)
  {
    if (csv != null)
    {
      ExportCodeSystemContentRequestType parameter = new ExportCodeSystemContentRequestType();

      // Login
      LoginType login = new LoginType();
      login.setSessionID(de.fhdo.helper.SessionHelper.getSessionId());
      if (login.getSessionID().length() > 0)
        parameter.setLogin(login);

      parameter.setCodeSystem(new CodeSystem());
      parameter.getCodeSystem().setId(csv.getCodeSystem().getId());
      CodeSystemVersion csvE = new CodeSystemVersion();
      csvE.setVersionId(csv.getVersionId());
      parameter.getCodeSystem().getCodeSystemVersions().add(csvE);

      // Export Tyep
      ExportType eType = new ExportType();
      eType.setFormatId(formatId);            // TODO 193 = ClaML, 194 = CSV, 195 SVS
      eType.setUpdateCheck(false);
      parameter.setExportInfos(eType);

      // Optional: ExportParameter
      ExportParameterType eParameterType = new ExportParameterType();
      eParameterType.setAssociationInfos("");
      eParameterType.setCodeSystemInfos(true);
      eParameterType.setTranslations(true);
      parameter.setExportParameter(eParameterType);

      logger.debug("Export-Service-Aufruf...");

      // WS-Aufruf
      Return response = WebServiceHelper.exportCodeSystemContent(parameter);

      // WS-Antwort
      try
      {
        if (response.getReturnInfos().getStatus() == de.fhdo.terminologie.ws.administration.Status.OK)
        {
          //Messagebox.show(Labels.getLabel("contentCSVSDefault.exportClaMLSuccessful"));

          rowProgress.setVisible(true);
          rowHinweis.setVisible(false);
          lProgress.setValue(response.getReturnInfos().getMessage());

          
          downloadFile(formatId, response.getExportInfos().getFilecontent(), sdfFilename.format(new Date()) + "_" + csv.getCodeSystem().getName() + "_" + csv.getName());
        }
        else
          Messagebox.show(Labels.getLabel("common.error") + "\n" + response.getReturnInfos().getMessage() + "\n" + Labels.getLabel("contentCSVSDefault.exportClaMLFailed"));
      }
      catch (Exception e)
      {
        logger.error("Fehler beim Speichern einer Datei: " + e.getLocalizedMessage());
      }
    }
    else if(vsv != null)
    {
      // ValueSet-Export
      
      ExportValueSetContentRequestType parameter = new ExportValueSetContentRequestType();

      // Login
      LoginType login = new LoginType();
      login.setSessionID(de.fhdo.helper.SessionHelper.getSessionId());
      if (login.getSessionID().length() > 0)
        parameter.setLogin(login);

      parameter.setValueSet(new ValueSet());
      parameter.getValueSet().setId(vsv.getValueSet().getId());
			
			//Matthias add ValueSetName in parameter
			parameter.getValueSet().setName(vsv.getValueSet().getName());
			
      ValueSetVersion vsvE = new ValueSetVersion();
      vsvE.setVersionId(vsv.getVersionId());
      parameter.getValueSet().getValueSetVersions().add(vsvE);

      // Export Type
      ExportType eType = new ExportType();
      eType.setFormatId(formatId);            // TODO 193 = ClaML, 194 = CSV, 195 = SVS
      eType.setUpdateCheck(false);
      parameter.setExportInfos(eType);

      // Optional: ExportParameter
      ExportParameterType eParameterType = new ExportParameterType();
      eParameterType.setAssociationInfos("");
      eParameterType.setCodeSystemInfos(false);
      eParameterType.setTranslations(false);
      parameter.setExportParameter(eParameterType);

      logger.debug("Export-Service-Aufruf...");

      // WS-Aufruf
      ExportValueSetContentResponse.Return response = WebServiceHelper.exportValueSetContent(parameter);

      // WS-Antwort
      try
      {
        if (response.getReturnInfos().getStatus() == de.fhdo.terminologie.ws.administration.Status.OK)
        {
          rowProgress.setVisible(true);
          rowHinweis.setVisible(false);
          lProgress.setValue(response.getReturnInfos().getMessage());

          downloadFile(formatId, response.getExportInfos().getFilecontent(), sdfFilename.format(new Date()) + "_" + vsv.getValueSet().getName() + "_" + vsv.getName());
        }
        else
          Messagebox.show(Labels.getLabel("common.error") + "\n" + response.getReturnInfos().getMessage() + "\n" + Labels.getLabel("contentCSVSDefault.exportClaMLFailed"));
      }
      catch (Exception e)
      {
        logger.error("Fehler beim Speichern einer Datei: " + e.getLocalizedMessage());
      }
    }

  }

  private void downloadFile(long formatId, byte[] bytes, String name)
  {
		name = name.replace("/", "-");
		
    if (formatId == 193 || formatId == 195)
    {
      Filedownload.save(bytes,
              "application/xml; charset-UTF-8",
              name + ".xml");
    }
    else if (formatId == 194)
    {
      Filedownload.save(bytes,
              "text/csv; charset-UTF-8",
              name + ".csv");
    }
    else
    {
      Filedownload.save(bytes,
              "text/plain",
              name + ".txt");
    }
  }

  /* formatId: 193 = ClaML, 194 = CSV */
  public void onClick$bExport()
  {
    long idFormat;

    if (cboxFormat.getValue().contains("ClaML")) // TODO Wert aus Domain
      idFormat = 193l;
    else if (cboxFormat.getValue().contains("CSV"))
      idFormat = 194l;
    else if (cboxFormat.getValue().contains("SVS"))
      idFormat = 195l;
    else
      return;

    export(idFormat);
  }

  public void onClick$bClose()
  {
    window.detach();
  }
}
