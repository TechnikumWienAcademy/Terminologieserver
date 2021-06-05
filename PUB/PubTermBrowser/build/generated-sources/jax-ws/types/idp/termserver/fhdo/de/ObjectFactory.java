
package types.idp.termserver.fhdo.de;

import javax.xml.bind.annotation.XmlRegistry;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the types.idp.termserver.fhdo.de package. 
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


    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: types.idp.termserver.fhdo.de
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link Role }
     * 
     */
    public Role createRole() {
        return new Role();
    }

    /**
     * Create an instance of {@link TermUser }
     * 
     */
    public TermUser createTermUser() {
        return new TermUser();
    }

    /**
     * Create an instance of {@link Organisation }
     * 
     */
    public Organisation createOrganisation() {
        return new Organisation();
    }

    /**
     * Create an instance of {@link Collaborationuser }
     * 
     */
    public Collaborationuser createCollaborationuser() {
        return new Collaborationuser();
    }

}
