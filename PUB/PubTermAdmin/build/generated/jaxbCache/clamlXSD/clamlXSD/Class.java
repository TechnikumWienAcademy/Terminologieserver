//
// Diese Datei wurde mit der JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.5-2 generiert 
// Siehe <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// �nderungen an dieser Datei gehen bei einer Neukompilierung des Quellschemas verloren. 
// Generiert: 2021.04.27 um 01:50:29 PM CEST 
//


package clamlXSD;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


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
 *         &lt;element ref="{}Meta" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element ref="{}SuperClass" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element ref="{}SubClass" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element ref="{}ModifiedBy" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element ref="{}ExcludeModifier" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element ref="{}Rubric" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element ref="{}History" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="variants" type="{http://www.w3.org/2001/XMLSchema}IDREFS" />
 *       &lt;attribute name="kind" use="required" type="{http://www.w3.org/2001/XMLSchema}IDREF" />
 *       &lt;attribute name="code" use="required" type="{http://www.w3.org/2001/XMLSchema}NMTOKEN" />
 *       &lt;attribute name="usage" type="{http://www.w3.org/2001/XMLSchema}IDREF" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "meta",
    "superClass",
    "subClass",
    "modifiedBy",
    "excludeModifier",
    "rubric",
    "history"
})
@XmlRootElement(name = "Class")
public class Class {

    @XmlElement(name = "Meta")
    protected List<Meta> meta;
    @XmlElement(name = "SuperClass")
    protected List<SuperClass> superClass;
    @XmlElement(name = "SubClass")
    protected List<SubClass> subClass;
    @XmlElement(name = "ModifiedBy")
    protected List<ModifiedBy> modifiedBy;
    @XmlElement(name = "ExcludeModifier")
    protected List<ExcludeModifier> excludeModifier;
    @XmlElement(name = "Rubric")
    protected List<Rubric> rubric;
    @XmlElement(name = "History")
    protected List<History> history;
    @XmlAttribute(name = "variants")
    @XmlIDREF
    @XmlSchemaType(name = "IDREFS")
    protected List<Object> variants;
    @XmlAttribute(name = "kind", required = true)
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
    protected Object kind;
    @XmlAttribute(name = "code", required = true)
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "NMTOKEN")
    protected String code;
    @XmlAttribute(name = "usage")
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
    protected Object usage;

    /**
     * Gets the value of the meta property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the meta property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getMeta().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Meta }
     * 
     * 
     */
    public List<Meta> getMeta() {
        if (meta == null) {
            meta = new ArrayList<Meta>();
        }
        return this.meta;
    }

    /**
     * Gets the value of the superClass property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the superClass property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getSuperClass().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link SuperClass }
     * 
     * 
     */
    public List<SuperClass> getSuperClass() {
        if (superClass == null) {
            superClass = new ArrayList<SuperClass>();
        }
        return this.superClass;
    }

    /**
     * Gets the value of the subClass property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the subClass property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getSubClass().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link SubClass }
     * 
     * 
     */
    public List<SubClass> getSubClass() {
        if (subClass == null) {
            subClass = new ArrayList<SubClass>();
        }
        return this.subClass;
    }

    /**
     * Gets the value of the modifiedBy property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the modifiedBy property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getModifiedBy().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link ModifiedBy }
     * 
     * 
     */
    public List<ModifiedBy> getModifiedBy() {
        if (modifiedBy == null) {
            modifiedBy = new ArrayList<ModifiedBy>();
        }
        return this.modifiedBy;
    }

    /**
     * Gets the value of the excludeModifier property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the excludeModifier property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getExcludeModifier().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link ExcludeModifier }
     * 
     * 
     */
    public List<ExcludeModifier> getExcludeModifier() {
        if (excludeModifier == null) {
            excludeModifier = new ArrayList<ExcludeModifier>();
        }
        return this.excludeModifier;
    }

    /**
     * Gets the value of the rubric property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the rubric property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getRubric().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Rubric }
     * 
     * 
     */
    public List<Rubric> getRubric() {
        if (rubric == null) {
            rubric = new ArrayList<Rubric>();
        }
        return this.rubric;
    }

    /**
     * Gets the value of the history property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the history property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getHistory().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link History }
     * 
     * 
     */
    public List<History> getHistory() {
        if (history == null) {
            history = new ArrayList<History>();
        }
        return this.history;
    }

    /**
     * Gets the value of the variants property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the variants property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getVariants().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Object }
     * 
     * 
     */
    public List<Object> getVariants() {
        if (variants == null) {
            variants = new ArrayList<Object>();
        }
        return this.variants;
    }

    /**
     * Ruft den Wert der kind-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Object }
     *     
     */
    public Object getKind() {
        return kind;
    }

    /**
     * Legt den Wert der kind-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Object }
     *     
     */
    public void setKind(Object value) {
        this.kind = value;
    }

    /**
     * Ruft den Wert der code-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCode() {
        return code;
    }

    /**
     * Legt den Wert der code-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCode(String value) {
        this.code = value;
    }

    /**
     * Ruft den Wert der usage-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Object }
     *     
     */
    public Object getUsage() {
        return usage;
    }

    /**
     * Legt den Wert der usage-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Object }
     *     
     */
    public void setUsage(Object value) {
        this.usage = value;
    }

}
