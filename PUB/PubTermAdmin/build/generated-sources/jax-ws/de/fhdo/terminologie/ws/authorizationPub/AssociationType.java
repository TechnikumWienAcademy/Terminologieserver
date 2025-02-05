
package de.fhdo.terminologie.ws.authorizationPub;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java-Klasse f�r associationType complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="associationType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="codeSystemEntityVersion" type="{de.fhdo.termserver.types}codeSystemEntityVersion" minOccurs="0"/>
 *         &lt;element name="codeSystemEntityVersionAssociations" type="{de.fhdo.termserver.types}codeSystemEntityVersionAssociation" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="codeSystemEntityVersionId" type="{http://www.w3.org/2001/XMLSchema}long" minOccurs="0"/>
 *         &lt;element name="forwardName" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="reverseName" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "associationType", propOrder = {
    "codeSystemEntityVersion",
    "codeSystemEntityVersionAssociations",
    "codeSystemEntityVersionId",
    "forwardName",
    "reverseName"
})
public class AssociationType {

    protected CodeSystemEntityVersion codeSystemEntityVersion;
    @XmlElement(nillable = true)
    protected List<CodeSystemEntityVersionAssociation> codeSystemEntityVersionAssociations;
    protected Long codeSystemEntityVersionId;
    protected String forwardName;
    protected String reverseName;

    /**
     * Ruft den Wert der codeSystemEntityVersion-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link CodeSystemEntityVersion }
     *     
     */
    public CodeSystemEntityVersion getCodeSystemEntityVersion() {
        return codeSystemEntityVersion;
    }

    /**
     * Legt den Wert der codeSystemEntityVersion-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link CodeSystemEntityVersion }
     *     
     */
    public void setCodeSystemEntityVersion(CodeSystemEntityVersion value) {
        this.codeSystemEntityVersion = value;
    }

    /**
     * Gets the value of the codeSystemEntityVersionAssociations property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the codeSystemEntityVersionAssociations property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getCodeSystemEntityVersionAssociations().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link CodeSystemEntityVersionAssociation }
     * 
     * 
     */
    public List<CodeSystemEntityVersionAssociation> getCodeSystemEntityVersionAssociations() {
        if (codeSystemEntityVersionAssociations == null) {
            codeSystemEntityVersionAssociations = new ArrayList<CodeSystemEntityVersionAssociation>();
        }
        return this.codeSystemEntityVersionAssociations;
    }

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
     * Ruft den Wert der forwardName-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getForwardName() {
        return forwardName;
    }

    /**
     * Legt den Wert der forwardName-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setForwardName(String value) {
        this.forwardName = value;
    }

    /**
     * Ruft den Wert der reverseName-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getReverseName() {
        return reverseName;
    }

    /**
     * Legt den Wert der reverseName-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setReverseName(String value) {
        this.reverseName = value;
    }

}
