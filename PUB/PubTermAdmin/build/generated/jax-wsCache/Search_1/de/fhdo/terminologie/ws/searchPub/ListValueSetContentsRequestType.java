
package de.fhdo.terminologie.ws.searchPub;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java-Klasse für listValueSetContentsRequestType complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="listValueSetContentsRequestType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="login" type="{http://search.ws.terminologie.fhdo.de/}loginType" minOccurs="0"/>
 *         &lt;element name="valueSet" type="{de.fhdo.termserver.types}valueSet"/>
 *         &lt;element name="sortingParameter" type="{http://search.ws.terminologie.fhdo.de/}sortingType" minOccurs="0"/>
 *         &lt;element name="readMetadataLevel" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "listValueSetContentsRequestType", propOrder = {
    "login",
    "valueSet",
    "sortingParameter",
    "readMetadataLevel"
})
public class ListValueSetContentsRequestType {

    protected LoginType login;
    @XmlElement(required = true)
    protected ValueSet valueSet;
    protected SortingType sortingParameter;
    protected Boolean readMetadataLevel;

    /**
     * Ruft den Wert der login-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link LoginType }
     *     
     */
    public LoginType getLogin() {
        return login;
    }

    /**
     * Legt den Wert der login-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link LoginType }
     *     
     */
    public void setLogin(LoginType value) {
        this.login = value;
    }

    /**
     * Ruft den Wert der valueSet-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link ValueSet }
     *     
     */
    public ValueSet getValueSet() {
        return valueSet;
    }

    /**
     * Legt den Wert der valueSet-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link ValueSet }
     *     
     */
    public void setValueSet(ValueSet value) {
        this.valueSet = value;
    }

    /**
     * Ruft den Wert der sortingParameter-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link SortingType }
     *     
     */
    public SortingType getSortingParameter() {
        return sortingParameter;
    }

    /**
     * Legt den Wert der sortingParameter-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link SortingType }
     *     
     */
    public void setSortingParameter(SortingType value) {
        this.sortingParameter = value;
    }

    /**
     * Ruft den Wert der readMetadataLevel-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isReadMetadataLevel() {
        return readMetadataLevel;
    }

    /**
     * Legt den Wert der readMetadataLevel-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setReadMetadataLevel(Boolean value) {
        this.readMetadataLevel = value;
    }

}
