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
package de.fhdo;

import de.fhdo.collaboration.helper.LoginHelper;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zul.Label;
import org.zkoss.zul.Window;

/**
 *
 * @author Philipp Urbauer
 */

public class Activation extends Window implements org.zkoss.zk.ui.ext.AfterCompose
{

  private boolean activated = false;
  
  public void backToTermBrowser()
  {
    Executions.sendRedirect("../../TermBrowser/gui/main/main.zul");
  }

  public Activation()
  {
    String activationMD5 = "" + (String) Executions.getCurrent().getParameter("reg");

    if (activationMD5.length() > 0 && activationMD5.equals("null") == false)
    {
      // Benutzer jetzt aktivieren(!)
      activated = LoginHelper.getInstance().activate(activationMD5);
    }
  }

  public void afterCompose()
  {
      ((Label)getFellow("labelVersion")).setValue("Aktivierung erfolgreich!");
      if(activated == true){
          ((Label)getFellow("labelVersion")).setValue("Aktivierung erfolgreich!");
      }else{
          ((Label)getFellow("labelVersion")).setValue("Aktivierung fehlgeschlagen!\n Bitte wenden Sie sich an die/den AdministratorIn!");
      }
  }
}
