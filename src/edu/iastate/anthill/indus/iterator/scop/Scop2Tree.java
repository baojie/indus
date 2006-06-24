package edu.iastate.anthill.indus.iterator.scop;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Vector;

import javax.swing.JFrame;
import javax.swing.JScrollPane;

import edu.iastate.anthill.indus.IndusConstants;
import edu.iastate.anthill.indus.IndusDB;
import edu.iastate.anthill.indus.iterator.DB2Tree;
import edu.iastate.anthill.indus.tree.TypedTree;

/**
 * Read scop database and build a jtree
 *
 *
 * CREATE OR REPLACE VIEW scop AS 
     SELECT scop_hie.sunid AS id, ((scop_des.scop_id::text || ' ('::text) || scop_des.description) || ' )'::text AS name, scop_hie.parent
     FROM scop_hie, scop_des
     WHERE scop_hie.sunid::text = scop_des.sunid::text; 
 *
 *
 * @author Jie Bao
 * @since 1.0 2005-03-03
 */
public class Scop2Tree
    extends DB2Tree
{
    public Scop2Tree(Connection db)
    {
        super(db);
    }
    
    // 2006-06-23
    protected String findComments(String id)
    {
        return defaultFindComments("scop", "id", "name", id);
    }

    // 2005-03-31
    public String getRootId()
    {
        return "0";
    }

//  2006-06-23
    protected Vector getChildren(String from_id)
    {
        return defaultGetChildren("scop", "id", "parent", from_id, null, null);
    }

//  2006-06-23
    protected Vector getParent(String from_id)
    {
        return defaultGetParent("scop", "id", "parent", from_id, null, null);
    }

    ////////////////////////////  OLD OBSOLETE CODE //////////////////////////////////////////
    
    protected Vector getChildrenOld(String fromSunid)
    {
        // read the database and build the tree
        String sql = "SELECT children FROM scop_hie WHERE sunid = '" +
            fromSunid + "'";
        Vector vec = new Vector();
        try
        {
            // Create a result set containing all data from my_table
            Statement stmt = db.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            // Fetch each row from the result set
            if (rs.next()) // we should have at most 1 record
            {
                // Get the data from the row using the column name
                String children = rs.getString("children").trim(); // could be -
                if (children.compareTo("-") == 0)
                {
                    return vec;
                }
                else
                {
                    // parse it
                    // eg fromSunid = 46456
                    // parent = 0

                    String kids[] = children.split(","); // children = 46457,46556,63445,63450,46625,46688,81602,46928,81297,46954,46965,46996,47004,47013,47026,47039,47044,47049,47054,47059,47071,63500,47076,47081,47089,47094,47112,47143,81729,47161,63519,47239,47265,47322,47335,47363,47379,47390,89042,47395,47400,47405,47412,47445,47453,47458,47472,47575,47586,47591,81766,47597,47615,47643,101214,47654,47667,89063,47680,63561,101223,89068,47685,47693,81777,81782,47698,47718,69035,47723,47728,47740,47751,63569,47756,101232,47761,81789,101237,47768,47835,101256,47851,47856,101261,47861,47873,47894,47911,47916,47927,47932,47937,47942,47953,69059,69064,47972,48018,81632,69069,89081,101277,47978,69074,101282,101287,47985,48007,48012,101306,48023,101311,48033,48044,101316,48049,48055,48064,48075,81821,81826,48080,101321,48091,48096,81831,74747,101326,101331,48107,48112,48139,89094,101338,101343,48144,48149,48162,101352,48167,81836,48172,48178,48200,48207,48255,48263,63591,48299,48304,48309,48316,81871,81877,81274,88945,48333,81885,101385,101390,89123,48339,48344,101398,48349,48365,63599,81890,48370,48483,48492,48497,81384,81385,48507,48536,48546,48551,48556,101446,48575,48591,89154,48599,48607,48612,69117,81922,81929,101472,101477,89161,81934,48618,48646,48651,48656,69124,101488,48661,48694
                    for (int i = 0; i < kids.length; i++)
                    {
                        vec.add(kids[i]);
                    }
                }
            }
        }
        catch (SQLException e)
        {
        }
        return vec;
    }

    protected String findCommentsOld(String id)
    {
        return sunid2scopid(id);
    }

    String sunid2scopid(String sunid)
    {
        return defaultFindComments("scop_des", "sunid", "scop_id", sunid);
    }

    // 2005-03-31
    protected Vector getParentOld(String from_id)
    {
        return defaultGetParent("scop_hie", "sunid", "parent", from_id, null, null);
    }

    public static void main(String[] args)
    {

        IndusDB conn = new IndusDB();                
        conn.connect(IndusConstants.dbURL);

        Scop2Tree mm = new Scop2Tree(conn.db);
        TypedTree t = mm.getTree("46456", 1);
        conn.disconnect();

        // show it
        JFrame frame = new JFrame();
        frame.setSize(800, 600);
        JScrollPane scr = new JScrollPane(t);
        frame.getContentPane().add(scr);
        frame.setVisible(true);
        System.out.print(t);

    }




}
