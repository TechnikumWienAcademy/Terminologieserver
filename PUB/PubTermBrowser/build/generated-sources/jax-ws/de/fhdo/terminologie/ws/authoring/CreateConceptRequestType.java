
package de.fhdo.terminologie.ws.authoring;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import types.termserver.fhdo.de.CodeSystem;
import types.termserver.fhdo.de.CodeSystemEntity;
import types.termserver.fhdo.de.Property;


/**
 * <p>Java-Klasse für createConceptRequestType complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="createConceptRequestType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="codeSystem" type="{de.fhdo.termserver.types}codeSystem" minOccurs="0"/>
 *         &lt;element name="codeSystemEntity" type="{de.fhdo.termserver.types}codeSystemEntity" minOccurs="0"/>
 *         &lt;element name="login" type="{http://authoring.ws.terminologie.fhdo.de/}loginType" minOccurs="0"/>
 *         &lt;element name="property" type="{de.fhdo.termserver.types}property" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "createConceptRequestType", propOrder = {
    "codeSystem",
    "codeSystemEntity",
    "login",
    "property"
})
public class CreateConceptRequestType {

    protected CodeSystem codeSystem;
    protected CodeSystemEntity codeSystemEntity;
    protected LoginType login;
    @XmlElement(nillable = true)
    protected List<Property> property;

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
     * Ruft den Wert der codeSystemEntity-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link CodeSystemEntity }
     *     
     */
    public CodeSystemEntity getCodeSystemEntity() {
        return codeSystemEntity;
    }

    /**
     * Legt den Wert der codeSystemEntity-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link CodeSystemEntity }
     *     
     */
    public void setCodeSystemEntity(CodeSystemEntity value) {
        this.codeSystemEntity = value;
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
     * Gets the value of the property property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the property property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getProperty().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Property }
     * 
     * 
     */
    public List<Property> getProperty() {
        if (property == null) {
            property = new ArrayList<Property>();
        }
        return this.property;
    }

}
