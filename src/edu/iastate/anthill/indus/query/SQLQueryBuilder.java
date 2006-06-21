package edu.iastate.anthill.indus.query;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Vector;

import edu.iastate.anthill.indus.datasource.mapping.BridgeRule;
import edu.iastate.utils.Debug;

import Zql.ZConstant;
import Zql.ZExp;
import Zql.ZExpression;
import Zql.ZFromItem;
import Zql.ZQuery;
import Zql.ZSelectItem;

/**
 * Class to build query and some utility classes to improve the 
 * ZQL library
 * @author Jie Bao
 * @since 1.0
 */
public class SQLQueryBuilder
{

    public static String AVH_OP[] = new String[]
        {
        "=", "!=", ">=", ">", "<", "<="};

    /**
     * to check is a operator a AVH operator
     * @param op String
     * @return boolean
     * @author Jie Bao
     * @since 2005-03-20
     */
    public static boolean isAvhOp(String op)
    {
        return Arrays.asList(AVH_OP).contains(op);
    }

    /**
     *
     * @param values Set
     * @param typ int ZConstant.NULL , NUMBER, STRING
     * @return ZExp
     * @author Jie Bao
     * @since 2005-03-19
     */
    public static ZExp buildValueSet(Collection values, int typ)
    {
        if (values.size() == 0)
        {
            return null;
        }
        else if (values.size() == 1)
        {
            return new ZConstantEx(values.toArray()[0].toString(), typ);
        }
        else
        {
            ZExpression valueSet = new ZExpression(",");
            for (Iterator it = values.iterator(); it.hasNext(); )
            {
                Object v = it.next();
                ZConstantEx value = new ZConstantEx(v.toString(), typ);
                valueSet.addOperand(value);
            }
            return valueSet;
        }

    }

    /**
     * @author Jie Bao
     * @since 2005-03-19
     */
    public static void testBuildSimpleQuery()
    {
        ZQuery q = new ZQuery();

        Vector select = new Vector();
        ZSelectItem s = new ZSelectItem("id");
        select.add(s);
        q.addSelect(select);

        Vector from = new Vector();
        ZFromItem f = new ZFromItem("mytable");
        from.add(f);
        q.addFrom(from);

        ZConstantEx field = new ZConstantEx("col", ZConstantEx.COLUMNNAME);

        HashSet ss = new HashSet();
        ss.add("5");
        ss.add("6");
        ZExp valueSet = SQLQueryBuilder.buildValueSet(ss, ZConstantEx.NUMBER);
        ZExpression e = new ZExpression("IN", field, valueSet);
        q.addWhere(e);

        System.out.println(q);
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
        if (exp.getOperands() != null &&
            ( (exp.getOperator().compareToIgnoreCase("AND") == 0) ||
             (exp.getOperator().compareToIgnoreCase("OR") == 0)))
        {
            if (exp.getOperands().size() == 1)
            {
                return exp.getOperand(0);
            }
        }
        return exp;
    }
    
    // Jie Bao 2006-06-18
    public static boolean hasOperand(ZExpression e)
    {
        Vector op = e.getOperands();
        if (op == null) return false;
        else return (op.size() !=0);                
    }
    
    public static ZExp optimize(ZExpression exp)
    {
        while(removeNullClause(exp)) {;}
        
                
        return removeOrphanAndOr(exp);
        
        // other oprimizations...
    }
    
    // Jie Bao 2006-06-19
    public static boolean removeNullClause(ZExpression exp)
    {
        System.out.println("    -- removeNullClause " + exp);
        boolean changed = false;
        
        Vector<ZExp> opr = exp.getOperands();
        for (Iterator it = opr.iterator(); it.hasNext();)
        {
            ZExp e = (ZExp) it.next();
            if (e instanceof ZExpression)
            {                
                if (hasOperand((ZExpression)e)) {
                    
                    //removeOrphanAndOr
                    
                    changed = changed || removeNullClause((ZExpression)e);
                }
                else {
//                  remove this expression from its parent
                    it.remove();
                    Debug.trace("remove : "+ e);
                    changed = true;
                }               
            }
        }
        
        return changed;        
    }

    /**
     *
     * @param columnName String
     * @param op String
     * @param value String
     * @param valueType  - int could be ZConstantEx.NUMBER or ZConstantEx.STRING
     * @return ZExpression
     * @author Jie Bao
     * @since 2005-03-20
     */
    public static ZExpression buildAttributeValuePair(String columnName,
        String op, String value, int valueType)
    {
        ZConstantEx op1 = new ZConstantEx(columnName, ZConstantEx.COLUMNNAME);
        ZConstantEx op2 = new ZConstantEx(value, valueType);
        //Debug.trace(op2.getType());
        ZExpression exp = new ZExpression(op, op1, op2);
        return exp;
    }

    /**
     * Get the select items as string array
     * @param localQuery ZQuery
     * @return String[]
     * @since 2005-03-24
     */
    public static String[] selectList(ZQuery localQuery)
    {
        Vector select = localQuery.getSelect();
        String selectArray[] = new String[select.size()];
        for (int i = 0; i < select.size(); i++)
        {
            ZSelectItem item = (ZSelectItem) select.elementAt(i);
            ZExp zz = item.getExpression();
            selectArray[i] = zz.toString();
        }
        return selectArray;
    }

    public static ZConstant TRUE = new ZConstant("true", ZConstant.UNKNOWN);
    public static ZConstant FALSE = new ZConstant("false", ZConstant.UNKNOWN);
    
    /**
     * Print the structure of the query
     * @author Jie Bao
     * @since 2006-06-15
     * @param q
     */
    public static String printZExpression(ZExpression e)
    {
    	//e.getOperator()
    	return e.toReversePolish();
    }    
}
