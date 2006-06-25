package edu.iastate.anthill.indus.iterator.mips;

import java.sql.Connection;
import java.util.Vector;

import javax.swing.JFrame;
import javax.swing.JScrollPane;

import edu.iastate.anthill.indus.IndusConstants;
import edu.iastate.anthill.indus.IndusDB;
import edu.iastate.anthill.indus.iterator.DB2Tree;
import edu.iastate.anthill.indus.tree.TypedTree;
import edu.iastate.utils.lang.StopWatch;

/**
 * @author Jie Bao
 * @since 1.0
 */
public class MIPS2Tree extends DB2Tree
{

    public MIPS2Tree(Connection db)
    {
        super(db);
    }

    @Override
    public Vector<String[]> getChildrenFast(String cacheTable)
    {
        return defaultGetChildren("mips", "mips_id", "parent", null, null,
                "mips", "mips_id", "description", cacheTable);
    }

    protected String findComments(String id)
    {
        return defaultFindComments("mips", "mips_id", "description", id);
    }

    protected Vector getChildren(String from_id)
    {
        return defaultGetChildren("mips", "mips_id", "parent", from_id, null,
                null);
    }

    protected Vector getParent(String from_id)
    {
        return defaultGetParent("mips", "mips_id", "parent", from_id, null,
                null);
    }

    public static void main(String[] args)
    {
        IndusDB conn = new IndusDB();
        conn.connect(IndusConstants.dbURL);

        MIPS2Tree mm = new MIPS2Tree(conn.db);
        
        StopWatch w = new StopWatch();
        w.start();
        TypedTree t = mm.getTree("01", -1);
        w.stop();
        System.out.println(w.print());
        
        conn.disconnect();

        // show it
        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);
        JScrollPane scr = new JScrollPane(t);
        frame.getContentPane().add(scr);
        frame.setVisible(true);
        //System.out.print(t);
    }

    // 2005-03-31
    public String getRootId()
    {
        return ""; // the root is ''
    }
}
