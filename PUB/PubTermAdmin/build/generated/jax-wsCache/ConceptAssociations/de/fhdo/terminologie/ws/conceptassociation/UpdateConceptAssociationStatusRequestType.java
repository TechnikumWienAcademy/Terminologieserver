
package de.fhdo.terminologie.ws.conceptassociation;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;
import types.termserver.fhdo.de.CodeSystemEntityVersionAssociation;


/**
 * <p>Java-Klasse für updateConceptAssociationStatusRequestType complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="updateConceptAssociationStatusRequestType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="codeSystemEntityVersionAssociation" type="{de.fhdo.termserver.types}codeSystemEntityVersionAssociation" minOccurs="0"/>
 *         &lt;element name="login" type="{http://conceptAssociation.ws.terminologie.fhdo.de/}loginType" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "updateConceptAssociationStatusRequestType", propOrder = {
    "codeSystemEntityVersionAssociation",
    "login"
})
public class UpdateConceptAssociationStatusRequestType {

    protected CodeSystemEntityVersionAssociation codeSystemEntityVersionAssociation;
    protected LoginType login;

    /**
     * Ruft den Wert der codeSystemEntityVersionAssociation-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link CodeSystemEntityVersionAssociation }
     *     
     */
    public CodeSystemEntityVersionAssociation getCodeSystemEntityVersionAssociation() {
        return codeSystemEntityVersionAssociation;
    }

    /**
     * Legt den Wert der codeSystemEntityVersionAssociation-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link CodeSystemEntityVersionAssociation }
     *     
     */
    public void setCodeSystemEntityVersionAssociation(CodeSystemEntityVersionAssociation value) {
        this.codeSystemEntityVersionAssociation = value;
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
