package edu.iastate.anthill.indus.iterator.ec;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import edu.iastate.anthill.indus.IndusConstants;
import edu.iastate.anthill.indus.IndusDB;
import edu.iastate.utils.io.IOUtils;
import edu.iastate.utils.sql.JDBCUtils;

/**
 * @author Jie Bao , baojie@cs.iastate.edu
 * @since 2005-03-31
 *

 CREATE TABLE ec
 (
 id char(20) NOT NULL,
 name varchar(256),
 parent varchar,
 CONSTRAINT "EC_pkey" PRIMARY KEY (id)
 )
 WITHOUT OIDS;
 ALTER TABLE ec OWNER TO indus;

 EC file format
 ftp://ftp.expasy.org/databases/enzyme/release/enzuser.txt

 */
public class ECIterator extends IndusDB
{
    public static void test()
    {
        ECIterator ec = new ECIterator();

        //ec.connect(IndusConstants.dbLocalURL);
        ec.connect(IndusConstants.dbURL); 
        ec.clearAllData();
        ec.loadToDB();
        ec.disconnect();
    }

    /**
     * loadToDB
     * Note: a Unix host can only accept passive ftp visit
     */
    private void loadToDB()
    {
        loadTop3Level();
        loadLeaves();
    }

    /**
     * We just parse ID and DE line in this version
     *
     *  Example
     ID   1.1.1.2
     DE   Alcohol dehydrogenase (NADP+).
     AN   Aldehyde reductase (NADPH).
     CA   An alcohol + NADP(+) = an aldehyde + NADPH.
     CF   Zinc.
     CC   -!- Some members of this group oxidize only primary alcohols; others act
     CC       also on secondary alcohols.
     CC   -!- May be identical with EC 1.1.1.19, EC 1.1.1.33 and EC 1.1.1.55.
     CC   -!- A-specific with respect to NADPH.
     PR   PROSITE; PDOC00061;
     DR   P35630, ADH1_ENTHI ;  Q24857, ADH3_ENTHI ;  Q04894, ADH6_YEAST ;
     DR   P25377, ADH7_YEAST ;  O57380, ADH8_RANPE ;  P0A4X1, ADHC_MYCBO ;
     DR   P0A4X0, ADHC_MYCTU ;  P25984, ADH_CLOBE  ;  P75214, ADH_MYCPN  ;
     DR   P14941, ADH_THEBR  ;  O70473, AK1A1_CRIGR;  P14550, AK1A1_HUMAN;
     DR   Q9JII6, AK1A1_MOUSE;  P50578, AK1A1_PIG  ;  P51635, AK1A1_RAT  ;
     DR   Q9UUN9, ALD2_SPOSA ;  P27800, ALDX_SPOSA ;
     //


     The currently  used line  types, along with their respective line codes,
     are listed below:

     ID  Identification                         (Begins each entry; 1 per entry)
     DE  Description (official name)            (>=1 per entry)
     AN  Alternate name(s)                      (>=0 per entry)
     CA  Catalytic activity                     (>=0 per entry)
     CF  Cofactor(s)                            (>=0 per entry)
     CC  Comments                               (>=0 per entry)
     DI  Disease(s) associated with the enzyme  (>=0 per entry)
     PR  Cross-references to PROSITE            (>=0 per entry)
     DR  Cross-references to SWISS-PROT         (>=0 per entry)
     //  Termination line                       (Ends each entry; 1 per entry)


     Some entries  do not  contain all of the line types, and some line types
     occur many  times in  a single  entry. Each  entry must  begin  with  an
     identification line (ID) and end with a terminator line (//).

     A detailed description of each line type is given in the next section of
     this document.

     @author Jie Bao
     @since 2005-03-31
     */
    private void loadLeaves()
    {
        //String url = "ftp://ftp.expasy.org/databases/enzyme/release/enzyme.dat";

        try
        {
            //BufferedReader in = IOUtils.openInputStream(url);
            FileInputStream fin = new FileInputStream("c:\\tmp\\enzyme.dat");

            BufferedReader in = IOUtils.openInputStream(fin);

            String str;
            int count = 0;
            String ID = "", DE = "", AN = "", CA = "", CF = "", CC = "", DI = "", PR = "", DR = "";
            // read it line by line
            while ((str = in.readLine()) != null)
            {
                //System.out.println(str);
                String value = "";
                if (str.length()> 3)
                {
                    value = str.substring(3).trim();    
                }
                
                
                if (str.matches("\\s*")) // blank
                {
                    continue;
                }
                else if (str.startsWith("ID")) // Identification
                {
                    ID = value;
                }
                else if (str.startsWith("DE")) // Description (official name) 
                {
                    DE += value;
                }
                else if (str.startsWith("AN"))
                {
                    AN += value;
                }
                else if (str.startsWith("CA"))
                {
                    CA += value;
                }
                else if (str.startsWith("CF"))
                {
                    CF += value;
                }
                else if (str.startsWith("CC"))
                {
                    CC += value;
                }
                else if (str.startsWith("DI"))
                {
                    DI += value;
                }
                else if (str.startsWith("PR"))
                {
                    PR += value;
                }
                else if (str.startsWith("DR"))
                {
                    DR += value;
                }
                else if (str.startsWith("//")) // Termination line
                {
                    if (!ID.equals(""))
                    {
                        Map<String,String> map = new HashMap<String,String>();
                        map.put("id", ID);
                        map.put("parent", getParentId(ID));
                        map.put("de", DE);
                        map.put("an", AN);
                        map.put("ca", CA);
                        map.put("cf", CF);
                        map.put("cc", CC);
                        map.put("di", DI);
                        map.put("pr", PR);
                        map.put("dr", DR);

                        //System.out.print(++count + " : ");
                        if (!JDBCUtils.insertOrUpdateDatabase(db, "ec", map,
                                "id"))
                        {
                            //Debug.pause();
                        }
                        map.clear();
                    }
                    ID = DE = AN = CA = CF = CC = DI = PR = DR = "";
                }
            } // while
        }
        catch (IOException ex)
        {
            ex.printStackTrace();
        }

    }

