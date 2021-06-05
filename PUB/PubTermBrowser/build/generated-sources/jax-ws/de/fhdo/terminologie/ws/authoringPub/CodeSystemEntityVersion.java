
package de.fhdo.terminologie.ws.authoringPub;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.XMLGregorianCalendar;


/**
 * <p>Java-Klasse für codeSystemEntityVersion complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="codeSystemEntityVersion">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="associationTypes" type="{de.fhdo.termserver.types}associationType" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="codeSystemConcepts" type="{de.fhdo.termserver.types}codeSystemConcept" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="codeSystemEntity" type="{de.fhdo.termserver.types}codeSystemEntity" minOccurs="0"/>
 *         &lt;element name="codeSystemEntityVersionAssociationsForCodeSystemEntityVersionId1" type="{de.fhdo.termserver.types}codeSystemEntityVersionAssociation" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="codeSystemEntityVersionAssociationsForCodeSystemEntityVersionId2" type="{de.fhdo.termserver.types}codeSystemEntityVersionAssociation" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="codeSystemMetadataValues" type="{de.fhdo.termserver.types}codeSystemMetadataValue" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="conceptValueSetMemberships" type="{de.fhdo.termserver.types}conceptValueSetMembership" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="effectiveDate" type="{http://www.w3.org/2001/XMLSchema}dateTime" minOccurs="0"/>
 *         &lt;element name="insertTimestamp" type="{http://www.w3.org/2001/XMLSchema}dateTime" minOccurs="0"/>
 *         &lt;element name="isLeaf" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="majorRevision" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="minorRevision" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="previousVersionId" type="{http://www.w3.org/2001/XMLSchema}long" minOccurs="0"/>
 *         &lt;element name="propertyVersions" type="{de.fhdo.termserver.types}propertyVersion" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="status" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="statusDate" type="{http://www.w3.org/2001/XMLSchema}dateTime" minOccurs="0"/>
 *         &lt;element name="valueSetMetadataValues" type="{de.fhdo.termserver.types}valueSetMetadataValue" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="versionId" type="{http://www.w3.org/2001/XMLSchema}long" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "codeSystemEntityVersion", namespace = "de.fhdo.termserver.types", propOrder = {
    "associationTypes",
    "codeSystemConcepts",
    "codeSystemEntity",
    "codeSystemEntityVersionAssociationsForCodeSystemEntityVersionId1",
    "codeSystemEntityVersionAssociationsForCodeSystemEntityVersionId2",
    "codeSystemMetadataValues",
    "conceptValueSetMemberships",
    "effectiveDate",
    "insertTimestamp",
    "isLeaf",
    "majorRevision",
    "minorRevision",
    "previousVersionId",
    "propertyVersions",
    "status",
    "statusDate",
    "valueSetMetadataValues",
    "versionId"
})
public class CodeSystemEntityVersion {

    @XmlElement(nillable = true)
    protected List<AssociationType> associationTypes;
    @XmlElement(nillable = true)
    protected List<CodeSystemConcept> codeSystemConcepts;
    protected CodeSystemEntity codeSystemEntity;
    @XmlElement(nillable = true)
    protected List<CodeSystemEntityVersionAssociation> codeSystemEntityVersionAssociationsForCodeSystemEntityVersionId1;
    @XmlElement(nillable = true)
    protected List<CodeSystemEntityVersionAssociation> codeSystemEntityVersionAssociationsForCodeSystemEntityVersionId2;
    @XmlElement(nillable = true)
    protected List<CodeSystemMetadataValue> codeSystemMetadataValues;
    @XmlElement(nillable = true)
    protected List<ConceptValueSetMembership> conceptValueSetMemberships;
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar effectiveDate;
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar insertTimestamp;
    protected Boolean isLeaf;
    protected Integer majorRevision;
    protected Integer minorRevision;
    protected Long previousVersionId;
    @XmlElement(nillable = true)
    protected List<PropertyVersion> propertyVersions;
    protected Integer status;
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar statusDate;
    @XmlElement(nillable = true)
    protected List<ValueSetMetadataValue> valueSetMetadataValues;
    protected Long versionId;

    /**
     * Gets the value of the associationTypes property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the associationTypes property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getAssociationTypes().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link AssociationType }
     * 
     * 
     */
    public List<AssociationType> getAssociationTypes() {
        if (associationTypes == null) {
            associationTypes = new ArrayList<AssociationType>();
        }
        return this.associationTypes;
    }

    /**
     * Gets the value of the codeSystemConcepts property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the codeSystemConcepts property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getCodeSystemConcepts().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link CodeSystemConcept }
     * 
     * 
     */
    public List<CodeSystemConcept> getCodeSystemConcepts() {
        if (codeSystemConcepts == null) {
            codeSystemConcepts = new ArrayList<CodeSystemConcept>();
        }
        return this.codeSystemConcepts;
    }

    /**
     * Ruft den Wert der codeSystemEntity-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link CodeSystemEntity }
     *     
     */
    public CodeSystemEntity getCodeSystemEntity() {
        return codeSystemEntity;
    }

    /**
     * Legt den Wert der codeSystemEntity-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link CodeSystemEntity }
     *     
     */
    public void setCodeSystemEntity(CodeSystemEntity value) {
        this.codeSystemEntity = value;
    }

    /**
     * Gets the value of the codeSystemEntityVersionAssociationsForCodeSystemEntityVersionId1 property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the codeSystemEntityVersionAssociationsForCodeSystemEntityVersionId1 property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getCodeSystemEntityVersionAssociationsForCodeSystemEntityVersionId1().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link CodeSystemEntityVersionAssociation }
     * 
     * 
     */
    public List<CodeSystemEntityVersionAssociation> getCodeSystemEntityVersionAssociationsForCodeSystemEntityVersionId1() {
        if (codeSystemEntityVersionAssociationsForCodeSystemEntityVersionId1 == null) {
            codeSystemEntityVersionAssociationsForCodeSystemEntityVersionId1 = new ArrayList<CodeSystemEntityVersionAssociation>();
        }
        return this.codeSystemEntityVersionAssociationsForCodeSystemEntityVersionId1;
    }

    /**
     * Gets the value of the codeSystemEntityVersionAssociationsForCodeSystemEntityVersionId2 property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the codeSystemEntityVersionAssociationsForCodeSystemEntityVersionId2 property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getCodeSystemEntityVersionAssociationsForCodeSystemEntityVersionId2().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link CodeSystemEntityVersionAssociation }
     * 
     * 
     */
    public List<CodeSystemEntityVersionAssociation> getCodeSystemEntityVersionAssociationsForCodeSystemEntityVersionId2() {
        if (codeSystemEntityVersionAssociationsForCodeSystemEntityVersionId2 == null) {
            codeSystemEntityVersionAssociationsForCodeSystemEntityVersionId2 = new ArrayList<CodeSystemEntityVersionAssociation>();
        }
        return this.codeSystemEntityVersionAssociationsForCodeSystemEntityVersionId2;
    }

    /**
     * Gets the value of the codeSystemMetadataValues property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the codeSystemMetadataValues property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getCodeSystemMetadataValues().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link CodeSystemMetadataValue }
     * 
     * 
     */
    public List<CodeSystemMetadataValue> getCodeSystemMetadataValues() {
        if (codeSystemMetadataValues == null) {
            codeSystemMetadataValues = new ArrayList<CodeSystemMetadataValue>();
        }
        return this.codeSystemMetadataValues;
    }

    /**
     * Gets the value of the conceptValueSetMemberships property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the conceptValueSetMemberships property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getConceptValueSetMemberships().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link ConceptValueSetMembership }
     * 
     * 
     */
    public List<ConceptValueSetMembership> getConceptValueSetMemberships() {
        if (conceptValueSetMemberships == null) {
            conceptValueSetMemberships = new ArrayList<ConceptValueSetMembership>();
        }
        return this.conceptValueSetMemberships;
    }

    /**
     * Ruft den Wert der effectiveDate-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getEffectiveDate() {
        return effectiveDate;
    }

    /**
     * Legt den Wert der effectiveDate-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setEffectiveDate(XMLGregorianCalendar value) {
        this.effectiveDate = value;
    }

    /**
     * Ruft den Wert der insertTimestamp-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getInsertTimestamp() {
        return insertTimestamp;
    }

    /**
     * Legt den Wert der insertTimestamp-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setInsertTimestamp(XMLGregorianCalendar value) {
        this.insertTimestamp = value;
    }

    /**
     * Ruft den Wert der isLeaf-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isIsLeaf() {
        return isLeaf;
    }

    /**
     * Legt den Wert der isLeaf-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setIsLeaf(Boolean value) {
        this.isLeaf = value;
    }

    /**
     * Ruft den Wert der majorRevision-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getMajorRevision() {
        return majorRevision;
    }

    /**
     * Legt den Wert der majorRevision-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setMajorRevision(Integer value) {
        this.majorRevision = value;
    }

    /**
     * Ruft den Wert der minorRevision-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getMinorRevision() {
        return minorRevision;
    }

    /**
     * Legt den Wert der minorRevision-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setMinorRevision(Integer value) {
        this.minorRevision = value;
    }

    /**
     * Ruft den Wert der previousVersionId-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public Long getPreviousVersionId() {
        return previousVersionId;
    }

    /**
     * Legt den Wert der previousVersionId-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setPreviousVersionId(Long value) {
        this.previousVersionId = value;
    }

    /**
     * Gets the value of the propertyVersions property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the propertyVersions property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getPropertyVersions().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link PropertyVersion }
     * 
     * 
     */
    public List<PropertyVersion> getPropertyVersions() {
        if (propertyVersions == null) {
            propertyVersions = new ArrayList<PropertyVersion>();
        }
        return this.propertyVersions;
    }

    /**
     * Ruft den Wert der status-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getStatus() {
        return status;
    }

    /**
     * Legt den Wert der status-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setStatus(Integer value) {
        this.status = value;
    }

    /**
     * Ruft den Wert der statusDate-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getStatusDate() {
        return statusDate;
    }

    /**
     * Legt den Wert der statusDate-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setStatusDate(XMLGregorianCalendar value) {
        this.statusDate = value;
    }

    /**
     * Gets the value of the valueSetMetadataValues property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the valueSetMetadataValues property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getValueSetMetadataValues().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link ValueSetMetadataValue }
     * 
     * 
     */
    public List<ValueSetMetadataValue> getValueSetMetadataValues() {
        if (valueSetMetadataValues == null) {
            valueSetMetadataValues = new ArrayList<ValueSetMetadataValue>();
        }
        return this.valueSetMetadataValues;
    }

    /**
     * Ruft den Wert der versionId-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public Long getVersionId() {
        return versionId;
    }

    /**
     * Legt den Wert der versionId-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setVersionId(Long value) {
        this.versionId = value;
    }

}
