//
// Diese Datei wurde mit der JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.5-2 generiert 
// Siehe <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// �nderungen an dieser Datei gehen bei einer Neukompilierung des Quellschemas verloren. 
// Generiert: 2021.04.27 um 01:50:29 PM CEST 
//


package clamlXSD;

import java.util.ArrayList;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlElementRefs;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlMixed;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
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
 *         &lt;choice>
 *           &lt;element ref="{}Reference" maxOccurs="unbounded" minOccurs="0"/>
 *           &lt;element ref="{}Term" maxOccurs="unbounded" minOccurs="0"/>
 *           &lt;element ref="{}Para" maxOccurs="unbounded" minOccurs="0"/>
 *           &lt;element ref="{}Include" maxOccurs="unbounded" minOccurs="0"/>
 *           &lt;element ref="{}IncludeDescendants" maxOccurs="unbounded" minOccurs="0"/>
 *           &lt;element ref="{}Fragment" maxOccurs="unbounded" minOccurs="0"/>
 *           &lt;element ref="{}List" maxOccurs="unbounded" minOccurs="0"/>
 *           &lt;element ref="{}Table" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;/choice>
 *       &lt;/sequence>
 *       &lt;attribute name="variants" type="{http://www.w3.org/2001/XMLSchema}IDREFS" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "content"
})
@XmlRootElement(name = "Label")
public class Label {

    @XmlElementRefs({
        @XmlElementRef(name = "IncludeDescendants", type = IncludeDescendants.class, required = false),
        @XmlElementRef(name = "Table", type = Table.class, required = false),
        @XmlElementRef(name = "Fragment", type = Fragment.class, required = false),
        @XmlElementRef(name = "Para", type = Para.class, required = false),
        @XmlElementRef(name = "List", type = clamlXSD.List.class, required = false),
        @XmlElementRef(name = "Reference", type = Reference.class, required = false),
        @XmlElementRef(name = "Include", type = Include.class, required = false),
        @XmlElementRef(name = "Term", type = Term.class, required = false)
    })
    @XmlMixed
    protected java.util.List<Object> content;
    @XmlAttribute(name = "variants")
    @XmlIDREF
    @XmlSchemaType(name = "IDREFS")
    protected java.util.List<Object> variants;

    /**
     * Gets the value of the content property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the content property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getContent().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link IncludeDescendants }
     * {@link Table }
     * {@link Fragment }
     * {@link String }
     * {@link Para }
     * {@link clamlXSD.List }
     * {@link Reference }
     * {@link Include }
     * {@link Term }
     * 
     * 
     */
    public java.util.List<Object> getContent() {
        if (content == null) {
            content = new ArrayList<Object>();
        }
        return this.content;
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
    public java.util.List<Object> getVariants() {
        if (variants == null) {
            variants = new ArrayList<Object>();
        }
        return this.variants;
    }

}
