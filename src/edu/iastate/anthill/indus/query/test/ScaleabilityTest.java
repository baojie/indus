/**
 * 
 */
package edu.iastate.anthill.indus.query.test;

import java.util.Random;
import java.util.Set;
import java.util.Vector;

import javax.swing.tree.DefaultMutableTreeNode;

import Zql.ZExpression;
import Zql.ZQuery;

import edu.iastate.anthill.indus.agent.InfoReader;
import edu.iastate.anthill.indus.datasource.type.AVH;
import edu.iastate.anthill.indus.gui.IndusBasis;
import edu.iastate.anthill.indus.gui.User;
import edu.iastate.anthill.indus.query.SQLQueryPlanner;
import edu.iastate.anthill.indus.query.ZConstantEx;
import edu.iastate.anthill.indus.tree.TypedNode;
import edu.iastate.anthill.indus.tree.TypedTree;
import edu.iastate.utils.lang.Serialization;
import edu.iastate.utils.lang.StopWatch;

/**
 * @author baojie
 * @since 2006-07-18
 */
public class ScaleabilityTest
{
public static void main(String[] args)
    {
        try
        {
            System.out.println("Query Translation Scaleability Test");

            SQLQueryPlanner planner = getPlannerInstance();

            IndusBasis.user = new User();
            IndusBasis.user.name = "baojie";

            // load query
            String fileName = "D:/enzyme.zql";
            ZQuery myZQuery = (ZQuery) Serialization.loadFromFile(fileName);

            System.out.println(myZQuery);

            AVH d = (AVH) InfoReader.readDataType("Scop", true);
            TypedTree t = d.getTreeAVH();
            Vector<DefaultMutableTreeNode> nodes = t.findAllNode(false);
            int nodeCount = nodes.size();

            //          select a term from the tree randomly            
            Random generator = new Random();
            
            StopWatch w = new StopWatch();
            w.start();
            
            for (int i = 0; i < 10000; i++)
            {   
                int randomIndex = generator.nextInt(nodeCount);

                TypedNode n = (TypedNode) nodes.elementAt(randomIndex);
                String term = n.getUserObject().toString();
                Set s = t.findAllOffspring(n);
                w.peek();System.out.print(w.print()+ ", ");
                System.out.print(i+ " , "+ term+ ","+s.size()+",");

                // replace the selection condition
                makeQuery(myZQuery, term);
                //System.err.println(myZQuery);
                
                planner.doQuery(myZQuery, "enzyme", false);
                ;
            }
        }
        catch (Exception e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        System.out.print("#, branch,size,translationTime,executionTime,resultCount,queryComplexity");
    }

    private static void makeQuery(ZQuery myZQuery, String XXX)
    {
        ZExpression where = (ZExpression) myZQuery.getWhere();
        ZConstantEx xxx = new ZConstantEx(XXX, ZConstantEx.AVH);
        Vector v = where.getOperands();
        v.setElementAt(xxx, 1);
    }

    /**
     * @return QueryPlanner
     * @since 2005-03-25
     */
    public static SQLQueryPlanner getPlannerInstance()
    {
        IndusBasis basis = new IndusBasis();
        SQLQueryPlanner planner = new SQLQueryPlanner(basis.indusCacheDB.db,
            basis.indusSystemDB.db);
        return planner;
    }
}
