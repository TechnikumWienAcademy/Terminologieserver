
package de.fhdo.terminologie.ws.idp.usermanagement;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;
import types.idp.termserver.fhdo.de.Collaborationuser;
import types.idp.termserver.fhdo.de.Organisation;
import types.idp.termserver.fhdo.de.Role;
import types.idp.termserver.fhdo.de.TermUser;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the de.fhdo.terminologie.ws.idp.usermanagement package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {

    private final static QName _Role_QNAME = new QName("http://userManagement.idp.ws.terminologie.fhdo.de/", "role");
    private final static QName _SaveTermUser_QNAME = new QName("http://userManagement.idp.ws.terminologie.fhdo.de/", "saveTermUser");
    private final static QName _GetCollaborationUserListResponse_QNAME = new QName("http://userManagement.idp.ws.terminologie.fhdo.de/", "getCollaborationUserListResponse");
    private final static QName _GetUserListResponse_QNAME = new QName("http://userManagement.idp.ws.terminologie.fhdo.de/", "getUserListResponse");
    private final static QName _SaveTermUserResponse_QNAME = new QName("http://userManagement.idp.ws.terminologie.fhdo.de/", "saveTermUserResponse");
    private final static QName _Organisation_QNAME = new QName("http://userManagement.idp.ws.terminologie.fhdo.de/", "organisation");
    private final static QName _GetUserList_QNAME = new QName("http://userManagement.idp.ws.terminologie.fhdo.de/", "getUserList");
    private final static QName _SaveCollaborationUser_QNAME = new QName("http://userManagement.idp.ws.terminologie.fhdo.de/", "saveCollaborationUser");
    private final static QName _GetCollaborationUserList_QNAME = new QName("http://userManagement.idp.ws.terminologie.fhdo.de/", "getCollaborationUserList");
    private final static QName _SaveCollaborationUserResponse_QNAME = new QName("http://userManagement.idp.ws.terminologie.fhdo.de/", "saveCollaborationUserResponse");
    private final static QName _DeleteUser_QNAME = new QName("http://userManagement.idp.ws.terminologie.fhdo.de/", "deleteUser");
    private final static QName _TermUser_QNAME = new QName("http://userManagement.idp.ws.terminologie.fhdo.de/", "termUser");
    private final static QName _Collaborationuser_QNAME = new QName("http://userManagement.idp.ws.terminologie.fhdo.de/", "collaborationuser");
    private final static QName _DeleteUserResponse_QNAME = new QName("http://userManagement.idp.ws.terminologie.fhdo.de/", "deleteUserResponse");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: de.fhdo.terminologie.ws.idp.usermanagement
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link GetUserListResponse }
     * 
     */
    public GetUserListResponse createGetUserListResponse() {
        return new GetUserListResponse();
    }

    /**
     * Create an instance of {@link GetCollaborationUserListResponse }
     * 
     */
    public GetCollaborationUserListResponse createGetCollaborationUserListResponse() {
        return new GetCollaborationUserListResponse();
    }

    /**
     * Create an instance of {@link GetCollaborationUserResponseType }
     * 
     */
    public GetCollaborationUserResponseType createGetCollaborationUserResponseType() {
        return new GetCollaborationUserResponseType();
    }

    /**
     * Create an instance of {@link ReturnType }
     * 
     */
    public ReturnType createReturnType() {
        return new ReturnType();
    }

    /**
     * Create an instance of {@link SaveTermUser }
     * 
     */
    public SaveTermUser createSaveTermUser() {
        return new SaveTermUser();
    }

    /**
     * Create an instance of {@link SaveTermUserResponse }
     * 
     */
    public SaveTermUserResponse createSaveTermUserResponse() {
        return new SaveTermUserResponse();
    }

    /**
     * Create an instance of {@link GetUserList }
     * 
     */
    public GetUserList createGetUserList() {
        return new GetUserList();
    }

    /**
     * Create an instance of {@link SaveCollaborationUser }
     * 
     */
    public SaveCollaborationUser createSaveCollaborationUser() {
        return new SaveCollaborationUser();
    }

    /**
     * Create an instance of {@link GetCollaborationUserList }
     * 
     */
    public GetCollaborationUserList createGetCollaborationUserList() {
        return new GetCollaborationUserList();
    }

    /**
     * Create an instance of {@link SaveCollaborationUserResponse }
     * 
     */
    public SaveCollaborationUserResponse createSaveCollaborationUserResponse() {
        return new SaveCollaborationUserResponse();
    }

    /**
     * Create an instance of {@link DeleteUser }
     * 
     */
    public DeleteUser createDeleteUser() {
        return new DeleteUser();
    }

    /**
     * Create an instance of {@link GetTermUserResponseType }
     * 
     */
    public GetTermUserResponseType createGetTermUserResponseType() {
        return new GetTermUserResponseType();
    }

    /**
     * Create an instance of {@link DeleteUserResponse }
     * 
     */
    public DeleteUserResponse createDeleteUserResponse() {
        return new DeleteUserResponse();
    }

    /**
     * Create an instance of {@link LoginType }
     * 
     */
    public LoginType createLoginType() {
        return new LoginType();
    }

    /**
     * Create an instance of {@link SaveTermUserResponseType }
     * 
     */
    public SaveTermUserResponseType createSaveTermUserResponseType() {
        return new SaveTermUserResponseType();
    }

    /**
     * Create an instance of {@link GetCollaborationUserRequestType }
     * 
     */
    public GetCollaborationUserRequestType createGetCollaborationUserRequestType() {
        return new GetCollaborationUserRequestType();
    }

    /**
     * Create an instance of {@link GetTermUserRequestType }
     * 
     */
    public GetTermUserRequestType createGetTermUserRequestType() {
        return new GetTermUserRequestType();
    }

    /**
     * Create an instance of {@link DeleteUserResponseType }
     * 
     */
    public DeleteUserResponseType createDeleteUserResponseType() {
        return new DeleteUserResponseType();
    }

    /**
     * Create an instance of {@link SaveTermUserRequestType }
     * 
     */
    public SaveTermUserRequestType createSaveTermUserRequestType() {
        return new SaveTermUserRequestType();
    }

    /**
     * Create an instance of {@link SaveCollaborationUserRequestType }
     * 
     */
    public SaveCollaborationUserRequestType createSaveCollaborationUserRequestType() {
        return new SaveCollaborationUserRequestType();
    }

    /**
     * Create an instance of {@link SaveCollaborationUserResponseType }
     * 
     */
    public SaveCollaborationUserResponseType createSaveCollaborationUserResponseType() {
        return new SaveCollaborationUserResponseType();
    }

    /**
     * Create an instance of {@link DeleteUserRequestType }
     * 
     */
    public DeleteUserRequestType createDeleteUserRequestType() {
        return new DeleteUserRequestType();
    }

    /**
     * Create an instance of {@link GetUserListResponse.Return }
     * 
     */
    public GetUserListResponse.Return createGetUserListResponseReturn() {
        return new GetUserListResponse.Return();
    }

    /**
     * Create an instance of {@link GetCollaborationUserListResponse.Return }
     * 
     */
    public GetCollaborationUserListResponse.Return createGetCollaborationUserListResponseReturn() {
        return new GetCollaborationUserListResponse.Return();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Role }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://userManagement.idp.ws.terminologie.fhdo.de/", name = "role")
    public JAXBElement<Role> createRole(Role value) {
        return new JAXBElement<Role>(_Role_QNAME, Role.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link SaveTermUser }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://userManagement.idp.ws.terminologie.fhdo.de/", name = "saveTermUser")
    public JAXBElement<SaveTermUser> createSaveTermUser(SaveTermUser value) {
        return new JAXBElement<SaveTermUser>(_SaveTermUser_QNAME, SaveTermUser.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetCollaborationUserListResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://userManagement.idp.ws.terminologie.fhdo.de/", name = "getCollaborationUserListResponse")
    public JAXBElement<GetCollaborationUserListResponse> createGetCollaborationUserListResponse(GetCollaborationUserListResponse value) {
        return new JAXBElement<GetCollaborationUserListResponse>(_GetCollaborationUserListResponse_QNAME, GetCollaborationUserListResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetUserListResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://userManagement.idp.ws.terminologie.fhdo.de/", name = "getUserListResponse")
    public JAXBElement<GetUserListResponse> createGetUserListResponse(GetUserListResponse value) {
        return new JAXBElement<GetUserListResponse>(_GetUserListResponse_QNAME, GetUserListResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link SaveTermUserResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://userManagement.idp.ws.terminologie.fhdo.de/", name = "saveTermUserResponse")
    public JAXBElement<SaveTermUserResponse> createSaveTermUserResponse(SaveTermUserResponse value) {
        return new JAXBElement<SaveTermUserResponse>(_SaveTermUserResponse_QNAME, SaveTermUserResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Organisation }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://userManagement.idp.ws.terminologie.fhdo.de/", name = "organisation")
    public JAXBElement<Organisation> createOrganisation(Organisation value) {
        return new JAXBElement<Organisation>(_Organisation_QNAME, Organisation.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetUserList }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://userManagement.idp.ws.terminologie.fhdo.de/", name = "getUserList")
    public JAXBElement<GetUserList> createGetUserList(GetUserList value) {
        return new JAXBElement<GetUserList>(_GetUserList_QNAME, GetUserList.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link SaveCollaborationUser }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://userManagement.idp.ws.terminologie.fhdo.de/", name = "saveCollaborationUser")
    public JAXBElement<SaveCollaborationUser> createSaveCollaborationUser(SaveCollaborationUser value) {
        return new JAXBElement<SaveCollaborationUser>(_SaveCollaborationUser_QNAME, SaveCollaborationUser.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetCollaborationUserList }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://userManagement.idp.ws.terminologie.fhdo.de/", name = "getCollaborationUserList")
    public JAXBElement<GetCollaborationUserList> createGetCollaborationUserList(GetCollaborationUserList value) {
        return new JAXBElement<GetCollaborationUserList>(_GetCollaborationUserList_QNAME, GetCollaborationUserList.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link SaveCollaborationUserResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://userManagement.idp.ws.terminologie.fhdo.de/", name = "saveCollaborationUserResponse")
    public JAXBElement<SaveCollaborationUserResponse> createSaveCollaborationUserResponse(SaveCollaborationUserResponse value) {
        return new JAXBElement<SaveCollaborationUserResponse>(_SaveCollaborationUserResponse_QNAME, SaveCollaborationUserResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link DeleteUser }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://userManagement.idp.ws.terminologie.fhdo.de/", name = "deleteUser")
    public JAXBElement<DeleteUser> createDeleteUser(DeleteUser value) {
        return new JAXBElement<DeleteUser>(_DeleteUser_QNAME, DeleteUser.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link TermUser }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://userManagement.idp.ws.terminologie.fhdo.de/", name = "termUser")
    public JAXBElement<TermUser> createTermUser(TermUser value) {
        return new JAXBElement<TermUser>(_TermUser_QNAME, TermUser.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Collaborationuser }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://userManagement.idp.ws.terminologie.fhdo.de/", name = "collaborationuser")
    public JAXBElement<Collaborationuser> createCollaborationuser(Collaborationuser value) {
        return new JAXBElement<Collaborationuser>(_Collaborationuser_QNAME, Collaborationuser.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link DeleteUserResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://userManagement.idp.ws.terminologie.fhdo.de/", name = "deleteUserResponse")
    public JAXBElement<DeleteUserResponse> createDeleteUserResponse(DeleteUserResponse value) {
        return new JAXBElement<DeleteUserResponse>(_DeleteUserResponse_QNAME, DeleteUserResponse.class, null, value);
    }

}
