
package de.fhdo.terminologie.ws.authoring;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;
import types.termserver.fhdo.de.CodeSystemEntity;


/**
 * <p>Java-Klasse f�r maintainConceptAssociationTypeRequestType complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="maintainConceptAssociationTypeRequestType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="codeSystemEntity" type="{de.fhdo.termserver.types}codeSystemEntity" minOccurs="0"/>
 *         &lt;element name="login" type="{http://authoring.ws.terminologie.fhdo.de/}loginType" minOccurs="0"/>
 *         &lt;element name="versioning" type="{http://authoring.ws.terminologie.fhdo.de/}versioningType" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "maintainConceptAssociationTypeRequestType", propOrder = {
    "codeSystemEntity",
    "login",
    "versioning"
})
public class MaintainConceptAssociationTypeRequestType {

    protected CodeSystemEntity codeSystemEntity;
    protected LoginType login;
    protected VersioningType versioning;

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
     * Ruft den Wert der versioning-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link VersioningType }
     *     
     */
    public VersioningType getVersioning() {
        return versioning;
    }

    /**
     * Legt den Wert der versioning-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link VersioningType }
     *     
     */
    public void setVersioning(VersioningType value) {
        this.versioning = value;
    }

}
