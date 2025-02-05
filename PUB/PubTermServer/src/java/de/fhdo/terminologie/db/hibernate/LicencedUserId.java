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
 * LicencedUserId generated by hbm2java
 */
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
@XmlRootElement
@XmlType(namespace="de.fhdo.termserver.types")
@Embeddable
public class LicencedUserId  implements java.io.Serializable {


     private Long userId;
     private Long codeSystemVersionId;

    public LicencedUserId() {
    }

    public LicencedUserId(Long userId, Long codeSystemVersionId) {
       this.userId = userId;
       this.codeSystemVersionId = codeSystemVersionId;
    }
   

    @Column(name="userId", nullable=false)
    public Long getUserId() {
        return this.userId;
    }
    
    public void setUserId(Long userId) {
        this.userId = userId;
    }

    @Column(name="codeSystemVersionId", nullable=false)
    public Long getCodeSystemVersionId() {
        return this.codeSystemVersionId;
    }
    
    public void setCodeSystemVersionId(Long codeSystemVersionId) {
        this.codeSystemVersionId = codeSystemVersionId;
    }


     @Override
   public boolean equals(Object other) {
         if ( (this == other ) ) return true;
		 if ( (other == null ) ) return false;
		 if ( !(other instanceof LicencedUserId) ) return false;
		 LicencedUserId castOther = ( LicencedUserId ) other; 
         
		 return (this.getUserId()==castOther.getUserId())
 && (this.getCodeSystemVersionId()==castOther.getCodeSystemVersionId());
   }
   
     @Override
   public int hashCode() {
         int result = 17;
         
         result = 37 * result + (int) ((long)this.getUserId());
         result = 37 * result + (int) ((long)this.getCodeSystemVersionId());
         return result;
   }   


}