    String getParentId(String id)
    {
        int lastdot = id.lastIndexOf(".");
        String parent = (lastdot == -1) ? "" : id.substring(0, lastdot);
        return parent;
    }

    private void loadTop3Level()
    {
        //String url = "ftp://ftp.expasy.org/databases/enzyme/release/enzclass.txt";
        try
        {
            //            FtpUtils ftp = new FtpUtils();
            //            BufferedReader in = IOUtils.openUnixFtpInputStream("ftp.expasy.org",
            //                "Anonymous", "null", "/databases/enzyme/release/enzclass.txt", ftp);
            FileInputStream fin = new FileInputStream("c:\\tmp\\enzclass.txt");
            BufferedReader in = IOUtils.openInputStream(fin);
            String str;
            int count = 0;
            // read it line by line
            while ((str = in.readLine()) != null)
            {
                //System.out.println(str);
                
                if (str.matches("\\s*")) // blank
                {
                    continue;
                }
                else if (!Character.isDigit(str.charAt(0))) // header start with non number
                {
                    // ignore it
                }
                else
                // an entry
                {
                    // 2 colums divided by the last -
                    // eg:
                    //1. -. -.-  Oxidoreductases.
                    //1. 1. -.-   Acting on the CH-OH group of donors.
                    //1. 1. 1.-    With NAD(+) or NADP(+) as acceptor.

                    int blank = str.indexOf(".- ");
                    String id = str.substring(0, blank).trim();

                    id = id.replaceAll("[\\-|\\s+]", ""); // remove blank and -
                    // remove all tail .
                    while (id.endsWith("."))
                    {
                        id = id.substring(0, id.length() - 1); // remove a dot
                    }

                    String des = str.substring(blank + 2, str.length()).trim();

                    Map<String,String> map = new HashMap<String, String>();
                    map.put("id", id);
                    map.put("parent", getParentId(id));
                    map.put("de", des);

                    //System.out.print(++count + " : ");
                    if (!JDBCUtils.insertOrUpdateDatabase(db, "ec", map, "id"))
                    {
                        System.out.println("database updating failed");
                    }
                    //Debug.trace("pause");
                    map.clear();

                }
            } // while
            //ftp.closeDataConnection();
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
        JDBCUtils.clearTable(db, "ec");
    }

    public static void main(String[] args)
    {
        test();
    }
}
