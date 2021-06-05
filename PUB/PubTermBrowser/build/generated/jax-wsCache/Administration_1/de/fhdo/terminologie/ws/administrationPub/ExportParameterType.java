
package de.fhdo.terminologie.ws.administrationPub;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.XMLGregorianCalendar;


/**
 * <p>Java-Klasse für exportParameterType complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="exportParameterType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="associationInfos" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="codeSystemInfos" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *         &lt;element name="dateFrom" type="{http://www.w3.org/2001/XMLSchema}dateTime" minOccurs="0"/>
 *         &lt;element name="translations" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "exportParameterType", propOrder = {
    "associationInfos",
    "codeSystemInfos",
    "dateFrom",
    "translations"
})
public class ExportParameterType {

    protected String associationInfos;
    protected boolean codeSystemInfos;
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar dateFrom;
    protected boolean translations;

    /**
     * Ruft den Wert der associationInfos-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAssociationInfos() {
        return associationInfos;
    }

    /**
     * Legt den Wert der associationInfos-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAssociationInfos(String value) {
        this.associationInfos = value;
    }

    /**
     * Ruft den Wert der codeSystemInfos-Eigenschaft ab.
     * 
     */
    public boolean isCodeSystemInfos() {
        return codeSystemInfos;
    }

    /**
     * Legt den Wert der codeSystemInfos-Eigenschaft fest.
     * 
     */
    public void setCodeSystemInfos(boolean value) {
        this.codeSystemInfos = value;
    }

    /**
     * Ruft den Wert der dateFrom-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getDateFrom() {
        return dateFrom;
    }

    /**
     * Legt den Wert der dateFrom-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setDateFrom(XMLGregorianCalendar value) {
        this.dateFrom = value;
    }

    /**
     * Ruft den Wert der translations-Eigenschaft ab.
     * 
     */
    public boolean isTranslations() {
        return translations;
    }

    /**
     * Legt den Wert der translations-Eigenschaft fest.
     * 
     */
    public void setTranslations(boolean value) {
        this.translations = value;
    }

}
