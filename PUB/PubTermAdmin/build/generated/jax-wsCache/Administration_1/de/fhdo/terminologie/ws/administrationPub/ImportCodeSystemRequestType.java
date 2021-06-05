
package de.fhdo.terminologie.ws.administrationPub;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java-Klasse für importCodeSystemRequestType complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="importCodeSystemRequestType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="codeSystem" type="{de.fhdo.termserver.types}codeSystem" minOccurs="0"/>
 *         &lt;element name="importId" type="{http://www.w3.org/2001/XMLSchema}long" minOccurs="0"/>
 *         &lt;element name="importInfos" type="{http://administration.ws.terminologie.fhdo.de/}importType" minOccurs="0"/>
 *         &lt;element name="login" type="{http://administration.ws.terminologie.fhdo.de/}loginType" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "importCodeSystemRequestType", propOrder = {
    "codeSystem",
    "importId",
    "importInfos",
    "login"
})
public class ImportCodeSystemRequestType {

    protected CodeSystem codeSystem;
    protected Long importId;
    protected ImportType importInfos;
    protected LoginType login;

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

    /**
     * Ruft den Wert der importId-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public Long getImportId() {
        return importId;
    }

    /**
     * Legt den Wert der importId-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setImportId(Long value) {
        this.importId = value;
    }

    /**
     * Ruft den Wert der importInfos-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link ImportType }
     *     
     */
    public ImportType getImportInfos() {
        return importInfos;
    }

    /**
     * Legt den Wert der importInfos-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link ImportType }
     *     
     */
    public void setImportInfos(ImportType value) {
        this.importInfos = value;
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

}
