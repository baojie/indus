/**
 * 
 */
package edu.iastate.anthill.indus.query.test;

import Zql.ZExpression;
import edu.iastate.anthill.indus.datasource.SampleBuilder;
import edu.iastate.anthill.indus.query.SQLQueryOptimizer;
import edu.iastate.anthill.indus.query.ZqlUtils;

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
    
    public static void testRewriteLargeIN()
    {
        ZExpression z = SampleBuilder.buildSampleINClause(true);
        System.out.println( ZqlUtils.printZExpression(z));
        
        
    }
    
    public static void main(String[] args)
    {
        testRewriteLargeIN();
    }
}
