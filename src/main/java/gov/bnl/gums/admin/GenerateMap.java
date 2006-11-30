/*
 * GUMS2MapUser.java
 *
 * Created on June 9, 2004, 1:44 PM
 */
package gov.bnl.gums.admin;


import org.apache.axis.AxisFault;

import org.apache.commons.cli.*;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;

import java.net.ConnectException;
import java.net.InetAddress;
import java.net.URL;
import gov.bnl.gums.command.AbstractCommand;


/**
 * @author carcassi
 */
public abstract class GenerateMap extends RemoteCommand {
    protected org.apache.commons.cli.Options buildOptions() {
        Options options = new Options();
        Option mapfile = new Option("f", "file", true,
                "saves in the specified file; prints to the console by default");

        options.addOption(mapfile);

        return options;
    }

    protected void execute(org.apache.commons.cli.CommandLine cmd)
        throws Exception {
        String hostname = null;

        if (cmd.getArgs().length == 0) {
            if (isUsingProxy()) {
                failForWrongParameters("Service DN is missing");
            }
                
            try {
                hostname = getClientDN();
            } catch (Exception e) {
                System.err.print("Couldn't retrieve the DN of the service/host");
                System.exit(-1);
            }
        } else if (cmd.getArgs().length == 1) {
            hostname = cmd.getArgs()[0];
        } else {
            failForWrongParameters("Too many arguments...");
        }

        String file = cmd.getOptionValue("f");

        try {
            if (file == null) {
                // print to std out if no filename entered
                System.out.println(generateMap(hostname));
            } else {
                PrintStream filename = new PrintStream(new FileOutputStream(
                            file));

                filename.print(generateMap(hostname));
            }
        } catch (FileNotFoundException e) {
            System.err.println("Couldn't open file " + file + ": " +
                e.getMessage());
            System.exit(-1);
        }
    }

    protected abstract String generateMap(String hostname)
        throws Exception;
}
