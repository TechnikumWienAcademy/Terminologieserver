/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.fhdo.helper;

import de.fhdo.Definitions;
import de.fhdo.collaboration.db.classes.Collaborationuser;
import de.fhdo.db.HibernateUtil;
import de.fhdo.db.hibernate.TermUser;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.joda.time.DateTime;
import org.opensaml.saml2.core.Assertion;
import org.opensaml.saml2.core.Attribute;
import org.opensaml.saml2.core.AttributeStatement;
import org.opensaml.saml2.core.AttributeValue;
import org.opensaml.saml2.core.Audience;
import org.opensaml.saml2.core.AudienceRestriction;
import org.opensaml.saml2.core.AuthnContext;
import org.opensaml.saml2.core.AuthnContextClassRef;
import org.opensaml.saml2.core.AuthnStatement;
import org.opensaml.saml2.core.Conditions;
import org.opensaml.saml2.core.EncryptedAssertion;
import org.opensaml.saml2.core.Issuer;
import org.opensaml.saml2.core.NameID;
import org.opensaml.saml2.core.NameIDType;
import org.opensaml.saml2.core.Response;
import org.opensaml.saml2.core.Subject;
import org.opensaml.saml2.core.SubjectConfirmation;
import org.opensaml.saml2.core.SubjectConfirmationData;
import org.opensaml.saml2.encryption.Encrypter;
import org.opensaml.xml.Configuration;
import org.opensaml.xml.encryption.EncryptionConstants;
import org.opensaml.xml.encryption.EncryptionException;
import org.opensaml.xml.encryption.EncryptionParameters;
import org.opensaml.xml.encryption.KeyEncryptionParameters;
import org.opensaml.xml.io.MarshallingException;
import org.opensaml.xml.schema.XSBoolean;
import org.opensaml.xml.schema.XSBooleanValue;
import org.opensaml.xml.schema.XSInteger;
import org.opensaml.xml.schema.XSString;
import org.opensaml.xml.schema.impl.XSBooleanBuilder;
import org.opensaml.xml.schema.impl.XSIntegerBuilder;
import org.opensaml.xml.schema.impl.XSStringBuilder;
import org.opensaml.xml.signature.Signature;
import org.opensaml.xml.signature.SignatureConstants;
import org.opensaml.xml.signature.SignatureException;
import org.opensaml.xml.signature.Signer;
import org.zkoss.zk.ui.Sessions;

/**
 * This is a helper class to create and edit assertions based on the SAML
 * standard.
 *
 * @author Christoph Kösner
 */
public class AssertionHelper {

    private static final org.apache.log4j.Logger logger = de.fhdo.logging.Logger4j.getInstance().getLogger();
    private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    
    public static Assertion buildAssertion(String prevId, String username, String recipient) {
        Assertion assertion = SAMLHelper.buildSAMLObject(Assertion.class);
        Issuer issuer = SAMLHelper.buildSAMLObject(Issuer.class);
        issuer.setValue(Definitions.IDP_NAME);
        assertion.setIssuer(issuer);
        assertion.setIssueInstant(new DateTime());
        assertion.setID(SAMLHelper.generateSecureRandomId());
        assertion.getAuthnStatements().add(buildAuthnStatement());
        assertion.setSubject(buildSubject(prevId, username, recipient));
        return assertion;
    }

    /**
     *
     * @return the authnstatement
     */
    private static AuthnStatement buildAuthnStatement() {
        AuthnStatement authnStatement = SAMLHelper.buildSAMLObject(AuthnStatement.class);
        AuthnContext authnContext = SAMLHelper.buildSAMLObject(AuthnContext.class);
        AuthnContextClassRef authnContextClassRef = SAMLHelper.buildSAMLObject(AuthnContextClassRef.class);
        authnContextClassRef.setAuthnContextClassRef(AuthnContext.PASSWORD_AUTHN_CTX);
        authnContext.setAuthnContextClassRef(authnContextClassRef);
        authnStatement.setAuthnContext(authnContext);
        authnStatement.setAuthnInstant(new DateTime());
        return authnStatement;
    }

