package edu.iastate.anthill.indus.iterator.scop;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import edu.iastate.anthill.indus.IndusConstants;
import edu.iastate.anthill.indus.IndusDB;

import edu.iastate.utils.io.IOUtils;
import edu.iastate.utils.sql.pgJDBCUtils;
import edu.iastate.utils.string.ParserUtils;

/*
 Lo Conte L., Brenner S. E., Hubbard T.J.P., Chothia C., Murzin A. (2002).
 SCOP database in 2002: refinements accommodate structural genomics.
 Nucl. Acid Res. 30(1), 264-267
 http://nar.oupjournals.org/cgi/content/full/30/1/264

 "One of the files, dir.hie.scop.txt, has no precursor in releases before 1.55.
 It represents the SCOP hierarchy in terms of sunid. Each entry corresponds to
 a node in the tree and has two additional fields: the sunid of the parent of
 that node (i.e. the node one step up in the tree), and the list of sunids for
 the children of that node (i.e. the nodes one step down in the tree).

 A second file, dir.cla.scop.txt, contains a description of all domains, their
 definition and their classification, both in terms of sunid and sccs.

 The third file, dir.des.scop.txt, contains a description of each node in the
 hierarchy, including English names for proteins, families, superfamilies, folds
 and classes."
 */

/**
 * 
The scop parseable files can be found in scop.zip under this folder 

Actually, I found the following PostgreSQL sentences are more efficient: 
 
COPY scop_des (sunid,a2,scop_id,scop_short_name,description) FROM 'c:/tmp/dir.des.scop.txt_1.67'  WITH DELIMITER '\t';
COPY scop_hie (sunid,parent,children) FROM  'c:/tmp/dir.hie.scop.txt_1.67'  WITH DELIMITER '\t';
COPY scop_cla (scop_short_name,pdb_id,pdb_chain,scop_id,sunid,cl,cf,sf,fa,dm,sp,px) FROM  'c:/tmp/dir.cla.scop.txt_1.67'  WITH DELIMITER '\t';
COPY scop_com (sunid,comments) FROM 'c:/tmp/dir.com.scop.txt_1.67'  WITH DELIMITER '!';

However, the data files should be cleaned manually before loading

 * 
 * @author Jie Bao
 * @since 1.0 2005-02-22
 */
public class ScopIterator extends IndusDB
{
    public ScopIterator()
    {}

    public static void main(String[] args)
    {
        test();
    }
    public static void test()
    {
        ScopIterator loader = new ScopIterator();

        loader.connect(IndusConstants.dbURL); 
        loader.clearAllData();
        loader.loadToDB();
        loader.disconnect();
    }

    /**
     * loadToDB
     * @since 2005-03-03
     */
    private void loadToDB()
    {
        loadDes();
        loadCla();
        loadHie();
        loadCom();
    }

    private void loadCom()
    {
        //String url =
        //    "http://scop.mrc-lmb.cam.ac.uk/scop/parse/dir.com.scop.txt_1.67";
        // read it line by line
        System.out.println("Com");
        try
        {
            FileInputStream fin = new FileInputStream(
                    "c:\\tmp\\dir.com.scop.txt_1.67");
            BufferedReader in = IOUtils.openInputStream(fin);
            String str, header = "";
            int count = 0;
            while ((str = in.readLine()) != null)
            {
                if (str.matches("\\s*")) // blank
                {
                    continue;
                }
                else if (str.startsWith("#")) // header
                {
                    header += str + "\n";
                }
                else
                // an entry
                {
                    // 2 colums divided by !
                    // eg:
                    //46457 ! core: 6 helices; folded leaf, partly opened
                    //46459 ! lack the first helix (A)
                    String[] col = str.split("!");
                    if (col.length != 2)
                    {
                        continue;
                    }
                    Map map = new HashMap();
                    map.put("sunid", col[0].trim()); // varchar(10) NOT NULL,
                    map.put("comments", col[1].trim()); // text

                    if ((count++ % 100) == 0) System.out.print(count + " ");
                    if (!pgJDBCUtils.insertDatabase(db, "scop_com", map))
                    {
                        //Debug.pause();
                    }
                    map.clear();

                }
            }

            // insert header
            addHeader("com", header);

        }
        catch (IOException ex)
        {}
    }

