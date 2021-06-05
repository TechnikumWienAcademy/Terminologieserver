
package de.fhdo.terminologie.ws.idp.usermanagement;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java-Klasse für getUserList complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="getUserList">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="parameter" type="{http://userManagement.idp.ws.terminologie.fhdo.de/}getTermUserRequestType" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "getUserList", propOrder = {
    "parameter"
})
public class GetUserList {

    protected GetTermUserRequestType parameter;

    /**
     * Ruft den Wert der parameter-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link GetTermUserRequestType }
     *     
     */
    public GetTermUserRequestType getParameter() {
        return parameter;
    }

    /**
     * Legt den Wert der parameter-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link GetTermUserRequestType }
     *     
     */
    public void setParameter(GetTermUserRequestType value) {
        this.parameter = value;
    }

}
