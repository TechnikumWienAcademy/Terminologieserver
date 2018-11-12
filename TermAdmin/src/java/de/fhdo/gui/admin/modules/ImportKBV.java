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
package de.fhdo.gui.admin.modules;

import de.fhdo.db.hibernate.CodeSystem;
import de.fhdo.db.hibernate.CodeSystemVersion;
import de.fhdo.helper.SessionHelper;
import de.fhdo.terminologie.ws.administration.ImportCodeSystemRequestType;
import de.fhdo.terminologie.ws.administration.ImportCodeSystemResponse;
import de.fhdo.terminologie.ws.administration.ImportType;
import de.fhdo.terminologie.ws.administration.LoginType;
import ehd._001.KeytabsTyp;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.util.HashSet;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.ws.soap.MTOMFeature;
import org.zkoss.util.media.Media;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.UploadEvent;
import org.zkoss.zk.ui.ext.AfterCompose;
import org.zkoss.zk.ui.util.Configuration;
import org.zkoss.zul.Button;
import org.zkoss.zul.Fileupload;
import org.zkoss.zul.Label;
import org.zkoss.zul.Progressmeter;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Window;


/**
 *
 * @author Robert MÃ¼tzner
 */
public class ImportKBV extends Window implements AfterCompose
{

  private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();
  byte[] bytes;
  CodeSystem selectedCodeSystem;

  public ImportKBV()
  {
  }

  public void onDateinameSelect(Event event)
  {
    try
    {
      
      //Media[] medias = Fileupload.get("Laden Sie eine XML-Datei hoch", "Datei hochladen", 50240);
      
      bytes = null;

      Media media = ((UploadEvent) event).getMedia();
       
      

      //if (medias != null && medias.length > 0)
      if(media != null)
      {
        //Media media = medias[0];
        if(media.getContentType().equals("text/xml") 
                || media.getContentType().equals("application/ms-excel") 
                || media.getContentType().equals("text/csv")
                //Matthias media type added
                || media.getContentType().equals("application/vnd.ms-excel")
                //3.2.20
                || media.getContentType().equals("application/soap+xml")){  
            if (media.isBinary())
            {
              logger.debug("media.isBinary()");

              if (media.inMemory())
              {
                logger.debug("media.getByteData()");

                bytes = media.getByteData();
                //f.setData(media.getByteData());
              }
              else
              {
                logger.debug("media.getStreamData()");

                InputStream input = media.getStreamData();
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                int bytesRead;
                byte[] tempBuffer = new byte[8192 * 2];
                while ((bytesRead = input.read(tempBuffer)) != -1)
                {
                  baos.write(tempBuffer, 0, bytesRead);
                }

                bytes = baos.toByteArray();
                //f.setData(baos.toByteArray());
                baos.close();
              }
            }
            else
            {
              logger.debug("media.isBinary() is false");
              //bytes = media.getStringData().getBytes(Charset.forName("UTF-8"));
              //logger.debug(new String(media.getStringData(),);

              logger.debug("------------------------------------------");
              bytes = media.getStringData().getBytes(Charset.forName("ISO-8859-1"));

              //bytes = media.getStringData().getBytes();
              //bytes = new String(bytes, "UTF-8").getBytes("ISO-8859-1");
              //bytes = media.getStringData().getBytes();
            }
        }
        //CharsetDecoder decoder = Charset.forName("ISO-8859-1").newDecoder();
        //InputStream is = new ByteArrayInputStream(bytes);
        //ByteBuffer bb = ByteBuffer.allocate(EMBEDDED)
        //decoder.decode();
        //String text = new String(bytes, Charset.forName("ISO-8859-1"));
        String text = new String(bytes);
        logger.debug(text);

        //Charset.forName("ISO-8859-1")
        // Datei auswerten
        // XML-Datei laden
        JAXBContext jc = JAXBContext.newInstance("ehd._001");
        Unmarshaller unmarshaller = jc.createUnmarshaller();
        //unmarshaller.setProperty(Marshaller.JAXB_ENCODING, "ISO-8859-1");

        InputStream is = new ByteArrayInputStream(bytes);
        //BufferedReader r = new BufferedReader(
        //        new InputStreamReader(is, "ISO-8859-1"));

        //ByteArrayInputStream input = new ByteArrayInputStream(bytes);
        Object o = unmarshaller.unmarshal(is);

        JAXBElement<ehd._001.KeytabsTyp> doc = (JAXBElement<ehd._001.KeytabsTyp>) o;
        KeytabsTyp root = doc.getValue();
        ehd._001.KeytabTyp keytab = root.getKeytab().get(0);
        
        selectedCodeSystem = new CodeSystem();
        selectedCodeSystem.setName(keytab.getSN());
        selectedCodeSystem.setCodeSystemVersions(new HashSet<de.fhdo.db.hibernate.CodeSystemVersion>());
        CodeSystemVersion csv = new CodeSystemVersion();
        csv.setName(keytab.getSV());
        csv.setOid(keytab.getS());
        selectedCodeSystem.getCodeSystemVersions().add(csv);
        
        for(int i=0;i<keytab.getKey().size() && i<3;++i)
        {
          ((Label)getFellow("labelCode" + (i + 1))).setValue(keytab.getKey().get(i).getV());
          //((Label)getFellow("labelCode" + (i + 1))).setValue(keytab.getKey().get(i).getDN());
          //((Label)getFellow("labelWert" + (i + 1))).setValue(Encoding.GetEncoding("ISO-8859-1").GetString(keytab.getKey().get(i).getDN()));
          ((Label)getFellow("labelWert" + (i + 1))).setValue(keytab.getKey().get(i).getDN());
          //((Label)getFellow("labelWert" + (i + 1))).setValue(new String(keytab.getKey().get(i).getDN().getBytes("ISO-8859-1"), Charset.defaultCharset()));
        }
        
        //r.close();
        is.close();

        logger.debug("ct: " + media.getContentType());
        logger.debug("format: " + media.getFormat());
        logger.debug("byte-length: " + bytes.length);
        logger.debug("bytes: " + bytes);


        Textbox tb = (Textbox) getFellow("textboxDateiname");
        tb.setValue(media.getName());
        
        ((Label)getFellow("labelCodesystem")).setValue(keytab.getSN());
        ((Label)getFellow("labelVersion")).setValue(keytab.getSV());
        ((Label)getFellow("labelOID")).setValue(keytab.getS());
      }
    }
    catch (Exception ex)
    {
      logger.error("Fehler beim Laden eines Dokuments: " + ex.getMessage());
    }

    showStatus();

  }

