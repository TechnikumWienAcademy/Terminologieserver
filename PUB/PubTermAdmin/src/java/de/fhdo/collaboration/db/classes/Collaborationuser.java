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
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import org.hibernate.annotations.GenericGenerator;

/**
 * Collaborationuser generated by hbm2java
 */
@Entity
@Table(name = "collaborationuser")
public class Collaborationuser implements java.io.Serializable
{

  private Long id;
  private Organisation organisation;
  private String username;
  private String password;
  private String salt;
  private String name;
  private String firstName;
  private String city;
  private String country;
  private String email;
  private String note;
  private String phone;
  private String street;
  private String title;
  private String zip;
  private Boolean activated;
  private Date activationTime;
  private String activationMd5;
  private Boolean enabled;
  private Boolean hidden;
  private Boolean deleted;
  private Boolean sendMail;
  private Set<Rating> ratings = new HashSet<Rating>(0);
  private Set<Discussiongroup> discussiongroups = new HashSet<Discussiongroup>(0);
  private Set<Privilege> privileges = new HashSet<Privilege>(0);
  private Set<Link> links = new HashSet<Link>(0);
  private Set<Role> roles = new HashSet<Role>(0);
  private Set<Discussion> discussions = new HashSet<Discussion>(0);
  private Set<Proposalstatuschange> proposalstatuschanges = new HashSet<Proposalstatuschange>(0);
  private Set<Proposal> proposals = new HashSet<Proposal>(0);
  private Set<AssignedTerm> assignedTerms = new HashSet<AssignedTerm>(0);

  public Collaborationuser()
  {
  }

  public Collaborationuser(String username, String password)
  {
    this.username = username;
    this.password = password;
  }

  public Collaborationuser(Organisation organisation, String username, Boolean sendMail, String password, String salt, String name, String firstName, String city, String country, String email, String note, String phone, String street, String title, String zip, Set<Rating> ratings, Set<Discussiongroup> discussiongroups, Set<Privilege> privileges, Set<Link> links, Set<Role> roles, Set<Discussion> discussions, Set<Proposalstatuschange> proposalstatuschanges, Set<Proposal> proposals, Set<AssignedTerm> assignedTerms, Boolean hidden, Boolean enabled)
  {
    this.organisation = organisation;
    this.username = username;
    this.password = password;
    this.salt = salt;
    this.name = name;
    this.firstName = firstName;
    this.city = city;
    this.country = country;
    this.email = email;
    this.note = note;
    this.phone = phone;
    this.street = street;
    this.title = title;
    this.zip = zip;
    this.ratings = ratings;
    this.discussiongroups = discussiongroups;
    this.privileges = privileges;
    this.links = links;
    this.roles = roles;
    this.discussions = discussions;
    this.proposalstatuschanges = proposalstatuschanges;
    this.proposals = proposals;
    this.assignedTerms = assignedTerms;
    this.sendMail = sendMail;
    this.hidden = hidden;
    this.enabled  = enabled;
  }

  @Id
  @GeneratedValue(strategy = IDENTITY, generator="IdOrGenerated")
  @GenericGenerator(name="IdOrGenerated", strategy="de.fhdo.db.UseIdOrGenerate")
  @Column(name = "id", unique = true, nullable = false)
  public Long getId()
  {
    return this.id;
  }

