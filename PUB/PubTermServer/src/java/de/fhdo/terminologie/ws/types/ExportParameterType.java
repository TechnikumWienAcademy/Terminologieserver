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
package de.fhdo.terminologie.ws.types;

import java.util.Date;

/**
 *
 * @author Nico Hänsch
 */
public class ExportParameterType {
    private boolean codeSystemInfos;
    private boolean translations;
    private String associationInfos;
    private Date dateFrom;

    /**
     * @return the codeSystemInfos
     */
    public boolean getCodeSystemInfos() {
        return codeSystemInfos;
    }

    /**
     * @param codeSystemInfos the codeSystemInfos to set
     */
    public void setCodeSystemInfos(boolean codeSystemInfos) {
        this.codeSystemInfos = codeSystemInfos;
    }

    /**
     * @return the translations
     */
    public boolean getTranslations() {
        return translations;
    }

    /**
     * @param translations the translations to set
     */
    public void setTranslations(boolean translations) {
        this.translations = translations;
    }

    /**
     * @return the associationInfos
     */
    public String getAssociationInfos() {
        return associationInfos;
    }

    /**
     * @param associationInfos the associationInfos to set
     */
    public void setAssociationInfos(String associationInfos) {
        this.associationInfos = associationInfos;
    }

  /**
   * @return the dateFrom
   */
  public Date getDateFrom()
  {
    return dateFrom;
  }

  /**
   * @param dateFrom the dateFrom to set
   */
  public void setDateFrom(Date dateFrom)
  {
    this.dateFrom = dateFrom;
  }
    
}
