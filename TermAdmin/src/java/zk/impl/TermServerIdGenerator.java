/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package zk.impl;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Desktop;
import org.zkoss.zk.ui.Page;
import org.zkoss.zk.ui.metainfo.ComponentInfo;

/**
 *
 * @author puraner
 */
public class TermServerIdGenerator implements org.zkoss.zk.ui.sys.IdGenerator
{
    public String nextDesktopId(Desktop desktop) {
        if (desktop.getAttribute("Id_Num") == null) {
            String number = "0";
            desktop.setAttribute("Id_Num", number);
        }
        return null;
    }
 
    public String nextPageUuid(Page page) {
        return null;
    }

    public String nextComponentUuid(Desktop desktop, Component cmpnt, ComponentInfo ci)
    {
        int i = Integer.parseInt(desktop.getAttribute("Id_Num").toString());
        i++;// Start from 1
        desktop.setAttribute("Id_Num", String.valueOf(i));
        return "zk_comp_" + i;
    }
}