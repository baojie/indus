package edu.iastate.anthill.indus.query;

import java.sql.Connection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Stack;
import java.util.Vector;

import Zql.ZExp;
import Zql.ZExpression;
import edu.iastate.anthill.indus.IndusBasis;
import edu.iastate.utils.sql.JDBCUtils;

/**
 * @author Jie Bao , baojie@cs.iastate.edu
 * @since 2005-03-24 
 */
public class SQLQueryOptimizer
{
    String     tempTable = "opt" + IndusBasis.getTimeStamp();
    Connection db;

    // 2006-06-29 Jie Bao
    public SQLQueryOptimizer(Connection db)
    {
        this.db = db;
        // create the temp tbale
        String sql = "CREATE TABLE " + tempTable + " (id text, mark integer);";
        JDBCUtils.updateDatabase(db, sql);
    }

    public void close()
    {
        String sql = "DROP TABLE " + tempTable;
        JDBCUtils.updateDatabase(db, sql);
    }

    public ZExp optimize(ZExpression exp)
    {
        //String s = ZqlUtils.printZExpression(exp);
        //System.out.println(s);

        rewriteLargeIN(exp);

        while (removeNullClause(exp))
        {
            ;
        }
        while (isOrphanAndOr(exp))
        {
            ZExp zz = removeOrphanAndOr(exp);
            if (!(exp instanceof ZExpression))
                break;
            else exp = (ZExpression) zz;
        }

        //s = ZqlUtils.printZExpression(exp);
        //System.out.println(s);

        return exp;

        // other oprimizations...
    }

    /**
     * @param z
     * @return
     * 
     * @author baojie
     * @since 2006-06-28
     */
    private void rewriteLargeIN(ZExpression exp)
    {
        Vector<ZExp> opr = exp.getOperands();
        for (Iterator it = opr.iterator(); it.hasNext();)
        {
            ZExp e = (ZExp) it.next();
            if (e instanceof ZExpression)
            {
                // check it is a long list
                ZExpression ee= (ZExpression) e;
                
                String op = ee.getOperator();
                
                rewriteLargeIN(exp);
            }
            
        }

        return;
    }

    // Jie Bao 2006-06-18
    public static boolean hasOperand(ZExpression e)
    {
        Vector op = e.getOperands();
        if (op == null)
            return false;
        else return (op.size() != 0);
    }

    //  Jie Bao 2006-06-19
    public static boolean removeNullClause(ZExpression exp)
    {
        //System.out.println("    -- removeNullClause " + exp);
        boolean changed = false;

        Vector<ZExp> opr = exp.getOperands();
        for (Iterator it = opr.iterator(); it.hasNext();)
        {
            ZExp e = (ZExp) it.next();
            if (e instanceof ZExpression)
            {
                if (hasOperand((ZExpression) e))
                {

                    //removeOrphanAndOr

                    changed = changed || removeNullClause((ZExpression) e);
                }
                else
                {
                    //                  remove this expression from its parent
                    it.remove();
                    //Debug.trace("remove : " + e);
                    changed = true;
                }
            }
        }

        return changed;
    }

    /**
     *  AND (v1) => v1
     *  OR (v1) -> v1
     * @param exp ZExpression
     * @return ZExp
     * @since 2005-03-22
     */
    public static ZExp removeOrphanAndOr(ZExpression exp)
    {
        if (exp.getOperands() != null
                && ((exp.getOperator().compareToIgnoreCase("AND") == 0) || (exp
                        .getOperator().compareToIgnoreCase("OR") == 0)))
        {
            if (exp.getOperands().size() == 1) { return exp.getOperand(0); }
        }
        return exp;
    }

    /**
     * @author baojie
     * @since 2006-06-21
     * @param exp
     * @return
     */
    public static boolean isOrphanAndOr(ZExpression exp)
    {
        if (exp.getOperands() != null
                && ((exp.getOperator().compareToIgnoreCase("AND") == 0) || (exp
                        .getOperator().compareToIgnoreCase("OR") == 0)))
        {
            if (exp.getOperands().size() == 1) { return true; }
        }
        return false;
    }

    /**
     * optimzeQuery
     * 
     * remove duplicated brackets, like ((a >1 )) will be (a>1)
     *         ((((a=2)) OR (b=2)))
     *   -->    ( (a=2)  OR (b=2)) 
     * 
     * 
     * @param strQuery
     *            String
     * @return String
     * @author Jie Bao
     * @since 2005-10-19
     */
    public static String removeDupBrackets(char[] strQuery)
    {
        // use a stack
        Stack<Integer> s = new Stack<Integer>();
        Map<Integer, Integer> closing2opening = new HashMap<Integer, Integer>();

        // detect all pairs of '(' and ')'
        for (int i = 0; i < strQuery.length; i++)
        {
            char c = strQuery[i];
            if (c == '(')
            {
                s.push(i); // find a opening, push the position into the stack
            }
            if (c == ')')
            {
                Integer open = s.pop();
                closing2opening.put(i, open);
            }
        }
        // remove redundant ones
        for (int i = 0; i < strQuery.length; i++)
        {
            char c = strQuery[i];
            if (c == ')') // for a char ')'
            {
                if (i > 0)
                {
                    char last = strQuery[i - 1];
                    if (last == ')') // if the last char is also ')'
                    {
                        int c_open = closing2opening.get(i);
                        int last_open = closing2opening.get(i - 1);
                        if (last_open - c_open == 1) // their opening '(' are
                        // adjacent
                        {
                            // delete current ')' and its '('
                            strQuery[i] = ' ';
                            strQuery[c_open] = ' ';
                        }
                    }
                }
            }
        }

        return new String(strQuery);
    }

}
