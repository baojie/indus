package edu.iastate.anthill.indus.iterator.mapping;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import edu.iastate.anthill.indus.IndusDB;

import edu.iastate.utils.io.IOUtils;
import edu.iastate.utils.sql.pgJDBCUtils;
import edu.iastate.utils.string.ParserUtils;
import edu.iastate.utils.sql.JDBCUtils;

/**
 * To handle the mapping between some databases
 *
 * @author Jie Bao
 * @since 1.0 2005-02-17
 */
public class PDB2SwissProt
    extends IndusDB implements MappingDB
{
    public PDB2SwissProt()
    {
    }

    /**
     * 2005-02-17
     * Read the PDB to Swissprot mapping
     */
    public void loadToDB()
    {
        String url = "http://www.expasy.org/cgi-bin/lists?pdbtosp.txt";
        String s = IOUtils.readTextFromURL(url);

        // delete all hyperlinks
        s = ParserUtils.replaceAll("<.*?>", "", s);

        // find useful part
        String startstr = "__________________________________________";
        int begin = s.indexOf(startstr) + startstr.length();
        int end = s.indexOf(
            "----------------------------------------------------------------------------",
            begin);
        s = s.substring(begin, end);

        // parse it
        String ss[] = s.split("\n");

        String lastPDB = "0000", lastMethod = "null";
        String PDB_ID, Method, SP_NAME, SP_ID;

        for (int i = 0; i < 10 /*ss.length*/; i++)
        {
            System.out.println(i + " : " + ss[i]);
            if (ss[i].matches("\\s*"))
            {
                //System.out.println(ss[i] + " is blank");
                continue;
            }
            //System.out.println(ss[i] + " is not blank");

            String words[] = ss[i].split(ParserUtils.WHITESPACE +
                                         ParserUtils.ONE_OR_MORE);
            //System.out.print(words.length);
            if (ss[i].substring(0, 4).compareTo("    ") == 0) // it is the continuing of the last PDB entry
            // eg:                 NFC2_HUMAN  (Q13469)
            {
                System.out.println("follow up");
                if (words.length >= 3)
                {
                    PDB_ID = lastPDB;
                    Method = lastMethod;
                    SP_NAME = words[1]; // words[0] is empty
                    SP_ID = words[2].replaceAll("\\(|\\)|\\,", "");
                    addEntry(PDB_ID, Method, SP_NAME, SP_ID);
                    if (words.length == 5)
                    {
                        SP_NAME = words[3];
                        SP_ID = words[4].replaceAll("\\(|\\)|\\,", "");
                        addEntry(PDB_ID, Method, SP_NAME, SP_ID);
                    }
                }

            }
            else // a new PDB entry
            // eg: 1A03  NMR       S10A6_RABIT (P30801)
            {
                System.out.println("new entry");

                PDB_ID = words[0];
                Method = words[1];
                SP_NAME = words[2];
                SP_ID = words[3].replaceAll("\\(|\\)|\\,", "");
                addEntry(PDB_ID, Method, SP_NAME, SP_ID);

                if (words.length == 6)
                {
                    SP_NAME = words[4];
                    SP_ID = words[5].replaceAll("\\(|\\)|\\,", "");
                    addEntry(PDB_ID, Method, SP_NAME, SP_ID);
                }

                lastPDB = PDB_ID;
                lastMethod = Method;
            }

        }

//        System.out.println(s.substring(end-4000,end));
    }

    public void addEntry(String PDB_ID, String Method, String SP_NAME,
                         String SP_ID)
    {
        // add it into the database
        boolean fail = false;
        try
        {
            // insert new
            String sss = "INSERT INTO pdbtosp (pdb_id, method, sp_name, sp_id)" +
                " VALUES (" + pgJDBCUtils.toDBString(PDB_ID) + "," +
                pgJDBCUtils.toDBString(Method) + "," +
                pgJDBCUtils.toDBString(SP_NAME) + "," +
                pgJDBCUtils.toDBString(SP_ID) + ")";
            System.out.println(sss);

            PreparedStatement updatest = db.prepareStatement(sss);
            updatest.executeUpdate();
            updatest.close();
            //System.out.println("insert");
        }
        catch (SQLException ex)
        {
            System.out.println(ex.toString());
            fail = true;
            //ex.printStackTrace();
        }

    }

    public static void test()
    {
        PDB2SwissProt mapping = new PDB2SwissProt();

        mapping.connect();
        mapping.loadToDB();
        mapping.disconnect();
    }

    public boolean createTable()
    {
        String sql = "CREATE TABLE pdbtosp " +
            "( " +
            " pdb_id char(4) NOT NULL, " +
            " method char(10), " +
            " sp_name char(30), " +
            " sp_id char(6) NOT NULL, " +
            " CONSTRAINT \"PDBTOSP_pkey\" PRIMARY KEY (pdb_id, sp_id) " +
            ")";
        return JDBCUtils.updateDatabase(this.db, sql);

    }
}
