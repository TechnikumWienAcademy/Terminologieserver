
package de.fhdo.terminologie.ws.idp.userManagement;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java-Klasse für organisation complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="organisation">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="collaborationusers" type="{de.fhdo.termserver.idp.types}collaborationuser" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="id" type="{http://www.w3.org/2001/XMLSchema}long" minOccurs="0"/>
 *         &lt;element name="organisation" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="organisationAbbr" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="organisationLink" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "organisation", namespace = "de.fhdo.termserver.idp.types", propOrder = {
    "collaborationusers",
    "id",
    "organisation",
    "organisationAbbr",
    "organisationLink"
})
public class Organisation {

    @XmlElement(nillable = true)
    protected List<Collaborationuser> collaborationusers;
    protected Long id;
    protected String organisation;
    protected String organisationAbbr;
    protected String organisationLink;

    /**
     * Gets the value of the collaborationusers property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the collaborationusers property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getCollaborationusers().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Collaborationuser }
     * 
     * 
     */
    public List<Collaborationuser> getCollaborationusers() {
        if (collaborationusers == null) {
            collaborationusers = new ArrayList<Collaborationuser>();
        }
        return this.collaborationusers;
    }

    /**
     * Ruft den Wert der id-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public Long getId() {
        return id;
    }

    /**
     * Legt den Wert der id-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setId(Long value) {
        this.id = value;
    }

    /**
     * Ruft den Wert der organisation-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getOrganisation() {
        return organisation;
    }

    /**
     * Legt den Wert der organisation-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setOrganisation(String value) {
        this.organisation = value;
    }

    /**
     * Ruft den Wert der organisationAbbr-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getOrganisationAbbr() {
        return organisationAbbr;
    }

    /**
     * Legt den Wert der organisationAbbr-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setOrganisationAbbr(String value) {
        this.organisationAbbr = value;
    }

    /**
     * Ruft den Wert der organisationLink-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getOrganisationLink() {
        return organisationLink;
    }

    /**
     * Legt den Wert der organisationLink-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setOrganisationLink(String value) {
        this.organisationLink = value;
    }

}
