//
// Diese Datei wurde mit der JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.5-2 generiert 
// Siehe <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// �nderungen an dieser Datei gehen bei einer Neukompilierung des Quellschemas verloren. 
// Generiert: 2021.04.27 um 01:51:58 PM CEST 
//


package clamlBindingXSD;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java-Klasse f�r anonymous complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{}Caption" minOccurs="0"/>
 *         &lt;element ref="{}THead" minOccurs="0"/>
 *         &lt;element ref="{}TBody" minOccurs="0"/>
 *         &lt;element ref="{}TFoot" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="class" type="{http://www.w3.org/2001/XMLSchema}string" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "caption",
    "tHead",
    "tBody",
    "tFoot"
})
@XmlRootElement(name = "Table")
public class Table {

    @XmlElement(name = "Caption")
    protected Caption caption;
    @XmlElement(name = "THead")
    protected THead tHead;
    @XmlElement(name = "TBody")
    protected TBody tBody;
    @XmlElement(name = "TFoot")
    protected TFoot tFoot;
    @XmlAttribute(name = "class")
    protected String clazz;

    /**
     * Ruft den Wert der caption-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Caption }
     *     
     */
    public Caption getCaption() {
        return caption;
    }

    /**
     * Legt den Wert der caption-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Caption }
     *     
     */
    public void setCaption(Caption value) {
        this.caption = value;
    }

    /**
     * Ruft den Wert der tHead-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link THead }
     *     
     */
    public THead getTHead() {
        return tHead;
    }

    /**
     * Legt den Wert der tHead-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link THead }
     *     
     */
    public void setTHead(THead value) {
        this.tHead = value;
    }

    /**
     * Ruft den Wert der tBody-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link TBody }
     *     
     */
    public TBody getTBody() {
        return tBody;
    }

    /**
     * Legt den Wert der tBody-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link TBody }
     *     
     */
    public void setTBody(TBody value) {
        this.tBody = value;
    }

    /**
     * Ruft den Wert der tFoot-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link TFoot }
     *     
     */
    public TFoot getTFoot() {
        return tFoot;
    }

    /**
     * Legt den Wert der tFoot-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link TFoot }
     *     
     */
    public void setTFoot(TFoot value) {
        this.tFoot = value;
    }

    /**
     * Ruft den Wert der clazz-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getClazz() {
        return clazz;
    }

    /**
     * Legt den Wert der clazz-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setClazz(String value) {
        this.clazz = value;
    }

}
