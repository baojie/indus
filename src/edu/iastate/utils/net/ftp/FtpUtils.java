package edu.iastate.utils.net.ftp;

/*
 * :::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
 *		 FILENAME: ftp6.java
 *		CLASSNAME: ftp6
 *		COPYRIGHT: Copyright 1999 Kenneth R. Kress
 * RCS Id: $Id: FtpUtils.java,v 1.1 2006/06/14 22:32:40 baojie Exp $
 * :::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
 * History
 * $Log: FtpUtils.java,v $
 * Revision 1.1  2006/06/14 22:32:40  baojie
 * *** empty log message ***
 *
 *
 */
/*
 * :::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
 *		IMPORTS
 * :::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
 */
// I/O
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.PrintWriter;
// Networking
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
// Date
import java.util.Date;
import java.util.StringTokenizer;
import java.io.File;
import sun.net.TelnetOutputStream;
import sun.net.ftp.FtpClient;
import edu.iastate.utils.io.FileUtils;
import java.io.InputStream;

public class FtpUtils
{
    /*
     * :::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
     *		DESCRIPTION:
     * :::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
     *
     */

    /*
     * It would be nice if there were a way to use a generic term
     * and hide the intricacies from the user. That is, use a
     * generic get and decide in secret whether to open a PORT
     * connection or a PASV connection.
     *
     */
    static String myName = "ftp6";
    static String USAGE = "java " + myName + " [options]" + " [params]";
    static boolean DEBUGGING = true; // turned off by default
    private int controlPort = 21;
    static String eol = "\r\n"; // what FTP/telnet expects

// Instance variables
    Socket cs = null; // main socket for control fcns
    // Socket                  ps = null;   // passive connection socket
    Socket ds = null; // generic data socket
    ServerSocket ss = null; // data socket chosen by client
    int dataPort = 0;
    String serverName = "";
    String userName = "";
    PrintWriter OUT = null;
    PrintWriter DOUT = null; // for second out
    LineNumberReader IN = null;
    BufferedReader DIN = null; // for reading from data connection

    String timedCommand = null;
    // cmdResponse: returned by server after command
    StringBuffer cmdResponse = new StringBuffer("default");

    Date startTime = null;
    Date stopTime = null;

    /*
     * # :::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::	#
     * #		CONSTRUCTORS
     * # :::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::	#
     */

// Constructor

    public FtpUtils()
    {
        super(); // do nothing
    } // end ftp6 constructor

    public FtpUtils(String ftpServer)
    {
        super();
        this.serverName = ftpServer;
    } // end ftp6 constructor

// convenience constructor
    public FtpUtils(String ftpServer, String userName)
    {
        super();
        this.serverName = ftpServer;
        this.userName = userName;
    } // end ftp6 constructor

    /*
     * # :::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::	#
     * #		METHODS
     * # :::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::	#
     */

    public void setControlPort(int newPort)
    {
        this.controlPort = newPort;
    }

    public int getControlPort(int newPort)
    {
        return this.controlPort;
    }

    public boolean sendCommand(String command) throws IOException
    {
        return (sendCommand(command, false));
    }

    public boolean sendCommand(String command, boolean saveResults) throws
        IOException
    {
        boolean answer;
        // reset cmdResponse
        cmdResponse.setLength(0); // truncate the old cmdResponse
        resetTime();
        setTimedCommand(command);
        setStartTime();
        System.out.println("in sendCommand: " + command);
        OUT.write(command);
        OUT.flush();
        System.out.println("in sendCommand: waiting for response to: " +
                           command);
        answer = getResponse(saveResults);
        System.out.println("sendCommand: answer: " + answer);
        setStopTime();
        System.out.println(
            "Timed Command: "
            + command
            + ": "
            + getDuration()
            );
        return (answer);
    } // end sendCommand

    public void setTimedCommand(String command)
    {
        timedCommand = command;
    } // end setTimedCommand

