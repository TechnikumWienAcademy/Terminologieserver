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

import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zk.ui.Execution;
import org.zkoss.zk.ui.Executions;

/**
 *
 * @author Becker
 */
public class SendBackHelper
{
  private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();
  private boolean active;  
  
  public static Integer SENDBACK_NAME = 1,
                        SENDBACK_DESCRIPTION = 2,
                        SENDBACK_CODE = 4,
                        SENDBACK_NAME_DESCRIPTION = 3,
                        SENDBACK_NAME_CODE = 5,
                        SENDBACK_DESCRIPTION_CODE = 6,
                        SENDBACK_NAME_DESCRIPTION_CODE = 7;

  public SendBackHelper(){
      initialize();
  }
  
  
  /** 
   * Check if sendback should be active or not.
   * Required Parameters:
   * - sbContent + length ==6  (sbContent=123456)
   * - at least one type > 0
   */
  public void initialize()
  {
    logger.debug("SendBackHelper - Initialize()");
    active = false;        
    String qureyString = Executions.getCurrent().getDesktop().getQueryString();
    if(qureyString == null || qureyString.indexOf("sbContent") == -1)
        return;
    
    int    startIndex = qureyString.indexOf("sbContent") + "sbContent".length() + 1;    
    String sbContent  = qureyString.substring(startIndex, startIndex + 6);
    
    logger.debug("SendBackHelper - Initialize() - sbContent == " + sbContent);
    String sendBackApplicationName  = ParameterHelper.getString("sbAppName");

    // Pruefe ob Sendback genutzt werden soll
    if (sbContent != null && sbContent.length() == 6){
        try{            
            SessionHelper.setValue("typeDV",   Integer.valueOf(sbContent.substring(0, 1)));
            SessionHelper.setValue("typeCS",   Integer.valueOf(sbContent.substring(1, 2)));
            SessionHelper.setValue("typeCSV",  Integer.valueOf(sbContent.substring(2, 3)));
            SessionHelper.setValue("typeVS",   Integer.valueOf(sbContent.substring(3, 4)));
            SessionHelper.setValue("typeVSV",  Integer.valueOf(sbContent.substring(4, 5)));
            SessionHelper.setValue("typeCSEV", Integer.valueOf(sbContent.substring(5, 6)));
            SessionHelper.setValue("sendBackApplicationName", sendBackApplicationName);       
            if((Integer)SessionHelper.getSessionObjectByName("typeDV")  > 0 || (Integer)SessionHelper.getSessionObjectByName("typeCS")   > 0 || 
               (Integer)SessionHelper.getSessionObjectByName("typeCSV") > 0 || (Integer)SessionHelper.getSessionObjectByName("typeVS")   > 0 ||
               (Integer)SessionHelper.getSessionObjectByName("typeVSV") > 0 || (Integer)SessionHelper.getSessionObjectByName("typeCSEV") > 0){                
                active = true;
            }
            else {
                active = false;
            }
        } catch (Exception e){
            e.printStackTrace();
            active = false;            
        }
    }
  }  
  
  public void sendBack(String text){
        logger.debug("sendBack-postMethod:");
        String javaScript = "window.top.postMessage('"+ text + "', '\\*')"; // Aus sicherheitsgruenden sollte * ersetzt werden durch die domain des TS. Auf der empf�ngerseite kann dann        
        logger.debug(javaScript);
        Clients.evalJavaScript(javaScript);            
    }

  public String getSendBackApplicationName()
  {
    String s = (String) SessionHelper.getSessionObjectByName("sendBackApplicationName");
 
    if (s != null)
      return s;
    return "";
  }

  public String getSendBackMethodName()
  {
    String s = (String) SessionHelper.getSessionObjectByName("sendBackMethodName");
    if (s != null)
      return s;
    return "";
  }

  public Integer getSendBackTypeDV()
  {
    if ((Integer) SessionHelper.getSessionObjectByName("typeDV") != null)
      return (Integer) SessionHelper.getSessionObjectByName("typeDV");
    return -1;
  }