    /**
     *
     * @param prevID the ID of the authn statement, on which is responded
     * @param username the username
     * @param assertionConsumer address of the assertion consumer service
     * @return
     */
    public static Subject buildSubject(String prevID, String username, String assertionConsumer) {
        Subject subject = SAMLHelper.buildSAMLObject(Subject.class);
        SubjectConfirmation subjectConfirmation = SAMLHelper.buildSAMLObject(SubjectConfirmation.class);
        subjectConfirmation.setMethod(SubjectConfirmation.METHOD_BEARER);
        SubjectConfirmationData subjectConfirmationData = SAMLHelper.buildSAMLObject(SubjectConfirmationData.class);
        subjectConfirmationData.setInResponseTo(prevID != null ? prevID : "");
        subjectConfirmationData.setNotBefore(new DateTime().minusDays(2));
        subjectConfirmationData.setNotOnOrAfter(new DateTime().plusDays(2));
        subjectConfirmationData.setRecipient(assertionConsumer);
        subjectConfirmation.setSubjectConfirmationData(subjectConfirmationData);
        NameID nameID = SAMLHelper.buildSAMLObject(NameID.class);
        nameID.setFormat(NameIDType.TRANSIENT);
        nameID.setValue(username);
        subject.setNameID(nameID);
        subject.getSubjectConfirmations().add(subjectConfirmation);
        return subject;
    }

    /**
     *
     * @param assertionConsumer
     * @return the conditions
     */
    public static Conditions buildConditions(String assertionConsumer) {
        Conditions conditions = SAMLHelper.buildSAMLObject(Conditions.class);
        conditions.setNotBefore(new DateTime().minusDays(2));
        conditions.setNotOnOrAfter(new DateTime().plusDays(2));
        AudienceRestriction audienceRestriction = SAMLHelper.buildSAMLObject(AudienceRestriction.class);
        Audience audience = SAMLHelper.buildSAMLObject(Audience.class);
        audience.setAudienceURI(assertionConsumer);
        audienceRestriction.getAudiences().add(audience);
        conditions.getAudienceRestrictions().add(audienceRestriction);
        return conditions;
    }

    public static Attribute getSessIDAttribute(String sessID) {
        Attribute newAttribute = SAMLHelper.buildSAMLObject(Attribute.class);
        newAttribute.setName("sessId");
        logger.info("added sessID " + sessID + " to assertion.");
        XSString idValue
                = ((XSStringBuilder) Configuration.getBuilderFactory().getBuilder(XSString.TYPE_NAME))
                .buildObject(AttributeValue.DEFAULT_ELEMENT_NAME, XSString.TYPE_NAME);
        idValue.setValue(sessID);
        newAttribute.getAttributeValues().add(idValue);
        return newAttribute;
    }

    public static Attribute getCollabSessIDAttribute(String sessID) {
        Attribute newAttribute = SAMLHelper.buildSAMLObject(Attribute.class);
        newAttribute.setName("collab_sessId");
        logger.info("added collab_sessId " + sessID + " to assertion.");

        XSString idValue
                = ((XSStringBuilder) Configuration.getBuilderFactory().getBuilder(XSString.TYPE_NAME))
                .buildObject(AttributeValue.DEFAULT_ELEMENT_NAME, XSString.TYPE_NAME);
        idValue.setValue(sessID);
        newAttribute.getAttributeValues().add(idValue);
        return newAttribute;
    }

    public static Attribute getNameAttribute(String name) {
        Attribute newAttribute = SAMLHelper.buildSAMLObject(Attribute.class);
        newAttribute.setName("username");
        logger.info("added username " + name + " to assertion.");

        XSString idValue
                = ((XSStringBuilder) Configuration.getBuilderFactory().getBuilder(XSString.TYPE_NAME))
                .buildObject(AttributeValue.DEFAULT_ELEMENT_NAME, XSString.TYPE_NAME);
        idValue.setValue(name);
        newAttribute.getAttributeValues().add(idValue);
        return newAttribute;
    }

