
package de.fhdo.terminologie.ws.idp.usermanagement;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java-Klasse für saveCollaborationUser complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="saveCollaborationUser">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="parameter" type="{http://userManagement.idp.ws.terminologie.fhdo.de/}saveCollaborationUserRequestType" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "saveCollaborationUser", propOrder = {
    "parameter"
})
public class SaveCollaborationUser {

    protected SaveCollaborationUserRequestType parameter;

    /**
     * Ruft den Wert der parameter-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link SaveCollaborationUserRequestType }
     *     
     */
    public SaveCollaborationUserRequestType getParameter() {
        return parameter;
    }

    /**
     * Legt den Wert der parameter-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link SaveCollaborationUserRequestType }
     *     
     */
    public void setParameter(SaveCollaborationUserRequestType value) {
        this.parameter = value;
    }

}
