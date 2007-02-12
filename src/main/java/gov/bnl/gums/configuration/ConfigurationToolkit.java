/*
 * ConfigurationToolkit.java
 *
 * Created on June 4, 2004, 10:51 AM
 */

package gov.bnl.gums.configuration;

import gov.bnl.gums.groupToAccount.GroupToAccountMapping;
import gov.bnl.gums.hostToGroup.CertificateHostToGroupMapping;
import gov.bnl.gums.userGroup.VirtualOrganization;

import java.io.IOException;
import java.util.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXParseException;
import org.xml.sax.SAXException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.apache.commons.beanutils.MethodUtils;
import org.apache.commons.digester.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/** Contains the logic on how to parse an XML configuration file to create a
 * correctly built Configuration object.
 *
 * @author  Gabriele Carcassi
 */
class ConfigurationToolkit {
    private static Log log = LogFactory.getLog(ConfigurationToolkit.class); 
    
    public class SimpleErrorHandler implements ErrorHandler {
        public boolean error = false;
    	
        public void error(SAXParseException exception) {
        	log.error(exception.getMessage());
        	error = true;
        }
             
        public void fatalError(SAXParseException exception) {
        	log.fatal(exception.getMessage());
        	error = true;
        }
             
        public void warning(SAXParseException exception) {
        	log.warn(exception.getMessage());
        }
    }
    
    // Rule for handling the list of accountMappers within a groupToAccountMapping
    private static class AccountMapperListRule extends Rule {
        
        public void begin(String str, String str1, org.xml.sax.Attributes attributes) throws java.lang.Exception {
            if (attributes.getValue("accountMappers") != null) {
                Configuration conf = (Configuration) getDigester().getRoot();
                Object mapping = getDigester().peek();
                StringTokenizer tokens = new StringTokenizer(attributes.getValue("accountMappers"), ",");
                while (tokens.hasMoreTokens()) {
                    String accountMapperName = tokens.nextToken().trim();
                    Object accountMapper = conf.getAccountMappers().get(accountMapperName);
                    if (accountMapper == null) {
                        throw new IllegalArgumentException("The accountMapper '" + accountMapperName + "' is used within a groupToAccountMapping, but it was not defined.");
                    }
                    MethodUtils.invokeMethod(mapping, "addAccountMapper", accountMapperName);
                }
            }
        }
        
    }

    // Rule for handling the list of groupToAccountMappings within a hostToGroupMapping
    private static class GroupListRule extends Rule {
        
        public void begin(String str, String str1, org.xml.sax.Attributes attributes) throws java.lang.Exception {
            if (attributes.getValue("groupToAccountMappings") != null) {
                Configuration conf = (Configuration) getDigester().getRoot();
                Object mapping = getDigester().peek();
                StringTokenizer tokens = new StringTokenizer(attributes.getValue("groupToAccountMappings"), ",");
                while (tokens.hasMoreTokens()) {
                    String groupToAccountMappingName = tokens.nextToken().trim();
                    Object groupToAccountMapping = conf.getGroupToAccountMappings().get(groupToAccountMappingName);
                    if (groupToAccountMapping == null) {
                        throw new IllegalArgumentException("The groupToAccountMapping '" + groupToAccountMappingName + "' is used within a hostToGroupMapping, but it was not defined.");
                    }
                    MethodUtils.invokeMethod(mapping, "addGroupToAccountMapping", groupToAccountMappingName);
                }
            }
        }
        
    }    
    
    private static class PassRule extends SetPropertiesRule {
    	PassRule(String [] excludes) {
    		super(excludes, new String[]{});
    		setIgnoreMissingProperty(false);
    	}
    }

    //  Rule for handling a reference to a persistent Factory
    private static class PersistenceFactoryRule extends Rule {
        
        public void begin(String str, String str1, org.xml.sax.Attributes attributes) throws java.lang.Exception {
            if (attributes.getValue("persistenceFactory") != null) {
                Configuration conf = (Configuration) getDigester().getRoot();
                Object mapper = getDigester().peek();
                String persistenceFactoryName = attributes.getValue("persistenceFactory").trim();
                Object persistenceFactory = conf.getPersistenceFactories().get(persistenceFactoryName);
                if (persistenceFactory == null) {
                    throw new IllegalArgumentException("The persistence factory '" + persistenceFactoryName + "' is used, but it was not defined.");
                }
                MethodUtils.invokeMethod(mapper, "setPersistenceFactory", new Object[] {persistenceFactoryName});
            }
        }
        
    }
    
