/*
 * AdminConfiguration.java
 *
 * Created on November 3, 2004, 10:51 AM
 */
package gov.bnl.gums.command;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.MissingResourceException;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author carcassi
 */
public class Configuration {
    private static Log log = LogFactory.getLog(Configuration.class);
    private static Configuration conf = new Configuration();
    public static Configuration getInstance() {
        return conf;
    }
    private URL locationURL;
    private URL authZLocationURL;
    private boolean direct;

    private boolean loaded;

    public URL getGUMSAuthZLocation() {
        if (!loaded) {
            loadConf();
        }

        if (authZLocationURL == null) {
            throw new RuntimeException(
                "Couldn't find gums.authz URL within gums-client.properties.");
        }

        return authZLocationURL;
    }
    
    /**
     * TODO: write doc
     *
     * @return TODO: write doc
     */
    public URL getGUMSLocation() {
        if (!loaded) {
            loadConf();
        }

        return locationURL;
    }

    public boolean isDirect() {
        if (!loaded) {
            loadConf();
        }

        return direct;
    }

    private void loadConf() {
        PropertyResourceBundle prop = (PropertyResourceBundle) ResourceBundle.getBundle(
                "gums-client");

        if (prop == null) {
            throw new RuntimeException(
                "Couldn't find gums-client.properties configuration file.");
        }

        String location = prop.getString("gums.location");

        if (location == null) {
            throw new RuntimeException(
                "Couldn't find gums.location URL within gums-client.properties.");
        }
        
        if (location.equals("direct")) {
            direct = true;
            loaded = true;
            return;
        }
        
        String authZLocation = null;
        try {
            authZLocation = prop.getString("gums.authz");
        } catch (MissingResourceException e) {
            // Fail silently: might not be needed
            log.warn("gums.authz not found within gums-client.properties");
        }
        
        try {
            locationURL = new URL(location);
            loaded = true;
        } catch (MalformedURLException e) {
            throw new RuntimeException("The value in gums.location '" +
                location + "' is not a valid url: " + e.getMessage(), e);
        }
        
        try {
            if (authZLocation != null) {
                authZLocationURL = new URL(authZLocation);
            }
        } catch (MalformedURLException e) {
            throw new RuntimeException("The value in gums.authz '" +
                location + "' is not a valid url: " + e.getMessage(), e);
        }
    }
}
