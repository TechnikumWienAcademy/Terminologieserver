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
public class PrivilegeDiscGroupInfo{
    private Long discussionGroupId;
    private Long discussionGroupHeadId;
    private String discussionGroupName = "";
    private String discussionGroupHead = "";
    private Boolean privExists;
    private Long privId;
 
    public PrivilegeDiscGroupInfo(String discussionGroupName, String discussionGroupHead) {
        this.discussionGroupName = discussionGroupName;
        this.discussionGroupHead = discussionGroupHead;
    }

    public PrivilegeDiscGroupInfo(Long discussionGroupId, Long discussionGroupHeadId, String discussionGroupName, String discussionGroupHead, Boolean privExists, Long privId) {
        this.discussionGroupId = discussionGroupId;
        this.discussionGroupHeadId = discussionGroupHeadId;
        this.discussionGroupName = discussionGroupName;
        this.discussionGroupHead = discussionGroupHead;
        this.privExists = privExists;
        this.privId = privId;
    }

    public Long getDiscussionGroupId() {
        return discussionGroupId;
    }

    public void setDiscussionGroupId(Long discussionGroupId) {
        this.discussionGroupId = discussionGroupId;
    }

    public Long getDiscussionGroupHeadId() {
        return discussionGroupHeadId;
    }

    public void setDiscussionGroupHeadId(Long discussionGroupHeadId) {
        this.discussionGroupHeadId = discussionGroupHeadId;
    }

    public String getDiscussionGroupName() {
        return discussionGroupName;
    }

    public void setDiscussionGroupName(String discussionGroupName) {
        this.discussionGroupName = discussionGroupName;
    }

    public String getDiscussionGroupHead() {
        return discussionGroupHead;
    }

    public void setDiscussionGroupHead(String discussionGroupHead) {
        this.discussionGroupHead = discussionGroupHead;
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