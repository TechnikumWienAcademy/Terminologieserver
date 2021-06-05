
package de.fhdo.terminologie.ws.conceptassociation;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;
import types.termserver.fhdo.de.CodeSystemEntity;
import types.termserver.fhdo.de.CodeSystemEntityVersionAssociation;


/**
 * <p>Java-Klasse für listConceptAssociationsRequestType complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="listConceptAssociationsRequestType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="codeSystemEntity" type="{de.fhdo.termserver.types}codeSystemEntity" minOccurs="0"/>
 *         &lt;element name="codeSystemEntityVersionAssociation" type="{de.fhdo.termserver.types}codeSystemEntityVersionAssociation" minOccurs="0"/>
 *         &lt;element name="directionBoth" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="login" type="{http://conceptAssociation.ws.terminologie.fhdo.de/}loginType" minOccurs="0"/>
 *         &lt;element name="lookForward" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="reverse" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "listConceptAssociationsRequestType", propOrder = {
    "codeSystemEntity",
    "codeSystemEntityVersionAssociation",
    "directionBoth",
    "login",
    "lookForward",
    "reverse"
})
public class ListConceptAssociationsRequestType {

    protected CodeSystemEntity codeSystemEntity;
    protected CodeSystemEntityVersionAssociation codeSystemEntityVersionAssociation;
    protected Boolean directionBoth;
    protected LoginType login;
    protected Boolean lookForward;
    protected Boolean reverse;

    /**
     * Ruft den Wert der codeSystemEntity-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link CodeSystemEntity }
     *     
     */
    public CodeSystemEntity getCodeSystemEntity() {
        return codeSystemEntity;
    }

    /**
     * Legt den Wert der codeSystemEntity-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link CodeSystemEntity }
     *     
     */
    public void setCodeSystemEntity(CodeSystemEntity value) {
        this.codeSystemEntity = value;
    }

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
     * Ruft den Wert der directionBoth-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isDirectionBoth() {
        return directionBoth;
    }

    /**
     * Legt den Wert der directionBoth-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setDirectionBoth(Boolean value) {
        this.directionBoth = value;
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
     * Ruft den Wert der lookForward-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isLookForward() {
        return lookForward;
    }

    /**
     * Legt den Wert der lookForward-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setLookForward(Boolean value) {
        this.lookForward = value;
    }

    /**
     * Ruft den Wert der reverse-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isReverse() {
        return reverse;
    }

    /**
     * Legt den Wert der reverse-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setReverse(Boolean value) {
        this.reverse = value;
    }

}
