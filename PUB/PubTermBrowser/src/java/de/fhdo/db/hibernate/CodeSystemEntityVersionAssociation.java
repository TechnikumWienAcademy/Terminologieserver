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
// Generated 10.10.2012 15:20:29 by Hibernate Tools 3.2.1.GA


import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import static javax.persistence.GenerationType.IDENTITY;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 * CodeSystemEntityVersionAssociation generated by hbm2java
 */
@Entity
@Table(name="code_system_entity_version_association"
    
)
public class CodeSystemEntityVersionAssociation  implements java.io.Serializable {


     private Long id;
     private CodeSystemEntityVersion codeSystemEntityVersionByCodeSystemEntityVersionId2;
     private CodeSystemEntityVersion codeSystemEntityVersionByCodeSystemEntityVersionId1;
     private AssociationType associationType;
     private Long leftId;
     private Integer associationKind;
     private Integer status;
     private Date statusDate;
     private Date insertTimestamp;

    public CodeSystemEntityVersionAssociation() {
    }

	
    public CodeSystemEntityVersionAssociation(CodeSystemEntityVersion codeSystemEntityVersionByCodeSystemEntityVersionId2, CodeSystemEntityVersion codeSystemEntityVersionByCodeSystemEntityVersionId1, AssociationType associationType, Date statusDate, Date insertTimestamp) {
        this.codeSystemEntityVersionByCodeSystemEntityVersionId2 = codeSystemEntityVersionByCodeSystemEntityVersionId2;
        this.codeSystemEntityVersionByCodeSystemEntityVersionId1 = codeSystemEntityVersionByCodeSystemEntityVersionId1;
        this.associationType = associationType;
        this.statusDate = statusDate;
        this.insertTimestamp = insertTimestamp;
    }
    public CodeSystemEntityVersionAssociation(CodeSystemEntityVersion codeSystemEntityVersionByCodeSystemEntityVersionId2, CodeSystemEntityVersion codeSystemEntityVersionByCodeSystemEntityVersionId1, AssociationType associationType, Long leftId, Integer associationKind, Integer status, Date statusDate, Date insertTimestamp) {
       this.codeSystemEntityVersionByCodeSystemEntityVersionId2 = codeSystemEntityVersionByCodeSystemEntityVersionId2;
       this.codeSystemEntityVersionByCodeSystemEntityVersionId1 = codeSystemEntityVersionByCodeSystemEntityVersionId1;
       this.associationType = associationType;
       this.leftId = leftId;
       this.associationKind = associationKind;
       this.status = status;
       this.statusDate = statusDate;
       this.insertTimestamp = insertTimestamp;
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
    @JoinColumn(name="codeSystemEntityVersionId2", nullable=false)
    public CodeSystemEntityVersion getCodeSystemEntityVersionByCodeSystemEntityVersionId2() {
        return this.codeSystemEntityVersionByCodeSystemEntityVersionId2;
    }
    
    public void setCodeSystemEntityVersionByCodeSystemEntityVersionId2(CodeSystemEntityVersion codeSystemEntityVersionByCodeSystemEntityVersionId2) {
        this.codeSystemEntityVersionByCodeSystemEntityVersionId2 = codeSystemEntityVersionByCodeSystemEntityVersionId2;
    }
@ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="codeSystemEntityVersionId1", nullable=false)
    public CodeSystemEntityVersion getCodeSystemEntityVersionByCodeSystemEntityVersionId1() {
        return this.codeSystemEntityVersionByCodeSystemEntityVersionId1;
    }
    
    public void setCodeSystemEntityVersionByCodeSystemEntityVersionId1(CodeSystemEntityVersion codeSystemEntityVersionByCodeSystemEntityVersionId1) {
        this.codeSystemEntityVersionByCodeSystemEntityVersionId1 = codeSystemEntityVersionByCodeSystemEntityVersionId1;
    }
@ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="associationTypeId", nullable=false)
    public AssociationType getAssociationType() {
        return this.associationType;
    }
    
    public void setAssociationType(AssociationType associationType) {
        this.associationType = associationType;
    }
    
    @Column(name="leftId")
    public Long getLeftId() {
        return this.leftId;
    }
    
    public void setLeftId(Long leftId) {
        this.leftId = leftId;
    }
    
    @Column(name="associationKind")
    public Integer getAssociationKind() {
        return this.associationKind;
    }
    
    public void setAssociationKind(Integer associationKind) {
        this.associationKind = associationKind;
    }
    
    @Column(name="status")
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
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="insertTimestamp", nullable=false, length=19)
    public Date getInsertTimestamp() {
        return this.insertTimestamp;
    }
    
    public void setInsertTimestamp(Date insertTimestamp) {
        this.insertTimestamp = insertTimestamp;
    }




}

