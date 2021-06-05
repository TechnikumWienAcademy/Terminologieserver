
package de.fhdo.terminologie.ws.authoringPub;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java-Klasse für DeleteValueSetConceptMetadataValueResponse complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="DeleteValueSetConceptMetadataValueResponse">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="return" minOccurs="0">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="returnInfos" type="{http://authoring.ws.terminologie.fhdo.de/}returnType" minOccurs="0"/>
 *                   &lt;element name="valueSetMetadataValue" type="{de.fhdo.termserver.types}valueSetMetadataValue" maxOccurs="unbounded" minOccurs="0"/>
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
@XmlType(name = "DeleteValueSetConceptMetadataValueResponse", propOrder = {
    "_return"
})
public class DeleteValueSetConceptMetadataValueResponse {

    @XmlElement(name = "return")
    protected DeleteValueSetConceptMetadataValueResponse.Return _return;

    /**
     * Ruft den Wert der return-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link DeleteValueSetConceptMetadataValueResponse.Return }
     *     
     */
    public DeleteValueSetConceptMetadataValueResponse.Return getReturn() {
        return _return;
    }

    /**
     * Legt den Wert der return-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link DeleteValueSetConceptMetadataValueResponse.Return }
     *     
     */
    public void setReturn(DeleteValueSetConceptMetadataValueResponse.Return value) {
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
     *         &lt;element name="valueSetMetadataValue" type="{de.fhdo.termserver.types}valueSetMetadataValue" maxOccurs="unbounded" minOccurs="0"/>
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
        "valueSetMetadataValue"
    })
    public static class Return {

        protected ReturnType returnInfos;
        @XmlElement(nillable = true)
        protected List<ValueSetMetadataValue> valueSetMetadataValue;

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
         * Gets the value of the valueSetMetadataValue property.
         * 
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the valueSetMetadataValue property.
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getValueSetMetadataValue().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link ValueSetMetadataValue }
         * 
         * 
         */
        public List<ValueSetMetadataValue> getValueSetMetadataValue() {
            if (valueSetMetadataValue == null) {
                valueSetMetadataValue = new ArrayList<ValueSetMetadataValue>();
            }
            return this.valueSetMetadataValue;
        }

    }

}
