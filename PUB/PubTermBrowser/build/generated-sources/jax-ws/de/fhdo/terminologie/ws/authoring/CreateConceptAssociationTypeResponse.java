
package de.fhdo.terminologie.ws.authoring;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import types.termserver.fhdo.de.CodeSystemEntity;


/**
 * <p>Java-Klasse f�r CreateConceptAssociationTypeResponse complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="CreateConceptAssociationTypeResponse">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="return" minOccurs="0">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="returnInfos" type="{http://authoring.ws.terminologie.fhdo.de/}returnType" minOccurs="0"/>
 *                   &lt;element name="codeSystemEntity" type="{de.fhdo.termserver.types}codeSystemEntity" minOccurs="0"/>
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
@XmlType(name = "CreateConceptAssociationTypeResponse", propOrder = {
    "_return"
})
public class CreateConceptAssociationTypeResponse {

    @XmlElement(name = "return")
    protected CreateConceptAssociationTypeResponse.Return _return;

    /**
     * Ruft den Wert der return-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link CreateConceptAssociationTypeResponse.Return }
     *     
     */
    public CreateConceptAssociationTypeResponse.Return getReturn() {
        return _return;
    }

    /**
     * Legt den Wert der return-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link CreateConceptAssociationTypeResponse.Return }
     *     
     */
    public void setReturn(CreateConceptAssociationTypeResponse.Return value) {
        this._return = value;
    }


    /**
     * <p>Java-Klasse f�r anonymous complex type.
     * 
     * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
     * 
     * <pre>
     * &lt;complexType>
     *   &lt;complexContent>
     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *       &lt;sequence>
     *         &lt;element name="returnInfos" type="{http://authoring.ws.terminologie.fhdo.de/}returnType" minOccurs="0"/>
     *         &lt;element name="codeSystemEntity" type="{de.fhdo.termserver.types}codeSystemEntity" minOccurs="0"/>
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
        "codeSystemEntity"
    })
    public static class Return {

        protected ReturnType returnInfos;
        protected CodeSystemEntity codeSystemEntity;

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

    }

}