    private void loadHie()
    {
        //String url = "http://scop.mrc-lmb.cam.ac.uk/scop/parse/dir.hie.scop.txt_1.67";
        // read it line by line
        System.out.println("Hie");
        try
        {
            FileInputStream fin = new FileInputStream(
                    "c:\\tmp\\dir.hie.scop.txt_1.67");
            BufferedReader in = IOUtils.openInputStream(fin);
            String str, header = "";
            int count = 0;
            while ((str = in.readLine()) != null)
            {
                if (str.matches("\\s*")) // blank
                {
                    continue;
                }
                else if (str.startsWith("#")) // header
                {
                    header += str + "\n";
                }
                else
                // an entry
                {
                    // 3 colums divided by \t
                    // eg:
                    //46456	0	46457,46556,63445,63450,46625,46688,81602,46928,81297,46954,46965,46996,47004,47013,47026,47039,47044,47049,47054,47059,47071,63500,47076,47081,47089,47094,47112,47143,81729,47161,63519,47239,47265,47322,47335,47363,47379,47390,89042,47395,47400,47405,47412,47445,47453,47458,47472,47575,47586,47591,81766,47597,47615,47643,101214,47654,47667,89063,47680,63561,101223,89068,47685,47693,81777,81782,47698,47718,69035,47723,47728,47740,47751,63569,47756,101232,47761,81789,101237,47768,47835,101256,47851,47856,101261,47861,47873,47894,47911,47916,47927,47932,47937,47942,47953,69059,69064,47972,48018,81632,69069,89081,101277,47978,69074,101282,101287,47985,48007,48012,101306,48023,101311,48033,48044,101316,48049,48055,48064,48075,81821,81826,48080,101321,48091,48096,81831,74747,101326,101331,48107,48112,48139,89094,101338,101343,48144,48149,48162,101352,48167,81836,48172,48178,48200,48207,48255,48263,63591,48299,48304,48309,48316,81871,81877,81274,88945,48333,81885,101385,101390,89123,48339,48344,101398,48349,48365,63599,81890,48370,48483,48492,48497,81384,81385,48507,48536,48546,48551,48556,101446,48575,48591,89154,48599,48607,48612,69117,81922,81929,101472,101477,89161,81934,48618,48646,48651,48656,69124,101488,48661,48694
                    //46457	46456	46458,46548
                    String[] col = str.split("\\t");
                    if (col.length != 3)
                    {
                        continue;
                    }
                    Map map = new HashMap();
                    map.put("sunid", col[0]); // varchar(10) NOT NULL,
                    map.put("parent", col[1]); // varchar(10),
                    map.put("children", col[2]); // text,

                    if ((count++ % 100) == 0) System.out.print(count + " ");
                    if (!pgJDBCUtils.insertDatabase(db, "scop_hie", map))
                    {
                        //Debug.pause();
                    }
                    map.clear();

                }
            }

            // insert header
            addHeader("hie", header);

        }
        catch (IOException ex)
        {}
    }

