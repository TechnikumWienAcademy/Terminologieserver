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
package de.fhdo.db.hibernate;
// Generated 24.10.2011 10:08:21 by Hibernate Tools 3.2.1.GA

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

/**
 MetadataParameter generated by hbm2java
 */
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement
@XmlType(namespace = "de.fhdo.termserver.idp.types")
@Entity
@Table(name = "metadata_parameter")
public class MetadataParameter implements java.io.Serializable
{
  private Long id;
  private CodeSystem codeSystem;
  private ValueSet valueSet;
  private String paramName;
  private String paramDatatype;
  private String metadataParameterType;
  private Set<CodeSystemMetadataValue> codeSystemMetadataValues = new HashSet<CodeSystemMetadataValue>(0);
  private Set<ValueSetMetadataValue> valueSetMetadataValues = new HashSet<ValueSetMetadataValue>(0);

  public MetadataParameter()
  {
  }

  public MetadataParameter(String paramName)
  {
    this.paramName = paramName;
  }

  public MetadataParameter(String paramName, String paramDatatype, String metadataParameterType, Set<CodeSystemMetadataValue> codeSystemMetadataValues, Set<ValueSetMetadataValue> valueSetMetadataValues)
  {
    this.paramName = paramName;
    this.paramDatatype = paramDatatype;
    this.metadataParameterType = metadataParameterType;
    this.codeSystemMetadataValues = codeSystemMetadataValues;
    this.valueSetMetadataValues = valueSetMetadataValues;
  }
  
  @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="codeSystemId")
    public CodeSystem getCodeSystem() {
        return this.codeSystem;
    }
    
    public void setCodeSystem(CodeSystem codeSystem) {
        this.codeSystem = codeSystem;
    }
  
    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="valueSetId")
    public ValueSet getValueSet() {
        return this.valueSet;
    }
    
    public void setValueSet(ValueSet valueSet) {
        this.valueSet = valueSet;
    }
    

  @Id
  @GeneratedValue(strategy = IDENTITY)
  @Column(name = "id", unique = true, nullable = false)
  public Long getId()
  {
    return this.id;
  }

  public void setId(Long id)
  {
    this.id = id;
  }

  @Column(name = "paramName", nullable = false, length = 65535)
  public String getParamName()
  {
    return this.paramName;
  }

  public void setParamName(String paramName)
  {
    this.paramName = paramName;
  }

  @Column(name = "paramDatatype", length = 65535)
  public String getParamDatatype()
  {
    return this.paramDatatype;
  }

  public void setParamDatatype(String paramDatatype)
  {
    this.paramDatatype = paramDatatype;
  }

  @Column(name = "metadataParameterType", length = 30)
  public String getMetadataParameterType()
  {
    return this.metadataParameterType;
  }

  public void setMetadataParameterType(String metadataParameterType)
  {
    this.metadataParameterType = metadataParameterType;
  }

  @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "metadataParameter")
  public Set<CodeSystemMetadataValue> getCodeSystemMetadataValues()
  {
    return this.codeSystemMetadataValues;
  }

  public void setCodeSystemMetadataValues(Set<CodeSystemMetadataValue> codeSystemMetadataValues)
  {
    this.codeSystemMetadataValues = codeSystemMetadataValues;
  }
  
  @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "metadataParameter")
  public Set<ValueSetMetadataValue> getValueSetMetadataValues()
  {
    return this.valueSetMetadataValues;
  }

  public void setValueSetMetadataValues(Set<ValueSetMetadataValue> valueSetMetadataValues)
  {
    this.valueSetMetadataValues = valueSetMetadataValues;
  }
}
