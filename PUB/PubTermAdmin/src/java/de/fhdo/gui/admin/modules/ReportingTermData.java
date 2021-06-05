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
package de.fhdo.gui.admin.modules;

/**
 *
 * @author Philipp Urbauer
 */
public class ReportingTermData {
    
    //Header-Bezeichner
    public static final String VOC_NAME_Header = "Name CS/VS";
    public static final String VERSION_NAME_Header = "Versionsname";
    public static final String OID_Header = "OID";
    public static final String VERSION_NUMBER_Header = "Version";
    public static final String TYPE_Header = "Type";
    public static final String NUMBER_CONCEPTS_Header = "#Konzepte";
    public static final String STATUS_Header = "Status";
    public static final String PUBLIC_DATE_Header = "Publikationsdatum"; 
    
    private String vokabularyName;
    private String versionName;
    private String oid;
    private String versionnumber;
    private String type;
    private String numberConcepts;
    private String status;
    private String releaseDate;

    public String getVokabularyName() {
        return vokabularyName;
    }

    public void setVokabularyName(String vokabularyName) {
        this.vokabularyName = vokabularyName;
    }

    public String getVersionName() {
        return versionName;
    }

    public void setVersionName(String versionName) {
        this.versionName = versionName;
    }

    public String getOid() {
        return oid;
    }

    public void setOid(String oid) {
        this.oid = oid;
    }

    public String getVersionnumber() {
        return versionnumber;
    }

    public void setVersionnumber(String versionnumber) {
        this.versionnumber = versionnumber;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getNumberConcepts() {
        return numberConcepts;
    }

    public void setNumberConcepts(String numberConcepts) {
        this.numberConcepts = numberConcepts;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(String releaseDate) {
        this.releaseDate = releaseDate;
    }
}
