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
package de.fhdo.collaboration;

import de.fhdo.logging.LoggingOutput;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.SuspendNotAllowedException;
import org.zkoss.zk.ui.ext.AfterCompose;
import org.zkoss.zul.Label;
import org.zkoss.zul.Window;

/**
 *
 * @author Robert Mützner
 */
public class Menu extends Window implements AfterCompose
{
  
  private Label headerLabel;  
    
  public Menu()
  {
    
  }

    public void afterCompose() {
        headerLabel = (Label)getFellow("headerLabel");
        headerLabel.setValue(de.fhdo.collaboration.db.DBSysParam.instance().getStringValue("systemType", null, null) + "\n Kollaborationsumgebung");
    }
  
  public void onBackToTermBrowser()
  {
    Executions.getCurrent().sendRedirect("/gui/main/main.zul");
  }
  
  public void onShowWorkflow()
  {
    try
    {
      Window win = (Window) Executions.createComponents(
              "/collaboration/workflow.zul",
              null, null);
      win.setMaximizable(false);
      win.doModal();
    }
    catch (SuspendNotAllowedException ex)
    {
      LoggingOutput.outputException(ex, this);
    }
  }
  
  public void callOidRegister(){
      Executions.getCurrent().sendRedirect("https://www.gesundheit.gv.at/OID_Frontend/", "_blank");
  }
  
  public void onUeberClicked()
  {
    try
    {
      Window win = (Window) Executions.createComponents(
              "/gui/info/about.zul",
              null, null);
      win.setMaximizable(false);
      win.doModal();
    }
    catch (SuspendNotAllowedException ex)
    {
      LoggingOutput.outputException(ex, this);
    }
  }
}
