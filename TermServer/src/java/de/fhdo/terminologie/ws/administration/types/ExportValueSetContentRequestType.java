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

import de.fhdo.terminologie.db.hibernate.ValueSet;
import de.fhdo.terminologie.ws.types.ExportParameterType;
import de.fhdo.terminologie.ws.types.ExportType;
import de.fhdo.terminologie.ws.types.LoginType;

/**
 *
 * @author Robert Mützner (robert.muetzner@fh-dortmund.de)
 */
public class ExportValueSetContentRequestType
{
  //public static final long EXPORT_CLAML_ID = 193;
  public static final long EXPORT_CSV_ID = 194;
  public static final long EXPIRT_SVS_ID = 195;

  public static String getPossibleFormats()
  {
    String s = " Mögliche Export-Formate sind: 194(CSV) oder 195(SVS)";
    return s;
  }
  
  private ExportType exportInfos;
  private LoginType login;
  private ValueSet valueSet;
  private ExportParameterType exportParameter;
  private boolean loginAlreadyChecked;

    public boolean isLoginAlreadyChecked() {
        return loginAlreadyChecked;
    }

    public void setLoginAlreadyChecked(boolean loginAlreadyChecked) {
        this.loginAlreadyChecked = loginAlreadyChecked;
    }

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
   * @return the ValueSet
   */
  public ValueSet getValueSet()
  {
    return valueSet;
  }

  /**
   * @param valueSet the ValueSet to set
   */
  public void setValueSet(ValueSet valueSet)
  {
    this.valueSet = valueSet;
  }

  /**
   * @return the exportParameter
   */
  public ExportParameterType getExportParameter()
  {
    return exportParameter;
  }

  /**
   * @param exportParameter the exportParameter to set
   */
  public void setExportParameter(ExportParameterType exportParameter)
  {
    this.exportParameter = exportParameter;
  }
}