    public static Attribute getCollabNameAttribute(String name) {
        Attribute newAttribute = SAMLHelper.buildSAMLObject(Attribute.class);
        newAttribute.setName("collab_name");
        logger.info("added collab_name " + name + " to assertion.");

        XSString idValue
                = ((XSStringBuilder) Configuration.getBuilderFactory().getBuilder(XSString.TYPE_NAME))
                .buildObject(AttributeValue.DEFAULT_ELEMENT_NAME, XSString.TYPE_NAME);
        idValue.setValue(name);
        newAttribute.getAttributeValues().add(idValue);
        return newAttribute;
    }

    public static Attribute getCollabUserDataToMatch(Long collabId){
    
        SessionFactory sf = HibernateUtil.getSessionFactory(2);
        Session hb_session = sf.openSession();
        Collaborationuser user = (Collaborationuser) hb_session.get(Collaborationuser.class, collabId);
        
        Attribute newAttribute = SAMLHelper.buildSAMLObject(Attribute.class);
        newAttribute.setName("collab_data");
        logger.info("added collab_data " + collabId + " to assertion.");

        XSString idValue
                = ((XSStringBuilder) Configuration.getBuilderFactory().getBuilder(XSString.TYPE_NAME))
                .buildObject(AttributeValue.DEFAULT_ELEMENT_NAME, XSString.TYPE_NAME);
        
        //building dataString
        String splitter ="|";
        String dataString = "";
        
        if(user.getUsername()!= null && !user.getUsername().equals(""))
            dataString += user.getUsername() + splitter;
        else
            dataString += " " + splitter;
        
        if(user.getRoles().iterator().next().getName()!= null && !user.getRoles().iterator().next().getName().equals(""))
            dataString += user.getRoles().iterator().next().getName() + splitter;
        else
            dataString += " " + splitter;
        
        if(user.getPassword()!= null && !user.getPassword().equals(""))
            dataString += user.getPassword() + splitter;
        else
            dataString += " " + splitter;
        
        if(user.getSalt()!= null && !user.getSalt().equals(""))
            dataString += user.getSalt() + splitter;
        else
            dataString += " " + splitter;
            
        if(user.getName() != null && !user.getName().equals(""))
            dataString += user.getName() + splitter;
        else
            dataString += " " + splitter;
        
        if(user.getFirstName() != null && !user.getFirstName().equals(""))
            dataString += user.getFirstName() + splitter;
        else
            dataString += " " + splitter;
        
        if(user.getEmail() != null && !user.getEmail().equals(""))
            dataString += user.getEmail() + splitter;
        else
            dataString += " " + splitter;
        
        if(user.getSendMail() != null)
            dataString += String.valueOf(user.getSendMail()) + splitter;
       
        if(user.getOrganisation().getOrganisation() != null && !user.getOrganisation().getOrganisation().equals(""))
            dataString += user.getOrganisation().getOrganisation() + splitter;
        else
            dataString += " " + splitter;
        
        if(user.getOrganisation().getOrganisationAbbr() != null && !user.getOrganisation().getOrganisationAbbr().equals(""))
            dataString += user.getOrganisation().getOrganisationAbbr() + splitter;
        else
            dataString += " " + splitter;
        
        if(user.getActivated() != null)
            dataString += String.valueOf(user.getActivated()) + splitter;
                    
        if(user.getActivationTime() != null)
            dataString += sdf.format(user.getActivationTime()) + splitter;
        else
            dataString += " " + splitter;
        
        if(user.getEnabled() != null)
            dataString += String.valueOf(user.getEnabled()) + splitter;
        
        if(user.getHidden() != null)
            dataString += String.valueOf(user.getHidden()) + splitter;
        
        if(user.getDeleted() != null)
            dataString += String.valueOf(user.getDeleted()) + splitter;
        
        hb_session.close();
        
        idValue.setValue(dataString);
        newAttribute.getAttributeValues().add(idValue);
        return newAttribute;
    }
    
