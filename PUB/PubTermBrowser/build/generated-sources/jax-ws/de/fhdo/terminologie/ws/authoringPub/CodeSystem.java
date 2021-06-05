
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
 * <p>Java-Klasse f�r codeSystem complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="codeSystem">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="autoRelease" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *         &lt;element name="codeSystemType" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="codeSystemVersions" type="{de.fhdo.termserver.types}codeSystemVersion" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="currentVersionId" type="{http://www.w3.org/2001/XMLSchema}long" minOccurs="0"/>
 *         &lt;element name="description" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="descriptionEng" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="domainValues" type="{de.fhdo.termserver.types}domainValue" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="id" type="{http://www.w3.org/2001/XMLSchema}long" minOccurs="0"/>
 *         &lt;element name="incompleteCS" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="insertTimestamp" type="{http://www.w3.org/2001/XMLSchema}dateTime" minOccurs="0"/>
 *         &lt;element name="metadataParameters" type="{de.fhdo.termserver.types}metadataParameter" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="name" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="responsibleOrganization" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="website" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "codeSystem", namespace = "de.fhdo.termserver.types", propOrder = {
    "autoRelease",
    "codeSystemType",
    "codeSystemVersions",
    "currentVersionId",
    "description",
    "descriptionEng",
    "domainValues",
    "id",
    "incompleteCS",
    "insertTimestamp",
    "metadataParameters",
    "name",
    "responsibleOrganization",
    "website"
})
public class CodeSystem {

    protected boolean autoRelease;
    protected String codeSystemType;
    @XmlElement(nillable = true)
    protected List<CodeSystemVersion> codeSystemVersions;
    protected Long currentVersionId;
    protected String description;
    protected String descriptionEng;
    @XmlElement(nillable = true)
    protected List<DomainValue> domainValues;
    protected Long id;
    protected Boolean incompleteCS;
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar insertTimestamp;
    @XmlElement(nillable = true)
    protected List<MetadataParameter> metadataParameters;
    protected String name;
    protected String responsibleOrganization;
    protected String website;

    /**
     * Ruft den Wert der autoRelease-Eigenschaft ab.
     * 
     */
    public boolean isAutoRelease() {
        return autoRelease;
    }

    /**
     * Legt den Wert der autoRelease-Eigenschaft fest.
     * 
     */
    public void setAutoRelease(boolean value) {
        this.autoRelease = value;
    }

    /**
     * Ruft den Wert der codeSystemType-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCodeSystemType() {
        return codeSystemType;
    }

    /**
     * Legt den Wert der codeSystemType-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCodeSystemType(String value) {
        this.codeSystemType = value;
    }

    /**
     * Gets the value of the codeSystemVersions property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the codeSystemVersions property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getCodeSystemVersions().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link CodeSystemVersion }
     * 
     * 
     */
    public List<CodeSystemVersion> getCodeSystemVersions() {
        if (codeSystemVersions == null) {
            codeSystemVersions = new ArrayList<CodeSystemVersion>();
        }
        return this.codeSystemVersions;
    }

    /**
     * Ruft den Wert der currentVersionId-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public Long getCurrentVersionId() {
        return currentVersionId;
    }

    /**
     * Legt den Wert der currentVersionId-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setCurrentVersionId(Long value) {
        this.currentVersionId = value;
    }

    /**
     * Ruft den Wert der description-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDescription() {
        return description;
    }

    /**
     * Legt den Wert der description-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDescription(String value) {
        this.description = value;
    }

    /**
     * Ruft den Wert der descriptionEng-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDescriptionEng() {
        return descriptionEng;
    }

    /**
     * Legt den Wert der descriptionEng-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDescriptionEng(String value) {
        this.descriptionEng = value;
    }

    /**
     * Gets the value of the domainValues property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the domainValues property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getDomainValues().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link DomainValue }
     * 
     * 
     */
    public List<DomainValue> getDomainValues() {
        if (domainValues == null) {
            domainValues = new ArrayList<DomainValue>();
        }
        return this.domainValues;
    }

    /**
     * Ruft den Wert der id-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public Long getId() {
        return id;
    }

    /**
     * Legt den Wert der id-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setId(Long value) {
        this.id = value;
    }

    /**
     * Ruft den Wert der incompleteCS-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isIncompleteCS() {
        return incompleteCS;
    }

    /**
     * Legt den Wert der incompleteCS-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setIncompleteCS(Boolean value) {
        this.incompleteCS = value;
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
     * Gets the value of the metadataParameters property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the metadataParameters property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getMetadataParameters().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link MetadataParameter }
     * 
     * 
     */
    public List<MetadataParameter> getMetadataParameters() {
        if (metadataParameters == null) {
            metadataParameters = new ArrayList<MetadataParameter>();
        }
        return this.metadataParameters;
    }

    /**
     * Ruft den Wert der name-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getName() {
        return name;
    }

    /**
     * Legt den Wert der name-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setName(String value) {
        this.name = value;
    }

    /**
     * Ruft den Wert der responsibleOrganization-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getResponsibleOrganization() {
        return responsibleOrganization;
    }

    /**
     * Legt den Wert der responsibleOrganization-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setResponsibleOrganization(String value) {
        this.responsibleOrganization = value;
    }

    /**
     * Ruft den Wert der website-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getWebsite() {
        return website;
    }

    /**
     * Legt den Wert der website-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setWebsite(String value) {
        this.website = value;
    }

}
