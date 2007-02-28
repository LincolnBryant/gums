/*
 * GecosLdpaAccountMapperTest.java
 * JUnit based test
 *
 * Created on May 10, 2005, 11:58 AM
 */

package gov.bnl.gums.account;

import gov.bnl.gums.configuration.Configuration;
import gov.bnl.gums.persistence.LDAPPersistenceFactory;
import junit.framework.*;
import java.util.Properties;
import javax.naming.NamingEnumeration;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchResult;

/**
 *
 * @author carcassi
 */
public class GecosLdapAccountMapperTest extends TestCase {
    GecosLdapAccountMapper mapper;
    
    public GecosLdapAccountMapperTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
        mapper = new GecosLdapAccountMapper(new Configuration(), "ldapAccountMapper");
        mapper.setJndiLdapUrl( LDAPPersistenceFactory.readLdapProperties().getProperty("ldap.java.naming.provider.url") );
    }

    protected void tearDown() throws Exception {
    }

    public static Test suite() {
        TestSuite suite = new TestSuite(GecosLdapAccountMapperTest.class);
        
        return suite;
    }

    public void testMap() {
        GecosMap map = mapper.createMap();
        String account = map.findAccount("John", "Smith");
        assertEquals("jsmith", account);
        account = map.findAccount("Jane", "Doe");
        assertEquals("jdoe", account);
    }
    
}
