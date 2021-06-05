package de.fhdo.helper;

import de.fhdo.Definitions;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import org.opensaml.common.impl.SecureRandomIdentifierGenerator;
import org.opensaml.ws.soap.soap11.Body;
import org.opensaml.ws.soap.soap11.Envelope;
import org.opensaml.xml.Configuration;
import org.opensaml.xml.XMLObject;
import org.opensaml.xml.XMLObjectBuilderFactory;
import org.opensaml.xml.io.Marshaller;
import org.opensaml.xml.io.MarshallingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;
import org.joda.time.DateTime;
import org.opensaml.saml2.core.Assertion;
import org.opensaml.saml2.core.AuthnRequest;
import org.opensaml.saml2.core.Issuer;
import org.opensaml.saml2.core.Response;
import org.opensaml.xml.io.MarshallerFactory;
import org.opensaml.xml.io.Unmarshaller;
import org.opensaml.xml.io.UnmarshallerFactory;
import org.opensaml.xml.io.UnmarshallingException;
import org.owasp.esapi.codecs.Base64;
import org.w3c.dom.Element;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSOutput;
import org.w3c.dom.ls.LSSerializer;
import org.xml.sax.SAXException;

/**
 * Created by Privat on 4/6/14.
 */
public class SAMLHelper {

    private static final Logger logger = LoggerFactory.getLogger(SAMLHelper.class);
    private static SecureRandomIdentifierGenerator secureRandomIdGenerator;

    static {
        try {
            secureRandomIdGenerator = new SecureRandomIdentifierGenerator();
        } catch (NoSuchAlgorithmException e) {
            logger.error(e.getMessage(), e);
        }
    }

    public static <T> T buildSAMLObject(final Class<T> clazz) {
        T object = null;
        try {
            XMLObjectBuilderFactory builderFactory = Configuration.getBuilderFactory();
            QName defaultElementName = (QName) clazz.getDeclaredField("DEFAULT_ELEMENT_NAME").get(null);
            object = (T) builderFactory.getBuilder(defaultElementName).buildObject(defaultElementName);
        } catch (IllegalAccessException | NoSuchFieldException e) {
            throw new IllegalArgumentException("Could not create SAML object");
        }

        return object;
    }

    public static String generateSecureRandomId() {
        return secureRandomIdGenerator.generateIdentifier();
    }

    public static void logSAMLObject(final XMLObject object) {
        try {
            DocumentBuilder builder;
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            builder = factory.newDocumentBuilder();
            Document document = builder.newDocument();
            Marshaller out = Configuration.getMarshallerFactory().getMarshaller(object);
            out.marshall(object, document);
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            StreamResult result = new StreamResult(new StringWriter());
            DOMSource source = new DOMSource(document);
            transformer.transform(source, result);
            String xmlString = result.getWriter().toString();

            logger.info(xmlString);
        } catch (ParserConfigurationException | MarshallingException | TransformerException e) {
            logger.error(e.getMessage(), e);
        }
    }

    public static Envelope wrapInSOAPEnvelope(final XMLObject xmlObject) throws IllegalAccessException {
        Envelope envelope = SAMLHelper.buildSAMLObject(Envelope.class);
        Body body = SAMLHelper.buildSAMLObject(Body.class);
        body.getUnknownXMLObjects().add(xmlObject);
        envelope.setBody(body);
        return envelope;
    }

    public static XMLObject unmarshall(byte[] b) throws SAXException, IOException, UnmarshallingException, ParserConfigurationException {
        ByteArrayInputStream bais = new ByteArrayInputStream(b);
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        documentBuilderFactory.setNamespaceAware(true);
        DocumentBuilder docBuilder = documentBuilderFactory.newDocumentBuilder();
        Document document = docBuilder.parse(bais);
        Element element = document.getDocumentElement();
        UnmarshallerFactory unmarshallerFactory = Configuration.getUnmarshallerFactory();
        Unmarshaller unmarshaller = unmarshallerFactory.getUnmarshaller(element);
        XMLObject responseXmlObj = unmarshaller.unmarshall(element);
        return responseXmlObj;
    }

    public static String marshallElement(XMLObject ar) throws Exception {
        MarshallerFactory marshallerFactory = Configuration.getMarshallerFactory();
        Element element = marshallerFactory.getMarshaller(ar).marshall(ar);
        ByteArrayOutputStream byteArrayOutputStrm = new ByteArrayOutputStream();
        DOMImplementationRegistry registry = DOMImplementationRegistry.newInstance();
        DOMImplementationLS impl = (DOMImplementationLS) registry.getDOMImplementation("LS");
        LSSerializer writer = impl.createLSSerializer();
        LSOutput output = impl.createLSOutput();
        output.setByteStream(byteArrayOutputStrm);
        writer.write(element, output);
        return byteArrayOutputStrm.toString();
    }

    public static String encode(String message) throws UnsupportedEncodingException {
        byte[] input = message.getBytes("UTF-8");
        byte[] output = new byte[5000];
        Deflater compresser = new Deflater();
        compresser.setInput(input);
        compresser.finish();
        int compressedDataLength = compresser.deflate(output);
        compresser.end();
        String encodedRequestMessage
                = Base64.encodeBytes(Arrays.copyOfRange(output, 0, compressedDataLength), Base64.URL_SAFE);
        return encodedRequestMessage;
    }

    public static String decode(String encodedStr) throws DataFormatException, UnsupportedEncodingException {
        byte[] base64DecodedByteArray = Base64.decode(encodedStr, Base64.URL_SAFE);
        byte[] output = new byte[5000];
        Inflater decompresser = new Inflater();
        decompresser.setInput(base64DecodedByteArray);
        int resultLength = decompresser.inflate(output);
        decompresser.end();
        return new String(output, 0, resultLength, "UTF-8");
    }

    public static Response getResponseFromRequestAndAssertion(Assertion a, AuthnRequest ar) {
        Response r = SAMLHelper.buildSAMLObject(Response.class);
        r.setID(generateSecureRandomId());
        r.setDestination(ar.getAssertionConsumerServiceURL());
        r.setInResponseTo(ar.getID());
        r.getAssertions().add(a);
        Issuer issuer = SAMLHelper.buildSAMLObject(Issuer.class);
        issuer.setValue(Definitions.IDP_NAME);
        r.setIssuer(issuer);
        r.setIssueInstant(new DateTime());
        return r;
    }
}
