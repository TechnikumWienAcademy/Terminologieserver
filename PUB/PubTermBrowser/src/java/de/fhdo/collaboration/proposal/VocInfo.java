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
package de.fhdo.collaboration.proposal;
/**
 *
 * @author Philipp Urbauer
 */
public class VocInfo{
    private Long versionId;
    private Long csId;
    private String vocabularyName = "";
    private String versionName = "";
 
    public VocInfo(String vocabularyName, String versionName, String type) {
        this.vocabularyName = vocabularyName;
        this.versionName = versionName;
    }

    public VocInfo(Long versionId, Long csId, String vocabularyName, String versionName) {
        this.versionId = versionId;
        this.csId = csId;
        this.vocabularyName = vocabularyName;
        this.versionName = versionName;
    }

    public Long getVersionId() {
        return versionId;
    }

    public void setVersionId(Long versionId) {
        this.versionId = versionId;
    }

    public String getVocabularyName() {
        return vocabularyName;
    }

    public void setVocabularyName(String vocabularyName) {
        this.vocabularyName = vocabularyName;
    }

    public String getVersionName() {
        return versionName;
    }

    public void setVersionName(String versionName) {
        this.versionName = versionName;
    }

    public Long getCsId() {
        return csId;
    }

    public void setCsId(Long csId) {
        this.csId = csId;
    }
}