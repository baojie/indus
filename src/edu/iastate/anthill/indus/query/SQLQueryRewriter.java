package edu.iastate.anthill.indus.query;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import javax.swing.tree.DefaultMutableTreeNode;

import edu.iastate.anthill.indus.datasource.type.AVH;
import edu.iastate.anthill.indus.tree.TypedTree;

import Zql.ZExp;
import Zql.ZExpression;
import Zql.ZQuery;

/**
 *  Rewrite query with an ontology
 *
 * <p>@author Jie Bao , baojie@cs.iastate.edu</p>
 * <p>@since 2005-03-29</p>
 */
public class SQLQueryRewriter
{
    public SQLQueryRewriter()
    {
    }

    /**
     *
     * @param attribute String
     * @param op String , could be =, !=, >, >=, <, <=
     * @param value String
     * @param ontology TypedTree
     * @return ZExpression
     * @author Jie Bao
     * @since 2005-03-19
     */
    public ZExpression rewriteAtomWhere(String attribute, String op,
                                        String value,
                                        TypedTree ontology)
    {
        // find the right set of nodes on the AVH tree
        //Debug.trace(attribute + " " + op + " " + value);

        Set values = new HashSet();

        String connector = "IN";
        if (op.compareTo("=") == 0)
        {
            values.add(value);
        }
        else if (op.compareTo("!=") == 0)
        {
            connector = "NOT IN";
            values.add(value);
        }
        else if (op.compareTo(">") == 0)
        {
            // find all nodes that higher than given value on the AVH
            // they are the node all the path from root to the given node (value)
            DefaultMutableTreeNode valueNode = TypedTree.findFirst(ontology,
                value);
            Set ancestorNode = TypedTree.findAncestor(valueNode);
            for (Iterator it = ancestorNode.iterator(); it.hasNext(); )
            {
                DefaultMutableTreeNode element = (DefaultMutableTreeNode) it.
                    next();
                values.add(element.getUserObject());
            }
        }
        else if (op.compareTo(">=") == 0)
        {
            DefaultMutableTreeNode valueNode = TypedTree.findFirst(ontology,
                value);
            Set ancestorNode = TypedTree.findAncestor(valueNode);
            for (Iterator it = ancestorNode.iterator(); it.hasNext(); )
            {
                DefaultMutableTreeNode element = (DefaultMutableTreeNode) it.
                    next();
                values.add(element.getUserObject());
            }
            values.add(value);
        }
        else if (op.compareTo("<") == 0)
        {
            // find all nodes that lower than given value on the AVH
            DefaultMutableTreeNode valueNode = TypedTree.findFirst(ontology,
                value);
            Set offspringNode = TypedTree.findAllOffspring(valueNode);
            for (Iterator it = offspringNode.iterator(); it.hasNext(); )
            {
                Object oo = it.next();
                //Debug.systrace(this,oo.getClass().getName());
                DefaultMutableTreeNode element = (DefaultMutableTreeNode) oo;
                values.add(element.getUserObject());
            }
        }
        else if (op.compareTo("<=") == 0)
        {
            DefaultMutableTreeNode valueNode = TypedTree.findFirst(ontology,
                value);
            //Debug.trace(valueNode);
            Set offspringNode = TypedTree.findAllOffspring(valueNode);
            for (Iterator it = offspringNode.iterator(); it.hasNext(); )
            {
                DefaultMutableTreeNode element = (DefaultMutableTreeNode) it.
                    next();
                values.add(element.getUserObject());
            }
            values.add(value);
        }

        if (values.size() == 0)
        {
            //no eligible node, this is possible only for > or <
            ZConstantEx one = new ZConstantEx("1", ZConstantEx.NUMBER);
            ZConstantEx zero = new ZConstantEx("0", ZConstantEx.NUMBER);

            // higher than nothing => true 1 > 0
            // lower than nothing => false 1 < 0
            return new ZExpression(op, one, zero);
        }
        else
        {
            ZExp valueSet = SQLQueryBuilder.buildValueSet(values,
                ZConstantEx.AVH);
            ZConstantEx field = new ZConstantEx(attribute,
                                                ZConstantEx.COLUMNNAME);
            ZExpression e = new ZExpression(connector, field, valueSet);
            return e;
        }
    }

    public boolean isINClause(ZExpression clause)
    {
        if (clause.getOperands() != null)
        {
            if (clause.getOperands().size() != 2)
            {
                return false;
            }

            String op = clause.getOperator();
            if (op.compareToIgnoreCase("IN") != 0 &&
                op.compareToIgnoreCase("NOT IN") != 0)
            {
                return false;
            }
        }
        return true;
    }

