
package de.fhdo.terminologie.ws.idp.userManagement;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java-Klasse für saveCollaborationUserRequestType complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="saveCollaborationUserRequestType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="loginType" type="{http://userManagement.idp.ws.terminologie.fhdo.de/}loginType" minOccurs="0"/>
 *         &lt;element name="newEntry" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *         &lt;element name="role" type="{de.fhdo.termserver.idp.types}role" minOccurs="0"/>
 *         &lt;element name="termuser" type="{de.fhdo.termserver.idp.types}termUser" minOccurs="0"/>
 *         &lt;element name="user" type="{de.fhdo.termserver.idp.types}collaborationuser" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "saveCollaborationUserRequestType", propOrder = {
    "loginType",
    "newEntry",
    "role",
    "termuser",
    "user"
})
public class SaveCollaborationUserRequestType {

    protected LoginType loginType;
    protected boolean newEntry;
    protected Role role;
    protected TermUser termuser;
    protected Collaborationuser user;

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
     * Ruft den Wert der role-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Role }
     *     
     */
    public Role getRole() {
        return role;
    }

    /**
     * Legt den Wert der role-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Role }
     *     
     */
    public void setRole(Role value) {
        this.role = value;
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

    /**
     * Ruft den Wert der user-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Collaborationuser }
     *     
     */
    public Collaborationuser getUser() {
        return user;
    }

    /**
     * Legt den Wert der user-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Collaborationuser }
     *     
     */
    public void setUser(Collaborationuser value) {
        this.user = value;
    }

}
