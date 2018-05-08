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

/**
 *
 * @author Robert Mützner (robert.muetzner@fh-dortmund.de)
 */
public class LoginType
{
  private String sessionID, username, password, ip;

  /**
   * @return the sessionID
   */
  public String getSessionID()
  {
    return sessionID;
  }

  /**
   * @param sessionID the sessionID to set
   */
  public void setSessionID(String sessionID)
  {
    this.sessionID = sessionID;
  }

  /**
   * @return the username
   */
  public String getUsername()
  {
    return username;
  }

  /**
   * @param username the username to set
   */
  public void setUsername(String username)
  {
    this.username = username;
  }

  /**
   * @return the password
   */
  public String getPassword()
  {
    return password;
  }

  /**
   * @param password the password to set
   */
  public void setPassword(String password)
  {
    this.password = password;
  }

  /**
   * @return the ip
   */
  public String getIp()
  {
    return ip;
  }

  /**
   * @param ip the ip to set
   */
  public void setIp(String ip)
  {
    this.ip = ip;
  }
}
