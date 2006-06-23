package edu.iastate.anthill.indus.iterator.mapping;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import edu.iastate.anthill.indus.IndusConstants;
import edu.iastate.anthill.indus.IndusDB;

import edu.iastate.utils.sql.JDBCUtils;

/**
 * @author Jie Bao
 * @since 2005-05-15
 *
 * http://www.imb-jena.de/ImgLibPDB/pages/GO/goName2pdb.txt
 */
public class GO2PDB
    extends IndusDB implements MappingDB

{
    public static void main(String[] args)
    {
        GO2PDB go2pdb = new GO2PDB();
        go2pdb.test();
    }

    public void loadToDB()
    {
        //String url = "http://www.imb-jena.de/ImgLibPDB/pages/GO/goName2pdb.txt";

        // that could be a large file, so we read it line by line
        //BufferedReader in = IOUtils.openInputStream(url);
        // parse it
        String str;

        try
        {
            BufferedReader in = new BufferedReader(new FileReader(
                "goName2pdb.txt"));
            int count = 0;
            while ( (str = in.readLine()) != null)
            {
                //# PDB	SWP      	GO        	term_type         	obsolete	name
                //101M	MYG_PHYCA	GO:0005344	molecular_function	0	oxygen transporter activity
                System.out.println(str);
                if (str.charAt(0) == '#')
                {

                    continue;
                }
                String words[] = str.split("\\s+");
                String go_id = words[2], pdb_id = words[0];
                go_id = go_id.replaceAll("GO:", "");
                //System.out.println(goid + " , " + pdbid);
                count++;
                System.out.print(count + " : ");
                Map m = new HashMap();
                m.put("go_id", go_id);
                m.put("pdb_id", pdb_id);
                JDBCUtils.insertDatabase(db, "go2pdb", m);

            }
            in.close();
        }
        catch (IOException ex)
        {
            ex.printStackTrace();
        }

    }

    public boolean createTable()
    {
        String sql = "CREATE TABLE go2pdb" +
            "(" +
            "  go_id varchar(7)," +
            "  pdb_id varchar(4)" +
            ")";

        return JDBCUtils.updateDatabase(this.db, sql);
    }

    public static void test()
    {
        GO2PDB loader = new GO2PDB();

        loader.connect(IndusConstants.dbURL);
        loader.clearAllData();
        loader.loadToDB();
        loader.disconnect();
    }

    /**
     * clearAllData
     */
    private void clearAllData()
    {
        JDBCUtils.updateDatabase(db, "DELETE FROM go2pdb");

    }

}
