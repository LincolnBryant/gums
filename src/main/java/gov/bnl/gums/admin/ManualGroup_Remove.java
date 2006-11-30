/*
 * GUMS2MapUser.java
 *
 * Created on June 9, 2004, 1:44 PM
 */
package gov.bnl.gums.admin;


import org.apache.axis.AxisFault;

import org.apache.commons.cli.*;

import java.net.ConnectException;
import java.net.InetAddress;
import java.net.URL;
import gov.bnl.gums.command.AbstractCommand;


/**
 * @author carcassi
 */
public class ManualGroup_Remove extends RemoteCommand {
    static {
        command = new ManualGroup_Remove();
    }

    /**
     * Creates a new ManualGroup_Remove object.
     */
    public ManualGroup_Remove() {
        syntax = "PERSISTANCE GROUP USERDN1 [USERDN2] ...";
        description = "Removes a user from a manually managed group. " +
            "PERSISTANCE is the 'persistenceFactory' as defined in the configuration for the group." +
            "GROUP is the 'name' as defined in the configuration for the group.";
    }

    protected org.apache.commons.cli.Options buildOptions() {
        Options options = new Options();

        return options;
    }

    protected void execute(org.apache.commons.cli.CommandLine cmd)
        throws Exception {
        if (cmd.getArgs().length < 3) {
            failForWrongParameters("Missing parameters...");
        }

        String[] userDN = (cmd.getArgs());
        String persistenceManager = cmd.getArgs()[0];
        String groupName = cmd.getArgs()[1];

        for (int nArg = 2; nArg < cmd.getArgs().length; nArg++) {
            getGums().manualGroupRemove(persistenceManager, groupName, cmd.getArgs()[nArg]);
        }
    }
}
