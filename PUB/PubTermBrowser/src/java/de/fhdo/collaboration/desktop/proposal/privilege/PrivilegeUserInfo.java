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
package de.fhdo.collaboration.desktop.proposal.privilege;
/**
 *
 * @author Philipp Urbauer
 */
public class PrivilegeUserInfo {
    private Long collaborationuserId;
    private String firstName = "";
    private String name = "";
    private String organisation = "";
    private Boolean privExists;
    private Long privId;
 
    public PrivilegeUserInfo(String firstName, String name, String organisation) {
        this.firstName = firstName;
        this.name = name;
        this.organisation = organisation;
    }

    public PrivilegeUserInfo(Long collaborationuserId, String firstName, String name, String organisation, Boolean privExists, Long privId) {
        this.collaborationuserId = collaborationuserId;
        this.firstName = firstName;
        this.name = name;
        this.organisation = organisation;
        this.privExists = privExists;
        this.privId = privId;
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

    public Boolean getPrivExists() {
        return privExists;
    }

    public void setPrivExists(Boolean privExists) {
        this.privExists = privExists;
    }

    public Long getPrivId() {
        return privId;
    }

    public void setPrivId(Long privId) {
        this.privId = privId;
    }
}
