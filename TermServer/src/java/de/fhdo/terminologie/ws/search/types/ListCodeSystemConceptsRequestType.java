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

import de.fhdo.terminologie.db.hibernate.CodeSystem;
import de.fhdo.terminologie.db.hibernate.CodeSystemEntity;
import de.fhdo.terminologie.ws.types.LoginType;
import de.fhdo.terminologie.ws.types.PagingType;
import de.fhdo.terminologie.ws.types.SearchType;
import de.fhdo.terminologie.ws.types.SortingType;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

/**
 *
 * @author Robert Mützner (robert.muetzner@fh-dortmund.de)
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class ListCodeSystemConceptsRequestType
{
  @XmlElement(required = false)
  private LoginType login;
  
  @XmlElement(required = true)
  private CodeSystem codeSystem;
  
  @XmlElement(required = false)
  private CodeSystemEntity codeSystemEntity;

  @XmlElement(required = false)
  private SearchType searchParameter;
  
  @XmlElement(required = false)
  private PagingType pagingParameter;
  
  @XmlElement(required = false)
  private boolean lookForward;
  
  @XmlElement(required = false)
  private SortingType sortingParameter;
  
  
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
   * @return the codeSystemEntity
   */
  public CodeSystemEntity getCodeSystemEntity()
  {
    return codeSystemEntity;
  }

  /**
   * @param codeSystemEntity the codeSystemEntity to set
   */
  public void setCodeSystemEntity(CodeSystemEntity codeSystemEntity)
  {
    this.codeSystemEntity = codeSystemEntity;
  }

  /**
   * @return the searchParameter
   */
  public SearchType getSearchParameter()
  {
    return searchParameter;
  }

  /**
   * @param searchParameter the searchParameter to set
   */
  public void setSearchParameter(SearchType searchParameter)
  {
    this.searchParameter = searchParameter;
  }

  /**
   * @return the pagingParameter
   */
  public PagingType getPagingParameter()
  {
    return pagingParameter;
  }

  /**
   * @param pagingParameter the pagingParameter to set
   */
  public void setPagingParameter(PagingType pagingParameter)
  {
    this.pagingParameter = pagingParameter;
  }

  /**
   * @return the lookForward
   */
  public boolean isLookForward()
  {
    return lookForward;
  }

  /**
   * @param lookForward the lookForward to set
   */
  public void setLookForward(boolean lookForward)
  {
    this.lookForward = lookForward;
  }

  /**
   * @return the sortingParameter
   */
  public SortingType getSortingParameter()
  {
    return sortingParameter;
  }

  /**
   * @param sortingParameter the sortingParameter to set
   */
  public void setSortingParameter(SortingType sortingParameter)
  {
    this.sortingParameter = sortingParameter;
  }
}
