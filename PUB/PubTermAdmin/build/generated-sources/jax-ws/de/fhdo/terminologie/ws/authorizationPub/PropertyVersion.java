
package de.fhdo.terminologie.ws.authorizationPub;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.XMLGregorianCalendar;


/**
 * <p>Java-Klasse für propertyVersion complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="propertyVersion">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="codeSystemEntityVersion" type="{de.fhdo.termserver.types}codeSystemEntityVersion" minOccurs="0"/>
 *         &lt;element name="content" type="{http://www.w3.org/2001/XMLSchema}base64Binary" minOccurs="0"/>
 *         &lt;element name="contentMimetype" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="contentName" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="contentSize" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="description" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="insertTimestamp" type="{http://www.w3.org/2001/XMLSchema}dateTime" minOccurs="0"/>
 *         &lt;element name="languageId" type="{http://www.w3.org/2001/XMLSchema}long" minOccurs="0"/>
 *         &lt;element name="name" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="previousVersionId" type="{http://www.w3.org/2001/XMLSchema}long" minOccurs="0"/>
 *         &lt;element name="property" type="{de.fhdo.termserver.types}property" minOccurs="0"/>
 *         &lt;element name="propertyKindId" type="{http://www.w3.org/2001/XMLSchema}long" minOccurs="0"/>
 *         &lt;element name="status" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="statusDate" type="{http://www.w3.org/2001/XMLSchema}dateTime" minOccurs="0"/>
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
@XmlType(name = "propertyVersion", propOrder = {
    "codeSystemEntityVersion",
    "content",
    "contentMimetype",
    "contentName",
    "contentSize",
    "description",
    "insertTimestamp",
    "languageId",
    "name",
    "previousVersionId",
    "property",
    "propertyKindId",
    "status",
    "statusDate",
    "versionId"
})
public class PropertyVersion {

    protected CodeSystemEntityVersion codeSystemEntityVersion;
    protected byte[] content;
    protected String contentMimetype;
    protected String contentName;
    protected Integer contentSize;
    protected String description;
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar insertTimestamp;
    protected Long languageId;
    protected String name;
    protected Long previousVersionId;
    protected Property property;
    protected Long propertyKindId;
    protected Integer status;
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar statusDate;
    protected Long versionId;

    /**
     * Ruft den Wert der codeSystemEntityVersion-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link CodeSystemEntityVersion }
     *     
     */
    public CodeSystemEntityVersion getCodeSystemEntityVersion() {
        return codeSystemEntityVersion;
    }

    /**
     * Legt den Wert der codeSystemEntityVersion-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link CodeSystemEntityVersion }
     *     
     */
    public void setCodeSystemEntityVersion(CodeSystemEntityVersion value) {
        this.codeSystemEntityVersion = value;
    }

    /**
     * Ruft den Wert der content-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     byte[]
     */
    public byte[] getContent() {
        return content;
    }

    /**
     * Legt den Wert der content-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     byte[]
     */
    public void setContent(byte[] value) {
        this.content = value;
    }

    /**
     * Ruft den Wert der contentMimetype-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getContentMimetype() {
        return contentMimetype;
    }

    /**
     * Legt den Wert der contentMimetype-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setContentMimetype(String value) {
        this.contentMimetype = value;
    }

    /**
     * Ruft den Wert der contentName-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getContentName() {
        return contentName;
    }

    /**
     * Legt den Wert der contentName-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setContentName(String value) {
        this.contentName = value;
    }

    /**
     * Ruft den Wert der contentSize-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getContentSize() {
        return contentSize;
    }

    /**
     * Legt den Wert der contentSize-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setContentSize(Integer value) {
        this.contentSize = value;
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
     * Ruft den Wert der languageId-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public Long getLanguageId() {
        return languageId;
    }

    /**
     * Legt den Wert der languageId-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setLanguageId(Long value) {
        this.languageId = value;
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
     * Ruft den Wert der property-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Property }
     *     
     */
    public Property getProperty() {
        return property;
    }

    /**
     * Legt den Wert der property-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Property }
     *     
     */
    public void setProperty(Property value) {
        this.property = value;
    }

    /**
     * Ruft den Wert der propertyKindId-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public Long getPropertyKindId() {
        return propertyKindId;
    }

    /**
     * Legt den Wert der propertyKindId-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setPropertyKindId(Long value) {
        this.propertyKindId = value;
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
