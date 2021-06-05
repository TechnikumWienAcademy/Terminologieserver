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
package de.fhdo.db.hibernate;
// Generated 24.10.2011 10:08:21 by Hibernate Tools 3.2.1.GA


import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import static javax.persistence.GenerationType.IDENTITY;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

//added by PU

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
@XmlRootElement
@XmlType(namespace="de.fhdo.termserver.idp.types")
@Entity
@Table(name="value_set_metadata_value"
    
)
public class ValueSetMetadataValue  implements java.io.Serializable {


     private Long id;
     private MetadataParameter metadataParameter;
     private CodeSystemEntityVersion codeSystemEntityVersion;;
     private Long valuesetVersionId;
     private String parameterValue;

    public ValueSetMetadataValue() {
    }

	
    public ValueSetMetadataValue(String parameterValue) {
        this.parameterValue = parameterValue;
    }
    public ValueSetMetadataValue(CodeSystemEntityVersion codeSystemEntityVersion, Long valuesetVersionId, MetadataParameter metadataParameter, String parameterValue) {
       this.codeSystemEntityVersion = codeSystemEntityVersion;
       this.valuesetVersionId = valuesetVersionId;
       this.metadataParameter = metadataParameter;
       this.parameterValue = parameterValue;
    }
   
     @Id @GeneratedValue(strategy=IDENTITY)
    
    @Column(name="id", unique=true, nullable=false)
    public Long getId() {
        return this.id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="codeSystemEntityVersionId")
    public CodeSystemEntityVersion getCodeSystemEntityVersion() {
        return this.codeSystemEntityVersion;
    }
    
    public void setCodeSystemEntityVersion(CodeSystemEntityVersion codeSystemEntityVersion) {
        this.codeSystemEntityVersion = codeSystemEntityVersion;
    }
    
    @Column(name="valuesetVersionId", unique=true, nullable=false)
    public Long getValuesetVersionId() {
        return this.valuesetVersionId;
    }
    
    public void setValuesetVersionId(Long valuesetVersionId) {
        this.valuesetVersionId = valuesetVersionId;
    }
    
@ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="metadataParameterId")
    public MetadataParameter getMetadataParameter() {
        return this.metadataParameter;
    }
    
    public void setMetadataParameter(MetadataParameter metadataParameter) {
        this.metadataParameter = metadataParameter;
    }
    
    @Column(name="parameterValue", nullable=false, length=65535)
    public String getParameterValue() {
        return this.parameterValue;
    }
    
    public void setParameterValue(String parameterValue) {
        this.parameterValue = parameterValue;
    }




}


