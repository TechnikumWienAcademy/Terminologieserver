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

import de.fhdo.collaboration.db.DBSysParam;
import de.fhdo.collaboration.helper.CODES;
import de.fhdo.gui.main.modules.PopupWindow;
import de.fhdo.helper.DES;
import de.fhdo.helper.FetchCSandVSHelper;
import de.fhdo.helper.LoginHelper;
import de.fhdo.helper.SessionHelper;
import de.fhdo.helper.ViewHelper;
import de.fhdo.models.TreeModelVS;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.zkoss.zk.ui.event.EventListener;
import java.util.Map;
import javax.servlet.http.HttpSession;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zk.ui.SuspendNotAllowedException;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.Filedownload;
import org.zkoss.zul.Include;
import org.zkoss.zul.Menuitem;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Toolbarbutton;
import org.zkoss.zul.Window;

/**
 *
 *
 * @author Robert Mützner
 */
public class Menu extends Window implements org.zkoss.zk.ui.ext.AfterCompose //public class Menu extends GenericAutowireComposer
{

  private static org.apache.log4j.Logger logger;
  private String headerStr;
  private boolean isCollaboration;
	private boolean isAdministration;
  transient EventListener onMenuitemClicked;
    
  public Menu()
  {
     logger = de.fhdo.logging.Logger4j.getInstance().getLogger();
    if (logger.isDebugEnabled())
    {
      logger.debug("[Menu.java] Konstruktor");
    }

    isCollaboration = SessionHelper.isCollaborationActive();
		
		if (isCollaboration && (SessionHelper.getCollaborationUserRole().equals(CODES.ROLE_INHALTSVERWALTER) ||
				SessionHelper.getCollaborationUserRole().equals(CODES.ROLE_ADMIN))){
			isAdministration = true;
		} else {
			isAdministration = false;
		}

    if (isCollaboration){
      headerStr = de.fhdo.collaboration.db.DBSysParam.instance().getStringValue("systemType", null, null) + "\n" + Labels.getLabel("collab.collaborationArea");
    }else{
      headerStr = de.fhdo.collaboration.db.DBSysParam.instance().getStringValue("systemType", null, null) + "\n" + Labels.getLabel("common.terminologyBrowser");
    }
    logger.debug("isCollaboration: " + isCollaboration);
  }

  public void afterCompose()
  {

    ((Toolbarbutton) getFellow("tbb_Redirect")).setVisible(!isCollaboration && de.fhdo.collaboration.db.DBSysParam.instance().getBoolValue("redirectButtonVisible", null, null));
    ((Toolbarbutton) getFellow("tbb_Redirect")).setLabel(de.fhdo.collaboration.db.DBSysParam.instance().getStringValue("redirectLabel", null, null));
    ((Toolbarbutton) getFellow("tbb_Redirect")).setTooltip(de.fhdo.collaboration.db.DBSysParam.instance().getStringValue("redirectTooltip", null, null));
    
		Object obj = Sessions.getCurrent().getNativeSession();
		HttpSession httpSession = null;
		if (obj instanceof HttpSession){
			httpSession = (HttpSession) obj;
		}
		
    boolean loggedIn = SessionHelper.isUserLoggedIn();
    boolean isAdmin = SessionHelper.isAdmin();
    ((Menuitem) getFellow("menuitemAnmelden")).setDisabled(loggedIn);
    ((Menuitem) getFellow("menuitemAbmelden")).setDisabled(!loggedIn);
    ((Menuitem) getFellow("menuitemAdminSettings")).setDisabled((loggedIn && isAdmin) == true ? false:true);
    ((Menuitem) getFellow("menuitemDetails")).setDisabled(!loggedIn);

    ((Menuitem) getFellow("menuitemCollabLogin")).setDisabled(isCollaboration);
    ((Menuitem) getFellow("menuitemCollabLogoff")).setDisabled(!isCollaboration);
    
    if((SessionHelper.getCollaborationUserRole().equals(CODES.ROLE_ADMIN) || 
         SessionHelper.getCollaborationUserRole().equals(CODES.ROLE_INHALTSVERWALTER)) && isCollaboration){
          ((Toolbarbutton) getFellow("tbb_Vorschlag")).setVisible(true);
      }else{
          ((Toolbarbutton) getFellow("tbb_Vorschlag")).setVisible(false);
      }
  }
    
   
    public void onLogoBRZClicked() {
        logger.debug("onLogoBRZClicked()");
        System.gc();
        Executions.getCurrent().sendRedirect("http://www.brz.gv.at/", "_blank");
    }
                
  
  

