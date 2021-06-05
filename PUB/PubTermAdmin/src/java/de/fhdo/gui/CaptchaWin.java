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
package de.fhdo.gui;

import de.fhdo.helper.SessionHelper;
import de.fhdo.interfaces.IUpdate;
import org.zkoss.zk.ui.ext.AfterCompose;
import org.zkoss.zul.Row;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Window;

/**
 *
 * @author Robert Mützner
 */
public class CaptchaWin extends Window implements AfterCompose
{
  private IUpdate updateInterface;
  
  public CaptchaWin()
  {
  }

  public void afterCompose()
  {
    Textbox tb = (Textbox) getFellow("tfCaptcha");
    tb.focus();
  }

  public void captchaCheck()
  {
    Textbox tb = (Textbox) getFellow("tfCaptcha");
    org.zkforge.bwcaptcha.Captcha captcha = (org.zkforge.bwcaptcha.Captcha) getFellow("cpa");

    if (captcha.getValue().toLowerCase().equals(tb.getValue().toLowerCase()))
    {
      // Captcha korrekt, in Session speichern
      SessionHelper.setValue("captcha_correct", true);
      
      // Formular schließen
      showRow("warningRow", false);
      this.setVisible(false);
      this.detach();
      
      if(updateInterface != null)
        updateInterface.update(null);
    }
    else
    {
      // Fehlermeldung ausgeben
      showRow("warningRow", true);
    }
  }
  
  private void showRow(String RowID, boolean Visible)
  {
    Row row = (Row) getFellow(RowID);
    row.setVisible(Visible);
  }
  
  /**
   * @param updateInterface the updateInterface to set
   */
  public void setUpdateInterface(IUpdate updateInterface)
  {
    this.updateInterface = updateInterface;
  }

}
