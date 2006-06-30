/**
 * 
 */
package edu.iastate.anthill.indus.query.test;

import java.util.Vector;

import Zql.ZExpression;
import edu.iastate.anthill.indus.IndusConstants;
import edu.iastate.anthill.indus.IndusDB;
import edu.iastate.anthill.indus.gui.IndusMain;
import edu.iastate.anthill.indus.query.SQLQueryOptimizer;
import edu.iastate.anthill.indus.query.ZConstantEx;

/**
 * @author baojie
 * @since 2006-06-29
 *
 */
public class SQLQueryOptimizerTest
{
    public static void testRemoveDupBrackets()
    {
        String str = "((((a=2)) OR (b=2)))";
        System.out.println(str);
        str = SQLQueryOptimizer.removeDupBrackets(str.toCharArray());
        System.out.println(str);
    }

    public static ZExpression buildSampleINClause(Vector<String> values)
    {
        ZExpression valueSet = new ZExpression(",");

        for (String s : values)
            valueSet.addOperand(new ZConstantEx(s, ZConstantEx.AVH));

        ZConstantEx column = new ZConstantEx("producedAt",
                ZConstantEx.COLUMNNAME);

        ZExpression clause = new ZExpression("IN");
        clause.addOperand(column);
        clause.addOperand(valueSet);
        return clause;
    }

    public static void testRewriteLargeIN()
    {

        Vector<String> v1 = new Vector<String>();
        Vector<String> v2 = new Vector<String>();
        for (int i = 0; i < 10000; i++)
        {
            v1.add("a" + i);
            v2.add("b" + i);
        }
        ZExpression exp1 = buildSampleINClause(v1);
        ZExpression exp2 = buildSampleINClause(v2);
        ZExpression exp = new ZExpression("AND", exp1, exp2);

        IndusDB conn = new IndusDB();
        conn.connect(IndusConstants.dbURL);

        SQLQueryOptimizer opt = new SQLQueryOptimizer(conn.db);
        System.out.println(exp);
        opt.rewriteLargeIN(exp);
        System.out.println(exp);
        opt.close();
        conn.disconnect();

    }

    public static void main(String[] args)
    {
        testRewriteLargeIN();        
    }
}
