
package de.fhdo.terminologie.ws.authoringPub;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java-Klasse für removeTerminologyOrConceptRequestType complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="removeTerminologyOrConceptRequestType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="deleteInfo" type="{http://authoring.ws.terminologie.fhdo.de/}deleteInfo" minOccurs="0"/>
 *         &lt;element name="login" type="{http://authoring.ws.terminologie.fhdo.de/}loginType" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "removeTerminologyOrConceptRequestType", propOrder = {
    "deleteInfo",
    "login"
})
public class RemoveTerminologyOrConceptRequestType {

    protected DeleteInfo deleteInfo;
    protected LoginType login;

    /**
     * Ruft den Wert der deleteInfo-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link DeleteInfo }
     *     
     */
    public DeleteInfo getDeleteInfo() {
        return deleteInfo;
    }

    /**
     * Legt den Wert der deleteInfo-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link DeleteInfo }
     *     
     */
    public void setDeleteInfo(DeleteInfo value) {
        this.deleteInfo = value;
    }

    /**
     * Ruft den Wert der login-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link LoginType }
     *     
     */
    public LoginType getLogin() {
        return login;
    }

    /**
     * Legt den Wert der login-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link LoginType }
     *     
     */
    public void setLogin(LoginType value) {
        this.login = value;
    }

}
