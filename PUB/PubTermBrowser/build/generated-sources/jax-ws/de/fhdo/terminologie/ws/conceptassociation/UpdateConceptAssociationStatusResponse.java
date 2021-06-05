
package de.fhdo.terminologie.ws.conceptassociation;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java-Klasse für UpdateConceptAssociationStatusResponse complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="UpdateConceptAssociationStatusResponse">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="return" type="{http://conceptAssociation.ws.terminologie.fhdo.de/}updateConceptAssociationStatusResponseType" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "UpdateConceptAssociationStatusResponse", propOrder = {
    "_return"
})
public class UpdateConceptAssociationStatusResponse {

    @XmlElement(name = "return")
    protected UpdateConceptAssociationStatusResponseType _return;

    /**
     * Ruft den Wert der return-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link UpdateConceptAssociationStatusResponseType }
     *     
     */
    public UpdateConceptAssociationStatusResponseType getReturn() {
        return _return;
    }

    /**
     * Legt den Wert der return-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link UpdateConceptAssociationStatusResponseType }
     *     
     */
    public void setReturn(UpdateConceptAssociationStatusResponseType value) {
        this._return = value;
    }

}
