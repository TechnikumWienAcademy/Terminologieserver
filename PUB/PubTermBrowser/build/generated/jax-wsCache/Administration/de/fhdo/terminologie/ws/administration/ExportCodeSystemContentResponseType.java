
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
 *         &lt;element name="exportInfos" type="{http://administration.ws.terminologie.fhdo.de/}exportType" minOccurs="0"/>
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
    "exportInfos"
})
@XmlRootElement(name = "exportCodeSystemContentResponseType")
public class ExportCodeSystemContentResponseType {

    protected ReturnType returnInfos;
    protected ExportType exportInfos;

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
     * Ruft den Wert der exportInfos-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link ExportType }
     *     
     */
    public ExportType getExportInfos() {
        return exportInfos;
    }

    /**
     * Legt den Wert der exportInfos-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link ExportType }
     *     
     */
    public void setExportInfos(ExportType value) {
        this.exportInfos = value;
    }

}
