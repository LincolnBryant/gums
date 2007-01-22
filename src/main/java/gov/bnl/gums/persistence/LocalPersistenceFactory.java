/*
 * BNLPersistenceFactory.java
 *
 * Created on February 3, 2005, 9:52 AM
 */

package gov.bnl.gums.persistence;

import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import gov.bnl.gums.configuration.Configuration;
import gov.bnl.gums.db.AccountPoolMapperDB;
import gov.bnl.gums.db.ManualAccountMapperDB;
import gov.bnl.gums.db.ManualUserGroupDB;
import gov.bnl.gums.db.UserGroupDB;


/**
 *
 * @author carcassi
 */
public class LocalPersistenceFactory extends PersistenceFactory {
    
    private Log log = LogFactory.getLog(LocalPersistenceFactory.class);
    private HibernatePersistenceFactory mysql;
    private LDAPPersistenceFactory ldap;

    public LocalPersistenceFactory() {
    	super();
    	log.trace("HibernatePersistenceFactory instanciated");
    }    
    
    public LocalPersistenceFactory(Configuration configuration, String name) {
    	super(configuration, name);
        mysql = new HibernatePersistenceFactory(configuration, name+"_hib");
        ldap = new LDAPPersistenceFactory(configuration, name+"_mysql");
    	log.trace("HibernatePersistenceFactory instanciated");
    }
    
    public void setName(String name) {
    	setName(name);
    	if (getConfiguration()!=null) {
	        mysql = new HibernatePersistenceFactory(getConfiguration(), name+"_hib");
	        ldap = new LDAPPersistenceFactory(getConfiguration(), name+"_mysql");   	
    	}
    }
 
    public void setConfiguration(Configuration configuration) {
    	setConfiguration(configuration);
    	if (getName()!=null) {
	        mysql = new HibernatePersistenceFactory(getConfiguration(), getName()+"_hib");
	        ldap = new LDAPPersistenceFactory(getConfiguration(), getName()+"_mysql");   	
    	}
    }
    
    /**
     * Holds value of property synchGroups.
     */
    private boolean synchGroups;

    public UserGroupDB retrieveUserGroupDB(String name) {
        return mysql.retrieveUserGroupDB(name);
    }

    public ManualUserGroupDB retrieveManualUserGroupDB(String name) {
        return mysql.retrieveManualUserGroupDB(name);
    }

    public ManualAccountMapperDB retrieveManualAccountMapperDB(String name) {
        return mysql.retrieveManualAccountMapperDB(name);
    }

    public AccountPoolMapperDB retrieveAccountPoolMapperDB(String name) {
        StringTokenizer tokens = new StringTokenizer(name, ".");
        if (!tokens.hasMoreTokens()) {
            return mysql.retrieveAccountPoolMapperDB(name);
        }
        String pool = tokens.nextToken();
        if (!tokens.hasMoreTokens()) {
            return mysql.retrieveAccountPoolMapperDB(name);
        }
        String group = tokens.nextToken();
        List secondaryGroups = new ArrayList();
        while (tokens.hasMoreTokens()) {
            secondaryGroups.add(tokens.nextToken());
        }
        return new LocalAccountPoolMapperDB(pool, group, secondaryGroups);
    }
    
    private class LocalAccountPoolMapperDB implements AccountPoolMapperDB {
        
        private AccountPoolMapperDB mysqlDB;
        private String group;
        private List secondaryGroups;
        
        public LocalAccountPoolMapperDB(String poolname, String group, List secondaryGroups) {
            this.group = group;
            this.secondaryGroups = secondaryGroups;
            mysqlDB = mysql.retrieveAccountPoolMapperDB(poolname);
        }
        
        public void unassignUser(String user) {
            mysqlDB.unassignUser(user);
        }

        public String retrieveAccount(String userDN) {
            String username = mysqlDB.retrieveAccount(userDN);
            if (synchGroups) {
                assignGroups(username);
            }
            return username;
        }
        
        private void assignGroups(String username) {
            ldap.changeGroupID(username, group);
            log.trace("Assigned '" + group + "' to '" + username);
            Iterator iter = secondaryGroups.iterator();
            while (iter.hasNext()) {
                String group = (String) iter.next();
                ldap.addToSecondaryGroup(username, group);
                log.trace("Assigned secondary group '" + group + "' to '" + username);
            }
        }

        public String assignAccount(String userDN) {
            String username = mysqlDB.assignAccount(userDN);
            if (username == null) return null;
            log.trace("Assigned '" + username + "' to '" + userDN);
            try {
                assignGroups(username);
                return username;
            } catch (RuntimeException e) {
                log.trace("Group assignment failed: unassign user", e);
                unassignUser(username);
                log.trace("User unassigned");
                throw e;
            }
        }

        public void addAccount(String account) {
            mysqlDB.addAccount(account);
        }

        public List retrieveUsersNotUsedSince(Date date) {
            return mysqlDB.retrieveUsersNotUsedSince(date);
        }

        public java.util.Map retrieveAccountMap() {
            return mysqlDB.retrieveAccountMap();
        }
        
    }
    public void setProperties(Properties properties) {
        super.setProperties(properties);
        mysql.setProperties(getMySQLProperties());
        ldap.setProperties(getLDAPProperties());
    }
    
    public Properties getMySQLProperties() {
        return filterProperties("mysql.");
    }
    
    private Properties filterProperties(String prefix) {
        Properties filtered = new Properties();
        Enumeration keys = getProperties().propertyNames();
        while (keys.hasMoreElements()) {
            String name = (String) keys.nextElement();
            if (name.startsWith(prefix)) {
                filtered.setProperty(name.substring(prefix.length()), getProperties().getProperty(name));
            }
        }
        return filtered;
    }
    
    public Properties getLDAPProperties() {
        return filterProperties("ldap.");
    }

    /**
     * Getter for property synchGroups.
     * @return Value of property synchGroups.
     */
    public boolean isSynchGroups() {

        return this.synchGroups;
    }

    /**
     * Setter for property synchGroups.
     * @param synchGroups New value of property synchGroups.
     */
    public void setSynchGroups(boolean synchGroups) {

        this.synchGroups = synchGroups;
    }
    
    public Object clone() {
    	LocalPersistenceFactory persistenceFactory = new LocalPersistenceFactory(getConfiguration(), getName());
    	persistenceFactory.setProperties((Properties)getProperties().clone());
    	return persistenceFactory;
    }
   
}
