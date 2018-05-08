/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.fhdo.collaboration.discgroup;

import de.fhdo.interfaces.IUpdateModal;
import de.fhdo.list.IGenericListActions;
import java.util.HashMap;
import java.util.Map;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zul.Window;

/**
 *
 * @author Philipp Urbauer
 */
public class ListenerReadOnlyGroups implements IGenericListActions, IUpdateModal{

    private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();
    private IUpdateModal up = null;
    
    public ListenerReadOnlyGroups(IUpdateModal up) {
        this.up = up;
    }
    
    public void onNewClicked(String id) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public void onEditClicked(String id, Object data) {
        logger.debug("onEditClicked()");

        if (data != null && data instanceof DiscGroupData)
        {
          DiscGroupData dgd = (DiscGroupData) data;

          try
          {
            Map map = new HashMap();
            map.put("dgd", dgd);
            map.put("showOnly", true);

            Window win = (Window) Executions.createComponents(
                    "/collaboration/discgroup/discGroupDetails.zul", null, map);

            ((DiscGroupDetails) win).setUpdateListInterface(this);

            win.doModal();
          }
          catch (Exception ex)
          {
            logger.debug("Fehler beim Öffnen der DiscGroupDetails: " + ex.getLocalizedMessage());
            ex.printStackTrace();
          }
        }
    }

    public void onDeleted(String id, Object data) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public void onSelected(String id, Object data) {
        
        logger.debug("onEditClicked()");

        if (data != null && data instanceof DiscGroupData)
        {
          DiscGroupData dgd = (DiscGroupData) data;

          try
          {
            Map map = new HashMap();
            map.put("dgd", dgd);
            map.put("showOnly", true);

            Window win = (Window) Executions.createComponents(
                    "/collaboration/discgroup/discGroupDetails.zul", null, map);

            ((DiscGroupDetails) win).setUpdateListInterface(this);

            win.doModal();
          }
          catch (Exception ex)
          {
            logger.debug("Fehler beim Öffnen der DiscGroupDetails: " + ex.getLocalizedMessage());
            ex.printStackTrace();
          }
        }
    }

    public void update(Object o, boolean edited) {
    
        up.update(o, edited);        
    }
}