    public static Attribute getAdminUserDataToMatch(Long adminId){
           
        SessionFactory sf = HibernateUtil.getSessionFactory(1);
        Session hb_session = sf.openSession();
        TermUser userTerm = (TermUser )hb_session.get(TermUser.class, adminId);
        
        Attribute newAttribute = SAMLHelper.buildSAMLObject(Attribute.class);
        newAttribute.setName("term_data");
        logger.info("added term_data " + adminId + " to assertion.");

        XSString idValue
                = ((XSStringBuilder) Configuration.getBuilderFactory().getBuilder(XSString.TYPE_NAME))
                .buildObject(AttributeValue.DEFAULT_ELEMENT_NAME, XSString.TYPE_NAME);
        
        //building dataString
        String splitter ="|";
        String dataString = "";
        
        if(userTerm.getName()!= null && !userTerm.getName().equals(""))
            dataString += userTerm.getName() + splitter;
        else
            dataString += " " + splitter;
        
        if(userTerm.getPassw()!= null && !userTerm.getPassw().equals(""))
            dataString += userTerm.getPassw() + splitter;
        else
            dataString += " " + splitter;
        
        dataString += String.valueOf(userTerm.isIsAdmin()) + splitter;

        if(userTerm.getSalt()!= null && !userTerm.getSalt().equals(""))
            dataString += userTerm.getSalt() + splitter;
        else
            dataString += " " + splitter;
        
        if(userTerm.getEmail()!= null && !userTerm.getEmail().equals(""))
            dataString += userTerm.getEmail() + splitter;
        else
            dataString += " " + splitter;
        
        if(userTerm.getUserName()!= null && !userTerm.getUserName().equals(""))
            dataString += userTerm.getUserName() + splitter;
        else
            dataString += " " + splitter;
        
        if(userTerm.getActivated() != null)
            dataString += String.valueOf(userTerm.getActivated()) + splitter;
        
        if(userTerm.getActivationTime() != null)
            dataString += sdf.format(userTerm.getActivationTime()) + splitter;
        else
            dataString += " " + splitter;
        
        if(userTerm.getActivationMd5() != null && !userTerm.getActivationMd5().equals(""))
            dataString += userTerm.getActivationMd5() + splitter;
        else
            dataString += " " + splitter;
        
        if(userTerm.getEnabled() != null)
            dataString += String.valueOf(userTerm.getEnabled()) + splitter;
        else
            dataString += " " + splitter;
        
        if(userTerm.getPseudonym() != null && !userTerm.getPseudonym().equals(""))
            dataString += String.valueOf(userTerm.getPseudonym()) + splitter;
        else
            dataString += " " + splitter;
        
        hb_session.close();
        
        idValue.setValue(dataString);
        newAttribute.getAttributeValues().add(idValue);
        return newAttribute;
    }
    
    public static Attribute getIDAttribute(Integer id) {
        Attribute newAttribute = SAMLHelper.buildSAMLObject(Attribute.class);
        newAttribute.setName("id");
        logger.info("added id " + id + " to assertion.");

        XSInteger idValue
                = ((XSIntegerBuilder) Configuration.getBuilderFactory().getBuilder(XSInteger.TYPE_NAME))
                .buildObject(AttributeValue.DEFAULT_ELEMENT_NAME, XSInteger.TYPE_NAME);
        idValue.setValue(id);
        newAttribute.getAttributeValues().add(idValue);
        return newAttribute;
    }

    public static Attribute getCollabId(Integer id) {
        Attribute newAttribute = SAMLHelper.buildSAMLObject(Attribute.class);
        newAttribute.setName("collab_id");
        logger.info("added collab_id " + id + " to assertion.");

        XSInteger idValue
                = ((XSIntegerBuilder) Configuration.getBuilderFactory().getBuilder(XSInteger.TYPE_NAME))
                .buildObject(AttributeValue.DEFAULT_ELEMENT_NAME, XSInteger.TYPE_NAME);
        idValue.setValue(id);
        newAttribute.getAttributeValues().add(idValue);
        return newAttribute;
    }

    public static void addAttributeToAssertion(Assertion as, Attribute at) {
        List<AttributeStatement> allStatements = as.getAttributeStatements();
        AttributeStatement thisStatement;
        if (allStatements.isEmpty()) {
            thisStatement = SAMLHelper.buildSAMLObject(AttributeStatement.class);
            allStatements.add(thisStatement);
        } else {
            thisStatement = allStatements.get(0);
        }
        /*boolean add = */
        thisStatement.getAttributes().add(at);
    }

