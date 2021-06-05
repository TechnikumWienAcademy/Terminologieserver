
package de.fhdo.terminologie.ws.conceptassociation;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import types.termserver.fhdo.de.CodeSystemEntityVersion;


/**
 * <p>Java-Klasse für TraverseConceptToRootResponse complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="TraverseConceptToRootResponse">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="return" minOccurs="0">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="returnInfos" type="{http://conceptAssociation.ws.terminologie.fhdo.de/}returnType" minOccurs="0"/>
 *                   &lt;element name="codeSystemEntityVersionRoot" type="{de.fhdo.termserver.types}codeSystemEntityVersion" minOccurs="0"/>
 *                 &lt;/sequence>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "TraverseConceptToRootResponse", propOrder = {
    "_return"
})
public class TraverseConceptToRootResponse {

    @XmlElement(name = "return")
    protected TraverseConceptToRootResponse.Return _return;

    /**
     * Ruft den Wert der return-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link TraverseConceptToRootResponse.Return }
     *     
     */
    public TraverseConceptToRootResponse.Return getReturn() {
        return _return;
    }

    /**
     * Legt den Wert der return-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link TraverseConceptToRootResponse.Return }
     *     
     */
    public void setReturn(TraverseConceptToRootResponse.Return value) {
        this._return = value;
    }


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
     *         &lt;element name="codeSystemEntityVersionRoot" type="{de.fhdo.termserver.types}codeSystemEntityVersion" minOccurs="0"/>
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
        "codeSystemEntityVersionRoot"
    })
    public static class Return {

        protected ReturnType returnInfos;
        protected CodeSystemEntityVersion codeSystemEntityVersionRoot;

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
         * Ruft den Wert der codeSystemEntityVersionRoot-Eigenschaft ab.
         * 
         * @return
         *     possible object is
         *     {@link CodeSystemEntityVersion }
         *     
         */
        public CodeSystemEntityVersion getCodeSystemEntityVersionRoot() {
            return codeSystemEntityVersionRoot;
        }

        /**
         * Legt den Wert der codeSystemEntityVersionRoot-Eigenschaft fest.
         * 
         * @param value
         *     allowed object is
         *     {@link CodeSystemEntityVersion }
         *     
         */
        public void setCodeSystemEntityVersionRoot(CodeSystemEntityVersion value) {
            this.codeSystemEntityVersionRoot = value;
        }

    }

}
