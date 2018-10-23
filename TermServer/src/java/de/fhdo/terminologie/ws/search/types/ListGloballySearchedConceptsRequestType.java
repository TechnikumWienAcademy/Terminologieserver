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
package de.fhdo.terminologie.ws.search.types;

import de.fhdo.terminologie.ws.types.LoginType;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

/**
 *
 * @author Philipp Urbauer
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class ListGloballySearchedConceptsRequestType
{
  @XmlElement(required = false)
  private LoginType login;
  
  @XmlElement(required = false)
  private Boolean codeSystemConceptSearch;
  
  @XmlElement(required = false)
  private String term;
  
  @XmlElement(required = false)
  private String code;
  
  @XmlElement(required = false)
  private boolean loginAlreadyChecked;

    public boolean isLoginAlreadyChecked() {
        return loginAlreadyChecked;
    }

    public void setLoginAlreadyChecked(boolean loginAlreadyChecked) {
        this.loginAlreadyChecked = loginAlreadyChecked;
    }

    public LoginType getLogin() {
        return login;
    }

    public void setLogin(LoginType login) {
        this.login = login;
    }

    public Boolean getCodeSystemConcepts() {
        return codeSystemConceptSearch;
    }

    public void setCodeSystemConcepts(Boolean codeSystemConcepts) {
        this.codeSystemConceptSearch = codeSystemConcepts;
    }

    public String getTerm() {
        return term;
    }

    public void setTerm(String term) {
        this.term = term;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}
