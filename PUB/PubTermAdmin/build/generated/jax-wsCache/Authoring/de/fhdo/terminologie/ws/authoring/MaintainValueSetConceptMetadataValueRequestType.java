
package de.fhdo.terminologie.ws.authoring;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import types.termserver.fhdo.de.ValueSetMetadataValue;


/**
 * <p>Java-Klasse für maintainValueSetConceptMetadataValueRequestType complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="maintainValueSetConceptMetadataValueRequestType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="login" type="{http://authoring.ws.terminologie.fhdo.de/}loginType" minOccurs="0"/>
 *         &lt;element name="valueSetMetadataValues" type="{de.fhdo.termserver.types}valueSetMetadataValue" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "maintainValueSetConceptMetadataValueRequestType", propOrder = {
    "login",
    "valueSetMetadataValues"
})
public class MaintainValueSetConceptMetadataValueRequestType {

    protected LoginType login;
    @XmlElement(nillable = true)
    protected List<ValueSetMetadataValue> valueSetMetadataValues;

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
     * Gets the value of the valueSetMetadataValues property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the valueSetMetadataValues property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getValueSetMetadataValues().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link ValueSetMetadataValue }
     * 
     * 
     */
    public List<ValueSetMetadataValue> getValueSetMetadataValues() {
        if (valueSetMetadataValues == null) {
            valueSetMetadataValues = new ArrayList<ValueSetMetadataValue>();
        }
        return this.valueSetMetadataValues;
    }

}
