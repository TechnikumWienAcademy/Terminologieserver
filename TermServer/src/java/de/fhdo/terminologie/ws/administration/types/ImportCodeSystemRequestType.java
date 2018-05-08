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

import de.fhdo.terminologie.db.hibernate.CodeSystem;
import de.fhdo.terminologie.ws.types.ImportType;
import de.fhdo.terminologie.ws.types.LoginType;

/**
 * 28.03.2012, Mützner: LOINC-Format hinzugefügt
 * 07.02.2013, Mützner: KBV Keytabs hinzugefügt
 * 
 * @author Bernhard Rimatzki
 */
public class ImportCodeSystemRequestType 
{
    public static final long IMPORT_CLAML_ID = 193;
    public static final long IMPORT_CSV_ID = 194;
    public static final long IMPORT_LOINC_ID = 200;
    public static final long IMPORT_LOINC_RELATIONS_ID = 201;
    public static final long IMPORT_KBV_KEYTABS_ID = 234;
    public static final long IMPORT_SVS_ID = 235;
    public static final long IMPORT_LeiKat_ID = 500;
    public static final long IMPORT_KAL_ID = 501;
    public static final long IMPORT_ICD_BMG_ID = 502;
    
    public static String getPossibleFormats()
    {
      String s = "Mögliche Import-Formate sind:\n193: ClaML\n194: CSV\n200: LOINC\n201: LOINC relations\n234: KBV Keytabs\n500: Leistungskatalog BMG AT\n501: KAL BMG AT\n502: ICD-10 BMG AT";
      return s;
    }
    
    private ImportType importInfos;
    private LoginType login;
    private CodeSystem codeSystem;
    private Long importId;

    public Long getImportId() {
        return importId;
    }

    public void setImportId(Long importId) {
        this.importId = importId;
    }

    /**
     * @return the importInfos
     */
    public ImportType getImportInfos() {
        return importInfos;
    }

    /**
     * @param importInfos the importInfos to set
     */
    public void setImportInfos(ImportType importInfos) {
        this.importInfos = importInfos;
    }

    /**
     * @return the login
     */
    public LoginType getLogin() {
        return login;
    }

    /**
     * @param login the login to set
     */
    public void setLogin(LoginType login) {
        this.login = login;
    }

    /**
     * @return the codeSystem
     */
    public CodeSystem getCodeSystem() {
        return codeSystem;
    }

    /**
     * @param codeSystem the codeSystem to set
     */
    public void setCodeSystem(CodeSystem codeSystem) {
        this.codeSystem = codeSystem;
    }
}
