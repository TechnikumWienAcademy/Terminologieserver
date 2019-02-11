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

package de.fhdo.terminologie.ws.administration.types;

import de.fhdo.terminologie.db.hibernate.CodeSystem;
import de.fhdo.terminologie.ws.types.ExportParameterType;
import de.fhdo.terminologie.ws.types.ExportType;
import de.fhdo.terminologie.ws.types.LoginType;

/**
 *
 * @author Bernhard
 */
public class ExportCodeSystemContentRequestType
{

  public static final long EXPORT_CLAML_ID = 193;
  public static final long EXPORT_CSV_ID = 194;
  public static final long EXPORT_SVS_ID = 195;

  public static String getPossibleFormats()
  {
    String s = " Mögliche Export-Formate sind: 193(ClaML) oder 194(CSV) oder 195(SVS)";
    return s;
  }
  private ExportType exportInfos;
  private LoginType login;
  private CodeSystem codeSystem;
  private ExportParameterType exportParameter;
  
  /**
   * @return the exportInfos
   */
  public ExportType getExportInfos()
  {
    return exportInfos;
  }

  /**
   * @param exportInfos the exportInfos to set
   */
  public void setExportInfos(ExportType exportInfos)
  {
    this.exportInfos = exportInfos;
  }

  /**
   * @return the login
   */
  public LoginType getLogin()
  {
    return login;
  }

  /**
   * @param login the login to set
   */
  public void setLogin(LoginType login)
  {
    this.login = login;
  }

  /**
   * @return the codeSystem
   */
  public CodeSystem getCodeSystem()
  {
    return codeSystem;
  }

  /**
   * @param codeSystem the codeSystem to set
   */
  public void setCodeSystem(CodeSystem codeSystem)
  {
    this.codeSystem = codeSystem;
  }

    /**
     * @return the exportParameter
     */
    public ExportParameterType getExportParameter() {
        return exportParameter;
    }

    /**
     * @param exportParameter the exportParameter to set
     */
    public void setExportParameter(ExportParameterType exportParameter) {
        this.exportParameter = exportParameter;
    }
}
