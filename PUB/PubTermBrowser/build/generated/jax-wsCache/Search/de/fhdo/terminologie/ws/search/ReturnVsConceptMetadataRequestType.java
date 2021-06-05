
package de.fhdo.terminologie.ws.search;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java-Klasse für returnVsConceptMetadataRequestType complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="returnVsConceptMetadataRequestType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="codeSystemEntityVersionId" type="{http://www.w3.org/2001/XMLSchema}long" minOccurs="0"/>
 *         &lt;element name="login" type="{http://search.ws.terminologie.fhdo.de/}loginType" minOccurs="0"/>
 *         &lt;element name="valuesetVersionId" type="{http://www.w3.org/2001/XMLSchema}long" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "returnVsConceptMetadataRequestType", propOrder = {
    "codeSystemEntityVersionId",
    "login",
    "valuesetVersionId"
})
public class ReturnVsConceptMetadataRequestType {

    protected Long codeSystemEntityVersionId;
    protected LoginType login;
    protected Long valuesetVersionId;

    /**
     * Ruft den Wert der codeSystemEntityVersionId-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public Long getCodeSystemEntityVersionId() {
        return codeSystemEntityVersionId;
    }

    /**
     * Legt den Wert der codeSystemEntityVersionId-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setCodeSystemEntityVersionId(Long value) {
        this.codeSystemEntityVersionId = value;
    }

    /**
     * Ruft den Wert der login-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link LoginType }
     *     
     */
    public LoginType getLogin() {
        return login;
    }

    /**
     * Legt den Wert der login-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link LoginType }
     *     
     */
    public void setLogin(LoginType value) {
        this.login = value;
    }

    /**
     * Ruft den Wert der valuesetVersionId-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public Long getValuesetVersionId() {
        return valuesetVersionId;
    }

    /**
     * Legt den Wert der valuesetVersionId-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setValuesetVersionId(Long value) {
        this.valuesetVersionId = value;
    }

}
