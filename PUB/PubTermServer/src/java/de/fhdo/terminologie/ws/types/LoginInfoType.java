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

import de.fhdo.terminologie.db.hibernate.TermUser;

/**
 *
 * @author Robert Mützner (robert.muetzner@fh-dortmund.de)
 */
public class LoginInfoType
{
  private LoginType login;
  private TermUser termUser;
  private java.util.Date lastTimestamp;
  private String lastIP;

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
   * @return the termUser
   */
  public TermUser getTermUser()
  {
    return termUser;
  }

  /**
   * @param termUser the termUser to set
   */
  public void setTermUser(TermUser termUser)
  {
    this.termUser = termUser;
  }

  /**
   * @return the lastTimestamp
   */
  public java.util.Date getLastTimestamp()
  {
    return lastTimestamp;
  }

  /**
   * @param lastTimestamp the lastTimestamp to set
   */
  public void setLastTimestamp(java.util.Date lastTimestamp)
  {
    this.lastTimestamp = lastTimestamp;
  }

  /**
   * @return the lastIP
   */
  public String getLastIP()
  {
    return lastIP;
  }

  /**
   * @param lastIP the lastIP to set
   */
  public void setLastIP(String lastIP)
  {
    this.lastIP = lastIP;
  }
  
}
