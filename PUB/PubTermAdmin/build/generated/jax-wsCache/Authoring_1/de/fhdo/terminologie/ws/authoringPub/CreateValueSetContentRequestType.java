
package de.fhdo.terminologie.ws.authoringPub;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java-Klasse für createValueSetContentRequestType complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="createValueSetContentRequestType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="codeSystemEntity" type="{de.fhdo.termserver.types}codeSystemEntity" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="conceptValueSetMembership" type="{de.fhdo.termserver.types}conceptValueSetMembership" minOccurs="0"/>
 *         &lt;element name="login" type="{http://authoring.ws.terminologie.fhdo.de/}loginType" minOccurs="0"/>
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
@XmlType(name = "createValueSetContentRequestType", propOrder = {
    "codeSystemEntity",
    "conceptValueSetMembership",
    "login",
    "valueSet"
})
public class CreateValueSetContentRequestType {

    @XmlElement(nillable = true)
    protected List<CodeSystemEntity> codeSystemEntity;
    protected ConceptValueSetMembership conceptValueSetMembership;
    protected LoginType login;
    protected ValueSet valueSet;

    /**
     * Gets the value of the codeSystemEntity property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the codeSystemEntity property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getCodeSystemEntity().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link CodeSystemEntity }
     * 
     * 
     */
    public List<CodeSystemEntity> getCodeSystemEntity() {
        if (codeSystemEntity == null) {
            codeSystemEntity = new ArrayList<CodeSystemEntity>();
        }
        return this.codeSystemEntity;
    }

    /**
     * Ruft den Wert der conceptValueSetMembership-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link ConceptValueSetMembership }
     *     
     */
    public ConceptValueSetMembership getConceptValueSetMembership() {
        return conceptValueSetMembership;
    }

    /**
     * Legt den Wert der conceptValueSetMembership-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link ConceptValueSetMembership }
     *     
     */
    public void setConceptValueSetMembership(ConceptValueSetMembership value) {
        this.conceptValueSetMembership = value;
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
