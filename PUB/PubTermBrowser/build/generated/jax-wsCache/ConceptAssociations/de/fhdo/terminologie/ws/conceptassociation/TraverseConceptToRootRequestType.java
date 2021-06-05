
package de.fhdo.terminologie.ws.conceptassociation;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;
import types.termserver.fhdo.de.CodeSystemEntity;


/**
 * <p>Java-Klasse für traverseConceptToRootRequestType complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="traverseConceptToRootRequestType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="codeSystemEntity" type="{de.fhdo.termserver.types}codeSystemEntity" minOccurs="0"/>
 *         &lt;element name="directionToRoot" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="login" type="{http://conceptAssociation.ws.terminologie.fhdo.de/}loginType" minOccurs="0"/>
 *         &lt;element name="readEntityDetails" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "traverseConceptToRootRequestType", propOrder = {
    "codeSystemEntity",
    "directionToRoot",
    "login",
    "readEntityDetails"
})
public class TraverseConceptToRootRequestType {

    protected CodeSystemEntity codeSystemEntity;
    protected Boolean directionToRoot;
    protected LoginType login;
    protected Boolean readEntityDetails;

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
     * Ruft den Wert der directionToRoot-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isDirectionToRoot() {
        return directionToRoot;
    }

    /**
     * Legt den Wert der directionToRoot-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setDirectionToRoot(Boolean value) {
        this.directionToRoot = value;
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
     * Ruft den Wert der readEntityDetails-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isReadEntityDetails() {
        return readEntityDetails;
    }

    /**
     * Legt den Wert der readEntityDetails-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setReadEntityDetails(Boolean value) {
        this.readEntityDetails = value;
    }

}
