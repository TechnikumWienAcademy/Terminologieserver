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
package de.fhdo.terminologie.ws.administration.types;

import de.fhdo.terminologie.db.hibernate.ValueSet;
import de.fhdo.terminologie.ws.types.ImportType;
import de.fhdo.terminologie.ws.types.LoginType;

/**
 *
 * @author Robert Mützner (robert.muetzner@fh-dortmund.de)
 */
public class ImportValueSetRequestType
{

    public static final long IMPORT_CSV_ID = 300;
    public static final long IMPORT_SVS_ID = 301;

    public static String getPossibleFormats()
    {
        String s = "Mögliche Import-Formate sind:\n300: CSV\n301: SVS";
        return s;
    }

    private ImportType importInfos;
    private LoginType login;
    private ValueSet valueSet;
    private Long importId;
    private boolean freigabeFix = false; //TRMMRK

    
    public void enableFreigabeFix() { freigabeFix = true; }
    
    public boolean freigabeFixed() { return freigabeFix; }
    /**
     * @return the importInfos
     */
    public ImportType getImportInfos()
    {
        return importInfos;
    }

    /**
     * @param importInfos the importInfos to set
     */
    public void setImportInfos(ImportType importInfos)
    {
        this.importInfos = importInfos;
    }

    /**
     * @return the login
     */
    public LoginType getLogin()
    {
        return login;
    }

    /**
     * @param login the login to set
     */
    public void setLogin(LoginType login)
    {
        this.login = login;
    }

    /**
     * @return the valueSet
     */
    public ValueSet getValueSet()
    {
        return valueSet;
    }

    /**
     * @param valueSet the valueSet to set
     */
    public void setValueSet(ValueSet valueSet)
    {
        this.valueSet = valueSet;
    }

    public Long getImportId()
    {
        return importId;
    }

    public void setImportId(Long importId)
    {
        this.importId = importId;
    }

}
