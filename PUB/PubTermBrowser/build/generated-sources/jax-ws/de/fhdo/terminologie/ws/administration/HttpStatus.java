
package de.fhdo.terminologie.ws.administration;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java-Klasse für httpStatus.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * <p>
 * <pre>
 * &lt;simpleType name="httpStatus">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="HTTP403"/>
 *     &lt;enumeration value="HTTP409"/>
 *     &lt;enumeration value="HTTP500"/>
 *     &lt;enumeration value="HTTP503"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "httpStatus")
@XmlEnum
public enum HttpStatus {

    @XmlEnumValue("HTTP403")
    HTTP_403("HTTP403"),
    @XmlEnumValue("HTTP409")
    HTTP_409("HTTP409"),
    @XmlEnumValue("HTTP500")
    HTTP_500("HTTP500"),
    @XmlEnumValue("HTTP503")
    HTTP_503("HTTP503");
    private final String value;

    HttpStatus(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static HttpStatus fromValue(String v) {
        for (HttpStatus c: HttpStatus.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
