
package de.fhdo.terminologie.ws.administration;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;
import types.termserver.fhdo.de.ValueSet;


/**
 * <p>Java-Klasse f�r exportValueSetContentRequestType complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="exportValueSetContentRequestType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="exportInfos" type="{http://administration.ws.terminologie.fhdo.de/}exportType" minOccurs="0"/>
 *         &lt;element name="exportParameter" type="{http://administration.ws.terminologie.fhdo.de/}exportParameterType" minOccurs="0"/>
 *         &lt;element name="login" type="{http://administration.ws.terminologie.fhdo.de/}loginType" minOccurs="0"/>
 *         &lt;element name="loginAlreadyChecked" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *         &lt;element name="valueSet" type="{de.fhdo.termserver.types}valueSet" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "exportValueSetContentRequestType", propOrder = {
    "exportInfos",
    "exportParameter",
    "login",
    "loginAlreadyChecked",
    "valueSet"
})
public class ExportValueSetContentRequestType {

    protected ExportType exportInfos;
    protected ExportParameterType exportParameter;
    protected LoginType login;
    protected boolean loginAlreadyChecked;
    protected ValueSet valueSet;

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

    /**
     * Ruft den Wert der exportParameter-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link ExportParameterType }
     *     
     */
    public ExportParameterType getExportParameter() {
        return exportParameter;
    }

    /**
     * Legt den Wert der exportParameter-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link ExportParameterType }
     *     
     */
    public void setExportParameter(ExportParameterType value) {
        this.exportParameter = value;
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
     * Ruft den Wert der loginAlreadyChecked-Eigenschaft ab.
     * 
     */
    public boolean isLoginAlreadyChecked() {
        return loginAlreadyChecked;
    }

    /**
     * Legt den Wert der loginAlreadyChecked-Eigenschaft fest.
     * 
     */
    public void setLoginAlreadyChecked(boolean value) {
        this.loginAlreadyChecked = value;
    }

    /**
     * Ruft den Wert der valueSet-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link ValueSet }
     *     
     */
    public ValueSet getValueSet() {
        return valueSet;
    }

    /**
     * Legt den Wert der valueSet-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link ValueSet }
     *     
     */
    public void setValueSet(ValueSet value) {
        this.valueSet = value;
    }

}
