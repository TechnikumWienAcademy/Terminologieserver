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
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 * Statusrel generated by hbm2java
 */
@Entity
@Table(name="statusrel"
    
)
public class Statusrel  implements java.io.Serializable {


     private Long id;
     private Status statusByStatusIdFrom;
     private Action action;
     private Status statusByStatusIdTo;
     private Set<Role> roles = new HashSet<Role>(0);

    public Statusrel() {
    }

	
    public Statusrel(Status statusByStatusIdFrom, Status statusByStatusIdTo) {
        this.statusByStatusIdFrom = statusByStatusIdFrom;
        this.statusByStatusIdTo = statusByStatusIdTo;
    }
    public Statusrel(Status statusByStatusIdFrom, Action action, Status statusByStatusIdTo, Set<Role> roles) {
       this.statusByStatusIdFrom = statusByStatusIdFrom;
       this.action = action;
       this.statusByStatusIdTo = statusByStatusIdTo;
       this.roles = roles;
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
    @JoinColumn(name="statusIdFrom", nullable=false)
    public Status getStatusByStatusIdFrom() {
        return this.statusByStatusIdFrom;
    }
    
    public void setStatusByStatusIdFrom(Status statusByStatusIdFrom) {
        this.statusByStatusIdFrom = statusByStatusIdFrom;
    }
@ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="actionId")
    public Action getAction() {
        return this.action;
    }
    
    public void setAction(Action action) {
        this.action = action;
    }
@ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="statusIdTo", nullable=false)
    public Status getStatusByStatusIdTo() {
        return this.statusByStatusIdTo;
    }
    
    public void setStatusByStatusIdTo(Status statusByStatusIdTo) {
        this.statusByStatusIdTo = statusByStatusIdTo;
    }
@ManyToMany(cascade=CascadeType.ALL, fetch=FetchType.LAZY)
    @JoinTable(name="role2action", joinColumns = { 
        @JoinColumn(name="statusRelId", nullable=false, updatable=false) }, inverseJoinColumns = { 
        @JoinColumn(name="roleId", nullable=false, updatable=false) })
    public Set<Role> getRoles() {
        return this.roles;
    }
    
    public void setRoles(Set<Role> roles) {
        this.roles = roles;
    }




}