    public String getTimedCommand()
    {
        return (timedCommand);
    } // end setTimedCommand

    public void setStartTime()
    {
        startTime = new Date();
    } // end startTime

    public void setStopTime()
    {
        stopTime = new Date();
    } // end startTime

    public void resetTime()
    {
        stopTime = startTime = null;
        timedCommand = null;
    } // end startTime

    public long getDuration()
    {
        return (stopTime.getTime() - startTime.getTime());
    }

// getResponse()
//      read from the Reader a line at a time and check
//      the numeric codes prefixed to each line until
//      the first three digits are followed by a space
//      return true or false (success or failure)
    // error code explanation
    // 1xx  Positive preliminary reply
    // 2xx  Positive completion reply
    // 3xx  Positive Intermediate reply
    // 4xx  Negative transient completion reply
    // 5xx  Negative permanent completion reply
    public boolean getResponse() throws IOException
    {
        return (getResponse(false));
    } // end getResponse

    public boolean getResponse(boolean saveResults) throws IOException
    {
        // read until we get three digits and a space
        String line = null;
        boolean answer = false;
        String emsg = null;

        System.out.println("In getResponse: saveResults: " + saveResults);
        for (; ; )
        {
            line = IN.readLine();
            // append response
            emsg = extractMsg(line);
            System.out.println("getResponse: emsg: " + emsg);
            if (saveResults)
            {
                cmdResponse.append(emsg);
            }
            // read until the fourth character is not a hyphen
            if ( (line.length() > 3) && (line.charAt(3) != '-'))
            {
                break;
            } // end if-third char is not a hyphen
        } // end for

        // return true if successful, false otherwise

        if (line.substring(0, 1).equals("1") ||
            line.substring(0, 1).equals("2") ||
            line.substring(0, 1).equals("3"))
        {
            return true;
        }
        else
        {
            return false;
        } // end if-first char is 1 2 or 3
    } // end getResponse

// extracts message returned by server (chars 4 to ...)
    public String extractMsg(String msg)
    {
        String answer = "extractMsg failed";
        if (msg.length() > 4)
        {
            answer = msg.substring(4);
        } // end if msg.length > 4
        return answer;
    } // end extactMsg

// openAConnection(ftpServerName):
    public boolean openAConnection(String ftpServerName) throws IOException
    {
        // create s socket between client and server's control port (21)
        serverName = ftpServerName;
        cs = new Socket(serverName, controlPort);
        OUT = new PrintWriter(cs.getOutputStream(), true);
        InputStreamReader BIN = new InputStreamReader(cs.getInputStream());
        IN = new LineNumberReader(BIN);

        return (getResponse());
    } // end getConnection

// closeAConnection()
    public boolean closeAConnection()
    {
        try
        {
            cs.close();
            return true;
        }
        catch (IOException ioe)
        {
            System.out.println("IOException in closeAConnection: " + ioe);
            return false;
        } // end try-catch
    } // end closeAConnection

// login:
    public boolean login(String username, String password) throws IOException
    {
        userName = username;
        if (sendCommand("USER " + userName + eol) == false)
        {
            return false;
        }
        if (sendCommand("PASS " + password + eol) == false)
        {
            return false;
        }
        return true;
    } // end login

