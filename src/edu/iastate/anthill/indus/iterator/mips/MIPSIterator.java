package edu.iastate.anthill.indus.iterator.mips;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import edu.iastate.anthill.indus.IndusDB;

import edu.iastate.utils.io.IOUtils;
import edu.iastate.utils.net.ftp.FtpUtils;
import edu.iastate.utils.sql.pgJDBCUtils;
import edu.iastate.anthill.indus.*;

/**
 *
 * MIPS Functional Catalogue ftp://ftpmips.gsf.de/catalogue/funcat-2.0_scheme
 *
 * @author Jie Bao
 * @since 1.0 2005-03-04
 */
public class MIPSIterator
    extends IndusDB
{
    public static void test()
    {
        MIPSIterator mips = new MIPSIterator();

        mips.connect(IndusConstants.dbURL);
        mips.clearAllData();
        mips.loadToDB();
        mips.disconnect();
    }

    /**
     * loadToDB
     */
    private void loadToDB()
    {
        loadFunCat();

    }

    /**
     * loadFunCat
     * Note: a Unix host can only accept passive ftp visit
     */
    private void loadFunCat()
    {
        //String url = "ftp://ftpmips.gsf.de/catalogue/funcat-2.0_scheme";
        try
        {
            FtpUtils ftp = new FtpUtils();
            BufferedReader in = IOUtils.openUnixFtpInputStream("ftpmips.gsf.de",
                "Anonymous", "", "/catalogue/funcat-2.0_scheme", ftp);
            String str, header = "";
            int count = 0;
            // read it line by line
            while ( (str = in.readLine()) != null)
            {
                if (str.matches("\\s*")) // blank
                {
                    continue;
                }
                else if (str.startsWith("#")) // header
                {
                    header += str + "\n";
                }
                else // an entry
                {
                    // 2 colums divided by the first space
                    // eg:
                    // 01.02.01.09 catabolism of nitrogenous compounds
                    // 01.02.01.09.01 urea catabolism (not urea cycle)
                    int blank = str.indexOf(" ");
                    String id = str.substring(0, blank).trim();
                    int lastdot = id.lastIndexOf(".");
                    String parent = (lastdot == -1) ? "" :
                        id.substring(0, lastdot);
                    String des = str.substring(blank + 1, str.length()).trim();

                    Map map = new HashMap();
                    map.put("mips_id", id);
                    map.put("parent", parent);
                    map.put("description", des);

                    System.out.print(++count + " : ");
                    if (!pgJDBCUtils.insertDatabase(db, "mips", map))
                    {
                        //Debug.pause();
                    }
                    map.clear();

                }
            } // while
            ftp.closeDataConnection();
            // insert header
        }
        catch (IOException ex)
        {
            ex.printStackTrace();
        }

    }

    /**
     * clearAllData
     */
    private void clearAllData()
    {
        pgJDBCUtils.clearTable(db, "mips");
    }

    public static void main(String[] args)
    {
        test();
    }
}
