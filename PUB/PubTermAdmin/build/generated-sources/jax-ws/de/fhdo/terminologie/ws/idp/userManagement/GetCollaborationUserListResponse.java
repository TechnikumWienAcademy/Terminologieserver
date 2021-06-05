
package de.fhdo.terminologie.ws.idp.userManagement;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java-Klasse für getCollaborationUserListResponse complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="getCollaborationUserListResponse">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="return" minOccurs="0">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="returnInfos" type="{http://userManagement.idp.ws.terminologie.fhdo.de/}returnType" minOccurs="0"/>
 *                   &lt;element name="userList" type="{de.fhdo.termserver.idp.types}collaborationuser" maxOccurs="unbounded" minOccurs="0"/>
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
@XmlType(name = "getCollaborationUserListResponse", propOrder = {
    "_return"
})
public class GetCollaborationUserListResponse {

    @XmlElement(name = "return")
    protected GetCollaborationUserListResponse.Return _return;

    /**
     * Ruft den Wert der return-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link GetCollaborationUserListResponse.Return }
     *     
     */
    public GetCollaborationUserListResponse.Return getReturn() {
        return _return;
    }

    /**
     * Legt den Wert der return-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link GetCollaborationUserListResponse.Return }
     *     
     */
    public void setReturn(GetCollaborationUserListResponse.Return value) {
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
     *         &lt;element name="returnInfos" type="{http://userManagement.idp.ws.terminologie.fhdo.de/}returnType" minOccurs="0"/>
     *         &lt;element name="userList" type="{de.fhdo.termserver.idp.types}collaborationuser" maxOccurs="unbounded" minOccurs="0"/>
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
        "userList"
    })
    public static class Return {

        protected ReturnType returnInfos;
        @XmlElement(nillable = true)
        protected List<Collaborationuser> userList;

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
         * Gets the value of the userList property.
         * 
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the userList property.
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getUserList().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link Collaborationuser }
         * 
         * 
         */
        public List<Collaborationuser> getUserList() {
            if (userList == null) {
                userList = new ArrayList<Collaborationuser>();
            }
            return this.userList;
        }

    }

}
