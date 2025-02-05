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


import java.util.Date;
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
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 * Proposal generated by hbm2java
 */
@Entity
@Table(name="proposal"
    
)
public class Proposal  implements java.io.Serializable {


     private Long id;
     private Collaborationuser collaborationuser;
     private String description;
     private Integer status;
     private Date created;
     private Date validFrom;
     private Date validTo;
     private Date statusDate;
     private Date lastChangeDate;
     private String note;
     private String contentType;
     private Long vocabularyId;
     private Long vocabularyIdTwo;
     private String vocabularyName;
     private String vocabularyNameTwo;
     private Set<Privilege> privileges = new HashSet<Privilege>(0);
     private Set<Rating> ratings = new HashSet<Rating>(0);
     private Set<Proposalobject> proposalobjects = new HashSet<Proposalobject>(0);
     private Set<Discussion> discussions = new HashSet<Discussion>(0);
     private Set<Link> links = new HashSet<Link>(0);
     private Set<Proposalstatuschange> proposalstatuschanges = new HashSet<Proposalstatuschange>(0);

    public Proposal() {
    }

	
    public Proposal(Collaborationuser collaborationuser, Date created) {
        this.collaborationuser = collaborationuser;
        this.created = created;
    }
    public Proposal(Collaborationuser collaborationuser, String description, Integer status, Date created, Date validFrom, Date validTo, Date statusDate, Date lastChangeDate, String note, String contentType, Long vocabularyId, Long vocabularyIdTwo, String vocabularyName, String vocabularyNameTwo, Set<Privilege> privileges, Set<Rating> ratings, Set<Proposalobject> proposalobjects, Set<Discussion> discussions, Set<Link> links, Set<Proposalstatuschange> proposalstatuschanges) {
       this.collaborationuser = collaborationuser;
       this.description = description;
       this.status = status;
       this.created = created;
       this.validFrom = validFrom;
       this.validTo = validTo;
       this.statusDate = statusDate;
       this.lastChangeDate = lastChangeDate;
       this.note = note;
       this.contentType = contentType;
       this.vocabularyId = vocabularyId;
       this.vocabularyIdTwo = vocabularyIdTwo;
       this.vocabularyName = vocabularyName;
       this.vocabularyNameTwo = vocabularyNameTwo;
       this.privileges = privileges;
       this.ratings = ratings;
       this.proposalobjects = proposalobjects;
       this.discussions = discussions;
       this.links = links;
       this.proposalstatuschanges = proposalstatuschanges;
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
    @JoinColumn(name="collaborationUserId", nullable=false)
    public Collaborationuser getCollaborationuser() {
        return this.collaborationuser;
    }
    
    public void setCollaborationuser(Collaborationuser collaborationuser) {
        this.collaborationuser = collaborationuser;
    }
    
    @Column(name="description", length=65535)
    public String getDescription() {
        return this.description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    @Column(name="status")
    public Integer getStatus() {
        return this.status;
    }
    
    public void setStatus(Integer status) {
        this.status = status;
    }
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="created", nullable=false, length=19)
    public Date getCreated() {
        return this.created;
    }
    
    public void setCreated(Date created) {
        this.created = created;
    }
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="validFrom", length=19)
    public Date getValidFrom() {
        return this.validFrom;
    }
    
    public void setValidFrom(Date validFrom) {
        this.validFrom = validFrom;
    }
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="validTo", length=19)
    public Date getValidTo() {
        return this.validTo;
    }
    
    public void setValidTo(Date validTo) {
        this.validTo = validTo;
    }
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="statusDate", length=19)
    public Date getStatusDate() {
        return this.statusDate;
    }
    
    public void setStatusDate(Date statusDate) {
        this.statusDate = statusDate;
    }
    
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="lastChangeDate", length=19)
    public Date getLastChangeDate() {
        return this.lastChangeDate;
    }
    
    public void setLastChangeDate(Date lastChangeDate) {
        this.lastChangeDate = lastChangeDate;
    }
    
    @Column(name="note", length=65535)
    public String getNote() {
        return this.note;
    }
    
    public void setNote(String note) {
        this.note = note;
    }
    
    @Column(name="contentType", length=30)
    public String getContentType() {
        return this.contentType;
    }
    
    public void setContentType(String contentType) {
        this.contentType = contentType;
    }
    
    @Column(name="vocabularyId")
    public Long getVocabularyId() {
        return this.vocabularyId;
    }
    
    public void setVocabularyId(Long vocabularyId) {
        this.vocabularyId = vocabularyId;
    }
    
    @Column(name="vocabularyIdTwo")
    public Long getVocabularyIdTwo() {
        return this.vocabularyIdTwo;
    }
    
    public void setVocabularyIdTwo(Long vocabularyIdTwo) {
        this.vocabularyIdTwo = vocabularyIdTwo;
    }
    
    @Column(name="vocabularyName", length=64)
    public String getVocabularyName() {
        return this.vocabularyName;
    }
    
    public void setVocabularyName(String vocabularyName) {
        this.vocabularyName = vocabularyName;
    }
    
    @Column(name="vocabularyNameTwo", length=64)
    public String getVocabularyNameTwo() {
        return this.vocabularyNameTwo;
    }
    
    public void setVocabularyNameTwo(String vocabularyNameTwo) {
        this.vocabularyNameTwo = vocabularyNameTwo;
    }
    
@OneToMany(cascade=CascadeType.ALL, fetch=FetchType.LAZY, mappedBy="proposal")
    public Set<Privilege> getPrivileges() {
        return this.privileges;
    }
    
    public void setPrivileges(Set<Privilege> privileges) {
        this.privileges = privileges;
    }
@OneToMany(cascade=CascadeType.ALL, fetch=FetchType.LAZY, mappedBy="proposal")
    public Set<Rating> getRatings() {
        return this.ratings;
    }
    
    public void setRatings(Set<Rating> ratings) {
        this.ratings = ratings;
    }
@OneToMany(cascade=CascadeType.ALL, fetch=FetchType.LAZY, mappedBy="proposal")
    public Set<Proposalobject> getProposalobjects() {
        return this.proposalobjects;
    }
    
    public void setProposalobjects(Set<Proposalobject> proposalobjects) {
        this.proposalobjects = proposalobjects;
    }
@OneToMany(cascade=CascadeType.ALL, fetch=FetchType.LAZY, mappedBy="proposal")
    public Set<Discussion> getDiscussions() {
        return this.discussions;
    }
    
    public void setDiscussions(Set<Discussion> discussions) {
        this.discussions = discussions;
    }
@OneToMany(cascade=CascadeType.ALL, fetch=FetchType.LAZY, mappedBy="proposal")
    public Set<Link> getLinks() {
        return this.links;
    }
    
    public void setLinks(Set<Link> links) {
        this.links = links;
    }
@OneToMany(cascade=CascadeType.ALL, fetch=FetchType.LAZY, mappedBy="proposal")
    public Set<Proposalstatuschange> getProposalstatuschanges() {
        return this.proposalstatuschanges;
    }
    
    public void setProposalstatuschanges(Set<Proposalstatuschange> proposalstatuschanges) {
        this.proposalstatuschanges = proposalstatuschanges;
    }




}


