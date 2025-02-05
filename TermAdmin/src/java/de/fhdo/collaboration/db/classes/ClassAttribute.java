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
package de.fhdo.collaboration.db.classes;
// Generated 15.05.2013 18:02:38 by Hibernate Tools 3.2.1.GA


import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import static javax.persistence.GenerationType.IDENTITY;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 * ClassAttribute generated by hbm2java
 */
@Entity
@Table(name="class_attribute"
    
)
public class ClassAttribute  implements java.io.Serializable {


     private Long id;
     private Domain domain;
     private String className;
     private String attribute;

    public ClassAttribute() {
    }

    public ClassAttribute(Domain domain, String className, String attribute) {
       this.domain = domain;
       this.className = className;
       this.attribute = attribute;
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
    @JoinColumn(name="domain_id", nullable=false)
    public Domain getDomain() {
        return this.domain;
    }
    
    public void setDomain(Domain domain) {
        this.domain = domain;
    }
    
    @Column(name="class_name", nullable=false, length=100)
    public String getClassName() {
        return this.className;
    }
    
    public void setClassName(String className) {
        this.className = className;
    }
    
    @Column(name="attribute", nullable=false, length=100)
    public String getAttribute() {
        return this.attribute;
    }
    
    public void setAttribute(String attribute) {
        this.attribute = attribute;
    }




}