  public void setId(Long id)
  {
    this.id = id;
  }

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "organisation_ID")
  public Organisation getOrganisation()
  {
    return this.organisation;
  }

  public void setOrganisation(Organisation organisation)
  {
    this.organisation = organisation;
  }

  @Column(name = "username", nullable = false, length = 64)
  public String getUsername()
  {
    return this.username;
  }

  public void setUsername(String username)
  {
    this.username = username;
  }

  @Column(name = "password", nullable = false, length = 65535)
  public String getPassword()
  {
    return this.password;
  }

  public void setPassword(String password)
  {
    this.password = password;
  }

  @Column(name = "salt", length = 128)
  public String getSalt()
  {
    return this.salt;
  }

  public void setSalt(String salt)
  {
    this.salt = salt;
  }

  @Column(name = "name", length = 65535)
  public String getName()
  {
    return this.name;
  }

  public void setName(String name)
  {
    this.name = name;
  }

  @Column(name = "firstName", length = 65535)
  public String getFirstName()
  {
    return this.firstName;
  }

  public void setFirstName(String firstName)
  {
    this.firstName = firstName;
  }

  @Column(name = "city", length = 90)
  public String getCity()
  {
    return this.city;
  }

  public void setCity(String city)
  {
    this.city = city;
  }

  @Column(name = "country", length = 30)
  public String getCountry()
  {
    return this.country;
  }

  public void setCountry(String country)
  {
    this.country = country;
  }

  @Column(name = "email", length = 65535)
  public String getEmail()
  {
    return this.email;
  }

  public void setEmail(String email)
  {
    this.email = email;
  }

  @Column(name = "note", length = 65535)
  public String getNote()
  {
    return this.note;
  }

  public void setNote(String note)
  {
    this.note = note;
  }

  @Column(name = "phone", length = 128)
  public String getPhone()
  {
    return this.phone;
  }

  public void setPhone(String phone)
  {
    this.phone = phone;
  }

  @Column(name = "street", length = 128)
  public String getStreet()
  {
    return this.street;
  }

  public void setStreet(String street)
  {
    this.street = street;
  }

  @Column(name = "title", length = 32)
  public String getTitle()
  {
    return this.title;
  }

  public void setTitle(String title)
  {
    this.title = title;
  }

  @Column(name = "zip", length = 16)
  public String getZip()
  {
    return this.zip;
  }

  public void setZip(String zip)
  {
    this.zip = zip;
  }

  @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "collaborationuser")
  public Set<Rating> getRatings()
  {
    return this.ratings;
  }

  public void setRatings(Set<Rating> ratings)
  {
    this.ratings = ratings;
  }

  @ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  @JoinTable(name = "discussiongroup2collaborationuser", joinColumns =
  {
    @JoinColumn(name = "collaborationuserId", nullable = false, updatable = false)
  }, inverseJoinColumns =
  {
    @JoinColumn(name = "discussionGroupId", nullable = false, updatable = false)
  })
  public Set<Discussiongroup> getDiscussiongroups()
  {
    return this.discussiongroups;
  }

  public void setDiscussiongroups(Set<Discussiongroup> discussiongroups)
  {
    this.discussiongroups = discussiongroups;
  }

  @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "collaborationuser")
  public Set<Privilege> getPrivileges()
  {
    return this.privileges;
  }

  public void setPrivileges(Set<Privilege> privileges)
  {
    this.privileges = privileges;
  }

  @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "collaborationuser")
  public Set<Link> getLinks()
  {
    return this.links;
  }

  public void setLinks(Set<Link> links)
  {
    this.links = links;
  }

  @ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  @JoinTable(name = "role2collaborationuser", joinColumns =
  {
    @JoinColumn(name = "collaborationUserId", nullable = false, updatable = false)
  }, inverseJoinColumns =
  {
    @JoinColumn(name = "roleId", nullable = false, updatable = false)
  })
  public Set<Role> getRoles()
  {
    return this.roles;
  }

  public void setRoles(Set<Role> roles)
  {
    this.roles = roles;
  }

  @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "collaborationuser")
  public Set<Discussion> getDiscussions()
  {
    return this.discussions;
  }

  public void setDiscussions(Set<Discussion> discussions)
  {
    this.discussions = discussions;
  }

  @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "collaborationuser")
  public Set<Proposalstatuschange> getProposalstatuschanges()
  {
    return this.proposalstatuschanges;
  }

  public void setProposalstatuschanges(Set<Proposalstatuschange> proposalstatuschanges)
  {
    this.proposalstatuschanges = proposalstatuschanges;
  }

  @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "collaborationuser")
  public Set<Proposal> getProposals()
  {
    return this.proposals;
  }

  public void setProposals(Set<Proposal> proposals)
  {
    this.proposals = proposals;
  }
  
  @Column(name="activated")
    public Boolean getActivated() {
        return activated;
    }

    public void setActivated(Boolean activated) {
        this.activated = activated;
    }
    
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="activation_time", nullable=true, length=19)
    public Date getActivationTime() {
        return this.activationTime;
    }
    
    public void setActivationTime(Date activationTime) {
        this.activationTime = activationTime;
    }
    
    @Column(name="activation_md5", length=80)
    public String getActivationMd5() {
        return activationMd5;
    }

    public void setActivationMd5(String activationMd5) {
        this.activationMd5 = activationMd5;
    }
  
  @Column(name="enabled")
    public Boolean getEnabled() {
        return this.enabled;
    }
    
    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }
    
    @Column(name="hidden")
    public Boolean getHidden() {
        return this.hidden;
    }
    
    public void setHidden(Boolean hidden) {
        this.hidden = hidden;
    }
    
    @Column(name="deleted")
    public Boolean getDeleted() {
        return this.deleted;
    }
    
    public void setDeleted(Boolean deleted) {
        this.deleted = deleted;
    }
    
  @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "collaborationuser")
  public Set<AssignedTerm> getAssignedTerms()
  {
    return this.assignedTerms;
  }

  public void setAssignedTerms(Set<AssignedTerm> assignedTerms)
  {
    this.assignedTerms = assignedTerms;
  }
  
  @Column(name="sendMail")
    public Boolean getSendMail() {
        return sendMail;
    }

    public void setSendMail(Boolean sendMail) {
        this.sendMail = sendMail;
    }
}
