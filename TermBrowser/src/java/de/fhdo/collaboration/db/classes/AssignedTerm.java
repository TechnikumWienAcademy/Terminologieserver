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
 * AssignedTerm by Philipp Urbauer
 */
@Entity
@Table(name="assigned_term"
    
)
public class AssignedTerm  implements java.io.Serializable {


     private Long id;
     private Long classId;
     private String classname;
     private Collaborationuser collaborationuser;

    public AssignedTerm() {
    }

    public AssignedTerm(Long id, Long classId, String classname, Collaborationuser collaborationuser) {
       this.id = id;
       this.classId = classId;
       this.classname = classname;
       this.collaborationuser = collaborationuser;
    }
   
     @Id @GeneratedValue(strategy=IDENTITY)
    
    @Column(name="id", unique=true, nullable=false)
    public Long getId() {
        return this.id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    @Column(name="classId")
    public Long getClassId() {
        return this.classId;
    }
    
    public void setClassId(Long classId) {
        this.classId = classId;
    }
    
    @Column(name="classname", length=65535)
    public String getClassname() {
        return this.classname;
    }
    
    public void setClassname(String classname) {
        this.classname = classname;
    }
    
    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="collaborationUserId", nullable=false)
    public Collaborationuser getCollaborationuser() {
        return this.collaborationuser;
    }
    
    public void setCollaborationuser(Collaborationuser collaborationuser) {
        this.collaborationuser = collaborationuser;
    }

}


