
package de.fhdo.terminologie.ws.conceptassociation;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java-Klasse für ListConceptAssociations complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="ListConceptAssociations">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="parameter" type="{http://conceptAssociation.ws.terminologie.fhdo.de/}listConceptAssociationsRequestType" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ListConceptAssociations", propOrder = {
    "parameter"
})
public class ListConceptAssociations {

    protected ListConceptAssociationsRequestType parameter;

    /**
     * Ruft den Wert der parameter-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link ListConceptAssociationsRequestType }
     *     
     */
    public ListConceptAssociationsRequestType getParameter() {
        return parameter;
    }

    /**
     * Legt den Wert der parameter-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link ListConceptAssociationsRequestType }
     *     
     */
    public void setParameter(ListConceptAssociationsRequestType value) {
        this.parameter = value;
    }

}
