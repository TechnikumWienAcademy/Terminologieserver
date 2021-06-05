
package de.fhdo.terminologie.ws.idp.userManagement;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java-Klasse für deleteUserRequestType complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="deleteUserRequestType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="collaborationUser" type="{de.fhdo.termserver.idp.types}collaborationuser" minOccurs="0"/>
 *         &lt;element name="loginType" type="{http://userManagement.idp.ws.terminologie.fhdo.de/}loginType" minOccurs="0"/>
 *         &lt;element name="termuser" type="{de.fhdo.termserver.idp.types}termUser" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "deleteUserRequestType", propOrder = {
    "collaborationUser",
    "loginType",
    "termuser"
})
public class DeleteUserRequestType {

    protected Collaborationuser collaborationUser;
    protected LoginType loginType;
    protected TermUser termuser;

    /**
     * Ruft den Wert der collaborationUser-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Collaborationuser }
     *     
     */
    public Collaborationuser getCollaborationUser() {
        return collaborationUser;
    }

    /**
     * Legt den Wert der collaborationUser-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Collaborationuser }
     *     
     */
    public void setCollaborationUser(Collaborationuser value) {
        this.collaborationUser = value;
    }

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
     * Ruft den Wert der termuser-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link TermUser }
     *     
     */
    public TermUser getTermuser() {
        return termuser;
    }

    /**
     * Legt den Wert der termuser-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link TermUser }
     *     
     */
    public void setTermuser(TermUser value) {
        this.termuser = value;
    }

}
