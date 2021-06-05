
package de.fhdo.terminologie.ws.authoring;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java-Klasse für versioningType complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="versioningType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="createNewVersion" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="majorUpdate" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="minorUpdate" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "versioningType", propOrder = {
    "createNewVersion",
    "majorUpdate",
    "minorUpdate"
})
public class VersioningType {

    protected Boolean createNewVersion;
    protected Boolean majorUpdate;
    protected Boolean minorUpdate;

    /**
     * Ruft den Wert der createNewVersion-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isCreateNewVersion() {
        return createNewVersion;
    }

    /**
     * Legt den Wert der createNewVersion-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setCreateNewVersion(Boolean value) {
        this.createNewVersion = value;
    }

    /**
     * Ruft den Wert der majorUpdate-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isMajorUpdate() {
        return majorUpdate;
    }

    /**
     * Legt den Wert der majorUpdate-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setMajorUpdate(Boolean value) {
        this.majorUpdate = value;
    }

    /**
     * Ruft den Wert der minorUpdate-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isMinorUpdate() {
        return minorUpdate;
    }

    /**
     * Legt den Wert der minorUpdate-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setMinorUpdate(Boolean value) {
        this.minorUpdate = value;
    }

}