  private void clearHeader()
  {
  }

  public void onLogoutClicked()
  {
		String idp_url = DBSysParam.instance().getStringValue("idp_url", null, null);
		de.fhdo.helper.LoginHelper.getInstance().reset();
		de.fhdo.collaboration.helper.LoginHelper.getInstance().reset();
      //TODO put in database
      Executions.sendRedirect(idp_url + "/IDP/logout.zul");
//    if (isCollaboration)
//    {
//      de.fhdo.collaboration.helper.LoginHelper.getInstance().logout();
//      Executions.sendRedirect("/gui/main/main.zul");
//    }
//    else
//    {
//      de.fhdo.helper.LoginHelper.getInstance().logout();
//      Executions.sendRedirect("../../../TermAdmin/gui/admin/logout.zul");
//    }
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

  public void gotoMainView()
  {
    redirect("/gui/main/main.zul", Labels.getLabel("menu.pleaseWait"), null);
  }
  
  public void viewActualProceedings()
  {
     try
        {
          Window win = (Window) Executions.createComponents(
                  "/gui/info/actualProceedings.zul",
                  null, null);
          win.setMaximizable(false);
          win.doModal();
        }
        catch (SuspendNotAllowedException ex)
        {
          logger.error("Fehler in Klasse '" + Menu.class.getName()
                  + "': " + ex.getMessage());
        }
  }
  
  public void onImpressumClicked()
  {
     try
        {
          Window win = (Window) Executions.createComponents(
                  "/gui/info/impressum.zul",
                  null, null);
          win.setMaximizable(false);
          win.doModal();
        }
        catch (SuspendNotAllowedException ex)
        {
          logger.error("Fehler in Klasse '" + Menu.class.getName()
                  + "': " + ex.getMessage());
        }
  }
	
	public void prepareOverviewCSV(){
		
		String fileName = "Uebersicht Terminologien";
		SimpleDateFormat sdf = new SimpleDateFormat("YYYYMMdd");
		fileName += "_" + sdf.format(new Date());
		
		FetchCSandVSHelper helper = new FetchCSandVSHelper();
		byte[] info = helper.getAllCSandVS();
		
		Filedownload.save(info,
              "text/csv",
              fileName + ".csv");
		
		
		
		boolean stop = true;
	}
  
  public void viewAssociationEditor()
  {
    try
    {
//            if(SessionHelper.isUserLoggedIn()){
      Include inc = (Include) getRoot().getFellow("mainWindowCenter");
      inc.setSrc("/gui/main/modules/AssociationEditor.zul");
//            }
//            else{
//                Messagebox.show(Labels.getLabel("menu.loginRequiredForAssociationEditor"));
//            }
    }
    catch (Exception e)
    {
      Logger.getLogger(Menu.class.getName()).log(Level.SEVERE, null, e);
    }
  }

  private void redirect(String Src, String WaitText, String Parameter)
  {
    if (WaitText.length() > 0)
    {
      Clients.showBusy(WaitText);
    }

    clearHeader();
    if (Src.equals("null"))
    {
      //PersonHelper.getInstance().freeData();
      ViewHelper.gotoSrc(null);
    }
    else
    {
      if (Parameter != null && Parameter.contains("extern_link"))
      {
        Executions.getCurrent().sendRedirect(Src, "_blank");
      } /*else if(Parameter != null && Parameter.contains("PARAMETER"))
       {
       map.put(value[1], 1);
            
       //logger.debug("Parameter entdeckt: " + value[1]);
       }*/ else
      {
        ViewHelper.gotoSrc(Src);
      }
    }
    //initHeader();
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
              Messagebox.show(Labels.getLabel("menu.noSelection"));
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
    catch (SuspendNotAllowedException ex)
    {
      logger.error("Fehler in Klasse '" + Menu.class.getName()
              + "': " + ex.getMessage());
    }
  }

  /**
   * Klick auf den "Ãœber"-Button
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
    catch (SuspendNotAllowedException ex)
    {
      logger.error("Fehler in Klasse '" + Menu.class.getName()
              + "': " + ex.getMessage());
    }
  }

  public void login()
  {
    LoginHelper.getInstance().openLoginWindow();
  }

  public void loginSSo()
  {
    Executions.getCurrent().sendRedirect("/redirect_2.zul");
  }

  public void logout()
  {
		String idp_url = DBSysParam.instance().getStringValue("idp_url", null, null);
    de.fhdo.helper.LoginHelper.getInstance().reset();
		de.fhdo.collaboration.helper.LoginHelper.getInstance().reset();
    Executions.sendRedirect(idp_url + "/IDP/logout.zul");
  }

  public void showUADetails()
  {
    try
    {
      Map<String, Object> data = new HashMap<String, Object>();
      data.put("EditMode", PopupWindow.EDITMODE_DETAILSONLY);
      Window w = (Window) Executions.getCurrent().createComponents("/gui/main/modules/PopupUserDetails.zul", this, data);
      w.doModal();
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
  }

  public void callAdmin()
  {
    String path = de.fhdo.db.DBSysParam.instance().getStringValue("weblink", null, null);
    path += "/gui/admin/admin.zul";
    Executions.getCurrent().sendRedirect(path);
  }
  
  public void callEnquiry()
  {
      Executions.getCurrent().sendRedirect(de.fhdo.collaboration.db.DBSysParam.instance().getStringValue("redirectEnquiry", null, null));
  }
  
  public void callOidRegister(){
      //For AT!
      Executions.getCurrent().sendRedirect("https://www.gesundheit.gv.at/OID_Frontend/", "_blank");
  }

  
  //public void onForwardCollab(){
      /*Productive_AT*********************************************************************************************************************************/ 
      /**/ //Executions.getCurrent().sendRedirect("https://termcollab.gesundheit.gv.at/TermBrowser/index.zul");
      /**************************************************************************************************************************************************/ 
  //}
  
  public void onForward(){
  
      /*Productive_AT**********************************************************************************************************************************/ 
      /**/ Executions.getCurrent().sendRedirect(de.fhdo.collaboration.db.DBSysParam.instance().getStringValue("redirectFullMain", null, null));//"https://termpub.gesundheit.gv.at/TermBrowser/index.zul");
      /***************************************************************************************************************************************************/ 
  }
  
  public void collaborationClicked()
  {
    //SessionHelper.switchCollaboration();
    //Executions.getCurrent().sendRedirect(null);  // Seite neu laden
    Executions.getCurrent().sendRedirect("/redirect_1.zul");  // Seite neu laden  
  }
  
  public void onNewProposalClicked()
  {
    try
    {
      Map<String, Object> data = new HashMap<String, Object>();
      data.put("isExisting", false);
      Window w = (Window) Executions.getCurrent().createComponents("/collaboration/proposal/proposalDetails.zul", this, data);
      w.doModal();
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
  }
  
  public void onDesktopClicked()
  {
    Executions.getCurrent().sendRedirect("/collaboration/desktop/mainCollab.zul");
  }
  

  /**
   * @return the headerStr
   */
  public String getHeaderStr()
  {
    return headerStr;
  }

  /**
   * @return the isCollaboration
   */
  public boolean isIsCollaboration()
  {
    return isCollaboration;
  }
	
	public boolean isIsAdministration(){
		return isAdministration;
	}

  /**
   * @param isCollaboration the isCollaboration to set
   */
  public void setIsCollaboration(boolean isCollaboration)
  {
    this.isCollaboration = isCollaboration;
  }
  
  public boolean isNotCollaboration(){
  
      return !isCollaboration;
  }
  
}
