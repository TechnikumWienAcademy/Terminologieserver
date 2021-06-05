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

import de.fhdo.terminologie.db.hibernate.ConceptValueSetMembershipId;

/**
 *
 * @author Philipp Urbauer
 */
public class GlobalSearchResultEntry {
    
    private Boolean codeSystemEntry;
    private String term;
    private String code;
    private Long csevId;
    private ConceptValueSetMembershipId cvsmId;
    private String codeSystemName;
    private Long csId;
    private String codeSystemVersionName;
    private Long csvId;
    private String valueSetName;
    private Long vsId;
    private String valueSetVersionName;
    private Long vsvId;
    private String sourceCodeSystemInfo;
    

    public Boolean getCodeSystemEntry() {
        return codeSystemEntry;
    }

    public void setCodeSystemEntry(Boolean codeSystemEntry) {
        this.codeSystemEntry = codeSystemEntry;
    }

    public String getTerm() {
        return term;
    }

    public void setTerm(String term) {
        this.term = term;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getCodeSystemName() {
        return codeSystemName;
    }

    public void setCodeSystemName(String codeSystemName) {
        this.codeSystemName = codeSystemName;
    }

    public String getCodeSystemVersionName() {
        return codeSystemVersionName;
    }

    public void setCodeSystemVersionName(String codeSystemVersionName) {
        this.codeSystemVersionName = codeSystemVersionName;
    }

    public String getValueSetName() {
        return valueSetName;
    }

    public void setValueSetName(String valueSetName) {
        this.valueSetName = valueSetName;
    }

    public String getValueSetVersionName() {
        return valueSetVersionName;
    }

    public void setValueSetVersionName(String valueSetVersionName) {
        this.valueSetVersionName = valueSetVersionName;
    }

    public String getSourceCodeSystemInfo() {
        return sourceCodeSystemInfo;
    }

    public void setSourceCodeSystemInfo(String sourceCodeSystemInfo) {
        this.sourceCodeSystemInfo = sourceCodeSystemInfo;
    }

    public Long getCsevId() {
        return csevId;
    }

    public void setCsevId(Long csevId) {
        this.csevId = csevId;
    }

    public ConceptValueSetMembershipId getCvsmId() {
        return cvsmId;
    }

    public void setCvsmId(ConceptValueSetMembershipId cvsmId) {
        this.cvsmId = cvsmId;
    }

    public Long getCsId() {
        return csId;
    }

    public void setCsId(Long csId) {
        this.csId = csId;
    }

    public Long getCsvId() {
        return csvId;
    }

    public void setCsvId(Long csvId) {
        this.csvId = csvId;
    }

    public Long getVsId() {
        return vsId;
    }

    public void setVsId(Long vsId) {
        this.vsId = vsId;
    }

    public Long getVsvId() {
        return vsvId;
    }

    public void setVsvId(Long vsvId) {
        this.vsvId = vsvId;
    }
}
