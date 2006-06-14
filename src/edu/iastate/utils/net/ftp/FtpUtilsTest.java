package edu.iastate.utils.net.ftp;

/*
 * :::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
 *		 FILENAME: ftp6main.java
 *		CLASSNAME: ftp6main
 *		COPYRIGHT: Copyright 1999 Kenneth R. Kress
 * RCS Id: $Id: FtpUtilsTest.java,v 1.1 2006/06/14 22:32:40 baojie Exp $
 * :::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
 * History
 * $Log: FtpUtilsTest.java,v $
 * Revision 1.1  2006/06/14 22:32:40  baojie
 * *** empty log message ***
 *
 * Revision 1.2  1999/11/03 01:09:24  kkress
 * About to start a new version. This one works with PASV
 * connections and used to work with PORT connections.
 *
 * Revision 1.1  1999/10/21 03:46:08  kkress
 * Initial revision
 *
 *
 */
/*
 * :::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
 *		IMPORTS
 * :::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
 */

import java.io.IOException;

public class FtpUtilsTest
{
    /*
     * :::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
     *		DESCRIPTION:
     * :::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
     *
     */
    static String myName = "ftp6main";
    static String USAGE = "java "
        + myName
        + " ftpserver"
        + " username"
        + " password";
    static boolean DEBUGGING = true; // turned off by default

    /*
     * # :::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::	#
     * #		SUBROUTINES and CONSTANTS:
     * # :::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::	#
     */
    static String eol = "\r\n";
    static int maxread = 2048;

    /*
     * # :::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::	#
     * #		CODE BEGINS						#
     * # :::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::	#
     */
    public static void main(String[] args)
    {
        String ftpServer = null;
        String userName = null;
        String password = null;
        FtpUtils f = null;
        // if ( DEBUGGING ) { System.out.println(USAGE); }

        if (args.length < 3)
        {
            System.err.println(
                "\n"
                + "USAGE: "
                + USAGE
                + " ftpserver username password"
                + "\n");
            System.exit(1);
        } // end if
        ftpServer = args[0];
        userName = args[1];
        password = args[2];

        try
        {
            // connect
            f = new FtpUtils(ftpServer);
            if (f.openAConnection(ftpServer))
            {
                System.out.println("Main: Connection established");
            } // if getConnection

            // login
            if (f.login(userName, password))
            {
                System.out.println("Main: logged in");
            }
            else
            {
                System.out.println("Main: not logged in; warning Will Robinson");
            } // end if-login-else

            // PWD
            if (f.sendCommand("PWD" + f.eol, true))
            {
                System.out.println("Main: PWD");
                System.out.println("PWD results: " + f.cmdResponse);
            }
            else
            {
                System.out.println("Main: PWD failed; warning Will Robinson");
            } // end if-PWD-else

            // ::::::::::::::::::::::::::::::::::::::::::::::::::::
            // open a dataconnection to list a directory
            if (f.openDataConnection())
            {
                // if ( f.openDataConnection() ) {
                System.out.println("Passive Data Connection: open");
                if (f.sendCommand("NLST -CF *.txt" + f.eol))
                {
                    System.out.println("Main: List: Success");

                    if (f.getData())
                    {
                        System.out.println("    Passive getData: Success");
                    }
                    else
                    {
                        System.out.println("    Passive getData: Failure");
                    } // end if-getData

                }
                else
                {
                    System.out.println("Main: List: Failure");
                } // end if-LIST
                f.closeDataConnection();
            }
            else
            {
                System.out.println(
                    "PASV Data Connection failure; warning Will Robinson");
            } // end if-openDataConnection-else

            // ::::::::::::::::::::::::::::::::::::::::::::::::::::
            // open a dataconnection to download a file
            if (f.openPassiveDataConnection())
            {
                System.out.println("Data Connection: open");
                // prepare to time this
                f.resetTime();
                f.setStartTime();
                if (f.sendCommand("RETR" + " web_groups.txt" + f.eol))
                {
                    System.out.println("Main: RETR web_groups.txt: Success");

                    if (f.getData())
                    {
                        System.out.println("OpenDataConnection: got here");
                        f.setStopTime();
                        System.out.println("    getData: Success");
                        System.out.println(
                            "RETR" + " executed in " + f.getDuration()
                            + " milliseconds.");
                    }
                    else
                    {
                        f.setStopTime();
                        System.out.println("    getData: Failure");
                    } // end if-getData

                }
                else
                {
                    System.out.println("Main: RETR: Failure");
                } // end if-sendCommand
                f.setStopTime();
                f.closeDataConnection();
            }
            else
            {
                System.out.println(
                    "Data Connection: not; warning Will Robinson");
            } // end if-openDataConnection-else

            // upload a file
            if (f.openPassiveDataConnection())
            {
                System.out.println("Data Connection: open");
                // prepare to time this
                f.resetTime();
                f.setStartTime();
                if (f.sendCommand("STOR" + " bogus.txt" + f.eol))
                {
                    System.out.println("Main: STOR web_groups.txt: Success");
                    if (f.putData())
                    {
                        System.out.println("OpenDataConnection: got here");
                        f.setStopTime();
                        System.out.println("    getData: Success");
                        System.out.println(
                            "STOR"
                            + " executed in "
                            + f.getDuration()
                            + " milliseconds.");
                    }
                    else
                    {
                        f.setStopTime();
                        System.out.println("    getData: Failure");
                    } // end if-getData

                }
                else
                {
                    System.out.println("Main: STOR: Failure");
                } // end if-sendCommand
                f.setStopTime();
                // contained in putData: f.closeDataConnection();
            }
            else
            {
                System.out.println("Data Connection: STOR failed");
            } // end if-openPassiveDataConnection-else

            // stop the FTP connection
            if (f.closeAConnection())
            {
                System.out.println("Main: All done");
            }
            else
            {
                System.out.println("Main: not done; warning Will Robinson");
            } // end if-closeAConnection-else

        }
        catch (IOException ioe)
        {
            System.out.println("Main: IOException(new socket): " + ioe);
            System.exit(1);
        }

    } // end of main
} // end of class