    private static class PropertiesRule extends Rule {
        
        public void begin(String str, String str1, org.xml.sax.Attributes attributes) throws java.lang.Exception {
            Object digestor = getDigester().peek();
            Properties properties = new Properties();
            for (int nAtt = 0; nAtt < attributes.getLength(); nAtt++) {
                String name = attributes.getQName(nAtt);
                String value = attributes.getValue(nAtt);
                log.trace("Adding " + name + " " + value + " property");
                if (name.equals("name"))
                    MethodUtils.invokeMethod(digestor, "setName", new Object[] {value});
                else if (!name.equals("className"))
                	properties.setProperty(name, value);
            }
            MethodUtils.invokeMethod(digestor, "setProperties", properties);
        }
        
    };
    
    //  Rule for handling the list of userGroups within a groupToAccountMapping
    private static class UserGroupListRule extends Rule {
        
        public void begin(String str, String str1, org.xml.sax.Attributes attributes) throws java.lang.Exception {
            if (attributes.getValue("userGroups") != null) {
                Configuration conf = (Configuration) getDigester().getRoot();
                Object mapping = getDigester().peek();
                StringTokenizer tokens = new StringTokenizer(attributes.getValue("userGroups"), ",");
                while (tokens.hasMoreTokens()) {
                    String userGroupName = tokens.nextToken().trim();
                    Object userGroup = conf.getUserGroups().get(userGroupName);
                    if (userGroup == null) {
                        throw new IllegalArgumentException("The userGroup '" + userGroupName + "' is used within a groupToAccountMapping, but it was not defined.");
                    }
                    MethodUtils.invokeMethod(mapping, "addUserGroup", userGroupName);
                }
            }
        }
        
    }

    // Rule for handling getting the version - only reads a bit of the entire file
    private static class VersionRule extends Rule {
    	public void begin(String str, String str1, org.xml.sax.Attributes attributes) throws java.lang.Exception {
    		Object digestor = getDigester().peek();
            for (int nAtt = 0; nAtt < attributes.getLength(); nAtt++) {
            	String name = attributes.getQName(nAtt);
                String value = attributes.getValue(nAtt);
                if (name.equals("version"))
                	MethodUtils.invokeMethod(digestor, "concat", new Object[] {value});
            }
    	}
    }    
    
    //  Rule for handling a reference to a VO
    private static class VirtualOrganizationRule extends Rule {
        
        public void begin(String str, String str1, org.xml.sax.Attributes attributes) throws java.lang.Exception {
            if (attributes.getValue("virtualOrganization") != null) {
                Configuration conf = (Configuration) getDigester().getRoot();
                Object mapper = getDigester().peek();
                String virtualOrganizationName = attributes.getValue("virtualOrganization").trim();
                Object virtualOrganization = conf.getVirtualOrganizations().get(virtualOrganizationName);
                if (virtualOrganization == null) {
                    throw new IllegalArgumentException("The virtual organization '" + virtualOrganizationName + "' is used, but it was not defined.");
                }
                MethodUtils.invokeMethod(mapper, "setVirtualOrganization", new Object[] {virtualOrganizationName});
            }
        }
        
    }          
    
    public static String getVersion(String filename) throws IOException, SAXException {
        Digester digester = new Digester();
        digester.setValidating(false);
        digester.addSetProperties("gums");
        digester.addRule("gums", new VersionRule());
        digester.push(new String());
        log.trace("Loading the version from configuration file '" + filename + "'");
        String version = (String) digester.parse(filename);
        if (version.equals(""))
        	version = "1.1";
        return version;
    }
    
