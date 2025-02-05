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
package de.fhdo.terminologie.db.hibernate;
// Generated 24.10.2011 10:08:21 by Hibernate Tools 3.2.1.GA


import java.util.Date;
import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 * ConceptValueSetMembership generated by hbm2java
 */
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
@XmlRootElement
@XmlType(namespace="de.fhdo.termserver.types")
@Entity
@Table(name="concept_value_set_membership"
    
)
public class ConceptValueSetMembership  implements java.io.Serializable {


     private ConceptValueSetMembershipId id;
     private CodeSystemEntityVersion codeSystemEntityVersion;
     private ValueSetVersion valueSetVersion;
     private String valueOverride;
     private Integer status;
     private Date statusDate;
     private Boolean isStructureEntry;
     private Long orderNr;
     private String awbeschreibung;
     private String bedeutung;
     private String hinweise;

    public ConceptValueSetMembership() {
    }

	
    public ConceptValueSetMembership(ConceptValueSetMembershipId id, CodeSystemEntityVersion codeSystemEntityVersion, ValueSetVersion valueSetVersion) {
        this.id = id;
        this.codeSystemEntityVersion = codeSystemEntityVersion;
        this.valueSetVersion = valueSetVersion;
    }
    public ConceptValueSetMembership(ConceptValueSetMembershipId id, CodeSystemEntityVersion codeSystemEntityVersion, ValueSetVersion valueSetVersion, String valueOverride, Integer status, Date statusDate, Boolean isStructureEntry, Long orderNr) {
       this.id = id;
       this.codeSystemEntityVersion = codeSystemEntityVersion;
       this.valueSetVersion = valueSetVersion;
       this.valueOverride = valueOverride;
       this.status = status;
       this.statusDate = statusDate;
       this.isStructureEntry = isStructureEntry;
       this.orderNr = orderNr;
    }
   
     @EmbeddedId
    
    @AttributeOverrides( {
        @AttributeOverride(name="codeSystemEntityVersionId", column=@Column(name="codeSystemEntityVersionId", nullable=false) ), 
        @AttributeOverride(name="valuesetVersionId", column=@Column(name="valuesetVersionId", nullable=false) ) } )
    public ConceptValueSetMembershipId getId() {
        return this.id;
    }
    
    public void setId(ConceptValueSetMembershipId id) {
        this.id = id;
    }
@ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="codeSystemEntityVersionId", nullable=false, insertable=false, updatable=false)
    public CodeSystemEntityVersion getCodeSystemEntityVersion() {
        return this.codeSystemEntityVersion;
    }
    
    public void setCodeSystemEntityVersion(CodeSystemEntityVersion codeSystemEntityVersion) {
        this.codeSystemEntityVersion = codeSystemEntityVersion;
    }
@ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="valuesetVersionId", nullable=false, insertable=false, updatable=false)
    public ValueSetVersion getValueSetVersion() {
        return this.valueSetVersion;
    }
    
    public void setValueSetVersion(ValueSetVersion valueSetVersion) {
        this.valueSetVersion = valueSetVersion;
    }
    
    @Column(name="valueOverride", length=100)
    public String getValueOverride() {
        return this.valueOverride;
    }
    
    public void setValueOverride(String valueOverride) {
        this.valueOverride = valueOverride;
    }

    @Column(name="status", nullable=false)
    public Integer getStatus() {
        return this.status;
    }
    
    public void setStatus(Integer status) {
        this.status = status;
    }
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="statusDate", nullable=false, length=19)
    public Date getStatusDate() {
        return this.statusDate;
    }
    
    public void setStatusDate(Date statusDate) {
        this.statusDate = statusDate;
    }
    @Column(name="isStructureEntry")
    public Boolean getIsStructureEntry() {
        return isStructureEntry;
    }

    public void setIsStructureEntry(Boolean isStructureEntry) {
        this.isStructureEntry = isStructureEntry;
    }
    @Column(name="orderNr")
    public Long getOrderNr() {
        return orderNr;
    }

    public void setOrderNr(Long orderNr) {
        this.orderNr = orderNr;
    }
    
    @Column(name = "awbeschreibung", length = 65535)
    public String getAwbeschreibung() {
        return awbeschreibung;
    }

    public void setAwbeschreibung(String awbeschreibung) {
        this.awbeschreibung = awbeschreibung;
    }

    @Column(name = "bedeutung", length = 65535)
    public String getBedeutung() {
        return bedeutung;
    }

    public void setBedeutung(String bedeutung) {
        this.bedeutung = bedeutung;
    }

    @Column(name = "hinweise", length = 65535)
    public String getHinweise() {
        return hinweise;
    }

    public void setHinweise(String hinweise) {
        this.hinweise = hinweise;
    }
}


