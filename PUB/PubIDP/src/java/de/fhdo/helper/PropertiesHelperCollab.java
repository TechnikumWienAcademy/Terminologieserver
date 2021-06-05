/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.fhdo.helper;

import de.fhdo.db.DBSysParam;
import de.fhdo.db.DBSysParamCollab;

/**
 *
 * @author Christoph
 */
public class PropertiesHelperCollab extends PropertiesHelper{
    
    private static PropertiesHelperAdmin instance = null;

    public static PropertiesHelperAdmin getInstance() {
        return instance == null ? instance = new PropertiesHelperAdmin() : instance;
    }

    public PropertiesHelperCollab() {
        super(DBSysParamCollab.instance());
    }
}
