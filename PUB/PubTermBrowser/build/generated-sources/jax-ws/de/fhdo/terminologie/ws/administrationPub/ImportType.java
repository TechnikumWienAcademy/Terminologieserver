
package de.fhdo.terminologie.ws.administrationPub;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java-Klasse für importType complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="importType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="fileContentList" type="{http://administration.ws.terminologie.fhdo.de/}filecontentListEntry" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="filecontent" type="{http://www.w3.org/2001/XMLSchema}base64Binary" minOccurs="0"/>
 *         &lt;element name="formatId" type="{http://www.w3.org/2001/XMLSchema}long" minOccurs="0"/>
 *         &lt;element name="order" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="role" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "importType", propOrder = {
    "fileContentList",
    "filecontent",
    "formatId",
    "order",
    "role"
})
public class ImportType {

    @XmlElement(nillable = true)
    protected List<FilecontentListEntry> fileContentList;
    protected byte[] filecontent;
    protected Long formatId;
    protected Boolean order;
    protected String role;

    /**
     * Gets the value of the fileContentList property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the fileContentList property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getFileContentList().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link FilecontentListEntry }
     * 
     * 
     */
    public List<FilecontentListEntry> getFileContentList() {
        if (fileContentList == null) {
            fileContentList = new ArrayList<FilecontentListEntry>();
        }
        return this.fileContentList;
    }

    /**
     * Ruft den Wert der filecontent-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     byte[]
     */
    public byte[] getFilecontent() {
        return filecontent;
    }

    /**
     * Legt den Wert der filecontent-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     byte[]
     */
    public void setFilecontent(byte[] value) {
        this.filecontent = value;
    }

    /**
     * Ruft den Wert der formatId-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public Long getFormatId() {
        return formatId;
    }

    /**
     * Legt den Wert der formatId-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setFormatId(Long value) {
        this.formatId = value;
    }

    /**
     * Ruft den Wert der order-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isOrder() {
        return order;
    }

    /**
     * Legt den Wert der order-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setOrder(Boolean value) {
        this.order = value;
    }

    /**
     * Ruft den Wert der role-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRole() {
        return role;
    }

    /**
     * Legt den Wert der role-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRole(String value) {
        this.role = value;
    }

}
