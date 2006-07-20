/**
 * 
 */
package edu.iastate.anthill.indus.query.test;

import java.io.IOException;
import java.util.Set;
import java.util.Vector;

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
            String fileName = "D:/course.zql";
            ZQuery myZQuery = (ZQuery) Serialization.loadFromFile(fileName);
            
            System.out.println(myZQuery);
            
            
            AVH d = (AVH)InfoReader.readDataType("catalog_mini-cornell", true);
            TypedTree t = d.getTreeAVH();
            
            // select a term from the tree randomly
            String term = "History";
            
            //System.out.println(d == null);
            
            TypedNode n = (TypedNode) t.findFirst(t,term);
            Set s= t.findAllOffspring(n);
            

//          replace the selection condition
            makeQuery(myZQuery,term);                
            System.out.println(myZQuery);
            
            
            for (int i = 0 ; i < 1; i++) {
                planner.doQuery(myZQuery, "course", false);
            }
            System.out.println(s.size());
        }
        catch (Exception e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }    
    }

    private static void makeQuery(ZQuery myZQuery, String XXX)
    {
        ZExpression where = (ZExpression) myZQuery.getWhere();
        ZConstantEx xxx = new ZConstantEx(XXX, ZConstantEx.AVH);        
        Vector v = where.getOperands();
        v.setElementAt(xxx,1);
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
