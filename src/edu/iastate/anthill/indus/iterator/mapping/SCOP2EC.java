package edu.iastate.anthill.indus.iterator.mapping;

import edu.iastate.anthill.indus.IndusDB;

import edu.iastate.utils.io.IOUtils;
import edu.iastate.utils.sql.pgJDBCUtils;
import edu.iastate.utils.sql.JDBCUtils;

/**
 * @author Jie Bao
 * @since 1.0 2005-02-17
 */
public class SCOP2EC
    extends IndusDB implements MappingDB
{
    public SCOP2EC()
    {
    }

    public void loadToDB()
    {
        String url = "http://www.enzome.com/databases/data/scopec_1_1.63.txt";
        String s = IOUtils.readTextFromURL(url);

        // parse it
        String ss[] = s.split("\n");

        for (int i = 0; i < ss.length; i++)
        {
            // eg: ss[0] = d1eb7a1 59404 1.11.1.5 1 a.3.1.5 9 1eb7 A:1-164
            // The first column is the SCOP short name for the domain, eg d1xgmb1 is the first domain in chain B in protein 1xgm.
            // The second column is a unique domain identifier (sunid).
            // 3rd column is EC id,
            // The forth column is a counter and highlights when a domain has more than one EC entry.
            // 5th column is SCOP id
            // The six column is the source of the data, 0 = Catalytic Site Atlas, 3 = PSI-BLAST searches, 9 = single domain PDBs with EC assignments.
            // 7th column is PDB id
            // 8th column is PDB chain,


            System.out.println(i + " : " + ss[i]);

            String words[] = ss[i].split("\\s");
            addEntry(words);
            break;

        }

        //System.out.println(s);
    }

    public void addEntry(String words[])
    {
        // add it into the database
        // insert new
        String tableName = "scop2ec";
        String[] fields = new String[]
            {
            "scop_short_name", "sunid", "ec_id", "counter", "scop_id", "source",
            "pdb_id", "pdb_chain"};
        pgJDBCUtils.insertDatabase(db, tableName, fields, words);

    }

    public static void test()
    {
        SCOP2EC mapping = new SCOP2EC();

        mapping.connect();
        mapping.loadToDB();
        mapping.disconnect();
    }

    public boolean createTable()
    {
        String sql = "CREATE TABLE scop2ec " +
            " ( " +
            " scop_short_name char(7) NOT NULL, " +
            " sunid char(5), " +
            " ec_id char(20) NOT NULL, " +
            " counter char(1), " +
            " scop_id char(20), " +
            " source char(1), " +
            " pdb_id char(4), " +
            " pdb_chain char(50), " +
            " CONSTRAINT scop2ec_pkey PRIMARY KEY (scop_short_name, ec_id) " +
            ") ";
        return JDBCUtils.updateDatabase(this.db, sql);
    }
}
