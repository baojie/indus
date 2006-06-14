package edu.iastate.utils.io;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

import edu.iastate.utils.Utility;
import edu.iastate.utils.net.ftp.FtpUtils;

/**
 */
public abstract class IOUtils
{
    /**
     * Private constructor to avoid accidental instantiation.
     */
    private IOUtils()
    {};

    /**
     * Block size used when reading the input stream.
     */
    private static final int BLOCK_SIZE = 1024;

    /**
     * Reads given input stream into a String.
     * @param inputStream the input stream to read.
     * @param contentLength the length of the content.
     * @return the input stream contents, or an empty string if the operation
     * fails.
     */
    public static String read(InputStream inputStream, int contentLength)
    {
        StringBuffer t_sbResult = new StringBuffer();

        if (inputStream != null)
        {
            if (contentLength > 0)
            {
                try
                {
                    InputStreamReader t_isrReader =
                        new InputStreamReader(inputStream);

                    char[] t_acContents = new char[contentLength];

                    if (t_isrReader.ready())
                    {
                        t_isrReader.read(t_acContents);
                    }

                    t_sbResult.append(t_acContents);
                }
                catch (IOException ioException)
                {
                    /*
                     * Exception management is missing.
                     */
                }
            }
            else
            {
                try
                {
                    InputStreamReader t_isrReader =
                        new InputStreamReader(inputStream);

                    char[] t_acContents = new char[BLOCK_SIZE];

                    while (t_isrReader.ready())
                    {
                        int t_iCharsRead = t_isrReader.read(t_acContents);

                        char[] t_acCharsRead =
                            (t_iCharsRead == BLOCK_SIZE)
                            ? t_acContents
                            : Utility.subBuffer(
                                t_acContents, 0, t_iCharsRead);

                        t_sbResult.append(t_acCharsRead);
                    }
                }
                catch (IOException ioException)
                {
                    /*
                     * Exception management is missing.
                     */
                }
            }
        }

        return t_sbResult.toString();
    }

    /**
     * Reads an input stream and returns its contents.
     * @param input the input stream to be read.
     * @return the contents of the stream.
     * @throws IOException whenever the operation cannot be accomplished.
     */
    public static String read(InputStream input) throws IOException
    {
        StringBuffer t_sbResult = new StringBuffer();

        if (input != null)
        {
            /*
             * Instantiating an InputStreamReader object to read the contents.
             */
            InputStreamReader t_isrReader = new InputStreamReader(input);

            /*
             * It's faster to use BufferedReader class.
             */
            BufferedReader t_brBufferedReader =
                new BufferedReader(t_isrReader);

            String t_strLine = t_brBufferedReader.readLine();

            while (t_strLine != null)
            {
                t_sbResult.append(t_strLine + "\n");

                t_strLine = t_brBufferedReader.readLine();
            }
        }

        /*
         * End of the method.
         */
        return t_sbResult.toString();
    }

    /**
     * Reads the contents of an input stream and returns its contents, if
     * possible. If some exception occurs, returns an empty String.
     * @param input the input stream to be read.
     * @return the contents of the stream, or empty if reading cannot be
               accomplished.
     */
    public static String readIfPossible(InputStream input)
    {
        String result = new String();

        try
        {
            result = read(input);
        }
        catch (IOException ioException)
        {
            /*
             * We have chosen not to notify of exceptions, so this
             * block of code is only descriptive.
             */
        }

        return result;
    }

    static public String readURL(URL url)
    {
        StringBuffer content = new StringBuffer();

        // Read the plain text
        try
        {
            // Create a URL for the desired url

            // Read all the text returned by the server
            BufferedReader in = new BufferedReader(new InputStreamReader(url.
                openStream()));
            String str;
            while ( (str = in.readLine()) != null)
            {
                // str is one line of text; readLine() strips the newline character(s)
                content.append(str + "\n");
            }
            in.close();
        }
        catch (IOException e)
        {
            System.err.println("IO Error in query" + url);
            return null;
        }
        return content.toString();
    }

    static public String readURL(String pSourceURL)
    {
        try
        {
            URL url = new URL(pSourceURL);
            return readURL(url);
        }
        catch (MalformedURLException e)
        {
            System.err.println("URL is illegal");
        }
        return null;
    }

    static public String readTextFromURL(String pSourceURL)
    {
        StringBuffer rawText = new StringBuffer();

        // Read the plain text
        try
        {
            // Create a URL for the desired url
            URL url = new URL(pSourceURL);

            // Read all the text returned by the server
            BufferedReader in = new BufferedReader(new InputStreamReader(url.
                openStream()));
            String str;
            while ( (str = in.readLine()) != null)
            {
                // str is one line of text; readLine() strips the newline character(s)
                rawText.append(str + "\n");
            }
            in.close();
        }
        catch (MalformedURLException e)
        {
            System.err.println("URL is illegal");
        }
        catch (IOException e)
        {
            System.err.println("IO Error in query URL");
        }
        return rawText.toString();
    }

    /**
     *
     * @param stream InputStream
     * @return BufferedReader
     * @since 2005-03-05
     * @author Jie Bao
     */
    static public BufferedReader openInputStream(InputStream stream)
    {
        // Read all the text returned by the server
        BufferedReader in = new BufferedReader(new InputStreamReader(stream));
        return in;

    }

    static public BufferedReader openInputStream(String pSourceURL)
    {
        try
        {
            // Create a URL for the desired url
            URL url = new URL(pSourceURL);

            // Read all the text returned by the server
            return openInputStream(url.openStream());
        }
        catch (MalformedURLException e)
        {
            System.err.println("URL is illegal");
        }
        catch (IOException e)
        {
            System.err.println("IO Error in query URL");
            e.printStackTrace();
        }
        return null;
    }

    /**
     * open a passive ftp session
     * @param ftpServer String
     * @param userName String
     * @param password String
     * @param file String
     * @param ftp FtpUtils  - created as 'new FtpUtils()'. You don't need to open
     *         it explicitly, but have to close it by yourself. s.t.
     *         ftp.closeDataConnection() in your calling  function
     * @return BufferedReader
     *
     * @since 2005-03-05
     * @author Jie Bao
     */
    static public BufferedReader openUnixFtpInputStream(String ftpServer,
        String userName, String password, String file, FtpUtils ftp)
    {
        InputStream stream = ftp.openUnixFtpInputStream(ftpServer, userName,
            password, file);
        return openInputStream(stream);
    }

}
