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
package de.fhdo.gui.header;

import de.fhdo.db.DBSysParam;
import de.fhdo.helper.LoginHelper;
import de.fhdo.helper.PropertiesHelper;
import de.fhdo.helper.ViewHelper;
import java.util.HashMap;
import org.zkoss.zk.ui.event.EventListener;
import java.util.List;
import java.util.Map;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Window;

/**
 *
 *
 * @author Robert Mützner
 */
public class Menu extends Window implements org.zkoss.zk.ui.ext.AfterCompose
//public class Menu extends GenericAutowireComposer
{

  //private static org.zkoss.zk.ui.Session session = Sessions.getCurrent();
  private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();
  private String headerStr;
  transient EventListener onMenuitemClicked;

  public Menu()
  {
    if (logger.isDebugEnabled())
      logger.debug("[Menu.java] Konstruktor");

    headerStr = de.fhdo.db.DBSysParam.instance().getStringValue("systemType", null, null) + "\nVerwaltungsumgebung";
    
    //onMenuitemClicked = new OnClickListener(this);
  }

  public void afterCompose()
  {
    if (logger.isDebugEnabled())
      logger.debug("[Menu.java] afterCompose()");

    createMenu();
    initHeader();
  }

  private void clearHeader()
  {
  }

  public void onLogoutClicked()
  {
		String idp_url = DBSysParam.instance().getStringValue("idp_url", null, null);
    Executions.sendRedirect(idp_url + "/IDP/logout.zul");
    
    //if (DortmundHelper.getInstance().isFhDortmund())
    /*if (PropertiesHelper.getInstance().getLogin_type().equalsIgnoreCase("fhdo"))
    {
      de.fhdo.dortmund.LoginHelper.getInstance().logout();
      Executions.sendRedirect("/index.zul");
    }
    else */
//    if (PropertiesHelper.getInstance().getLogin_type().equalsIgnoreCase("userpass"))
//    {
//      LoginHelper.getInstance().logout();
//      Executions.sendRedirect("/index.zul");
//    }
//    else
//    {
//      LoginHelper.getInstance().logout();
//      Executions.sendRedirect("../../../TermBrowser/gui/admin/logout.zul");
//    }
  }

  public void onBackToTermBrowser()
  {
    Executions.sendRedirect("../../../TermBrowser/gui/main/main.zul");
  }

  public void init()
  {
    //if (logger.isDebugEnabled())
    //  logger.debug("[Menu.java] init()");
    //initHeader();
  }

  public void onLogoEBPGClicked()
  {
    logger.debug("onLogoEBPGClicked()");
    Executions.getCurrent().sendRedirect("http://www.ebpg-nrw.de/", "_blank");
  }

  public void onLogoFHClicked()
  {
    logger.debug("onLogoFHClicked()");
    Executions.getCurrent().sendRedirect("http://www.fh-dortmund.de/", "_blank");
  }

  public void onLogoNRWClicked()
  {
    logger.debug("onLogoNRWClicked()");
    Executions.getCurrent().sendRedirect("http://www.nrw.de/", "_blank");
  }

  public void onLogoEUClicked()
  {
    logger.debug("onLogoEUClicked()");
    Executions.getCurrent().sendRedirect("http://europa.eu/", "_blank");
  }

  public void onLogoBMGClicked()
  {
    logger.debug("onLogoBMGClicked()");
    Executions.getCurrent().sendRedirect("http://www.bmg.bund.de/", "_blank");
  }

  public void onLogoBMGATClicked()
  {
    logger.debug("onLogoBMGATClicked()");
    Executions.getCurrent().sendRedirect("http://www.bmgf.gv.at/", "_blank");
  }

  public void onLogoFHTWClicked()
  {
    logger.debug("onLogoFHTWClicked()");
    Executions.getCurrent().sendRedirect("http://www.technikum-wien.at/", "_blank");
  }

  public void onLogoELGAClicked()
  {
    logger.debug("onLogoELGAClicked()");
    Executions.getCurrent().sendRedirect("http://www.elga.gv.at/", "_blank");
  }

  public void onLogoBRZClicked()
  {
    logger.debug("onLogoBRZClicked()");
    Executions.getCurrent().sendRedirect("http://www.brz.gv.at/", "_blank");
  }

  private void createMenu()
  {
    if (logger.isDebugEnabled())
      logger.debug("[Menu.java] createMenu()");


  }

  private void redirect(String Src, String WaitText, String Parameter)
  {
    if (WaitText.length() > 0)
      Clients.showBusy(WaitText);

    clearHeader();
    if (Src.equals("null"))
    {
      ViewHelper.gotoSrc(null);
    }
    else
    {
      if (Parameter != null && Parameter.contains("extern_link"))
      {
        Executions.getCurrent().sendRedirect(Src, "_blank");
      }
      else
        ViewHelper.gotoSrc(Src);
    }
    initHeader();
  }

  public static void openModalDialog(String Src, String Parameter)
  {
    try
    {
      Map map = null;
      boolean fehler = false;

      logger.debug("openModalDialog mit Param: " + Parameter);

      //if (Parameter != null && Parameter.length() > 0 && Parameter.contains("SESSION"))
      if (Parameter != null && Parameter.length() > 0)
      {
        org.zkoss.zk.ui.Session session = Sessions.getCurrent();

        String[] param = Parameter.split(";");
        map = new HashMap();

        for (int i = 0; i < param.length; ++i)
        {
          String[] value = param[i].split(":");
          if (value.length > 1 && value[0].equals("SESSION_LONG"))
          {
            try
            {
              map.put(value[1], Long.parseLong(session.getAttribute(value[1]).toString()));

              logger.debug("Parameter (Long) entdeckt: " + value[1]);
            }
            catch (Exception e)
            {
              Messagebox.show("Keine Auswahl");
              fehler = true;
            }
          }
          else if (value.length > 1 && value[0].equals("SESSION_STR"))
          {
            map.put(value[1], session.getAttribute(value[1]).toString());

            logger.debug("Parameter entdeckt: " + value[1]);
          }
          else if (value.length > 1 && value[0].equals("PARAMETER"))
          {
            map.put(value[1], 1);

            logger.debug("Parameter entdeckt: " + value[1]);
          }
        }
      }

      if (fehler == false)
      {
        Window win = (Window) Executions.createComponents(
                Src,
                null, map);
        win.setMaximizable(true); //TODO Manche module müssen Maximiert werden können! Bitte im Modul setMax false machen!
        win.doModal();
      }
    }
    catch (Exception ex)
    {
      logger.error("Fehler in Klasse '" + Menu.class.getName()
              + "': " + ex.getMessage());
    }

  }

  private void removeAllChildren(Component Comp)
  {
    List childs = Comp.getChildren();
    if (childs != null)
      childs.clear();
  }

  private void initHeader()
  {
    if (logger.isDebugEnabled())
      logger.debug("[Menu.java] initHeader()");

  }

  public void onPatientlistClicked()
  {
    //clearHeader();
    //ViewHelper.gotoPatientlist();
    //initHeader();
  }

  /**
   * Klick auf den "Über"-Button
   */
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
    catch (Exception ex)
    {
      logger.error("Fehler in Klasse '" + Menu.class.getName()
              + "': " + ex.getMessage());
    }

  }

  /**
   * @return the headerStr
   */
  public String getHeaderStr()
  {
    return headerStr;
  }
}
