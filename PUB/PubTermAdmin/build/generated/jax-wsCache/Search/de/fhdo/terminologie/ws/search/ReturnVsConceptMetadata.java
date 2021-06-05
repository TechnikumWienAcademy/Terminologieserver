
package de.fhdo.terminologie.ws.search;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java-Klasse für ReturnVsConceptMetadata complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="ReturnVsConceptMetadata">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="parameter" type="{http://search.ws.terminologie.fhdo.de/}returnVsConceptMetadataRequestType" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ReturnVsConceptMetadata", propOrder = {
    "parameter"
})
public class ReturnVsConceptMetadata {

    protected ReturnVsConceptMetadataRequestType parameter;

    /**
     * Ruft den Wert der parameter-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link ReturnVsConceptMetadataRequestType }
     *     
     */
    public ReturnVsConceptMetadataRequestType getParameter() {
        return parameter;
    }

    /**
     * Legt den Wert der parameter-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link ReturnVsConceptMetadataRequestType }
     *     
     */
    public void setParameter(ReturnVsConceptMetadataRequestType value) {
        this.parameter = value;
    }

}