  public Integer getSendBackTypeCS()
  {
    if ((Integer) SessionHelper.getSessionObjectByName("typeCS") != null)
      return (Integer) SessionHelper.getSessionObjectByName("typeCS");
    return -1;
  }

  public Integer getSendBackTypeCSV()
  {
    if ((Integer) SessionHelper.getSessionObjectByName("typeCSV") != null)
      return (Integer) SessionHelper.getSessionObjectByName("typeCSV");
    return -1;
  }

  public Integer getSendBackTypeVS()
  {
    if ((Integer) SessionHelper.getSessionObjectByName("typeVS") != null)
      return (Integer) SessionHelper.getSessionObjectByName("typeVS");
    return -1;
  }

  public Integer getSendBackTypeVSV()
  {
    if ((Integer) SessionHelper.getSessionObjectByName("typeVSV") != null)
      return (Integer) SessionHelper.getSessionObjectByName("typeVSV");
    return -1;
  }

  public Integer getSendBackTypeCSEV()
  {
    if ((Integer) SessionHelper.getSessionObjectByName("typeCSEV") != null)
      return (Integer) SessionHelper.getSessionObjectByName("typeCSEV");
    return -1;
  }

  public String getSendBackTypeByInteger(Integer value)
  {
    String r = "No return type";

    if (value == SENDBACK_NAME)
      r = Labels.getLabel("common.name");
    if (value == SENDBACK_DESCRIPTION)
      r = Labels.getLabel("common.description");
    if (value == SENDBACK_CODE)
      r = Labels.getLabel("common.code");

    if (value == SENDBACK_NAME_DESCRIPTION)
      r = Labels.getLabel("common.name") + " & " + Labels.getLabel("common.description");
    if (value == SENDBACK_NAME_CODE)
      r = Labels.getLabel("common.name") + " & " + Labels.getLabel("common.code");
    if (value == SENDBACK_DESCRIPTION_CODE)
      r = Labels.getLabel("common.description") + " & " + Labels.getLabel("common.code");
    if (value == SENDBACK_NAME_DESCRIPTION_CODE)
      r = Labels.getLabel("common.name") + " & " + Labels.getLabel("common.description") + " & " + Labels.getLabel("common.code");

    return r;
  }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean aActive) {
        active = aActive;
    }
    
    //  public static void callJavaScript(Object data){
//        ScriptEngineManager manager = new ScriptEngineManager();  
//        ScriptEngine engine = manager.getEngineByName("JavaScript");  
//  
//        // JavaScript code in a String  
////        String script = "function hello(name) { print('Hello, ' + name); }";  
//        String script = "top.postMessage('"+ "test" + "', '*')";
//      try {
//          Bindings bindings = new SimpleBindings();
//          bindings.put("csev", data);          
//          
//          // evaluate script with bindings
//          engine.eval(script, bindings);            
//    
//  //        // javax.script.Invocable is an optional interface.  
//  //        // Check whether your script engine implements or not!  
//  //        // Note that the JavaScript engine implements Invocable interface.  
//  //        Invocable inv = (Invocable) engine;  
//  //  
//  //        inv.invokeFunction("hello", "Scripting!!" );  
//  //        inv.invokeFunction("hello", "Scripting!!" );
//      } catch (ScriptException ex) {
//          Logger.getLogger(SendBackHelper.class.getName()).log(Level.SEVERE, null, ex);
//      }
//  }

    public static SendBackHelper getInstance() {        
//        org.zkoss.zk.ui.Session session = Sessions.getCurrent();
//        logger.debug("SendBackHelper - session == " + session.toString());
        Execution exe = Executions.getCurrent();                   
//        logger.debug("SendBackHelper - d.getExe() Id == " + exe.getDesktop().getId());
//        logger.debug("SendBackHelper - Execution.getD Id == " + Executions.getCurrent().getDesktop().getId());

        SendBackHelper sbHelper = (SendBackHelper) exe.getAttribute("SendBackHelper");
        if(sbHelper == null){              
            Executions.getCurrent().getDesktop().getQueryString();
            
            sbHelper = new SendBackHelper();
            exe.setAttribute("SendBackHelper", sbHelper);
        }
        
        return sbHelper;
    }
}