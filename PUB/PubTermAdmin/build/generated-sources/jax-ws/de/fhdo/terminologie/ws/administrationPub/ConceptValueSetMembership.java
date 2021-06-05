
package de.fhdo.terminologie.ws.administrationPub;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.XMLGregorianCalendar;


/**
 * <p>Java-Klasse für conceptValueSetMembership complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="conceptValueSetMembership">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="awbeschreibung" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="bedeutung" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="codeSystemEntityVersion" type="{de.fhdo.termserver.types}codeSystemEntityVersion" minOccurs="0"/>
 *         &lt;element name="hinweise" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="id" type="{de.fhdo.termserver.types}conceptValueSetMembershipId" minOccurs="0"/>
 *         &lt;element name="isStructureEntry" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="orderNr" type="{http://www.w3.org/2001/XMLSchema}long" minOccurs="0"/>
 *         &lt;element name="status" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="statusDate" type="{http://www.w3.org/2001/XMLSchema}dateTime" minOccurs="0"/>
 *         &lt;element name="valueOverride" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="valueSetVersion" type="{de.fhdo.termserver.types}valueSetVersion" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "conceptValueSetMembership", namespace = "de.fhdo.termserver.types", propOrder = {
    "awbeschreibung",
    "bedeutung",
    "codeSystemEntityVersion",
    "hinweise",
    "id",
    "isStructureEntry",
    "orderNr",
    "status",
    "statusDate",
    "valueOverride",
    "valueSetVersion"
})
public class ConceptValueSetMembership {

    protected String awbeschreibung;
    protected String bedeutung;
    protected CodeSystemEntityVersion codeSystemEntityVersion;
    protected String hinweise;
    protected ConceptValueSetMembershipId id;
    protected Boolean isStructureEntry;
    protected Long orderNr;
    protected Integer status;
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar statusDate;
    protected String valueOverride;
    protected ValueSetVersion valueSetVersion;

    /**
     * Ruft den Wert der awbeschreibung-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAwbeschreibung() {
        return awbeschreibung;
    }

    /**
     * Legt den Wert der awbeschreibung-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAwbeschreibung(String value) {
        this.awbeschreibung = value;
    }

    /**
     * Ruft den Wert der bedeutung-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getBedeutung() {
        return bedeutung;
    }

    /**
     * Legt den Wert der bedeutung-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setBedeutung(String value) {
        this.bedeutung = value;
    }

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
     * Ruft den Wert der hinweise-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getHinweise() {
        return hinweise;
    }

    /**
     * Legt den Wert der hinweise-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setHinweise(String value) {
        this.hinweise = value;
    }

    /**
     * Ruft den Wert der id-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link ConceptValueSetMembershipId }
     *     
     */
    public ConceptValueSetMembershipId getId() {
        return id;
    }

    /**
     * Legt den Wert der id-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link ConceptValueSetMembershipId }
     *     
     */
    public void setId(ConceptValueSetMembershipId value) {
        this.id = value;
    }

    /**
     * Ruft den Wert der isStructureEntry-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isIsStructureEntry() {
        return isStructureEntry;
    }

    /**
     * Legt den Wert der isStructureEntry-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setIsStructureEntry(Boolean value) {
        this.isStructureEntry = value;
    }

    /**
     * Ruft den Wert der orderNr-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public Long getOrderNr() {
        return orderNr;
    }

    /**
     * Legt den Wert der orderNr-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setOrderNr(Long value) {
        this.orderNr = value;
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
     * Ruft den Wert der valueOverride-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getValueOverride() {
        return valueOverride;
    }

    /**
     * Legt den Wert der valueOverride-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setValueOverride(String value) {
        this.valueOverride = value;
    }

    /**
     * Ruft den Wert der valueSetVersion-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link ValueSetVersion }
     *     
     */
    public ValueSetVersion getValueSetVersion() {
        return valueSetVersion;
    }

    /**
     * Legt den Wert der valueSetVersion-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link ValueSetVersion }
     *     
     */
    public void setValueSetVersion(ValueSetVersion value) {
        this.valueSetVersion = value;
    }

}
