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
package de.fhdo.terminologie.ws.types;

import java.util.List;

/**
 *
 * @author Bernhard Rimatzki
 */
public class ImportType {
    
    private Long formatId;
    private byte[] filecontent;
    private List<FilecontentListEntry> fileContentList;
    private Boolean order;
    private String role;

    /**
     * @return the ID of the import, identifying it either as Claml, CSV or SVS import.
     */
    public Long getFormatId() {
        return formatId;
    }

    /**
     * @param formatId the formatId to set
     */
    public void setFormatId(Long formatId) {
        this.formatId = formatId;
    }

    /**
     * @return the filecontent
     */
    public byte[] getFilecontent() {
        return filecontent;
    }

    /**
     * @param filecontent the filecontent to set
     */
    public void setFilecontent(byte[] filecontent) {
        this.filecontent = filecontent;
    }

    public List<FilecontentListEntry> getFileContentList() {
        return fileContentList;
    }

    public void setFileContentList(List<FilecontentListEntry> fileContentList) {
        this.fileContentList = fileContentList;
    }

    public Boolean getOrder() {
        return order;
    }

    public void setOrder(Boolean order) {
        this.order = order;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