    /**
     * loadCla
     * @since 2005-03-03
     */
    private void loadCla()
    {
        //String url = "http://scop.mrc-lmb.cam.ac.uk/scop/parse/dir.cla.scop.txt_1.67";
        // read it line by line
        System.out.println("Cla");
        try
        {
            FileInputStream fin = new FileInputStream(
                    "c:\\tmp\\dir.cla.scop.txt_1.67");
            BufferedReader in = IOUtils.openInputStream(fin);
            String str, header = "";
            int count = 0;
            while ((str = in.readLine()) != null)
            {
                if (str.matches("\\s*")) // blank
                {
                    continue;
                }
                else if (str.startsWith("#")) // header
                {
                    header += str + "\n";
                }
                else
                // an entry
                {
                    // 6 columns divided by \t
                    // eg:
                    //d1dlwa_	1dlw	A:	a.1.1.1	14982	cl=46456,cf=46457,sf=46458,fa=46459,dm=46460,sp=46461,px=14982
                    //d1uvya_	1uvy	A:	a.1.1.1	100068	cl=46456,cf=46457,sf=46458,fa=46459,dm=46460,sp=46461,px=100068
                    String[] col = str.split("\\t");
                    if (col.length != 6)
                    {
                        continue;
                    }
                    Map map = new HashMap();
                    map.put("scop_short_name", col[0]); // varchar(7),
                    map.put("pdb_id", col[1]); //  varchar(4),
                    map.put("pdb_chain", col[2]); //  varchar(50),
                    map.put("scop_id", col[3]); // varchar(20),
                    map.put("sunid", col[4]); // varchar(10) NOT NULL,

                    // parse the remainings
                    map.put("cl", ParserUtils.getFirstNestedBlock("cl=", ",",
                            col[5], false)); //  varchar(10),
                    map.put("cf", ParserUtils.getFirstNestedBlock("cf=", ",",
                            col[5], false)); //  varchar(10),
                    map.put("sf", ParserUtils.getFirstNestedBlock("sf=", ",",
                            col[5], false)); //  varchar(10),
                    map.put("fa", ParserUtils.getFirstNestedBlock("fa=", ",",
                            col[5], false)); //  varchar(10),
                    map.put("dm", ParserUtils.getFirstNestedBlock("dm=", ",",
                            col[5], false)); //  varchar(10),
                    map.put("sp", ParserUtils.getFirstNestedBlock("sp=", ",",
                            col[5], false)); //  varchar(10),
                    map.put("px", col[5].subSequence(col[5].indexOf("px=") + 3,
                            col[5].length())); //  varchar(10)
                    if ((count++ % 100) == 0) System.out.print(count + " ");
                    if (!pgJDBCUtils.insertDatabase(db, "scop_cla", map))
                    {
                        //Debug.pause();
                    }
                    map.clear();
                }
            }

            // insert header
            addHeader("cla", header);

        }
        catch (IOException ex)
        {}
    }

    /**
     * loadDes
     * @since 2005-03-03
     */
    private void loadDes()
    {
        //String url =
        //    "http://scop.mrc-lmb.cam.ac.uk/scop/parse/dir.des.scop.txt_1.67";
        // read it line by line
        System.out.println("Des");
        try
        {
            FileInputStream fin = new FileInputStream(
                    "c:\\tmp\\dir.des.scop.txt_1.67");
            BufferedReader in = IOUtils.openInputStream(fin);
            String str, header = "";
            int count = 0;
            while ((str = in.readLine()) != null)
            {
                if (str.matches("\\s*")) // blank
                {
                    continue;
                }
                else if (str.startsWith("#")) // header
                {
                    header += str + "\n";
                }
                else
                // an entry
                {
                    // 5 colums divided by \t
                    // eg:
                    //92244	px	a.1.1.2	d1nwnb_	1nwn B:
                    //46466	sp	a.1.1.2	-	Clam (Lucina pectinata)
                    String[] col = str.split("\\t");
                    if (col.length != 5)
                    {
                        continue;
                    }
                    Map map = new HashMap();
                    map.put("sunid", col[0]); // varchar(5) NOT NULL,
                    map.put("a2", col[1]); // varchar(2),
                    map.put("scop_id", col[2]); // varchar(20),
                    map.put("scop_short_name", col[3]); // varchar(6),
                    map.put("description", col[4]); // varchar(100),

                    if ((count++ % 100) == 0) System.out.print(count + " ");
                    if (!pgJDBCUtils.insertDatabase(db, "scop_des", map))
                    {
                        //Debug.pause();
                    }
                    map.clear();

                }
            }

            // insert header
            addHeader("des", header);

        }
        catch (IOException ex)
        {}
    }

    /**
     * addHeader
     *
     * @param string String
     * @param string1 String
     */
    private void addHeader(String col, String header)
    {
        Map map = new HashMap();
        map.put("id", "1");
        map.put(col, header);
        pgJDBCUtils.insertOrUpdateDatabase(db, "scop_header", map, "id");
    }

    /**
     * clearAllData
     */
    private void clearAllData()
    {
        pgJDBCUtils.clearTable(db, "scop_header");
        pgJDBCUtils.clearTable(db, "scop_des");
        pgJDBCUtils.clearTable(db, "scop_cla");
        pgJDBCUtils.clearTable(db, "scop_hie");
        pgJDBCUtils.clearTable(db, "scop_com");
    }
}
