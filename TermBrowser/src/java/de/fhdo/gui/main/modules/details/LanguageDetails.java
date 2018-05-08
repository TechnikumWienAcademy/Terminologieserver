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
package de.fhdo.gui.main.modules.details;

import de.fhdo.helper.LanguageHelper;
import de.fhdo.interfaces.IUpdate;
import de.fhdo.interfaces.IUpdateModal;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.ext.AfterCompose;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Window;
import types.termserver.fhdo.de.CodeSystemConceptTranslation;

/**
 *
 * @author Robert Mützner
 */
public class LanguageDetails extends Window implements AfterCompose
{

  private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();
  
  private IUpdate updateListInterface;
  private Combobox cbLanguage;
  private Textbox tbValueOfLanguage;

  public LanguageDetails()
  {

  }

  public void afterCompose()
  {
      cbLanguage = (Combobox)getFellow("cbLanguage");
      tbValueOfLanguage = (Textbox)getFellow("tbValueOfLanguage");
      cbLanguage.setModel(LanguageHelper.getListModelList());
  }

  public void onOkClicked()
  {
  
      if(cbLanguage.getSelectedItem() == null || tbValueOfLanguage.getText() == null || tbValueOfLanguage.getText().equals(""))
      {
        Messagebox.show(Labels.getLabel("common.notemptyTranslationLanguage"), Labels.getLabel("common.error"), Messagebox.OK, Messagebox.INFORMATION);
        return;
      }
      
      CodeSystemConceptTranslation csct = new CodeSystemConceptTranslation();
      try
      {
        
        csct.setTerm(tbValueOfLanguage.getText());
        csct.setLanguageId(LanguageHelper.getLanguageIdByName(cbLanguage.getSelectedItem().getLabel()));

        this.setVisible(false);
        this.detach();

        if (updateListInterface != null)
            updateListInterface.update(csct);

    }
    catch (Exception e)
    {
      // Fehlermeldung ausgeben
      logger.error("Fehler in LanguageDetails.java: " + e.getMessage());
      e.printStackTrace();
    }
  }

  public void onCancelClicked()
  {
    this.setVisible(false);
    this.detach();

  }

  /**
   * @param updateListInterface the updateListInterface to set
   */
  public void setUpdateListInterface(IUpdate updateListInterface)
  {
    this.updateListInterface = updateListInterface;
  }
}
