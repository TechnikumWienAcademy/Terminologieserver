
package de.fhdo.terminologie.ws.authoringPub;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java-Klasse f�r getRemoveTerminologyOrConceptPubResponseResponse complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="getRemoveTerminologyOrConceptPubResponseResponse">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="return" type="{http://authoring.ws.terminologie.fhdo.de/}removeTerminologyOrConceptResponseType" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "getRemoveTerminologyOrConceptPubResponseResponse", propOrder = {
    "_return"
})
public class GetRemoveTerminologyOrConceptPubResponseResponse {

    @XmlElement(name = "return")
    protected RemoveTerminologyOrConceptResponseType _return;

    /**
     * Ruft den Wert der return-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link RemoveTerminologyOrConceptResponseType }
     *     
     */
    public RemoveTerminologyOrConceptResponseType getReturn() {
        return _return;
    }

    /**
     * Legt den Wert der return-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link RemoveTerminologyOrConceptResponseType }
     *     
     */
    public void setReturn(RemoveTerminologyOrConceptResponseType value) {
        this._return = value;
    }

}