    /**
     * rewrite IN and NOT IN clause to OR/AND clause
     * @param inClause ZExpression
     * @return ZExpression - the rewritten clause without IN / NOT IN
     *     or the orginal clause if its not a IN/NOT IN clause
     * @author Jie Bao
     * @since 2005-03-22
     */
    public ZExpression rewriteIN(ZExpression inClause)
    {
        //System.out.println(inClause);
        if (!isINClause(inClause))
        {
            return inClause;
        }

        String op = inClause.getOperator();
        String newOp, newAtomOp;
        if (op.compareToIgnoreCase("IN") == 0)
        {
            newOp = "OR";
            newAtomOp = "=";
        }
        else if (op.compareToIgnoreCase("NOT IN") == 0)
        {
            newOp = "AND";
            newAtomOp = "!=";
        }
        else
        {
            return inClause;
        }

        ZExpression newClause = new ZExpression(newOp);
        ZConstantEx field = (ZConstantEx) inClause.getOperand(0);
        //System.out.println("field  = " + field);

        ZExp values = inClause.getOperand(1);

        if (values instanceof ZConstantEx)
        {
            return new ZExpression(newAtomOp, field, values);
        }
        else
        {
            ZExpression zzz = (ZExpression) values;
            if (zzz.getOperands().size() == 1)
            {
                return new ZExpression(newAtomOp, field, zzz.getOperand(0));
            }
            else
            {
                assert zzz.getOperator().equals(",");
                Vector allValues = zzz.getOperands();

                for (Iterator it = allValues.iterator(); it.hasNext(); )
                {
                    ZConstantEx v = (ZConstantEx) it.next();
                    //System.out.println( v + " is AVH " + (v.getType()==ZConstantEx.AVH));
                    ZExpression ze = SQLQueryBuilder.buildAttributeValuePair(
                        field.
                        getValue(), newAtomOp, v.getValue(), v.getType());
                    newClause.addOperand(ze);
                }
                return newClause;
            }
        }
    }

    /**
     * A atom sentence if a root limitation if
     *  - op is "<="
     *  - value is the root of the ontology tree
     *
     * @param op String
     * @param value String
     * @param ontology TypedTree
     * @return boolean
     * @since 2005-03-22
     */
    public boolean isAtomRootLimitation(String op, String value,
                                        TypedTree ontology)
    {
        String root = ontology.getTop().getUserObject().toString();
        return (op.equals("<=") && value.equals(root));
    }

    /** an expression is an AVH expression if it
     *   1. has two oprand
     *   2. the first oprand is a column name (ZConstant)
     *   3. the second oprand is an AVH (string) (ZConstant)
     *   4. there is a AVH associated with the column name
     *   5. the opration is one of  =,!=>,>=,<,<=
     *
     * @param where ZExpression
     * @param attributeToAVH Map
     * @return boolean
     * @author Jie Bao
     * @since 2005-03-20
     */
    protected boolean isAtomAVHExpression(ZExpression where, Map attributeToAVH)
    {
        // condition 1,5
        //System.out.println("getOperands().size(): " + where.getOperands().size());
        if (where.getOperands() == null)
        {
            return false;
        }

        if (where.getOperands().size() != 2 ||
            !SQLQueryBuilder.isAvhOp(where.getOperator()))
        {
            return false;
        }

        // condition 2,3 part 1
        ZExp oprand1 = where.getOperand(0);
        ZExp oprand2 = where.getOperand(1);

        //System.out.println("oprand1 :" + oprand1 + " of " + oprand1.getClass());
        //System.out.println("oprand2 :" + oprand2 + " of " + oprand2.getClass());
        if (! (oprand1 instanceof ZConstantEx) &&
            ! (oprand2 instanceof ZConstantEx))
        {
            return false;
        }

        // condition 2,3 part 2
        ZConstantEx op1 = (ZConstantEx) oprand1;
        ZConstantEx op2 = (ZConstantEx) oprand2;

        //System.out.println("op1 type : " + op1.getType());
        //System.out.println("op2 type : " + op2.getType());
        //System.out.println("    "+ZConstant.COLUMNNAME + " is column name, " +
        //                   ZConstant.STRING + " is string");
        if (op1.getType() != ZConstantEx.COLUMNNAME ||
            op2.getType() != ZConstantEx.AVH)
        {
            return false;
        }

        // condition 4
        String columnName = op1.toString();
        //System.out.println("columnName = " + columnName);
        AVH avhTree = (AVH) attributeToAVH.get(columnName);
        if (avhTree == null)
        {
            //System.out.println("tree name = " + avhTree.getTypeName());
            return false;
        }

        return true;

    }

