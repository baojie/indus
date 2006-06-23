package edu.iastate.anthill.indus.iterator.mapping;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import edu.iastate.anthill.indus.IndusDB;

import edu.iastate.utils.io.IOUtils;
import edu.iastate.utils.sql.pgJDBCUtils;
import edu.iastate.utils.sql.JDBCUtils;

/**
 * @author Jie Bao
 * @since 1.0 2005-02-17
 */
public class MIPS2GO
    extends IndusDB implements MappingDB
{
    public MIPS2GO()
    {
    }

    public void loadToDB()
    {
        String url = "http://www.geneontology.org/external2go/mips2go";
        String s = IOUtils.readTextFromURL(url);

        // parse it
        String ss[] = s.split("\n");

        for (int i = 0; i < ss.length; i++)
        {
            // eg: ss[0] = MIPS_funcat:01 METABOLISM > GO:metabolism ; GO:0008152
            System.out.println(i + " : " + ss[i]);

            if (ss[i].matches("\\s*") || ss[i].startsWith("!"))
            {
                // it's a blank sentence or comments
                continue;
            }

            String words[] = ss[i].split(">");

            // eg: words[0] = MIPS_funcat:01 METABOLISM
            // words[0] is the MIPS information
            String mips = words[0];
            int m1 = mips.indexOf(" ");
            String mips_funcat = mips.substring(0,
                                                m1).replaceAll("MIPS_funcat:",
                "");
            String mips_description = mips.substring(m1 + 1);
            //System.out.println(mips_funcat);
            //System.out.println(mips_description);

            for (int j = 1; j < words.length; j++)
            {
                String go[] = words[j].split(";");
                if (go.length == 2)
                {
                    String go_term = go[0].replaceAll("\\s*GO:", "");
                    String go_id = go[1].replaceAll("\\s*GO:", "");

                    //System.out.println(go_id);
                    //System.out.println(go_term.length());
                    addEntry(mips_funcat, mips_description, go_id, go_term);
                }
            }

        }

        //System.out.println(s);
    }

    public void addEntry(String mips_funcat, String mips_description,
                         String go_id,
                         String go_term)
    {
        // add it into the database
        try
        {
            // insert new
            String sss =
                "INSERT INTO mips2go (mips_funcat, mips_description, go_id, go_term)" +
                " VALUES (" + pgJDBCUtils.toDBString(mips_funcat) + "," +
                pgJDBCUtils.toDBString(mips_description) + "," +
                pgJDBCUtils.toDBString(go_id) + "," +
                pgJDBCUtils.toDBString(go_term) + ")";
            System.out.println("     " + sss);

            PreparedStatement updatest = db.prepareStatement(sss);
            updatest.executeUpdate();
            updatest.close();
            //System.out.println("insert");
        }
        catch (SQLException ex)
        {
            System.out.println(ex.toString());
            //ex.printStackTrace();
        }

    }

    public static void test()
    {
        MIPS2GO mapping = new MIPS2GO();

        mapping.connect();
        mapping.loadToDB();
        mapping.disconnect();
    }

    public boolean createTable()
    {
        String sql = "CREATE TABLE mips2go " +
            "( " +
            "mips_funcat char(20) NOT NULL, " +
            "mips_description char(100), " +
            "go_id char(7) NOT NULL, " +
            "go_term char(100), " +
            "CONSTRAINT mips2go_pkey PRIMARY KEY (mips_funcat, go_id) " +
            "); ";
        return JDBCUtils.updateDatabase(this.db, sql);
    }
}
