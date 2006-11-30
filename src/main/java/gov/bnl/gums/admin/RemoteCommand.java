/*
 * AbstractWebCommand.java
 *
 * Created on November 4, 2004, 10:07 AM
 */
package gov.bnl.gums.admin;


import gov.bnl.gums.command.AbstractCommand;
import gov.bnl.gums.command.Configuration;
import java.util.Iterator;
import org.apache.axis.AxisFault;
import org.apache.axis.client.Stub;

import org.apache.commons.cli.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.net.ConnectException;
import java.net.URL;

import java.util.logging.Logger;
import gov.bnl.gums.admin.*;


/**
 * @author carcassi
 */
public abstract class RemoteCommand extends AbstractCommand {
    private Log log = LogFactory.getLog(RemoteCommand.class);

    private GUMSAPI clientStub;

    protected GUMSAPI getGums() {
        log.trace("Retrieving GUMS stub");
        if (clientStub != null) return clientStub;        
        try {
            GUMSAPIService service = new GUMSAPIServiceLocator();
            if (Configuration.getInstance().isDirect()) {
                log.info("Accessing direct implementation.");
                return new GUMSAPIImpl();
            } else {
                URL gumsLocation = Configuration.getInstance().getGUMSLocation();

                log.info("Accessing GUMS implementation at " + gumsLocation + ".");
                clientStub = service.getadmin(gumsLocation);
                Stub axisStub = (Stub) clientStub;
                axisStub.setMaintainSession(true);
                Iterator iter = axisStub._getPropertyNames();
                while (iter.hasNext()) {
                    String name = (String) iter.next();
                    log.trace("Client stub property '" + name + "' value '" + axisStub._getProperty(name));
                }
                return clientStub;
            }
        } catch (Exception e) {
            System.out.println("Couldn't initialize GUMS client:" +
                e.getMessage());
            log.fatal("Couldn't initialize GUMS client", e);
            System.exit(-1);

            return null;
        }
    }


}
