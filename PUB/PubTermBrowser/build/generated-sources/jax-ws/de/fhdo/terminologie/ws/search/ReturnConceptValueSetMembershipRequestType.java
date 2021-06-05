
package de.fhdo.terminologie.ws.search;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;
import types.termserver.fhdo.de.CodeSystemEntityVersion;
import types.termserver.fhdo.de.ValueSetVersion;


/**
 * <p>Java-Klasse für returnConceptValueSetMembershipRequestType complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="returnConceptValueSetMembershipRequestType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="codeSystemEntityVersion" type="{de.fhdo.termserver.types}codeSystemEntityVersion" minOccurs="0"/>
 *         &lt;element name="login" type="{http://search.ws.terminologie.fhdo.de/}loginType" minOccurs="0"/>
 *         &lt;element name="valueSetVersion" type="{de.fhdo.termserver.types}valueSetVersion" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "returnConceptValueSetMembershipRequestType", propOrder = {
    "codeSystemEntityVersion",
    "login",
    "valueSetVersion"
})
public class ReturnConceptValueSetMembershipRequestType {

    protected CodeSystemEntityVersion codeSystemEntityVersion;
    protected LoginType login;
    protected ValueSetVersion valueSetVersion;

    /**
     * Ruft den Wert der codeSystemEntityVersion-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link CodeSystemEntityVersion }
     *     
     */
    public CodeSystemEntityVersion getCodeSystemEntityVersion() {
        return codeSystemEntityVersion;
    }

    /**
     * Legt den Wert der codeSystemEntityVersion-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link CodeSystemEntityVersion }
     *     
     */
    public void setCodeSystemEntityVersion(CodeSystemEntityVersion value) {
        this.codeSystemEntityVersion = value;
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

    /**
     * Ruft den Wert der valueSetVersion-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link ValueSetVersion }
     *     
     */
    public ValueSetVersion getValueSetVersion() {
        return valueSetVersion;
    }

    /**
     * Legt den Wert der valueSetVersion-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link ValueSetVersion }
     *     
     */
    public void setValueSetVersion(ValueSetVersion value) {
        this.valueSetVersion = value;
    }

}
