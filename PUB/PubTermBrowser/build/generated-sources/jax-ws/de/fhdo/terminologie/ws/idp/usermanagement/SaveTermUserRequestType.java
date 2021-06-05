
package de.fhdo.terminologie.ws.idp.usermanagement;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;
import types.idp.termserver.fhdo.de.TermUser;


/**
 * <p>Java-Klasse für saveTermUserRequestType complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="saveTermUserRequestType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="loginType" type="{http://userManagement.idp.ws.terminologie.fhdo.de/}loginType" minOccurs="0"/>
 *         &lt;element name="newEntry" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *         &lt;element name="termUser" type="{de.fhdo.termserver.idp.types}termUser" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "saveTermUserRequestType", propOrder = {
    "loginType",
    "newEntry",
    "termUser"
})
public class SaveTermUserRequestType {

    protected LoginType loginType;
    protected boolean newEntry;
    protected TermUser termUser;

    /**
     * Ruft den Wert der loginType-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link LoginType }
     *     
     */
    public LoginType getLoginType() {
        return loginType;
    }

    /**
     * Legt den Wert der loginType-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link LoginType }
     *     
     */
    public void setLoginType(LoginType value) {
        this.loginType = value;
    }

    /**
     * Ruft den Wert der newEntry-Eigenschaft ab.
     * 
     */
    public boolean isNewEntry() {
        return newEntry;
    }

    /**
     * Legt den Wert der newEntry-Eigenschaft fest.
     * 
     */
    public void setNewEntry(boolean value) {
        this.newEntry = value;
    }

    /**
     * Ruft den Wert der termUser-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link TermUser }
     *     
     */
    public TermUser getTermUser() {
        return termUser;
    }

    /**
     * Legt den Wert der termUser-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link TermUser }
     *     
     */
    public void setTermUser(TermUser value) {
        this.termUser = value;
    }

}
