package edu.iastate.anthill.indus.agent;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import edu.iastate.anthill.indus.IndusConstants;
import edu.iastate.anthill.indus.datasource.type.AVH;

import edu.iastate.utils.Console;
import edu.iastate.utils.Debug;
import edu.iastate.utils.io.FileUtils;
import edu.iastate.utils.log.Logging;

/**
 *  Agent to communicate with data source editor, as TCP server
 * @author Jie Bao
 * @since 1.0
 */

public class IndusHttpServer
    extends Thread implements IndusCommand
{
    String m_rootPath;
    String port = "" + IndusConstants.DSPORT;
    static boolean running = true;

    final static public int DATATYPE = 0, SCHEMA = 1, MAPPING = 2, VIEW = 3;

    static final Console output = null;
    static void showConsole()
    {
        try
        {
            final Console output = new Console();
            output.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        }
        catch (IOException e)
        {
        }
    }

    public IndusHttpServer(String m_rootPath, String port)
    {
        this.m_rootPath = m_rootPath;

        if (port != null)
        {
            this.port = port;
        }

    }

    private String typeFile(String type)
    {
        return m_rootPath + "\\data\\type\\" + type + ".xml";
    }

    private String schemaFile(String schema)
    {
        return m_rootPath + "\\data\\schema\\" + schema + ".xml";
    }

    private String mappingFile(String name)
    {
        return m_rootPath + "\\data\\mapping\\" + name + ".xml";
    }

    private String viewFile(String view)
    {
        return m_rootPath + "\\data\\view\\" + view + ".xml";
    }

    private String getFilePath(String name, int type)
    {
        String path[] = new String[4];
        path[DATATYPE] = typeFile(name);
        path[SCHEMA] = schemaFile(name);
        path[MAPPING] = mappingFile(name);
        path[VIEW] = viewFile(name);
        return path[type];
    }

    public void run()
    {
        //String log = m_rootPath + "\\data\\datatype.txt";
        Logging logger = new Logging(m_rootPath + "\\log\\", "DSServer");

        String schemadir = m_rootPath + "\\data\\schema";
        String mappingdir = m_rootPath + "\\data\\mapping";
        String typedir = m_rootPath + "\\data\\type";
        String viewdir = m_rootPath + "\\data\\view";

        try
        {
            String clientSentence;

            ServerSocket welcomeSocket = new ServerSocket(Integer.parseInt(port));

            String icon = "images/db.gif";
            String hint = "INDUS Server launched at port " + port +
                "\nRoot path " + m_rootPath +
                "\nDouble click to shut down";
            String title = "INDUS Server launched";
            String details = "Port " + port + "\nRoot path " +
                m_rootPath;

            MyTrayNotifier t = new MyTrayNotifier(icon, hint, title, details);

            String logInfo =
                "\n==============\n" +
                new Date().toString() + " " +
                "INDUS Server started at port " +
                port + "\n";
            logger.saveLogbyDate(logInfo);

            System.out.println("\nINDUS Server launched at " +
                               new Date().toString());
            System.out.println("Basis path: " + m_rootPath);
            System.out.println("Port: " + port);
            System.out.println("Host: " + welcomeSocket.getInetAddress());

            while (true)
            {
                Socket connectionSocket = welcomeSocket.accept();

                BufferedReader inFromClient =
                    new BufferedReader(new InputStreamReader(
                        connectionSocket.
                        getInputStream()));

                DataOutputStream outToClient = new DataOutputStream(
                    connectionSocket.getOutputStream());

                clientSentence = inFromClient.readLine();
                logger.saveLogbyDate(new Date().toString() +
                                     " from client " +
                                     connectionSocket.getInetAddress() + " : "
                                     + clientSentence + "\n"
                    );

                if (clientSentence.startsWith(CMD_HELLO))
                {
                    handleHello(outToClient);
                }
                else if (clientSentence.startsWith(CMD_GET_ALL_TYPE))
                {
                    handleGetAllType(typedir, outToClient);
                }
                else if (clientSentence.startsWith(CMD_NEW_TYPE))
                {
                    handleNewType(outToClient, clientSentence);
                }
                else if (clientSentence.startsWith(CMD_GET_TYPE_DETAILS))
                {
                    handleGetDetails(outToClient, clientSentence, this.DATATYPE);
                }
                else if (clientSentence.startsWith(CMD_UPDATE_TYPE))
                {
                    handleUpdate(outToClient, clientSentence, this.DATATYPE);
                }
                else if (clientSentence.startsWith(CMD_UPDATE_SCHEMA))
                {
                    handleUpdate(outToClient, clientSentence, this.SCHEMA);
                }
                else if (clientSentence.startsWith(CMD_GET_ALL_SCHEMA))
                {
                    handleGetAll(schemadir, outToClient);
                }
                else if (clientSentence.startsWith(CMD_GET_SCHEMA_DETAILS))
                {
                    handleGetDetails(outToClient, clientSentence, this.SCHEMA);
                }
                else if (clientSentence.startsWith(CMD_UPDATE_MAPPING))
                {
                    handleUpdate(outToClient, clientSentence, this.MAPPING);
                }
                else if (clientSentence.startsWith(CMD_GET_ALL_MAPPING))
                {
                    handleGetAll(mappingdir, outToClient);
                }
                else if (clientSentence.startsWith(CMD_GET_MAPPING_DETAILS))
                {
                    handleGetDetails(outToClient, clientSentence, this.MAPPING);
                }
                else if (clientSentence.startsWith(CMD_DELETE_TYPE))
                {
                    handleDelete(outToClient, clientSentence, this.DATATYPE);
                }
                else if (clientSentence.startsWith(CMD_DELETE_SCHEMA)) // 2004-10-11
                {
                    handleDelete(outToClient, clientSentence, this.SCHEMA);
                }
                else if (clientSentence.startsWith(CMD_DELETE_MAPPING)) // 2004-10-13
                {
                    handleDelete(outToClient, clientSentence, this.MAPPING);
                }
                else if (clientSentence.startsWith(CMD_GET_ALL_VIEW)) // 2005-03-23
                {
                    handleGetAll(viewdir, outToClient);
                }
                else if (clientSentence.startsWith(CMD_DELETE_VIEW)) // 2005-03-23
                {
                    handleDelete(outToClient, clientSentence, this.VIEW);
                }
                else if (clientSentence.startsWith(CMD_GET_VIEW_DETAILS))
                {
                    handleGetDetails(outToClient, clientSentence, this.VIEW);
                }
                else if (clientSentence.startsWith(CMD_UPDATE_VIEW))
                {
                    handleUpdate(outToClient, clientSentence, this.VIEW);
                }
                else
                {
                    outToClient.writeBytes(RES_UNKNOWN_CMD +
                                           '\n');
                }
                if (!running)
                {
                    break;
                }
            }
            logInfo = "INDUS Server closed: " +
                new Date().toString() + "\n";
            logger.saveLogbyDate(logInfo);
        }
        catch (IOException ex)
        {
            String feed = "INDUS Server Error: " +
                new Date().toString() + " " + ex.getMessage() + "\n" +
                "Root path = " + this.m_rootPath +
                " , port = " + this.port + "\n";
            logger.saveLogbyDate(feed);
            Debug.trace(feed + "\n You can edit the configuration file " +
                        this.configFileName + " to change the rootpath or name");
        }

    }

    /**
     * Delete a specified file
     * @param outToClient DataOutputStream
     * @param clientSentence String
     * @param filePath String
     * @since 2005-03-23
     */
    private void handleDelete(DataOutputStream outToClient,
                              String clientSentence, int type)
    {
        try
        {
            String data[] = clientSentence.split(";");
            if (data[1].startsWith("name="))
            {
                String parts[] = data[1].split("=", 2);
                String name = parts[1];
                String filePath = getFilePath(name, type);

                if (FileUtils.isFileExists(filePath))
                {
                    boolean success = (new File(filePath)).delete();
                    if (success)
                    {
                        outToClient.writeBytes(RES_OK + "\n");
                    }
                }
            }
            outToClient.writeBytes(RES_GENERAL_ERROR + "\n");
        }
        catch (IOException ex)
        {
        }
    }

    /**
     * handleGetMappingDetails
     *
     * @param outToClient DataOutputStream
     * @param clientSentence String
     * @since 2004-10-03
     */
    private void handleGetDetails(DataOutputStream outToClient,
                                  String clientSentence, int type)
    {
        try
        {
            String data[] = clientSentence.split(";");
            if (data[1].startsWith("name="))
            {
                String parts[] = data[1].split("=", 2);
                String name = parts[1];
                String filePath = getFilePath(name, type);

                if (FileUtils.isFileExists(filePath))
                {
                    // make sure the xml file has no "\n"!!!
                    outToClient.writeBytes(FileUtils.readFile(filePath) + "\n");
                }
                else
                {
                    outToClient.writeBytes(RES_GENERAL_ERROR + "\n");
                }
            }
            else
            {
                outToClient.writeBytes(RES_GENERAL_ERROR + "\n");
            }
        }
        catch (IOException ex)
        {
        }
    }

    /**
     * handleUpdateMapping: create new or update existing file
     *
     * @param outToClient DataOutputStream
     * @param clientSentence String
     * @since 2004-10-03
     */
    private void handleUpdate(DataOutputStream outToClient,
                              String clientSentence, int type)
    {
        try
        {
            String data[] = clientSentence.split(";");
            if (data[1].startsWith("name="))
            {
                String parts[] = data[1].split("=", 2);
                String name = parts[1];
                String filePath = getFilePath(name, type);

                if (data.length > 2 && data[2].startsWith("value="))
                {
                    parts = data[2].split("=", 2);
                    String MappingText = parts[1];
                    FileUtils.writeFile(filePath,
                                        MappingText); // don't use any "\n"!!!

                }
                outToClient.writeBytes(RES_OK + "\n");
            }
            else
            {
                outToClient.writeBytes(RES_GENERAL_ERROR + "\n");
            }
        }
        catch (IOException ex)
        {
        }

    }

    /**
     * handleHello
     *
     * @param outToClient DataOutputStream
     * @version 2004-10-02
     */
    private void handleHello(DataOutputStream outToClient)
    {
        try
        {
            outToClient.writeBytes(RES_OK + "\n");
        }
        catch (IOException ex)
        {
        }
    }

    /**
     * handle GetAllSchema and GetAllMapping and GetAllView
     *
     * @param outToClient DataOutputStream
     * @param clientSentence String
     * @version 2004-09-30
     */
    private void handleGetAll(String path, DataOutputStream outToClient)
    {
        try
        {
            File dir = new File(path);

            // It is also possible to filter the list of returned files.
            // This example does not return any files that start with `.'.
            FilenameFilter filter = new FilenameFilter()
            {
                public boolean accept(File dir, String name)
                {
                    return name.endsWith(".xml");
                }
            };

            File[] files = dir.listFiles(filter);

            String res = "";
            for (int i = 0; i < files.length; i++)
            {
                String name = FileUtils.findName(files[i].getName());
                if (i > 0)
                {
                    res = res + ";" + name;
                }
                else
                {
                    res = name;
                }
            }

            outToClient.writeBytes(res + "\n");
        }
        catch (IOException e)
        {
        }
    }

    /**
     * handleNewType
     *
     * @param alltypes String
     * @param outToClient DataOutputStream
     */
    private void handleNewType(DataOutputStream outToClient,
                               String clientSentence)
    {
        try
        {
            String data[] = clientSentence.split(";");
            if (data[1].startsWith("name="))
            {
                String parts[] = data[1].split("=", 2);
                String name = parts[1];

                if (data.length > 2 && data[2].startsWith("type="))
                {
                    parts = data[2].split("=", 2);
                    String type = parts[1];
                    StringBuffer buf = new StringBuffer();
                    buf.append(
                        "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>" +
                        "<type>" +
                        "<subTypeOf>" + type + "</subTypeOf>");
                    if (type.equals("AVH"))
                    {
                        buf.append("<root>" + name + "_AVH" + "</root>");
                        // read DataTypePanel.XML2Tree()
                    }
                    buf.append("</type>"); // don't use any "\n"!!!
                    FileUtils.writeFile(typeFile(name), buf.toString());

                }
                outToClient.writeBytes(RES_OK + "\n");
            }
            else
            {
                outToClient.writeBytes(RES_GENERAL_ERROR + "\n");
            }
        }
        catch (IOException ex)
        {
        }
    }

    private void handleGetAllType(String path, DataOutputStream outToClient)
    {
        try
        {
            File dir = new File(path);

            // It is also possible to filter the list of returned files.
            // This example does not return any files that start with `.'.
            FilenameFilter filter = new FilenameFilter()
            {
                public boolean accept(File dir, String name)
                {
                    return name.endsWith(".xml");
                }
            };

            File[] files = dir.listFiles(filter);

            String res = AVH.DEFAULT_TYPES;
            for (int i = 0; i < files.length; i++)
            {
                String name = FileUtils.findName(files[i].getName());
                res = res + ";" + name;
            }
            outToClient.writeBytes(res + "\n");
        }
        catch (IOException e)
        {
        }
    }

    /**
     *
     * @return boolean
     * @since 2005-03-17
     */
    static private boolean pathValid(String path)
    {
        try
        {
            return FileUtils.isFileExists(path + "\\data");
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            return false;
        }
    }

    static class MyTrayNotifier
        extends TrayNotifier
    {
        public MyTrayNotifier(String imagePath, String iconText,
                              String baloonTitle,
                              String baloonText)
        {
            super(imagePath, iconText, baloonTitle, baloonText);
        }

        public void onDoubleClick()
        {
            int answer = JOptionPane.showConfirmDialog(this,
                "Shut down INDUS server?");
            if (answer == JOptionPane.YES_OPTION)
            {
                removeTrayIcon();
                System.exit(0);
            }
        }

        public void onRightClick()
        {
        }
    }

    static String configFileName = "indus-server.config.txt";

    /**
     * @since 2005-03-25
     * @return boolean
     */
    boolean loadConfig()
    {
        try
        {
            BufferedReader in = new BufferedReader(new FileReader(
                configFileName));
            m_rootPath = in.readLine();
            port = in.readLine();
            in.close();
            return (m_rootPath != null) && (port != null);
        }
        catch (IOException e)
        {
            return false;
        }
    }

    /**
     * @since 2005-03-25
     */
    void saveConfig()
    {
        try
        {
            BufferedWriter out = new BufferedWriter(new FileWriter(
                configFileName));
            out.write(m_rootPath + "\n" + this.port);
            out.close();
        }
        catch (IOException e)
        {
        }

    }

    /**
     * Create empty directories to save data
     * @since 2005-03-25
     */
    void initializeDirectory()
    {
        String schemadir = m_rootPath + "\\data\\schema";
        String mappingdir = m_rootPath + "\\data\\mapping";
        String typedir = m_rootPath + "\\data\\type";
        String viewdir = m_rootPath + "\\data\\view";
        String logdir = m_rootPath + "\\log";

        (new File(schemadir)).mkdirs();
        (new File(mappingdir)).mkdirs();
        (new File(typedir)).mkdirs();
        (new File(viewdir)).mkdirs();
        (new File(logdir)).mkdirs();
    }

    /**
     * Launch the server independently
     * @param args String[]
     * @since 2004-10-07
     *
     * Example of use: IndusHttpServer c:\baojie 2006
     *                 IndusHttpServer c:\baojie
     * Make sure the folder has subfolder data, data\types, data\schema, data\mapping
     *       data\view
     */
    public static void main(String[] args)
    {
        IndusHttpServer.showConsole();
        for (int i = 0; i < args.length; i++)
        {
            System.out.println("args[" + i + "]= " + args[i]);
        }

        IndusHttpServer server = new IndusHttpServer(null, null);

        if (!server.loadConfig())
        {
            if (args.length > 2)
            {
                System.out.println(
                    "USAGE: IndusHttpServer localDataFolder [port]");
                return;
            }

            String rootPane = "";
            String port = IndusConstants.DSPORT + "";
            if (args.length == 2)
            {
                rootPane = args[0];
                port = args[1];
            }
            else if (args.length == 1)
            {
                rootPane = args[0];
            }

            else if (args.length == 0)
            {
                rootPane = JOptionPane.showInputDialog(
                    "Please give the root path");
                if (rootPane == null)
                {
                    return;
                }
                port = JOptionPane.showInputDialog(
                    "Please give the port", port);
                if (port == null)
                {
                    return;
                }
            }

            if (!IndusHttpServer.pathValid(rootPane))
            {
                System.out.println(rootPane +
                                   " is not a valid path, please try again");
                return;
            }

            server.m_rootPath = rootPane;
            server.port = port;

            server.saveConfig();
        }
        try
        {
            if (!IndusHttpServer.pathValid(server.m_rootPath + "\\data"))
            {
                server.initializeDirectory();
            }
            server.start();
        }
        catch (Exception ex)
        {
            Debug.trace("\nINDUS Data Source Editor can't be started!");
            ex.printStackTrace();
        }
    }
}
