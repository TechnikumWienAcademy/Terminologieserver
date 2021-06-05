
package de.fhdo.terminologie.ws.searchPub;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java-Klasse für codeSystemConceptTranslation complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="codeSystemConceptTranslation">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="codeSystemConcept" type="{de.fhdo.termserver.types}codeSystemConcept" minOccurs="0"/>
 *         &lt;element name="description" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="id" type="{http://www.w3.org/2001/XMLSchema}long" minOccurs="0"/>
 *         &lt;element name="languageId" type="{http://www.w3.org/2001/XMLSchema}long" minOccurs="0"/>
 *         &lt;element name="term" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="termAbbrevation" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "codeSystemConceptTranslation", namespace = "de.fhdo.termserver.types", propOrder = {
    "codeSystemConcept",
    "description",
    "id",
    "languageId",
    "term",
    "termAbbrevation"
})
public class CodeSystemConceptTranslation {

    protected CodeSystemConcept codeSystemConcept;
    protected String description;
    protected Long id;
    protected Long languageId;
    protected String term;
    protected String termAbbrevation;

    /**
     * Ruft den Wert der codeSystemConcept-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link CodeSystemConcept }
     *     
     */
    public CodeSystemConcept getCodeSystemConcept() {
        return codeSystemConcept;
    }

    /**
     * Legt den Wert der codeSystemConcept-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link CodeSystemConcept }
     *     
     */
    public void setCodeSystemConcept(CodeSystemConcept value) {
        this.codeSystemConcept = value;
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
     * Ruft den Wert der termAbbrevation-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTermAbbrevation() {
        return termAbbrevation;
    }

    /**
     * Legt den Wert der termAbbrevation-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTermAbbrevation(String value) {
        this.termAbbrevation = value;
    }

}
