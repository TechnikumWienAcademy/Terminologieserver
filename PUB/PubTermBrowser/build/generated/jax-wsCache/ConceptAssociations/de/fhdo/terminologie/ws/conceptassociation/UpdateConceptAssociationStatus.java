
package de.fhdo.terminologie.ws.conceptassociation;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java-Klasse für UpdateConceptAssociationStatus complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="UpdateConceptAssociationStatus">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="parameter" type="{http://conceptAssociation.ws.terminologie.fhdo.de/}updateConceptAssociationStatusRequestType" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "UpdateConceptAssociationStatus", propOrder = {
    "parameter"
})
public class UpdateConceptAssociationStatus {

    protected UpdateConceptAssociationStatusRequestType parameter;

    /**
     * Ruft den Wert der parameter-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link UpdateConceptAssociationStatusRequestType }
     *     
     */
    public UpdateConceptAssociationStatusRequestType getParameter() {
        return parameter;
    }

    /**
     * Legt den Wert der parameter-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link UpdateConceptAssociationStatusRequestType }
     *     
     */
    public void setParameter(UpdateConceptAssociationStatusRequestType value) {
        this.parameter = value;
    }

}
