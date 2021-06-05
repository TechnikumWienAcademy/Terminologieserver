
package de.fhdo.terminologie.ws.administration;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java-Klasse für ImportCodeSystemCancelResponse complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="ImportCodeSystemCancelResponse">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="return" type="{http://administration.ws.terminologie.fhdo.de/}importCodeSystemCancelResponseType" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ImportCodeSystemCancelResponse", propOrder = {
    "_return"
})
public class ImportCodeSystemCancelResponse {

    @XmlElement(name = "return")
    protected ImportCodeSystemCancelResponseType _return;

    /**
     * Ruft den Wert der return-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link ImportCodeSystemCancelResponseType }
     *     
     */
    public ImportCodeSystemCancelResponseType getReturn() {
        return _return;
    }

    /**
     * Legt den Wert der return-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link ImportCodeSystemCancelResponseType }
     *     
     */
    public void setReturn(ImportCodeSystemCancelResponseType value) {
        this._return = value;
    }

}