  private void showStatus()
  {
    String s = "";

    if (bytes == null)
    {
      s += "Bitte wählen Sie eine Datei aus.";
    }

    if (selectedCodeSystem == null)
    {
      s += "\nBitte wählen Sie ein Codesystem aus.";
    }

    ((Label) getFellow("labelImportStatus")).setValue(s);

    ((Button) getFellow("buttonImport")).setDisabled(s.length() > 0);
  }

  public void startImport()
  {
    ((Button) getFellow("buttonImport")).setDisabled(true);
    ((Button) getFellow("buttonCancel")).setVisible(true);

    String session_id = SessionHelper.getValue("session_id").toString();

    String msg = "";
    Progressmeter progress = (Progressmeter) getFellow("progress");

    progress.setVisible(true);

    de.fhdo.terminologie.ws.administration.Administration_Service service = new de.fhdo.terminologie.ws.administration.Administration_Service();
    de.fhdo.terminologie.ws.administration.Administration port = service.getAdministrationPort(new MTOMFeature(true));

    // Login
    ImportCodeSystemRequestType request = new ImportCodeSystemRequestType();
    request.setLogin(new LoginType());
    request.getLogin().setSessionID("" + session_id);
    //logger.debug("Session-ID: ");

    // Codesystem
    request.setCodeSystem(new types.termserver.fhdo.de.CodeSystem());
    //request.getCodeSystem().setId(selectedCodeSystem.getId());
    request.getCodeSystem().setName(selectedCodeSystem.getName());

    types.termserver.fhdo.de.CodeSystemVersion csv = new types.termserver.fhdo.de.CodeSystemVersion();
    csv.setName(((CodeSystemVersion)selectedCodeSystem.getCodeSystemVersions().toArray()[0]).getName());
    csv.setOid(((CodeSystemVersion)selectedCodeSystem.getCodeSystemVersions().toArray()[0]).getOid());
    request.getCodeSystem().getCodeSystemVersions().add(csv);

    // Datei
    request.setImportInfos(new ImportType());
    request.getImportInfos().setFormatId(234l); // KBV-ID
    request.getImportInfos().setFilecontent(bytes);

    ImportCodeSystemResponse.Return response = port.importCodeSystem(request);
    //importWS = (Response<ImportCodeSystemResponse>) port.importCodeSystemAsync(request, this);

    msg = response.getReturnInfos().getMessage();
    logger.debug("Return: " + msg);



    progress.setVisible(false);
    ((Button) getFellow("buttonImport")).setDisabled(false);
    ((Button) getFellow("buttonCancel")).setVisible(false);
    ((Label) getFellow("labelImportStatus")).setValue(msg);


  }

  public void afterCompose()
  {
    showStatus();
  }
}
