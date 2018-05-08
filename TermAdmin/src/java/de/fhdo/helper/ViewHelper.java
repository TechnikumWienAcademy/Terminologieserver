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
package de.fhdo.helper;

import java.util.List;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zul.Window;

/**
 *
 * @author Robert Mützner
 */
public class ViewHelper
{

  private static org.zkoss.zk.ui.Session session = Sessions.getCurrent();

  public static void gotoSrc(String Src)
  {
    session.setAttribute("current_domain", Src);
    Executions.sendRedirect(Src);
  }

  public static void gotoAdmin()
  {
    //session.setAttribute("current_domain", Definitions.DOMAIN_ADMIN);
    //Executions.sendRedirect(Definitions.DOMAIN_ADMIN);
  }

  public static void gotoMain()
  {
    //session.setAttribute("current_domain", Definitions.DOMAIN_MAIN);
    //Executions.sendRedirect(Definitions.DOMAIN_MAIN);
  }

  public static void removeAllChildren(Component Comp)
  {
    List childs = Comp.getChildren();
    if (childs != null)
      childs.clear();
  }

  public static void showComponent(Window Win, String Comp, boolean Visible)
  {
    try
    {
      Component comp = Win.getFellow(Comp);
      if (comp != null)
      {
        comp.setVisible(Visible);
      }
    }
    catch (Exception e)
    {
    }
  }

  public static void showComponent(Component Comp, boolean Visible)
  {
    if (Comp != null)
    {
      Comp.setVisible(Visible);
    }
  }

  /*public static void gotoPatientlist()
  {
  session.setAttribute("current_domain", Definitions.DOMAIN_PATIENTLIST);
  PersonHelper.getInstance().freeData();
  Executions.sendRedirect("/gui/patientlist/main.zul");
  }*/

  /*public static void gotoPatientrecord()
  {
  //System.out.println("gotoPatientrecord");
  session.setAttribute("current_domain", OphepaDefs.DOMAIN_PATIENTRECORD);
  Executions.sendRedirect("/gui/patientrecord/main.zul");
  }*/
}
