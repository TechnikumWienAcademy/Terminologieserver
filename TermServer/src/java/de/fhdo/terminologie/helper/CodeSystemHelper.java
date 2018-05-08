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
package de.fhdo.terminologie.helper;

import de.fhdo.terminologie.db.hibernate.CodeSystem;
import de.fhdo.terminologie.db.hibernate.CodeSystemVersion;
import java.util.Iterator;

/**
 * @author Robert Mützner (robert.muetzner@fh-dortmund.de) (robert.muetzner@fh-dortmund.de)
 */
public class CodeSystemHelper
{

  public static long getCurrentVersionId(CodeSystem codeSystem)
  {
    long codeSystemVersionId = 0;
    
    if (codeSystem.getCurrentVersionId() > 0)
      codeSystemVersionId = codeSystem.getCurrentVersionId();
    else
    {
      if(codeSystem.getCodeSystemVersions() != null)
      {
        // Die höchste ID aller Versionen ermitteln
        Iterator<CodeSystemVersion> it = codeSystem.getCodeSystemVersions().iterator();
        long id = 0;
        while(it.hasNext())
        {
          CodeSystemVersion csv = it.next();
          if(csv.getVersionId() > id)
            id = csv.getVersionId();
        }
        
        codeSystemVersionId = id;
      }
    }
    
    return codeSystemVersionId;
  }
}
