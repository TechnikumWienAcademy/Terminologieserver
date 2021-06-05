
package de.fhdo.terminologie.ws.authoring;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import types.termserver.fhdo.de.CodeSystemMetadataValue;


/**
 * <p>Java-Klasse für maintainCodeSystemConceptMetadataValueRequestType complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="maintainCodeSystemConceptMetadataValueRequestType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="codeSystemMetadataValues" type="{de.fhdo.termserver.types}codeSystemMetadataValue" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="codeSystemVersionId" type="{http://www.w3.org/2001/XMLSchema}long" minOccurs="0"/>
 *         &lt;element name="login" type="{http://authoring.ws.terminologie.fhdo.de/}loginType" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "maintainCodeSystemConceptMetadataValueRequestType", propOrder = {
    "codeSystemMetadataValues",
    "codeSystemVersionId",
    "login"
})
public class MaintainCodeSystemConceptMetadataValueRequestType {

    @XmlElement(nillable = true)
    protected List<CodeSystemMetadataValue> codeSystemMetadataValues;
    protected Long codeSystemVersionId;
    protected LoginType login;

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

    /**
     * Ruft den Wert der codeSystemVersionId-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public Long getCodeSystemVersionId() {
        return codeSystemVersionId;
    }

    /**
     * Legt den Wert der codeSystemVersionId-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setCodeSystemVersionId(Long value) {
        this.codeSystemVersionId = value;
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
