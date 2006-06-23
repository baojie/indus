package edu.iastate.anthill.indus.iterator.mapping;

import edu.iastate.anthill.indus.IndusDB;

import edu.iastate.utils.io.IOUtils;
import edu.iastate.utils.sql.JDBCUtils;
import edu.iastate.utils.sql.pgJDBCUtils;

/**
 * @author Jie Bao
 * @since 1.0 2005-02-17
 */
public class EC2GO
    extends IndusDB implements MappingDB
{
    public EC2GO()
    {
    }

    public void loadToDB()
    {
        String url = "http://www.geneontology.org/external2go/ec2go";
        String s = IOUtils.readTextFromURL(url);

        // parse it
        String ss[] = s.split("\n");

        for (int i = 0; i < ss.length; i++)
        {
            // eg: ss[0] = EC:1.1.1.1 > GO:alcohol dehydrogenase activity ; GO:0004022
            System.out.println(i + " : " + ss[i]);

            if (ss[i].matches("\\s*") || ss[i].startsWith("!"))
            {
                // it's a blank sentence or comments
                continue;
            }

            String words[] = ss[i].split(">");

            // eg: words[0] = EC:1.1.1.1
            // words[0] is the EC information
            String ec_id = words[0].replaceAll("EC:", ""); ;
            System.out.println(ec_id);

            for (int j = 1; j < words.length; j++)
            {
                String go[] = words[j].split(";");
                if (go.length == 2)
                {
                    String go_term = go[0].replaceAll("\\s*GO:", "");
                    String go_id = go[1].replaceAll("\\s*GO:", "");

                    System.out.println(go_id);
                    System.out.println(go_term);
                    addEntry(ec_id, go_id, go_term);
                }
            }

        }

        //System.out.println(s);
    }

    public void addEntry(String ec_id,
                         String go_id,
                         String go_term)
    {
        // add it into the database
        // insert new
        String sss =
            "INSERT INTO ec2go (ec_id, go_id, go_term)" +
            " VALUES (" + pgJDBCUtils.toDBString(ec_id) + "," +
            pgJDBCUtils.toDBString(go_id) + "," +
            pgJDBCUtils.toDBString(go_term) + ")";
        System.out.println("     " + sss);
        pgJDBCUtils.updateDatabase(db, sss);
    }

    public static void test()
    {
        EC2GO mapping = new EC2GO();

        mapping.connect();
        mapping.loadToDB();
        mapping.disconnect();
    }

    public boolean createTable()
    {
        String sql = "CREATE TABLE ec2go " +
            "( ec_id char(20) NOT NULL, " +
            "  go_id char(7) NOT NULL, " +
            "  go_term char(100), " +
            "  CONSTRAINT ec2go_pkey PRIMARY KEY (ec_id, go_id) " +
            ")  ";
        return JDBCUtils.updateDatabase(this.db, sql);
    }
}
