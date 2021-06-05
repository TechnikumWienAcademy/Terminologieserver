
package de.fhdo.terminologie.ws.administration;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java-Klasse für exportType complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="exportType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="filecontent" type="{http://www.w3.org/2001/XMLSchema}base64Binary" minOccurs="0"/>
 *         &lt;element name="formatId" type="{http://www.w3.org/2001/XMLSchema}long" minOccurs="0"/>
 *         &lt;element name="updateCheck" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *         &lt;element name="url" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "exportType", propOrder = {
    "filecontent",
    "formatId",
    "updateCheck",
    "url"
})
public class ExportType {

    protected byte[] filecontent;
    protected Long formatId;
    protected boolean updateCheck;
    protected String url;

    /**
     * Ruft den Wert der filecontent-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     byte[]
     */
    public byte[] getFilecontent() {
        return filecontent;
    }

    /**
     * Legt den Wert der filecontent-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     byte[]
     */
    public void setFilecontent(byte[] value) {
        this.filecontent = value;
    }

    /**
     * Ruft den Wert der formatId-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public Long getFormatId() {
        return formatId;
    }

    /**
     * Legt den Wert der formatId-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setFormatId(Long value) {
        this.formatId = value;
    }

    /**
     * Ruft den Wert der updateCheck-Eigenschaft ab.
     * 
     */
    public boolean isUpdateCheck() {
        return updateCheck;
    }

    /**
     * Legt den Wert der updateCheck-Eigenschaft fest.
     * 
     */
    public void setUpdateCheck(boolean value) {
        this.updateCheck = value;
    }

    /**
     * Ruft den Wert der url-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getUrl() {
        return url;
    }

    /**
     * Legt den Wert der url-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setUrl(String value) {
        this.url = value;
    }

}
