
package de.fhdo.terminologie.ws.authoring;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import types.termserver.fhdo.de.CodeSystemMetadataValue;


/**
 * <p>Java-Klasse für MaintainCodeSystemConceptMetadataValueResponse complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="MaintainCodeSystemConceptMetadataValueResponse">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="return" minOccurs="0">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="returnInfos" type="{http://authoring.ws.terminologie.fhdo.de/}returnType" minOccurs="0"/>
 *                   &lt;element name="codeSystemMetadataValues" type="{de.fhdo.termserver.types}codeSystemMetadataValue" maxOccurs="unbounded" minOccurs="0"/>
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
@XmlType(name = "MaintainCodeSystemConceptMetadataValueResponse", propOrder = {
    "_return"
})
public class MaintainCodeSystemConceptMetadataValueResponse {

    @XmlElement(name = "return")
    protected MaintainCodeSystemConceptMetadataValueResponse.Return _return;

    /**
     * Ruft den Wert der return-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link MaintainCodeSystemConceptMetadataValueResponse.Return }
     *     
     */
    public MaintainCodeSystemConceptMetadataValueResponse.Return getReturn() {
        return _return;
    }

    /**
     * Legt den Wert der return-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link MaintainCodeSystemConceptMetadataValueResponse.Return }
     *     
     */
    public void setReturn(MaintainCodeSystemConceptMetadataValueResponse.Return value) {
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
     *         &lt;element name="codeSystemMetadataValues" type="{de.fhdo.termserver.types}codeSystemMetadataValue" maxOccurs="unbounded" minOccurs="0"/>
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
        "codeSystemMetadataValues"
    })
    public static class Return {

        protected ReturnType returnInfos;
        @XmlElement(nillable = true)
        protected List<CodeSystemMetadataValue> codeSystemMetadataValues;

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
         * Gets the value of the codeSystemMetadataValues property.
         * 
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the codeSystemMetadataValues property.
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getCodeSystemMetadataValues().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link CodeSystemMetadataValue }
         * 
         * 
         */
        public List<CodeSystemMetadataValue> getCodeSystemMetadataValues() {
            if (codeSystemMetadataValues == null) {
                codeSystemMetadataValues = new ArrayList<CodeSystemMetadataValue>();
            }
            return this.codeSystemMetadataValues;
        }

    }

}
