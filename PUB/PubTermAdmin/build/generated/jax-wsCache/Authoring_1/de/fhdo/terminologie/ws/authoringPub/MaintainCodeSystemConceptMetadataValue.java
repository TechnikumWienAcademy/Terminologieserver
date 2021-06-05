
package de.fhdo.terminologie.ws.authoringPub;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java-Klasse für MaintainCodeSystemConceptMetadataValue complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="MaintainCodeSystemConceptMetadataValue">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="parameter" type="{http://authoring.ws.terminologie.fhdo.de/}maintainCodeSystemConceptMetadataValueRequestType" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "MaintainCodeSystemConceptMetadataValue", propOrder = {
    "parameter"
})
public class MaintainCodeSystemConceptMetadataValue {

    protected MaintainCodeSystemConceptMetadataValueRequestType parameter;

    /**
     * Ruft den Wert der parameter-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link MaintainCodeSystemConceptMetadataValueRequestType }
     *     
     */
    public MaintainCodeSystemConceptMetadataValueRequestType getParameter() {
        return parameter;
    }

    /**
     * Legt den Wert der parameter-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link MaintainCodeSystemConceptMetadataValueRequestType }
     *     
     */
    public void setParameter(MaintainCodeSystemConceptMetadataValueRequestType value) {
        this.parameter = value;
    }

}
