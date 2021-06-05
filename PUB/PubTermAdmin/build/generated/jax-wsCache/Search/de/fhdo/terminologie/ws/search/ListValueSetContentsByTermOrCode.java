
package de.fhdo.terminologie.ws.search;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java-Klasse für ListValueSetContentsByTermOrCode complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="ListValueSetContentsByTermOrCode">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="parameter" type="{http://search.ws.terminologie.fhdo.de/}listValueSetContentsByTermOrCodeRequestType" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ListValueSetContentsByTermOrCode", propOrder = {
    "parameter"
})
public class ListValueSetContentsByTermOrCode {

    protected ListValueSetContentsByTermOrCodeRequestType parameter;

    /**
     * Ruft den Wert der parameter-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link ListValueSetContentsByTermOrCodeRequestType }
     *     
     */
    public ListValueSetContentsByTermOrCodeRequestType getParameter() {
        return parameter;
    }

    /**
     * Legt den Wert der parameter-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link ListValueSetContentsByTermOrCodeRequestType }
     *     
     */
    public void setParameter(ListValueSetContentsByTermOrCodeRequestType value) {
        this.parameter = value;
    }

}
