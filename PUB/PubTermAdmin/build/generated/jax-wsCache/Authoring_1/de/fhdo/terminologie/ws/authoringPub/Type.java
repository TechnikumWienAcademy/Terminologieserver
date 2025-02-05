
package de.fhdo.terminologie.ws.authoringPub;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java-Klasse f�r type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * <p>
 * <pre>
 * &lt;simpleType name="type">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="CODE_SYSTEM"/>
 *     &lt;enumeration value="CODE_SYSTEM_VERSION"/>
 *     &lt;enumeration value="VALUE_SET"/>
 *     &lt;enumeration value="VALUE_SET_VERSION"/>
 *     &lt;enumeration value="CODE_SYSTEM_ENTITY_VERSION"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "type")
@XmlEnum
public enum Type {

    CODE_SYSTEM,
    CODE_SYSTEM_VERSION,
    VALUE_SET,
    VALUE_SET_VERSION,
    CODE_SYSTEM_ENTITY_VERSION;

    public String value() {
        return name();
    }

    public static Type fromValue(String v) {
        return valueOf(v);
    }

}
