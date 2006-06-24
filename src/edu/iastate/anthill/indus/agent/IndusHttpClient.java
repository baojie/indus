package edu.iastate.anthill.indus.agent;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.UnknownHostException;

import edu.iastate.anthill.indus.IndusConstants;
import edu.iastate.anthill.indus.datasource.type.AVH;

/**
 * TCP client
 * @author Jie Bao
 * @since 1.0 - 2004-09-23
 */

public class IndusHttpClient implements IndusCommand
{
    protected IndusHttpClient()
    {
    }

    public String sendCmd(String cmd)
    {
        String res;

        try
        {
            Socket clientSocket = new Socket(IndusConstants.DSSERVER,
                                             IndusConstants.DSPORT);

            DataOutputStream outToServer =
                new DataOutputStream(clientSocket.getOutputStream());

            BufferedReader inFromServer =
                new BufferedReader(new InputStreamReader(clientSocket.
                getInputStream()));

            outToServer.writeBytes(cmd + '\n');
            res = inFromServer.readLine();
            clientSocket.close();
            return res;
        }
        catch (UnknownHostException ex)
        {
        }
        catch (IOException ex)
        {
        }
        return null;
    }

    /**
     * @param name String
     * @return String
     * @since 2004-10-08
     */
    protected static String getDetails(String cmd, String name)
    {
        IndusHttpClient client = new IndusHttpClient();
        String datatypeinXML = client.sendCmd(cmd + ";name=" + name);
        if (!RES_GENERAL_ERROR.equals(datatypeinXML))
        {
            return datatypeinXML;
        }
        return null;
    }

    /**
     * Get the super type of the type
     * @param name String
     * @return String
     * @since 2004-10-15
     */
    public static String getSuperType(String name)
    {
        String xml = getDetails(CMD_GET_TYPE_DETAILS, name);
        if (xml != null)
        {
            return AVH.parseSupertype(xml);
        }
        return null;
    }

    /**
     * Get the top level super type of the type
     * @param name String
     * @return String  the top level super type , null if no super type
     * @since 2004-10-15
     */
    public static String getTopSuperType(String name)
    {
        String supertype = getSuperType(name);

        while (supertype != null)
        {
            String t = getSuperType(supertype);
            if (t == null)
            {
                return supertype;
            }
            else
            {
                supertype = t;
            }
        }
        return null;
    }

    /**
     * testServer: test if server is working
     * @author Jie Bao
     * @version 2004-10-02
     */
    public static boolean testServer()
    {
        IndusHttpClient client = new IndusHttpClient();
        String res = client.sendCmd(CMD_HELLO);
        if (res == null)
        {
            return false;
        }
        else if (!res.equals(RES_OK))
        {
            return false;
        }
        return true;
    }

}
