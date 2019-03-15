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
package de.fhdo.db;

/**
 *
 * @author Robert Mützner
 */
import de.fhdo.collaboration.db.classes.Collaborationuser;
import de.fhdo.db.hibernate.TermUser;
import java.io.Serializable;
import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.id.IdentityGenerator;

public class UseIdOrGenerate extends IdentityGenerator
{

  @Override
  public Serializable generate(SessionImplementor session, Object obj) throws HibernateException
  {
    if (obj == null)
      throw new HibernateException(new NullPointerException());

    if (obj instanceof TermUser)
    {
      if ((((TermUser) obj).getId()) == null)
      {
        Serializable id = super.generate(session, obj);
        return id;
      }
      else
      {
        return ((TermUser) obj).getId();
      }
    }
    else if (obj instanceof Collaborationuser)
    {
      if ((((Collaborationuser) obj).getId()) == null)
      {
        Serializable id = super.generate(session, obj);
        return id;
      }
      else
      {
        return ((Collaborationuser) obj).getId();
      }
    }
    
    Serializable id = super.generate(session, obj);
    return id;
  }
}