    public static Digester retrieveDigester() {
        Digester digester = new Digester();
        digester.setValidating(false);
       
        digester.addSetProperties("gums");
        
        digester.addObjectCreate("gums/persistenceFactories/persistenceFactory", null, "className");
        digester.addSetProperties("gums/persistenceFactories/persistenceFactory");
        digester.addRule("gums/persistenceFactories/persistenceFactory", new PropertiesRule());
        digester.addSetNext("gums/persistenceFactories/persistenceFactory", "addPersistenceFactory", "gov.bnl.gums.persistence.PersistenceFactory");

        digester.addObjectCreate("gums/virtualOrganizations/virtualOrganization", VirtualOrganization.class);
        digester.addSetProperties("gums/virtualOrganizations/virtualOrganization");
        digester.addRule("gums/virtualOrganizations/virtualOrganization", new PassRule(new String[] {"persistenceFactory"}));
        digester.addRule("gums/virtualOrganizations/virtualOrganization", new PersistenceFactoryRule());
        digester.addSetNext("gums/virtualOrganizations/virtualOrganization", "addVirtualOrganization", "gov.bnl.gums.userGroup.VirtualOrganization");
        
        digester.addObjectCreate("gums/userGroups/userGroup", null, "className");
        digester.addRule("gums/userGroups/userGroup", new PassRule(new String[] {"className", "persistenceFactory", "virtualOrganization"}));
        digester.addRule("gums/userGroups/userGroup", new PersistenceFactoryRule());
        digester.addRule("gums/userGroups/userGroup", new VirtualOrganizationRule());
        digester.addSetNext("gums/userGroups/userGroup", "addUserGroup", "gov.bnl.gums.userGroup.UserGroup");

        digester.addObjectCreate("gums/accountMappers/accountMapper", null, "className");
        digester.addSetProperties("gums/accountMappers/accountMapper");
        digester.addRule("gums/accountMappers/accountMapper", new PassRule(new String[] {"className"}));
        digester.addRule("gums/accountMappers/accountMapper", new PersistenceFactoryRule());
        digester.addSetNext("gums/accountMappers/accountMapper", "addAccountMapper", "gov.bnl.gums.account.AccountMapper");
        
        digester.addObjectCreate("gums/groupToAccountMappings/groupToAccountMapping", GroupToAccountMapping.class);
        digester.addSetProperties("gums/groupToAccountMappings/groupToAccountMapping");
        digester.addRule("gums/groupToAccountMappings/groupToAccountMapping", new PassRule(new String[] {"userGroups", "accountMappers"}));
        digester.addRule("gums/groupToAccountMappings/groupToAccountMapping", new UserGroupListRule());
        digester.addRule("gums/groupToAccountMappings/groupToAccountMapping", new AccountMapperListRule());
        digester.addSetNext("gums/groupToAccountMappings/groupToAccountMapping", "addGroupToAccountMapping", "gov.bnl.gums.groupToAccount.GroupToAccountMapping");

        digester.addObjectCreate("gums/hostToGroupMappings/hostToGroupMapping", CertificateHostToGroupMapping.class);
        digester.addSetProperties("gums/hostToGroupMappings/hostToGroupMapping");
        digester.addRule("gums/hostToGroupMappings/hostToGroupMapping", new PassRule(new String[] {"groupToAccountMappings"}));
        digester.addRule("gums/hostToGroupMappings/hostToGroupMapping", new GroupListRule());
        digester.addSetNext("gums/hostToGroupMappings/hostToGroupMapping", "addHostToGroupMapping", "gov.bnl.gums.hostToGroup.HostToGroupMapping");
       
        return digester;
    }
    
    public static boolean validate(String configFile, String schemaFile) throws ParserConfigurationException, SAXException, IOException {
    	System.setProperty("javax.xml.parsers.DocumentBuilderFactory", "org.apache.xerces.jaxp.DocumentBuilderFactoryImpl");
    	DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        log.trace("DocumentBuilderFactory: "+ factory.getClass().getName());
        
        factory.setNamespaceAware(true);
        factory.setValidating(true);
        factory.setAttribute("http://java.sun.com/xml/jaxp/properties/schemaLanguage", "http://www.w3.org/2001/XMLSchema");
        factory.setAttribute("http://java.sun.com/xml/jaxp/properties/schemaSource", "file:"+schemaFile);
        
        DocumentBuilder builder = factory.newDocumentBuilder();
        SimpleErrorHandler errorHandler = new ConfigurationToolkit().new SimpleErrorHandler();
        builder.setErrorHandler( errorHandler );

        Document document = builder.parse(configFile); 
        
        if (errorHandler.error)
        	return false;

        Node rootNode  = document.getFirstChild();
        log.trace("Root node of config file: "+ rootNode.getNodeName()  );
        
        return true;
    }
    
    static synchronized Configuration loadConfiguration(String configFile) throws ParserConfigurationException, IOException, SAXException {
    	String schemaFile = configFile+".schema";
    	String transformFile = configFile+".xls";
        //if (getVersion(configFile).compareTo(GUMS.getVersion())<0)
        //	ConfigurationTransform.doTransform(configFile, transformFile);
		validate(configFile, schemaFile);
    	Digester digester = retrieveDigester();
    	Configuration configuration = new Configuration();
        digester.push(configuration);
        log.trace("Loading the configuration from file '" + configFile + "' using schema '" + schemaFile);
        return (Configuration) digester.parse(configFile);
    }
    
}
