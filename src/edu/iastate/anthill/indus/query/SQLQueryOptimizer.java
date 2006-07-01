package edu.iastate.anthill.indus.query;

import java.sql.Connection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Stack;
import java.util.Vector;

import Zql.ZConstant;
import Zql.ZExp;
import Zql.ZExpression;
import Zql.ZFromItem;
import Zql.ZQuery;
import Zql.ZSelectItem;

import edu.iastate.anthill.indus.IndusConstants;
import edu.iastate.anthill.indus.gui.IndusBasis;
import edu.iastate.utils.sql.JDBCUtils;

/**
 * @author Jie Bao , baojie@cs.iastate.edu
 * @since 2005-03-24 
 */
public class SQLQueryOptimizer
{
    String     tempTable = "indus_temp" + IndusBasis.getTimeStamp();
    boolean    writeable = false;
    Connection db;

    // 2006-06-29 Jie Bao
    public SQLQueryOptimizer(Connection db)
    {
        this.db = db;
        // create the temp tbale
        String sql = "CREATE TABLE " + tempTable + " (id text, mark integer);";
        writeable = JDBCUtils.updateDatabase(db, sql);
        //Debug.trace(writeable);
    }

    public void close()
    {
        String sql = "DROP TABLE " + tempTable;
        JDBCUtils.updateDatabase(db, sql);
    }
    
    // Jie Bao 2006-06-30
    public String optimize(ZQuery query,  boolean rewriteLongIN) 
    {
        // 2006-06-29 optimization
        ZExp z = optimize((ZExpression) query.getWhere(),rewriteLongIN);
        query.addWhere(z);// replace the old WHERE
        // {{ 2005-10-19 Jie Bao
        //String s = ZqlUtils.printZExpression((ZExpression) query.getWhere());
        //System.out.println(s);

        String strQuery = query.toString();
        strQuery = removeDupBrackets(strQuery.toCharArray());
        if (IndusConstants.DEBUG)
            System.out.println("Native query : " + strQuery);
        // }} 2005-10-19  
        
        return strQuery.toString();
    }

    public ZExp optimize(ZExpression exp, boolean rewriteLongIN)
    {
        //String s = ZqlUtils.printZExpression(exp);
        //System.out.println(s);
        if (exp == null) return exp;

        if (writeable && rewriteLongIN)
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

        // other optimalizations...
    }

    static int groupID = 0;

    /**
     * @param z
     * @return
     * 
     * @author baojie
     * @since 2006-06-28
     */
    public void rewriteLargeIN(ZExpression exp)
    {
        String sql = rewriteLargeIN1(exp);
        //System.out.println(sql);
        if (sql.length() > 0) JDBCUtils.updateDatabase(db, sql);
    }

    private String rewriteLargeIN1(ZExpression exp)
    {
        int limit = 20; // the max number of items in a list

        StringBuffer buf = new StringBuffer();

        Vector<ZExp> opr = exp.getOperands();
        
        if (opr == null) return "";
        
        for (int j = 0; j < opr.size(); j++)
        {
            ZExp e = opr.elementAt(j);
            if (e instanceof ZExpression)
            {
                // check it if is a long list
                ZExpression ee = (ZExpression) e;
                String op = ee.getOperator();

                if (op.equals(",") && ee.getOperands().size() >= limit)
                {
                    groupID++;
                    //System.out.println("   A long ',' list!");

                    for (Object item : ee.getOperands())
                    {
                        if (item instanceof ZConstant)
                        {
                            //System.out.println(((ZConstant)item).getValue());
                            String s = "INSERT INTO " + tempTable
                                    + " (id, mark) VALUES (" + item + ", "
                                    + groupID + " );\n";
                            buf.append(s);
                        }
                    }
                    String sql = buf.toString();
                    //System.out.println(sql);

                    ZQuery q = new ZQuery();
                    Vector select = new Vector();
                    select.add(new ZSelectItem("id"));
                    q.addSelect(select);

                    Vector from = new Vector();
                    from.add(new ZFromItem(tempTable));
                    q.addFrom(from);

                    ZExpression where = ZqlUtils.buildAttributeValuePair(
                            "mark", "=", groupID + "", ZConstantEx.NUMBER);
                    q.addWhere(where);
                    //System.out.println(q.toString());

                    opr.setElementAt(q, j);
                }
                else
                {
                    String s = rewriteLargeIN1(ee);
                    if (s != null)
                    {
                        buf.append(s);
                    }
                }

            }
        }
        return buf.toString();
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
        
        if (opr == null) return false;
        
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
