package com.nuance.cobra.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class Hostname
{
    private static Logger logger = LogManager.getLogger(Hostname.class);
    private static String hostname = null;

    //Java doesn't reliably return the hostname (on some JDKs)
    public synchronized static String getHostname()
    {
        logger.debug("getHostname() entry");
        if(Hostname.hostname != null)
        {
            //Sysadmin will probably reboot or restart services on a hostname change
            logger.debug("Returning cached hostname = {}", Hostname.hostname);
            return Hostname.hostname;
        }

        String hostname = null;

        //Use HOSTNAME or COMPUTERNAME
        if (System.getProperty("os.name").startsWith("Windows")) {
            // Windows will always set the 'COMPUTERNAME' variable
            hostname = System.getenv("COMPUTERNAME");
        } else {
            // If it is not Windows then it is most likely a Unix-like operating system
            // such as Solaris, AIX, HP-UX, Linux or MacOS.

            // Most modern shells (such as Bash or derivatives) sets the
            // HOSTNAME variable so lets try that first.
            hostname = System.getenv("HOSTNAME");
        }

        //Otherwise, use unix "hostname"
        //***Veracode says this is untrusted input***
        /*
        if (hostname == null) {
            ProcessBuilder pb = new ProcessBuilder("hostname");
            pb.redirectErrorStream(true);

            String output = ""; int rc = 1;
            try {
                Process process = pb.start();

                InputStream is = process.getInputStream();
                InputStreamReader isr = new InputStreamReader(is);
                BufferedReader br = new BufferedReader(isr);
                String line;
                while ((line = br.readLine()) != null) {
                    output += line;
                }

                rc = process.exitValue();
            } catch (Exception e) {
            }

            if (rc == 0) {
                hostname = output;
            }
        }
        */

        //Otherwise, use java (last resort)
        if (hostname == null) {
            try {
                InetAddress address = java.net.InetAddress.getLocalHost();
                hostname = address.getHostName();
            } catch (UnknownHostException e1) {
                hostname = "localhost";
            }
        }

        logger.debug("Returning newly computed hostname = {}", hostname);
        Hostname.hostname = hostname;
        return Hostname.hostname;
    }

}
