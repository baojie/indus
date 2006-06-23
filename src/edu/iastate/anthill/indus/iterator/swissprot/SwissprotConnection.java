package edu.iastate.anthill.indus.iterator.swissprot;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import edu.iastate.anthill.indus.IndusDB;

import edu.iastate.utils.io.IOUtils;
import edu.iastate.utils.io.URLUtils;
import edu.iastate.utils.string.StringUtils;

/**
 * @author Jie Bao
 * @since 2005-02-08
 */
public class SwissprotConnection
    extends IndusDB
{

    final public static String SPROT_BASE_URL = "http://us.expasy.org/sprot/";
    final public static String SPROT_RAW =
        "http://us.expasy.org/cgi-bin/get-sprot-raw.pl?"; //P01308
    public static String ID2URL_SPROT_RAW(String ID)
    {
        return SPROT_RAW + ID;
    }

    public SwissprotConnection()
    {

    }

    /**
     * To test if SwissProt database can be connected.
     *
     * @return YES or NO
     * @author Jie Bao
     * @version 2005-02-10
     */
    public boolean isConnectable()
    {
        return URLUtils.isURLValidate(SPROT_BASE_URL);
    }

    static public boolean isIDValid(String ID)
    {
        // most error can be checked here
        if (StringUtils.isStringEmpty(ID))
        {
            return false;
        }
        // like P01308  or Q8QVU9
        if ( (ID.length() == 6) && !Character.isDigit(ID.charAt(0)))
        {
            // go to swissprot do a remote check
            return true;
            //return URLUtils.isURLValidate(ID2URL_MMCIF(ID)) ;
        }
        else
        {
            return false;
        }
    }

    public void addEntry(String ID)
    {
        // read from swissprot site
        String str = doQueryRaw(ID);
        //str = str.replaceAll(";","!!!");
        //str = str.replaceAll("\n","***");

        // add it into the database
        // if it's already there

        try
        {
            String where = " WHERE id = '" + ID + "'";
            Statement st = db.createStatement();
            ResultSet rs = st.executeQuery(
                "SELECT id FROM swissprot" + where);

            if (rs.next())
            {
                System.out.print("Column 1 returned ");
                System.out.println(rs.getString(1));
            }
            else
            {
                // insert new
                String sss = "INSERT INTO swissprot (id, raw)" +
                    "VALUES ('" + ID + "', '" + "" + "')";
                System.out.println(sss);

                PreparedStatement updatest = db.prepareStatement(sss);
                updatest.executeUpdate();
                updatest.close();
                System.out.println("insert");
            }
            rs.close();
            st.close();
        }
        catch (SQLException ex)
        {
            ex.printStackTrace();
        }

    }

    /**
     * Get SwissProt plain text using  ID
     *
     * @param ID
     * @return
     */
    static public String doQueryRaw(String ID)
    {
        if (!isIDValid(ID))
        {
            System.err.println("SwissProt ID is illegal");
        }

        String pSourceURL = ID2URL_SPROT_RAW(ID);
//    JOptionPane.showMessageDialog(null, "Now try to query the PDB database");

        // Read the plain text
        return IOUtils.readTextFromURL(pSourceURL);
    }

    public void test()
    {
        connect();
        addEntry("P01308");
        disconnect();
    }
}
