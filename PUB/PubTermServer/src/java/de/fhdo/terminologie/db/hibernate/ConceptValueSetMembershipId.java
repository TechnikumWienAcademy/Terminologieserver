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


import javax.persistence.Column;
import javax.persistence.Embeddable;

/**
 * ConceptValueSetMembershipId generated by hbm2java
 */
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
@XmlRootElement
@XmlType(namespace="de.fhdo.termserver.types")
@Embeddable
public class ConceptValueSetMembershipId  implements java.io.Serializable {


     private Long codeSystemEntityVersionId;
     private Long valuesetVersionId;

    public ConceptValueSetMembershipId() {
    }

    public ConceptValueSetMembershipId(Long codeSystemEntityVersionId, Long valuesetVersionId) {
       this.codeSystemEntityVersionId = codeSystemEntityVersionId;
       this.valuesetVersionId = valuesetVersionId;
    }
   

    @Column(name="codeSystemEntityVersionId", nullable=false)
    public Long getCodeSystemEntityVersionId() {
        return this.codeSystemEntityVersionId;
    }
    
    public void setCodeSystemEntityVersionId(Long codeSystemEntityVersionId) {
        this.codeSystemEntityVersionId = codeSystemEntityVersionId;
    }

    @Column(name="valuesetVersionId", nullable=false)
    public Long getValuesetVersionId() {
        return this.valuesetVersionId;
    }
    
    public void setValuesetVersionId(Long valuesetVersionId) {
        this.valuesetVersionId = valuesetVersionId;
    }


     @Override
   public boolean equals(Object other) {
         if ( (this == other ) ) return true;
		 if ( (other == null ) ) return false;
		 if ( !(other instanceof ConceptValueSetMembershipId) ) return false;
		 ConceptValueSetMembershipId castOther = ( ConceptValueSetMembershipId ) other; 
         
		 return (this.getCodeSystemEntityVersionId()==castOther.getCodeSystemEntityVersionId())
 && (this.getValuesetVersionId()==castOther.getValuesetVersionId());
   }
   
     @Override
   public int hashCode() {
         int result = 17;
         
         result = 37 * result + (int) ((long)this.getCodeSystemEntityVersionId());
         result = 37 * result + (int) ((long)this.getValuesetVersionId());
         return result;
   }   


}

