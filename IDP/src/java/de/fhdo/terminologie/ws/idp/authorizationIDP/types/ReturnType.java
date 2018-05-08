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
package de.fhdo.terminologie.ws.idp.authorizationIDP.types;

import java.util.Date;
import types.termserver.fhdo.de.Session;

/**
 *
 * @author Robert Mützner (robert.muetzner@fh-dortmund.de)
 */
public class ReturnType
{

  /**
   * @return the overallErrorCategory
   */
  public OverallErrorCategory getOverallErrorCategory()
  {
    return overallErrorCategory;
  }

  /**
   * @param overallErrorCategory the overallErrorCategory to set
   */
  public void setOverallErrorCategory(OverallErrorCategory overallErrorCategory)
  {
    this.overallErrorCategory = overallErrorCategory;
  }

  /**
   * @return the status
   */
  public Status getStatus()
  {
    return status;
  }

  /**
   * @param status the status to set
   */
  public void setStatus(Status status)
  {
    this.status = status;
  }

  /**
   * @return the message
   */
  public String getMessage()
  {
    return message;
  }

  /**
   * @param message the message to set
   */
  public void setMessage(String message)
  {
    this.message = message;
  }

  /**
   * @return the count
   */
  public int getCount()
  {
    return count;
  }

  /**
   * @param count the count to set
   */
  public void setCount(int count)
  {
    this.count = count;
  }
	
	
  public enum OverallErrorCategory { INFO, WARN, ERROR};
  public enum Status { OK, FAILURE};

	public Date getLastTimeStamp() {
		return lastTimeStamp;
	}

	public void setLastTimeStamp(Date lastTimeStamp) {
		this.lastTimeStamp = lastTimeStamp;
	}

	public String getLastIP() {
		return lastIP;
	}

	public void setLastIP(String lastIP) {
		this.lastIP = lastIP;
	}

	public de.fhdo.db.hibernate.TermUser getTermUser() {
		return termUser;
	}

	public void setTermUser(de.fhdo.db.hibernate.TermUser termUser) {
		this.termUser = termUser;
	}
	
  private Date lastTimeStamp;
	private String lastIP;
	private de.fhdo.db.hibernate.TermUser termUser;
  private OverallErrorCategory overallErrorCategory;
  private Status status;
  private String message;
  private int count;
}
