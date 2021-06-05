
package de.fhdo.terminologie.ws.authorizationPub;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java-Klasse für checkLoginResponse complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="checkLoginResponse">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="return" minOccurs="0">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="returnInfos" type="{http://authorization.ws.terminologie.fhdo.de/}returnType" minOccurs="0"/>
 *                   &lt;element name="login" type="{http://authorization.ws.terminologie.fhdo.de/}loginType" minOccurs="0"/>
 *                   &lt;element name="termUser" type="{de.fhdo.termserver.types}termUser" minOccurs="0"/>
 *                 &lt;/sequence>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "checkLoginResponse", namespace = "http://authorization.ws.terminologie.fhdo.de/", propOrder = {
    "_return"
})
public class CheckLoginResponse {

    @XmlElement(name = "return")
    protected CheckLoginResponse.Return _return;

    /**
     * Ruft den Wert der return-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link CheckLoginResponse.Return }
     *     
     */
    public CheckLoginResponse.Return getReturn() {
        return _return;
    }

    /**
     * Legt den Wert der return-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link CheckLoginResponse.Return }
     *     
     */
    public void setReturn(CheckLoginResponse.Return value) {
        this._return = value;
    }


    /**
     * <p>Java-Klasse für anonymous complex type.
     * 
     * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
     * 
     * <pre>
     * &lt;complexType>
     *   &lt;complexContent>
     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *       &lt;sequence>
     *         &lt;element name="returnInfos" type="{http://authorization.ws.terminologie.fhdo.de/}returnType" minOccurs="0"/>
     *         &lt;element name="login" type="{http://authorization.ws.terminologie.fhdo.de/}loginType" minOccurs="0"/>
     *         &lt;element name="termUser" type="{de.fhdo.termserver.types}termUser" minOccurs="0"/>
     *       &lt;/sequence>
     *     &lt;/restriction>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "returnInfos",
        "login",
        "termUser"
    })
    public static class Return {

        protected ReturnType returnInfos;
        protected LoginType login;
        protected TermUser termUser;

        /**
         * Ruft den Wert der returnInfos-Eigenschaft ab.
         * 
         * @return
         *     possible object is
         *     {@link ReturnType }
         *     
         */
        public ReturnType getReturnInfos() {
            return returnInfos;
        }

        /**
         * Legt den Wert der returnInfos-Eigenschaft fest.
         * 
         * @param value
         *     allowed object is
         *     {@link ReturnType }
         *     
         */
        public void setReturnInfos(ReturnType value) {
            this.returnInfos = value;
        }

        /**
         * Ruft den Wert der login-Eigenschaft ab.
         * 
         * @return
         *     possible object is
         *     {@link LoginType }
         *     
         */
        public LoginType getLogin() {
            return login;
        }

        /**
         * Legt den Wert der login-Eigenschaft fest.
         * 
         * @param value
         *     allowed object is
         *     {@link LoginType }
         *     
         */
        public void setLogin(LoginType value) {
            this.login = value;
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

}
