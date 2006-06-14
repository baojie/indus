package edu.iastate.anthill.indus.iterator.go;

import java.sql.Connection;
import java.util.Vector;

import javax.swing.JFrame;
import javax.swing.JScrollPane;

import edu.iastate.anthill.indus.IndusConstants;
import edu.iastate.anthill.indus.IndusDB;
import edu.iastate.anthill.indus.iterator.DB2Tree;
import edu.iastate.anthill.indus.tree.TypedTree;

/**
 * @author Jie Bao
 * @since 1.0 2005-03-11
 */
public class Go2Tree
    extends DB2Tree
{
    public Go2Tree(Connection db)
    {
        super(db);
    }

    protected String findComments(String id)
    {
        return defaultFindComments("go_term", "go_id", "name", id);
    }

    protected Vector getChildren(String from_id)
    {
        return defaultGetChildren("go_relationship", "go_id1", "go_id2",
                                  from_id, "relation", "is_a");
    }

    public static void main(String[] args)
    {
        IndusDB conn = new IndusDB();
        conn.connect(IndusConstants.dbURL);

        Go2Tree mm = new Go2Tree(conn.db);
        TypedTree t = mm.getTree("0008150", 4);
        conn.disconnect();

        // show it
        JFrame frame = new JFrame();
        frame.setSize(800, 600);
        JScrollPane scr = new JScrollPane(t);
        frame.getContentPane().add(scr);
        frame.setVisible(true);
        System.out.print(t);
    }

    protected Vector getParent(String from_id)
    {
        return defaultGetParent("go_relationship", "go_id1", "go_id2",
                                from_id, "relation", "is_a");
    }

    public String getRootId()
    {
        return "0000000"; // our meta root for GO
    }

}
