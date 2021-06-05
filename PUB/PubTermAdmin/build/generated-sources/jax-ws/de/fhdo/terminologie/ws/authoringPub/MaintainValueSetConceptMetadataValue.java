
package de.fhdo.terminologie.ws.authoringPub;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java-Klasse für MaintainValueSetConceptMetadataValue complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="MaintainValueSetConceptMetadataValue">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="parameter" type="{http://authoring.ws.terminologie.fhdo.de/}maintainValueSetConceptMetadataValueRequestType" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "MaintainValueSetConceptMetadataValue", propOrder = {
    "parameter"
})
public class MaintainValueSetConceptMetadataValue {

    protected MaintainValueSetConceptMetadataValueRequestType parameter;

    /**
     * Ruft den Wert der parameter-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link MaintainValueSetConceptMetadataValueRequestType }
     *     
     */
    public MaintainValueSetConceptMetadataValueRequestType getParameter() {
        return parameter;
    }

    /**
     * Legt den Wert der parameter-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link MaintainValueSetConceptMetadataValueRequestType }
     *     
     */
    public void setParameter(MaintainValueSetConceptMetadataValueRequestType value) {
        this.parameter = value;
    }

}
