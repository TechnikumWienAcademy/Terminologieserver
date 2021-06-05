
package de.fhdo.terminologie.ws.search;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;
import types.termserver.fhdo.de.ConceptValueSetMembershipId;


/**
 * <p>Java-Klasse für globalSearchResultEntry complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="globalSearchResultEntry">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="code" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="codeSystemEntry" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="codeSystemName" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="codeSystemVersionName" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="csId" type="{http://www.w3.org/2001/XMLSchema}long" minOccurs="0"/>
 *         &lt;element name="csevId" type="{http://www.w3.org/2001/XMLSchema}long" minOccurs="0"/>
 *         &lt;element name="csvId" type="{http://www.w3.org/2001/XMLSchema}long" minOccurs="0"/>
 *         &lt;element name="cvsmId" type="{de.fhdo.termserver.types}conceptValueSetMembershipId" minOccurs="0"/>
 *         &lt;element name="sourceCodeSystemInfo" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="term" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="valueSetName" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="valueSetVersionName" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="vsId" type="{http://www.w3.org/2001/XMLSchema}long" minOccurs="0"/>
 *         &lt;element name="vsvId" type="{http://www.w3.org/2001/XMLSchema}long" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "globalSearchResultEntry", propOrder = {
    "code",
    "codeSystemEntry",
    "codeSystemName",
    "codeSystemVersionName",
    "csId",
    "csevId",
    "csvId",
    "cvsmId",
    "sourceCodeSystemInfo",
    "term",
    "valueSetName",
    "valueSetVersionName",
    "vsId",
    "vsvId"
})
public class GlobalSearchResultEntry {

    protected String code;
    protected Boolean codeSystemEntry;
    protected String codeSystemName;
    protected String codeSystemVersionName;
    protected Long csId;
    protected Long csevId;
    protected Long csvId;
    protected ConceptValueSetMembershipId cvsmId;
    protected String sourceCodeSystemInfo;
    protected String term;
    protected String valueSetName;
    protected String valueSetVersionName;
    protected Long vsId;
    protected Long vsvId;

    /**
     * Ruft den Wert der code-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCode() {
        return code;
    }

    /**
     * Legt den Wert der code-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCode(String value) {
        this.code = value;
    }

    /**
     * Ruft den Wert der codeSystemEntry-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isCodeSystemEntry() {
        return codeSystemEntry;
    }

    /**
     * Legt den Wert der codeSystemEntry-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setCodeSystemEntry(Boolean value) {
        this.codeSystemEntry = value;
    }

    /**
     * Ruft den Wert der codeSystemName-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCodeSystemName() {
        return codeSystemName;
    }

    /**
     * Legt den Wert der codeSystemName-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCodeSystemName(String value) {
        this.codeSystemName = value;
    }

    /**
     * Ruft den Wert der codeSystemVersionName-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCodeSystemVersionName() {
        return codeSystemVersionName;
    }

    /**
     * Legt den Wert der codeSystemVersionName-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCodeSystemVersionName(String value) {
        this.codeSystemVersionName = value;
    }

    /**
     * Ruft den Wert der csId-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public Long getCsId() {
        return csId;
    }

    /**
     * Legt den Wert der csId-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setCsId(Long value) {
        this.csId = value;
    }

    /**
     * Ruft den Wert der csevId-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public Long getCsevId() {
        return csevId;
    }

    /**
     * Legt den Wert der csevId-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setCsevId(Long value) {
        this.csevId = value;
    }

    /**
     * Ruft den Wert der csvId-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public Long getCsvId() {
        return csvId;
    }

    /**
     * Legt den Wert der csvId-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setCsvId(Long value) {
        this.csvId = value;
    }

    /**
     * Ruft den Wert der cvsmId-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link ConceptValueSetMembershipId }
     *     
     */
    public ConceptValueSetMembershipId getCvsmId() {
        return cvsmId;
    }

    /**
     * Legt den Wert der cvsmId-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link ConceptValueSetMembershipId }
     *     
     */
    public void setCvsmId(ConceptValueSetMembershipId value) {
        this.cvsmId = value;
    }

    /**
     * Ruft den Wert der sourceCodeSystemInfo-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSourceCodeSystemInfo() {
        return sourceCodeSystemInfo;
    }

    /**
     * Legt den Wert der sourceCodeSystemInfo-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSourceCodeSystemInfo(String value) {
        this.sourceCodeSystemInfo = value;
    }

    /**
     * Ruft den Wert der term-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTerm() {
        return term;
    }

    /**
     * Legt den Wert der term-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTerm(String value) {
        this.term = value;
    }

    /**
     * Ruft den Wert der valueSetName-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getValueSetName() {
        return valueSetName;
    }

    /**
     * Legt den Wert der valueSetName-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setValueSetName(String value) {
        this.valueSetName = value;
    }

    /**
     * Ruft den Wert der valueSetVersionName-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getValueSetVersionName() {
        return valueSetVersionName;
    }

    /**
     * Legt den Wert der valueSetVersionName-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setValueSetVersionName(String value) {
        this.valueSetVersionName = value;
    }

    /**
     * Ruft den Wert der vsId-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public Long getVsId() {
        return vsId;
    }

    /**
     * Legt den Wert der vsId-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setVsId(Long value) {
        this.vsId = value;
    }

    /**
     * Ruft den Wert der vsvId-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public Long getVsvId() {
        return vsvId;
    }

    /**
     * Legt den Wert der vsvId-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setVsvId(Long value) {
        this.vsvId = value;
    }

}