    public boolean openDataConnection() throws IOException
    {
        // let a random port be chosen (by whom?)
        StringBuffer dataReq = new StringBuffer("PORT ");
        ss = new ServerSocket(0);
        ds = null; // is this necessary?
        dataPort = ss.getLocalPort();
        InetAddress myAddr = ss.getInetAddress();
        InetAddress myIP = myAddr.getLocalHost();
        byte[] ipByteArray = myIP.getAddress();
        int tmpint = 0;

        // construct the ip octets and port: 10,128,3,1,5,42 (port 1322)
        for (int i = 0; i < ipByteArray.length; i++)
        {
            tmpint = ipByteArray[i];
            if (tmpint < 0)
            {
                tmpint += 256;
            } // make sure its positive: 0-255
            dataReq.append(tmpint).append(",");
        } // end for loop

        dataReq.append( (dataPort & 0xff00) >> 8).append(",");
        dataReq.append(dataPort & 0x00ff);
        return (sendCommand(dataReq + eol));
    } // end openDataConnection

// What should be done is to create a connection object/thread
// that is either an active or a passive connection.
// Regardless of the type, the objects should respond to
// the same requests.
    public boolean openPassiveDataConnection() throws IOException
    {

        boolean saveResults = true;
        ss = null; // is this necessary?
        // tell the server we want a passive connection
        System.out.println("Sending PASV");

        sendCommand("PASV" + eol, saveResults);

        // 19991031: Seem to be stuck in getResponse. Why aren't
        //      we getting a response.
        //  Answer: because we didn't sent eol after PASV
        // if (! sendCommand("PASV") ) return false;
        // stopping here

        String[] ipArray = new String[6];
        // get the string from cmdResponse
        String tmpString = cmdResponse.toString();
        int begin = tmpString.indexOf("(");
        int end = tmpString.indexOf(')');

        String aString = tmpString.substring(begin + 1, end);
        // read through the string (octet,octet,octet,octet,port,port)
        StringTokenizer tokString =
            new StringTokenizer(aString, ",");
        for (int i = 0; i < 6; i++)
        {
            ipArray[i] = tokString.nextToken();
        } // end for
        Integer tmpInteger = null;

        int highbyte = tmpInteger.parseInt(ipArray[4]);
        highbyte = highbyte << 8;

        int lowbyte = tmpInteger.parseInt(ipArray[5]);
        dataPort = (highbyte + lowbyte);

        // good to go Sun., Oct. 24, 1999
        // Create a new socket
        ds = new Socket(serverName, dataPort);

        // set the I/O
        // InputStreamReader BIN = new InputStreamReader( ds.getInputStream() );
        // DIN  = new LineNumberReader(BIN);
        return (true);
    } // end openPassiveDataConnection

// close either ss or ds or both
// return true if we did something; otherwise false
    public boolean closeDataConnection() throws IOException
    {
        boolean answer = false; // default
        dataPort = 0; // reset it
        if (ss != null)
        {
            ss.close();
            answer = true;
            ss = null;
        } // end if-ss
        if (ds != null)
        {
            ds.close();
            answer = true;
            ds = null;
        } // end if-ds
        return answer;
    } // end of closeDataConnection

// see if a serversocket data port is open
// if so, read from it and write to standard out
// else return false
    public boolean getData() throws IOException
    {
        String dataLine = null;
        System.out.println("getData: got here.");
        if (ds == null)
        {
            System.out.println("getData: DS is null.");
        }
        // first try the ss (PORT) and then the ds
        if (ds != null)
        {
            System.out.println("getData: DS not null");
        }
        else if (ss != null)
        {
            System.out.println("getData: SS not null");
            // try the ss
            // stopping here 991103; program hangs here if
            // a PASV connection is established first
            ds = ss.accept();
            System.out.println("getData: SS.accept");
        }
        else
        {
            System.out.println("getData: SS is null");
            return false;
        } // else if ( ds != null ) {
        // try the ds
        // }  // end if-ss==null

        System.out.println("getData: dataPort: " + dataPort);
        // getData create the DIN reader.

        InputStreamReader BIN = new InputStreamReader(ds.getInputStream());
        DIN = new LineNumberReader(BIN);
        // BufferedReader DIN = new BufferedReader(
        //     new InputStreamReader( ds.getInputStream() )
        // );

        // read until input line equals null
        while ( (dataLine = DIN.readLine()) != null)
        {
            System.out.println(dataLine);
        } // end while

        return (getResponse());
    } // end getData

// see if a serversocket data port is open
// if so, write to it and write to standard out
// else return false
    public boolean putData() throws IOException
    {
        String dataLine = null;
        System.out.println("putData: got here.");
        // first try the ds (PORT) and then the ss
        if (ds != null)
        {
            System.out.println("putData: DS not null");
        }
        else if (ss != null)
        {
            System.out.println("putData: SS not null");
            // try the ss
            ds = ss.accept();
            System.out.println("putData: SS.accept");
        }
        else
        {
            System.out.println("putData: SS is null");
            return false;
        } // else if ( ds != null ) {
        // try the ds
        // }  // end if-ss==null

        System.out.println("putData: dataPort: " + dataPort);
        // putData create the DOUT writer.
        OUT = new PrintWriter(ds.getOutputStream(), true);

        OUT.println("This is a very short file.");

        // stopping here. How does the FTP server know
        // I've stopped sending? Answer: EOF = (byte) 2.
        // or close the connection

        closeDataConnection();
        return (getResponse());
    } // end putData

