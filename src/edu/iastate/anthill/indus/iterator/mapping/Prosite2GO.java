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
public class Prosite2GO
    extends IndusDB implements MappingDB
{
    public Prosite2GO()
    {
    }

    public void loadToDB()
    {
        String url = "http://www.geneontology.org/external2go/prosite2go";
        String s = IOUtils.readTextFromURL(url);

        // parse it
        String ss[] = s.split("\n");

        for (int i = 0; i < ss.length; i++)
        {
            // eg: ss[0] = PROSITE:PS00011 GLA_1 > GO:calcium ion binding ; GO:0005509
            System.out.println(i + " : " + ss[i]);

            if (ss[i].matches("\\s*") || ss[i].startsWith("!"))
            {
                // it's a blank sentence or comments
                continue;
            }

            String words[] = ss[i].split(">");

            // eg: words[0] = PROSITE:PS00011 GLA_1
            // words[0] is the Prosite information
            String prosite = words[0];
            int m1 = prosite.indexOf(" ");
            String prosite_id = prosite.substring(0, m1).replaceAll("PROSITE:",
                "");
            String prosite_desp = prosite.substring(m1 + 1);
            //System.out.println(mips_funcat);

            for (int j = 1; j < words.length; j++)
            {
                String go[] = words[j].split(";");
                if (go.length == 2)
                {
                    String go_term = go[0].replaceAll("\\s*GO:", "");
                    String go_id = go[1].replaceAll("\\s*GO:", "");

                    //System.out.println(go_id);
                    //System.out.println(go_term.length());
                    addEntry(prosite_id, prosite_desp, go_id, go_term);
                }
            }

        }

        //System.out.println(s);
    }

    public void addEntry(String prosite_id, String prosite_desp,
                         String go_id,
                         String go_term)
    {
        // add it into the database
        try
        {
            // insert new
            String sss =
                "INSERT INTO prosite2go (prosite_id, prosite_desp, go_id, go_term)" +
                " VALUES (" + pgJDBCUtils.toDBString(prosite_id) + "," +
                pgJDBCUtils.toDBString(prosite_desp) + "," +
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
        Prosite2GO mapping = new Prosite2GO();

        mapping.connect();
        mapping.loadToDB();
        mapping.disconnect();
    }

    public boolean createTable()
    {
        String sql = "CREATE TABLE prosite2go " +
            "( " +
            "  prosite_id char(7) NOT NULL, " +
            "  prosite_desp char(100), " +
            "  go_id char(7) NOT NULL, " +
            "  go_term char(100) NOT NULL, " +
            "  CONSTRAINT prosite2go_pkey PRIMARY KEY (prosite_id, go_id) " +
            ") ";
        return JDBCUtils.updateDatabase(this.db, sql);

    }
}