    public static Attribute isAdminAttribute(boolean admin) {
        Attribute newAttribute = SAMLHelper.buildSAMLObject(Attribute.class);
        newAttribute.setName("isAdmin");
        logger.info("added isAdmin" + admin + " to assertion.");

        XSBoolean xmlBoolean
                = ((XSBooleanBuilder) Configuration.getBuilderFactory().getBuilder(XSBoolean.TYPE_NAME))
                .buildObject(AttributeValue.DEFAULT_ELEMENT_NAME, XSBoolean.TYPE_NAME);

        XSBooleanValue xmlBooleanValue = new XSBooleanValue();
        xmlBooleanValue.setValue(admin);
        xmlBoolean.setValue(xmlBooleanValue);
        newAttribute.getAttributeValues().add(xmlBoolean);
        return newAttribute;
    }

    /**
     * Adds a role to an existing Attribute or creates a new Attribute with the
     * rolename
     *
     * @param a the existing Attribute
     * @param rolename the rolename
     * @return the changed or newly created Attribute
     */
    public static Attribute roleAttribute(String rolename) {
        Attribute at;
//        if (a == null) {
        at = SAMLHelper.buildSAMLObject(Attribute.class);
        at.setName("roles");
//        } else {
//            at = a;
//        }
        XSString roleNameValue
                = ((XSStringBuilder) Configuration.getBuilderFactory().getBuilder(XSString.TYPE_NAME))
                .buildObject(AttributeValue.DEFAULT_ELEMENT_NAME, XSString.TYPE_NAME);

        roleNameValue.setValue(rolename);
        logger.info("added rolename" + rolename + " to assertion.");

        at.getAttributeValues().add(roleNameValue);
        return at;
    }

    public static String encodeAndMarshallAssertion(Assertion a) {
        try {
            String marshalled = SAMLHelper.marshallElement(a);
            return SAMLHelper.encode(marshalled);
        } catch (Exception ex) {
            Logger.getLogger(LoginHelperTest.class.getName()).log(Level.SEVERE, null, ex);
            return "";
        }
    }

    public static EncryptedAssertion encryptAssertion(Assertion assertion) {
        EncryptionParameters encryptionParameters = new EncryptionParameters();
        encryptionParameters.setAlgorithm(EncryptionConstants.ALGO_ID_BLOCKCIPHER_AES128);

        KeyEncryptionParameters keyEncryptionParameters = new KeyEncryptionParameters();
//        keyEncryptionParameters.setEncryptionCredential(SPCredentials.getCredential());
        keyEncryptionParameters.setAlgorithm(EncryptionConstants.ALGO_ID_KEYTRANSPORT_RSAOAEP);

        Encrypter encrypter = new Encrypter(encryptionParameters, keyEncryptionParameters);
        encrypter.setKeyPlacement(Encrypter.KeyPlacement.INLINE);

        try {
            EncryptedAssertion encryptedAssertion = encrypter.encrypt(assertion);
            return encryptedAssertion;
        } catch (EncryptionException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     *
     * @param assertion
     */
    public static void signAssertion(Assertion assertion) {
        Signature signature = SAMLHelper.buildSAMLObject(Signature.class);
//        signature.setSigningCredential(IDPCredentials.getCredential());
        signature.setSignatureAlgorithm(SignatureConstants.ALGO_ID_SIGNATURE_RSA_SHA1);
        signature.setCanonicalizationAlgorithm(SignatureConstants.ALGO_ID_C14N_EXCL_OMIT_COMMENTS);
        assertion.setSignature(signature);

        try {
            Configuration.getMarshallerFactory().getMarshaller(assertion).marshall(assertion);
        } catch (MarshallingException e) {
            throw new RuntimeException(e);
        }
        try {
            Signer.signObject(signature);
        } catch (SignatureException e) {
            throw new RuntimeException(e);
        }
    }
}
