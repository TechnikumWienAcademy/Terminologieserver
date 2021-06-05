
package de.fhdo.terminologie.ws.authoringPub;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java-Klasse für CreateValueSetConceptMetadataValue complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="CreateValueSetConceptMetadataValue">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="parameter" type="{http://authoring.ws.terminologie.fhdo.de/}createValueSetConceptMetadataValueRequestType" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CreateValueSetConceptMetadataValue", propOrder = {
    "parameter"
})
public class CreateValueSetConceptMetadataValue {

    protected CreateValueSetConceptMetadataValueRequestType parameter;

    /**
     * Ruft den Wert der parameter-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link CreateValueSetConceptMetadataValueRequestType }
     *     
     */
    public CreateValueSetConceptMetadataValueRequestType getParameter() {
        return parameter;
    }

    /**
     * Legt den Wert der parameter-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link CreateValueSetConceptMetadataValueRequestType }
     *     
     */
    public void setParameter(CreateValueSetConceptMetadataValueRequestType value) {
        this.parameter = value;
    }

}
