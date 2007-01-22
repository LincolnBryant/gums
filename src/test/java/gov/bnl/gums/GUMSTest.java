/*
 * GUMSTest.java
 * JUnit based test
 *
 * Created on June 9, 2004, 10:15 AM
 */

package gov.bnl.gums;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collection;

import junit.framework.*;

import gov.bnl.gums.configuration.*;
import gov.bnl.gums.groupToAccount.GroupToAccountMapping;
import gov.bnl.gums.userGroup.*;
import gov.bnl.gums.persistence.*;
import gov.bnl.gums.account.*;

/**
 *
 * @author carcassi
 */
public class GUMSTest extends TestCase {
    
    public GUMSTest(String testName) {
        super(testName);
    }
    
    public static Test suite() {
        TestSuite suite = new TestSuite(GUMSTest.class);
        return suite;
    }
    
    public void testConfiguration() {
		GUMS gums = new GUMS();
		Configuration conf = gums.getConfiguration();
		assertEquals(1, conf.getPersistenceFactories().size());
		assertEquals("persistenceFactoryA", ((HibernatePersistenceFactory) conf.getPersistenceFactories().get("persistenceFactoryA")).getName());
		assertEquals(3, conf.getGroupToAccountMappings().size());
		GroupToAccountMapping groupToAccountMapping = (GroupToAccountMapping) conf.getGroupToAccountMappings().get("groupToAccountMappingA");
		Collection userGroups = (Collection) groupToAccountMapping.getUserGroups();
		String userGroupName = (String)userGroups.iterator().next();
		assertEquals("persistenceFactoryA", ((ManualUserGroup)conf.getUserGroup( userGroupName )).getPersistenceFactory() );
		assertEquals("admin", (String)userGroups.iterator().next());
		assertEquals("write", conf.getUserGroup( userGroupName ).getAccess() );
		Collection accountMappers = (Collection) groupToAccountMapping.getAccountMappers();
		assertEquals("accountMapperA", (String)accountMappers.iterator().next());
		assertEquals(1, conf.getHostToGroupMappings().size());
    }
    
}
