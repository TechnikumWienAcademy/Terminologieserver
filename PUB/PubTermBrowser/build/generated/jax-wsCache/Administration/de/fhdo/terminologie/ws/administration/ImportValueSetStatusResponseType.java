
package de.fhdo.terminologie.ws.administration;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


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
 *         &lt;element name="returnInfos" type="{http://administration.ws.terminologie.fhdo.de/}returnType" minOccurs="0"/>
 *         &lt;element name="isRunning" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="currentIndex" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="totalCount" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
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
    "isRunning",
    "currentIndex",
    "totalCount"
})
@XmlRootElement(name = "importValueSetStatusResponseType")
public class ImportValueSetStatusResponseType {

    protected ReturnType returnInfos;
    protected Boolean isRunning;
    protected Integer currentIndex;
    protected Integer totalCount;

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
     * Ruft den Wert der isRunning-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isIsRunning() {
        return isRunning;
    }

    /**
     * Legt den Wert der isRunning-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setIsRunning(Boolean value) {
        this.isRunning = value;
    }

    /**
     * Ruft den Wert der currentIndex-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getCurrentIndex() {
        return currentIndex;
    }

    /**
     * Legt den Wert der currentIndex-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setCurrentIndex(Integer value) {
        this.currentIndex = value;
    }

    /**
     * Ruft den Wert der totalCount-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getTotalCount() {
        return totalCount;
    }

    /**
     * Legt den Wert der totalCount-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setTotalCount(Integer value) {
        this.totalCount = value;
    }

}
