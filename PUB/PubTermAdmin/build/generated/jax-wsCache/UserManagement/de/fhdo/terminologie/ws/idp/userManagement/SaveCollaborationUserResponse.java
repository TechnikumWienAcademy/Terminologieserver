
package de.fhdo.terminologie.ws.idp.userManagement;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java-Klasse für saveCollaborationUserResponse complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="saveCollaborationUserResponse">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="return" type="{http://userManagement.idp.ws.terminologie.fhdo.de/}saveCollaborationUserResponseType" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "saveCollaborationUserResponse", propOrder = {
    "_return"
})
public class SaveCollaborationUserResponse {

    @XmlElement(name = "return")
    protected SaveCollaborationUserResponseType _return;

    /**
     * Ruft den Wert der return-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link SaveCollaborationUserResponseType }
     *     
     */
    public SaveCollaborationUserResponseType getReturn() {
        return _return;
    }

    /**
     * Legt den Wert der return-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link SaveCollaborationUserResponseType }
     *     
     */
    public void setReturn(SaveCollaborationUserResponseType value) {
        this._return = value;
    }

}
