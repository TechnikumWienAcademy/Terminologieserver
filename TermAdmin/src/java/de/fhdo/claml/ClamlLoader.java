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
package de.fhdo.claml;

import clamlXSD.ClaML;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;

/**
 *
 * @author Michael
 */
public class ClamlLoader
{

  private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();
  private ClaML claml = null;

  public ClamlLoader(byte[] bytes)
  {
    String packagename = ClaML.class.getPackage().getName();
    logger.debug("Package: " + packagename);
    JAXBContext jc;
    Unmarshaller u;
    try
    {
      jc = JAXBContext.newInstance(packagename);
      u = jc.createUnmarshaller();

      //File file = new File("D:\\Temp\\ops2012syst_claml_20111103.xml");
      //File file = new File("temp_import.xml");
      OutputStream out = new FileOutputStream("temp_import.xml");
      out.write(bytes);
      out.close();

      //ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
      //bis.reset();
      this.claml = (ClaML) u.unmarshal(new File("temp_import.xml"));
    }
    catch (Exception ex)
    {
      logger.error("Fehler beim Parsen des ClaML-Dokuments: " + ex.getMessage());
      ex.printStackTrace();
    }

  }

  /**
   * @return the claml
   */
  public ClaML getClaml()
  {
    return claml;
  }

  /**
   * @param claml the claml to set
   */
  public void setClaml(ClaML claml)
  {
    this.claml = claml;
  }
}
