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
package de.fhdo.gui.admin.modules.collaboration;
/**
 *
 * @author Philipp Urbauer
 */
public class SvAssignmentData {
    
    private String termName;
    private Long classId;
    private String classname;
    private Long assignedTermId;
    private Long collaborationuserId;
    private String firstName = "";
    private String name = "";
    private String username = "";
    private String organisation = "";
    private boolean autoRelease = false;
 
    public SvAssignmentData(){
    
    }
    
    public SvAssignmentData(String firstName, String name, String organisation) {
        this.firstName = firstName;
        this.name = name;
        this.organisation = organisation;
    }

    public SvAssignmentData(Long collaborationuserId, String firstName, String name, String organisation) {
        this.collaborationuserId = collaborationuserId;
        this.firstName = firstName;
        this.name = name;
        this.organisation = organisation;
    }

    public Long getCollaborationuserId() {
        return collaborationuserId;
    }

    public void setCollaborationuserId(Long collaborationuserId) {
        this.collaborationuserId = collaborationuserId;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getOrganisation() {
        return organisation;
    }

    public void setOrganisation(String organisation) {
        this.organisation = organisation;
    }

    public String getTermName() {
        return termName;
    }

    public void setTermName(String termName) {
        this.termName = termName;
    }

    public Long getClassId() {
        return classId;
    }

    public void setClassId(Long classId) {
        this.classId = classId;
    }

    public String getClassname() {
        return classname;
    }

    public void setClassname(String classname) {
        this.classname = classname;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Long getAssignedTermId() {
        return assignedTermId;
    }

    public void setAssignedTermId(Long assignedTermId) {
        this.assignedTermId = assignedTermId;
    }
    
    public void setAutoRelease(boolean autoRelease)
    {
      this.autoRelease = autoRelease;
    }
    
    public boolean getAutoRelease()
    {
      return this.autoRelease;
    }
}