    /**
     * upload a flie to ftp
     *
     * @param localFile String
     * @param server String
     * @param user String
     * @param passwd String
     * @param remoteFile String
     * @return boolean
     * @throws IOException
     * @author Jie Bao
     * @since 2003-10
     */
    static public boolean putFtp(String localFile, String server, String user,
                                 String passwd, String remoteFile) throws
        IOException
    {
        try
        {
            // create ftp connection
            //      Debug.trace("create ftp connection");

            FtpClient client = new FtpClient();
            //      Debug.trace("openServer");
            client.openServer(server);
            //      Debug.trace("login");
            client.login(user, passwd);
            //      Debug.trace("binary");
            client.binary();

            // upload the file
            //      Debug.trace("upload the file");
            TelnetOutputStream ftpOut = client.put(remoteFile);

            byte[] buffer = FileUtils.getBytesFromFile(new File(localFile));
            ftpOut.write(buffer, 0, buffer.length);
            ftpOut.close();

            // check if it's uploaded
            //      Debug.trace("check if it's uploaded");
            /*      TelnetInputStream in = client.list();
             BufferedReader bin = new BufferedReader(new InputStreamReader(in));
                  String str = new String();
                  while (str != null) {
                    str = bin.readLine();
                    if (str != null) {
                      System.out.println(str);
                      addTextInfo("\n" + str);
                    }
                  }
             */
            //      client.closeServer();
            //      Debug.trace("ftp done");
            return true;

        }
        catch (IOException ex)
        {
            return false;
        }
    }



    /**
     *  remember to close!
     * @return InputStream
     * @since 2005-03-05
     * @author Jie Bao
     */
    public InputStream openUnixFtpInputStream(String ftpServer,
        String userName, String password, String file)
    {
        try
        {
            // connect
            serverName = ftpServer;
            if (!openAConnection(ftpServer))
            {
                return null;
            } // if getConnection
            // login
            if (!login(userName, password))
            {
                return null;
            }
            // ::::::::::::::::::::::::::::::::::::::::::::::::::::
            // open a dataconnection to download a file
            if (openPassiveDataConnection())
            {
                System.out.println("Data Connection: open");
                if (sendCommand("RETR " + file + eol))
                {
                    InputStream stream = getStream();
                    return stream;
                }
                //f.closeDataConnection();
            }
        }
        catch (IOException ex)
        {
            ex.printStackTrace();
        }
        return null;

    }

    public InputStream getStream() throws IOException
    {
        String dataLine = null;
        System.out.println("getData: got here.");
        if (ds == null)
        {
            System.out.println("getData: DS is null.");
        }
        // first try the ss (PORT) and then the ds
        if (ds != null)
        {
            System.out.println("getData: DS not null");
        }
        else if (ss != null)
        {
            System.out.println("getData: SS not null");
            // try the ss
            // stopping here 991103; program hangs here if
            // a PASV connection is established first
            ds = ss.accept();
            System.out.println("getData: SS.accept");
        }
        else
        {
            System.out.println("getData: SS is null");
            return null;
        }

        return ds.getInputStream();
    }
} // end of class
