
package de.fhdo.terminologie.ws.conceptassociation;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import types.termserver.fhdo.de.CodeSystemEntityVersionAssociation;


/**
 * <p>Java-Klasse für anonymous complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="returnInfos" type="{http://conceptAssociation.ws.terminologie.fhdo.de/}returnType" minOccurs="0"/>
 *         &lt;element name="codeSystemEntityVersionAssociation" type="{de.fhdo.termserver.types}codeSystemEntityVersionAssociation" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "returnInfos",
    "codeSystemEntityVersionAssociation"
})
@XmlRootElement(name = "returnConceptAssociationDetailsResponseType")
public class ReturnConceptAssociationDetailsResponseType {

    protected ReturnType returnInfos;
    protected CodeSystemEntityVersionAssociation codeSystemEntityVersionAssociation;

    /**
     * Ruft den Wert der returnInfos-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link ReturnType }
     *     
     */
    public ReturnType getReturnInfos() {
        return returnInfos;
    }

    /**
     * Legt den Wert der returnInfos-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link ReturnType }
     *     
     */
    public void setReturnInfos(ReturnType value) {
        this.returnInfos = value;
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

}
