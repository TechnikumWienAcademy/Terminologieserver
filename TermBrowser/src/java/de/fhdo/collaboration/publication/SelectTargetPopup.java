/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.fhdo.collaboration.publication;

import de.fhdo.collaboration.db.classes.Proposal;
import de.fhdo.collaboration.workflow.ProposalWorkflow;
import de.fhdo.collaboration.workflow.TerminologyReleaseManager;
import de.fhdo.helper.ArgumentHelper;
import de.fhdo.terminologie.ws.searchPub.CodeSystem;
import de.fhdo.terminologie.ws.searchPub.ValueSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.zkoss.bind.BindUtils;
import org.zkoss.bind.Binder;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.ContextParam;
import org.zkoss.bind.annotation.ContextType;
import org.zkoss.bind.annotation.ExecutionArgParam;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.ext.AfterCompose;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Label;
import org.zkoss.zul.ListModel;
import org.zkoss.zul.ListModelList;
import org.zkoss.zul.Selectbox;
import org.zkoss.zul.Window;

/**
 *
 * @author Stefan Puraner
 */
public class SelectTargetPopup extends Window implements AfterCompose
{
  private static org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();
  private List<Object> targets;
  private Label lSource;
  private ListModel<String> targetNamesModel;
  private Combobox cbTarget;
  private String source;
  private TerminologyReleaseManager manager;
  
  public void afterCompose()
  {
    targets = (List<Object>) ArgumentHelper.getWindowArgument("targets");
    source = ArgumentHelper.getWindowArgument("source").toString();
    List<String> targetNames_temp = new ArrayList<String>();
    if(targets.size() > 0)
    {
      if(targets.get(0) instanceof CodeSystem)
      {
        for(Object o : targets)
        {
          targetNames_temp.add(((CodeSystem) o).getName());
        }
      }
      else if(targets.get(0) instanceof ValueSet)
      {
        for(Object o : targets)
        {
          targetNames_temp.add(((ValueSet) o).getName());
        }
      }
      targetNamesModel = new ListModelList<String>(targetNames_temp);
      cbTarget = (Combobox) getFellow("targets");
      cbTarget.setModel(targetNamesModel);
      
      lSource = (Label) getFellow("source");
      lSource.setValue(source);
      
    }
  }
  
    @Command
    public void onOkClicked()
    {
        //pass selected CS VS to main window
        if (cbTarget.getSelectedItem() != null)
        {
            if (targets.get(0) instanceof CodeSystem)
            {
                for (Object o : targets)
                {
                    if (cbTarget.getSelectedItem().getValue().toString().equals(((CodeSystem) o).getName()))
                    {
                        ProposalWorkflow.getInstance().selectTargets(o);
                        if(this.manager != null)
                        {
                            manager.setTargetCS((CodeSystem) o);
                        }
                    }
                }
            }
            else if (targets.get(0) instanceof ValueSet)
            {
                for (Object o : targets)
                {
                    if (cbTarget.getSelectedItem().getValue().toString().equals(((ValueSet) o).getName()))
                    {
                        ProposalWorkflow.getInstance().selectTargets(o);
                        if(this.manager != null)
                        {
                            manager.setTargetVS((ValueSet) o);
                        }
                    }
                }
            }
            
            //Fenster schlie�en
            this.setVisible(false);
            this.detach();
        }
    }
    
    public void setReleaseManager(TerminologyReleaseManager manager)
    {
        this.manager = manager;
    }
}
