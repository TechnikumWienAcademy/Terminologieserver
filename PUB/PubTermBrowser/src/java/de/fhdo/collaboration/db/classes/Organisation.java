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


import java.util.HashSet;
import java.util.Set;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import static javax.persistence.GenerationType.IDENTITY;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

/**
 * Organisation generated by hbm2java
 */
@Entity
@Table(name="organisation"
    
)
public class Organisation  implements java.io.Serializable {


     private Long id;
     private String organisation;
     private String organisationAbbr;
     private String organisationLink;
     private Set<Collaborationuser> collaborationusers = new HashSet<Collaborationuser>(0);

    public Organisation() {
    }

	
    public Organisation(String organisation) {
        this.organisation = organisation;
    }
    public Organisation(String organisation, String organisationAbbr, String organisationLink, Set<Collaborationuser> collaborationusers) {
       this.organisation = organisation;
       this.organisationAbbr = organisationAbbr;
       this.organisationLink = organisationLink;
       this.collaborationusers = collaborationusers;
    }
   
     @Id @GeneratedValue(strategy=IDENTITY)
    
    @Column(name="id", unique=true, nullable=false)
    public Long getId() {
        return this.id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    @Column(name="organisation", nullable=false, length=65535)
    public String getOrganisation() {
        return this.organisation;
    }
    
    public void setOrganisation(String organisation) {
        this.organisation = organisation;
    }
    
    @Column(name="organisationAbbr", length=65535)
    public String getOrganisationAbbr() {
        return this.organisationAbbr;
    }
    
    public void setOrganisationAbbr(String organisationAbbr) {
        this.organisationAbbr = organisationAbbr;
    }
    
    @Column(name="organisationLink", length=65535)
    public String getOrganisationLink() {
        return this.organisationLink;
    }
    
    public void setOrganisationLink(String organisationLink) {
        this.organisationLink = organisationLink;
    }
@OneToMany(cascade=CascadeType.ALL, fetch=FetchType.LAZY, mappedBy="organisation")
    public Set<Collaborationuser> getCollaborationusers() {
        return this.collaborationusers;
    }
    
    public void setCollaborationusers(Set<Collaborationuser> collaborationusers) {
        this.collaborationusers = collaborationusers;
    }




}

