
package de.fhdo.terminologie.ws.searchPub;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java-Klasse f�r ListCodeSystemsInTaxonomy complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="ListCodeSystemsInTaxonomy">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="parameter" type="{http://search.ws.terminologie.fhdo.de/}listCodeSystemsInTaxonomyRequestType" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ListCodeSystemsInTaxonomy", propOrder = {
    "parameter"
})
public class ListCodeSystemsInTaxonomy {

    protected ListCodeSystemsInTaxonomyRequestType parameter;

    /**
     * Ruft den Wert der parameter-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link ListCodeSystemsInTaxonomyRequestType }
     *     
     */
    public ListCodeSystemsInTaxonomyRequestType getParameter() {
        return parameter;
    }

    /**
     * Legt den Wert der parameter-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link ListCodeSystemsInTaxonomyRequestType }
     *     
     */
    public void setParameter(ListCodeSystemsInTaxonomyRequestType value) {
        this.parameter = value;
    }

}