    /**
     *
     * @param where ZExpression
     *    eg: Location <= Iowa AND Distance = 40
     * @param attributeToAVH Map
     * @return ZExpression
     * @author Jie Bao
     * @since 2005-03-20
     */
    public ZExpression reWriteWhere(ZExpression where, Map attributeToAVH,
                                    boolean keepOrginal,
                                    boolean rewriteRootLimitation)
    {
        if (where == null)
        {
            return where;
        }
        //System.out.print(where);
        if (isAtomAVHExpression(where, attributeToAVH))
        {
            //System.out.println("  is AVH Expression ");
            String attribute = ( (ZConstantEx) where.getOperand(0)).getValue();
            String op = where.getOperator();
            String value = ( (ZConstantEx) where.getOperand(1)).getValue();
            AVH avhTree = (AVH) attributeToAVH.get(attribute);
            TypedTree ontology = avhTree.getTreeAVH();

            if (isAtomRootLimitation(op, value, ontology) &&
                !rewriteRootLimitation)
            {
                return null;
            }

            ZExpression rewritten = rewriteAtomWhere(attribute, op, value,
                ontology);
            if (keepOrginal)
            {
                return new ZExpression("OR", rewritten, where);
            }
            else
            {
                return rewritten;
            }
        }
        else
        {
            //System.out.println("  not AVHExpression ");
            // rewrite its component clauses
            ZExpression newExp = new ZExpression(where.getOperator());
            //System.out.println("      Expression: " + where);
            Vector ops = where.getOperands();
            if (ops == null)
            {
                return where;
            }

            for (Iterator it = ops.iterator(); it.hasNext(); )
            {
                ZExp element = (ZExp) it.next();
                //System.out.println("   sub expression: " + element +
                //                   " of " + element.getClass());

                if (element instanceof ZExpression)
                {
                    ZExpression zzz = reWriteWhere( (ZExpression) element,
                        attributeToAVH, keepOrginal, rewriteRootLimitation);
                    if (zzz != null)
                    {
                        newExp.addOperand(zzz);
                    }
                }
                else
                {
                    newExp.addOperand(element);
                }
            }
            return (ZExpression) SQLQueryBuilder.removeOrphanAndOr(newExp);
        }
    }

    /**
     *
     * @param oldQuery ZQuery
     * @param attributeToAVH Map String -> AVH
     *    a map from attribute name to it's AVH
     * @return ZQuery - the new query
     * @author Jie Bao
     * @since 2005-03-19
     */

    public ZQuery rewriteWithAVH(ZQuery oldQuery, Map attributeToAVH,
                                 boolean keepOriginal,
                                 boolean rewriteRoorLimitation)
    {
        ZQuery newQuery = new ZQuery();

        // others are the same
        newQuery.addSelect(oldQuery.getSelect());
        newQuery.addFrom(oldQuery.getFrom());
        newQuery.addGroupBy(oldQuery.getGroupBy());
        newQuery.addOrderBy(oldQuery.getOrderBy());
        newQuery.addSet(oldQuery.getSet());

        // rewrite where
        // we don't support nested query eg:
        // SELECT * FROM t1 WHERE (a1 IN SELECT id FROM t2)
        // value should always appear on the right side of an expression
        // eg id > 6, but no 6 < id
        ZExp where = oldQuery.getWhere();
        if (where instanceof ZExpression)
        {
            newQuery.addWhere(reWriteWhere( (ZExpression) where, attributeToAVH,
                                           keepOriginal, rewriteRoorLimitation));
        }
        else
        {
            newQuery.addWhere(where);
        }

        return newQuery;
    }

    /**
     * rewrite all IN and NOT In clause
     * @param oldQuery ZQuery
     * @return ZQuery
     * @since 2005-03-22
     */
    public ZExpression removeIN(ZExpression where)
    {
        ZExpression newExp = new ZExpression(where.getOperator());
        Vector ops = where.getOperands();
        for (Iterator it = ops.iterator(); it.hasNext(); )
        {
            ZExp element = (ZExp) it.next();

            if (element instanceof ZExpression)
            {
                ZExpression clause = (ZExpression) element;

                // if it's a in clause, rewrite it.
                //  - 2 operands
                //  - operator is IN or NOT IN
                if (isINClause(clause))
                {
                    newExp.addOperand(rewriteIN(clause));
                }
                // if not
                else
                {
                    newExp.addOperand(removeIN(clause));
                }
            }
            else
            {
                newExp.addOperand(element);
            }
        }
        return newExp;
    }
}
