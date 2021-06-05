
package de.fhdo.terminologie.ws.authoringPub;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java-Klasse für CreateCodeSystemResponse complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="CreateCodeSystemResponse">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="return" minOccurs="0">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="returnInfos" type="{http://authoring.ws.terminologie.fhdo.de/}returnType" minOccurs="0"/>
 *                   &lt;element name="codeSystem" type="{de.fhdo.termserver.types}codeSystem" minOccurs="0"/>
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
@XmlType(name = "CreateCodeSystemResponse", propOrder = {
    "_return"
})
public class CreateCodeSystemResponse {

    @XmlElement(name = "return")
    protected CreateCodeSystemResponse.Return _return;

    /**
     * Ruft den Wert der return-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link CreateCodeSystemResponse.Return }
     *     
     */
    public CreateCodeSystemResponse.Return getReturn() {
        return _return;
    }

    /**
     * Legt den Wert der return-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link CreateCodeSystemResponse.Return }
     *     
     */
    public void setReturn(CreateCodeSystemResponse.Return value) {
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
     *         &lt;element name="returnInfos" type="{http://authoring.ws.terminologie.fhdo.de/}returnType" minOccurs="0"/>
     *         &lt;element name="codeSystem" type="{de.fhdo.termserver.types}codeSystem" minOccurs="0"/>
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
        "codeSystem"
    })
    public static class Return {

        protected ReturnType returnInfos;
        protected CodeSystem codeSystem;

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
         * Ruft den Wert der codeSystem-Eigenschaft ab.
         * 
         * @return
         *     possible object is
         *     {@link CodeSystem }
         *     
         */
        public CodeSystem getCodeSystem() {
            return codeSystem;
        }

        /**
         * Legt den Wert der codeSystem-Eigenschaft fest.
         * 
         * @param value
         *     allowed object is
         *     {@link CodeSystem }
         *     
         */
        public void setCodeSystem(CodeSystem value) {
            this.codeSystem = value;
        }

    }

}
