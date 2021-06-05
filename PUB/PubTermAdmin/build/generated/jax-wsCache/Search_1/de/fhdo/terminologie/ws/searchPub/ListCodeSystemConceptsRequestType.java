
package de.fhdo.terminologie.ws.searchPub;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java-Klasse für listCodeSystemConceptsRequestType complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="listCodeSystemConceptsRequestType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="login" type="{http://search.ws.terminologie.fhdo.de/}loginType" minOccurs="0"/>
 *         &lt;element name="codeSystem" type="{de.fhdo.termserver.types}codeSystem"/>
 *         &lt;element name="codeSystemEntity" type="{de.fhdo.termserver.types}codeSystemEntity" minOccurs="0"/>
 *         &lt;element name="searchParameter" type="{http://search.ws.terminologie.fhdo.de/}searchType" minOccurs="0"/>
 *         &lt;element name="pagingParameter" type="{http://search.ws.terminologie.fhdo.de/}pagingType" minOccurs="0"/>
 *         &lt;element name="lookForward" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *         &lt;element name="sortingParameter" type="{http://search.ws.terminologie.fhdo.de/}sortingType" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "listCodeSystemConceptsRequestType", propOrder = {
    "login",
    "codeSystem",
    "codeSystemEntity",
    "searchParameter",
    "pagingParameter",
    "lookForward",
    "sortingParameter"
})
public class ListCodeSystemConceptsRequestType {

    protected LoginType login;
    @XmlElement(required = true)
    protected CodeSystem codeSystem;
    protected CodeSystemEntity codeSystemEntity;
    protected SearchType searchParameter;
    protected PagingType pagingParameter;
    protected boolean lookForward;
    protected SortingType sortingParameter;

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
     * Ruft den Wert der codeSystem-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link CodeSystem }
     *     
     */
    public CodeSystem getCodeSystem() {
        return codeSystem;
    }

    /**
     * Legt den Wert der codeSystem-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link CodeSystem }
     *     
     */
    public void setCodeSystem(CodeSystem value) {
        this.codeSystem = value;
    }

    /**
     * Ruft den Wert der codeSystemEntity-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link CodeSystemEntity }
     *     
     */
    public CodeSystemEntity getCodeSystemEntity() {
        return codeSystemEntity;
    }

    /**
     * Legt den Wert der codeSystemEntity-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link CodeSystemEntity }
     *     
     */
    public void setCodeSystemEntity(CodeSystemEntity value) {
        this.codeSystemEntity = value;
    }

    /**
     * Ruft den Wert der searchParameter-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link SearchType }
     *     
     */
    public SearchType getSearchParameter() {
        return searchParameter;
    }

    /**
     * Legt den Wert der searchParameter-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link SearchType }
     *     
     */
    public void setSearchParameter(SearchType value) {
        this.searchParameter = value;
    }

    /**
     * Ruft den Wert der pagingParameter-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link PagingType }
     *     
     */
    public PagingType getPagingParameter() {
        return pagingParameter;
    }

    /**
     * Legt den Wert der pagingParameter-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link PagingType }
     *     
     */
    public void setPagingParameter(PagingType value) {
        this.pagingParameter = value;
    }

    /**
     * Ruft den Wert der lookForward-Eigenschaft ab.
     * 
     */
    public boolean isLookForward() {
        return lookForward;
    }

    /**
     * Legt den Wert der lookForward-Eigenschaft fest.
     * 
     */
    public void setLookForward(boolean value) {
        this.lookForward = value;
    }

    /**
     * Ruft den Wert der sortingParameter-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link SortingType }
     *     
     */
    public SortingType getSortingParameter() {
        return sortingParameter;
    }

    /**
     * Legt den Wert der sortingParameter-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link SortingType }
     *     
     */
    public void setSortingParameter(SortingType value) {
        this.sortingParameter = value;
    }

}
