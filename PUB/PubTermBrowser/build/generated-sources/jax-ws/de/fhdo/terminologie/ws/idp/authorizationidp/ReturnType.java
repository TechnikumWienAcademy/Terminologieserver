
package de.fhdo.terminologie.ws.idp.authorizationidp;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.XMLGregorianCalendar;
import types.idp.termserver.fhdo.de.TermUser;


/**
 * <p>Java-Klasse für returnType complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="returnType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="count" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="lastIP" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="lastTimeStamp" type="{http://www.w3.org/2001/XMLSchema}dateTime" minOccurs="0"/>
 *         &lt;element name="message" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="overallErrorCategory" type="{http://authorizationIDP.idp.ws.terminologie.fhdo.de/}overallErrorCategory" minOccurs="0"/>
 *         &lt;element name="status" type="{http://authorizationIDP.idp.ws.terminologie.fhdo.de/}status" minOccurs="0"/>
 *         &lt;element name="termUser" type="{de.fhdo.termserver.idp.types}termUser" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "returnType", propOrder = {
    "count",
    "lastIP",
    "lastTimeStamp",
    "message",
    "overallErrorCategory",
    "status",
    "termUser"
})
public class ReturnType {

    protected int count;
    protected String lastIP;
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar lastTimeStamp;
    protected String message;
    protected OverallErrorCategory overallErrorCategory;
    protected Status status;
    protected TermUser termUser;

    /**
     * Ruft den Wert der count-Eigenschaft ab.
     * 
     */
    public int getCount() {
        return count;
    }

    /**
     * Legt den Wert der count-Eigenschaft fest.
     * 
     */
    public void setCount(int value) {
        this.count = value;
    }

    /**
     * Ruft den Wert der lastIP-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getLastIP() {
        return lastIP;
    }

    /**
     * Legt den Wert der lastIP-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLastIP(String value) {
        this.lastIP = value;
    }

    /**
     * Ruft den Wert der lastTimeStamp-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getLastTimeStamp() {
        return lastTimeStamp;
    }

    /**
     * Legt den Wert der lastTimeStamp-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setLastTimeStamp(XMLGregorianCalendar value) {
        this.lastTimeStamp = value;
    }

    /**
     * Ruft den Wert der message-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getMessage() {
        return message;
    }

    /**
     * Legt den Wert der message-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMessage(String value) {
        this.message = value;
    }

    /**
     * Ruft den Wert der overallErrorCategory-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link OverallErrorCategory }
     *     
     */
    public OverallErrorCategory getOverallErrorCategory() {
        return overallErrorCategory;
    }

    /**
     * Legt den Wert der overallErrorCategory-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link OverallErrorCategory }
     *     
     */
    public void setOverallErrorCategory(OverallErrorCategory value) {
        this.overallErrorCategory = value;
    }

    /**
     * Ruft den Wert der status-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Status }
     *     
     */
    public Status getStatus() {
        return status;
    }

    /**
     * Legt den Wert der status-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Status }
     *     
     */
    public void setStatus(Status value) {
        this.status = value;
    }

    /**
     * Ruft den Wert der termUser-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link TermUser }
     *     
     */
    public TermUser getTermUser() {
        return termUser;
    }

    /**
     * Legt den Wert der termUser-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link TermUser }
     *     
     */
    public void setTermUser(TermUser value) {
        this.termUser = value;
    }

}
