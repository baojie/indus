package edu.iastate.anthill.indus.query;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import edu.iastate.anthill.indus.datasource.mapping.BridgeRule;
import edu.iastate.anthill.indus.datasource.mapping.DataSourceMapping;
import edu.iastate.anthill.indus.datasource.mapping.NumericConnector;
import edu.iastate.anthill.indus.datasource.mapping.OntologyMapping;
import edu.iastate.anthill.indus.datasource.mapping.SchemaMapping;
import edu.iastate.anthill.indus.datasource.mapping.SimpleConnector;
import edu.iastate.anthill.indus.datasource.schema.Schema;
import edu.iastate.anthill.indus.datasource.type.AVH;
import edu.iastate.anthill.indus.tree.TypedTree;

import Zql.ZExp;
import Zql.ZExpression;
import Zql.ZFromItem;
import Zql.ZQuery;
import Zql.ZSelectItem;

/**
 * Translate query with a mapping file
 * @author Jie Bao
 * @since 1.0 2005-03-18
 */
public class SQLQueryTranslator
    extends SQLQueryRewriter
{
    /**
     * @since 2005-03-29
     * @param localSelect Vector
     * @param mapping DataSourceMapping
     * @param localSchema Schema
     * @param remoteSchema Schema
     */
    Vector translateInverseSelect(Vector localSelect, DataSourceMapping mapping
                                  , Schema localSchema, Schema remoteSchema)
    {
        SchemaMapping s_mapping = mapping.schemaMapping;

        Vector remoteSelects = new Vector();
        for (Iterator it = localSelect.iterator(); it.hasNext(); )
        {
            ZSelectItem col = (ZSelectItem) it.next();
            String colName = col.getColumn();

            BridgeRule r = s_mapping.findCompatibleOrEqual(colName);
            //Debug.trace("the rule: " + r);
            if (r != null)
            {
                String remoteColName = r.toTerm;
                ZSelectItem remoteCol;
                // 2005-03-29 make inverse mapping
                if (r.connector instanceof NumericConnector)
                {
                    NumericConnector nc = (NumericConnector) r.connector;
                    if (nc.inverseExpression != null)
                    {
                        String cast = "(CAST (" + remoteColName + " AS real))";
                        String str = nc.inverseExpression.toLowerCase().
                            replaceAll("x", cast);
                        remoteCol = new ZSelectItem(str);

                    }
                    else
                    {
                        remoteCol = new ZSelectItem(remoteColName);
                    }
                }
                else // string, avh, date, boolean
                {
                    // non numeric attribute,

                    //      colName --> remoteColName
                    // get their type
                    String localType = localSchema.getType(colName);
                    String remoteType = remoteSchema.getType(remoteColName);

                    // second, find the right AVH mapping in the DS mapping
                    OntologyMapping avhMapping = mapping.findAVHMapping(
                        localType, remoteType);
                    if (avhMapping == null) // no such mapping
                    {
                        remoteCol = new ZSelectItem(remoteColName);
                    }
                    else
                    {
                        //get the set of equalvalence
                        Vector equ = avhMapping.getEQU();
                        StringBuffer cast = new StringBuffer();
                        cast.append("CASE ");
                        for (int i = 0; i < equ.size(); i++)
                        {
                            BridgeRule rule = (BridgeRule) equ.elementAt(i);
                            cast.append(" WHEN " + remoteColName + " ='" +
                                        rule.toTerm + "' THEN '" + localType +
                                        ":" + rule.fromTerm + "' ");
                        }
                        // terms with no equalvalence
                        // put it as ontology_name : term_name
                        cast.append(" WHEN true THEN '" + remoteType + ":'||" +
                                    remoteColName + "  END");

                        remoteColName = cast.toString();
                        remoteCol = new ZSelectItem(remoteColName);
                    }
                }
                remoteSelects.add(remoteCol);
                //Debug.trace(colName + " is mapped to " + remoteColName);
            }
            else
            {
                // if there is no mapping for this attribute, return empty string
                ZSelectItem remoteCol = new ZSelectItem("null");
                remoteSelects.add(remoteCol);
            }
        }
        return remoteSelects;

    }

    /**
     * Translate a local query to remote query
     *
     * calling stack
     *     translateQuery
     *             -> translateWhere
     *                   -> translateAtomWhere
     *                            -> translateAVHAtomWhere
     *
     * @param localQuery ZQuery
     *    query over only one table
     * @param mapping DataSourceMapping
     *    mapping is a set of value mapping between AVH values
     *    we process only EQU(=), INTO(<=), ONTO(>=)
     *    We assume that the mapping is well-informed, that means
     *    all schema attribute and AVH values of local data source can find a
     *    proper mapped corresponding item in the remote data source
     * @return ZQuery
     * @since 2005-03-21
     */
    public ZQuery translateQuery(ZQuery localQuery, String remoteDataSource,
                                 DataSourceMapping mapping,
                                 Schema localSchema, Schema remoteSchema,
                                 boolean returnInLocalTerm)
    {
        SchemaMapping s_mapping = mapping.schemaMapping;
        //Debug.trace(s_mapping);

        ZQuery remoteQuery = new ZQuery();

        Vector remoteSelects = translateSelect(localQuery, mapping, localSchema,
                                               remoteSchema, returnInLocalTerm,
                                               s_mapping);
        remoteQuery.addSelect(remoteSelects);

        // from
        ZFromItem remoteFrom = new ZFromItem(remoteDataSource);
        Vector remoteFromVector = new Vector();
        remoteFromVector.add(remoteFrom);
        remoteQuery.addFrom(remoteFromVector);

        // where
        ZExp where = localQuery.getWhere();
        if (where == null ||
            "".equals(where.toString()) ||
            "()".equals(where.toString()))
        {}
        else if (where instanceof ZExpression)
        {
            remoteQuery.addWhere(translateWhere( (ZExpression) where, mapping,
                                                localSchema, remoteSchema));
        }
        else
        {
            remoteQuery.addWhere(where);
        }
        //Debug.trace("where = " + remoteQuery.getWhere());

        return remoteQuery;
    }

    /**
     * Translate select clause
     * @param localQuery ZQuery
     * @param mapping DataSourceMapping
     * @param localSchema Schema
     * @param remoteSchema Schema
     * @param returnInLocalTerm boolean
     * @param s_mapping SchemaMapping
     * @return Vector
     *
     * @author Jie Bao
     * @since 2005-03-29
     */
    private Vector translateSelect(ZQuery localQuery, DataSourceMapping mapping,
                                   Schema localSchema, Schema remoteSchema,
                                   boolean returnInLocalTerm,
                                   SchemaMapping s_mapping)
    {
        // select : we don't process * , only column names
        Vector localSelect = localQuery.getSelect();
        Vector remoteSelects = new Vector();
        if (returnInLocalTerm)
        {
            remoteSelects = translateInverseSelect(localSelect, mapping,
                localSchema,
                remoteSchema);
        }
        else
        {
            for (Iterator it = localSelect.iterator(); it.hasNext(); )
            {
                ZSelectItem col = (ZSelectItem) it.next();
                String colName = col.getColumn();

                BridgeRule r = s_mapping.findCompatibleOrEqual(colName);
                //Debug.trace("the rule: " + r);
                if (r != null)
                {
                    String remoteColName = r.toTerm;
                    ZSelectItem remoteCol;
                    remoteCol = new ZSelectItem(remoteColName);
                    remoteSelects.add(remoteCol);
                    //Debug.trace(colName + " is mapped to " + remoteColName);
                }
                else
                {
                    // if there is no mapping for this attribute, return empty string
                    ZSelectItem remoteCol = new ZSelectItem("null");
                    remoteSelects.add(remoteCol);
                    //Debug.trace(colName + " cannot be mapped");
                }
            }
        }
        return remoteSelects;
    }

    /**
     * translateWhere Translate a where clause with the given mapping
     * @param where ZExp
     * @param mapping DataSourceMapping
     * @return ZExp
     * @author Jie Bao
     * @since 2005-03-21
     */
    public ZExp translateWhere(ZExpression where,
                               DataSourceMapping mapping,
                               Schema localSchema,
                               Schema remoteSchema)

    {
        //System.out.println(where);
        if (isAtomWhere(where))
        {
            System.out.println("  is Atom Where Expression ");
            return translateAtomWhere(where, mapping, localSchema, remoteSchema);
        }
        else
        {
            //System.out.println("  not Atom Where Expression ");
            // rewrite its component clauses
            ZExpression newExp = new ZExpression(where.getOperator());
            Vector ops = where.getOperands();
            for (Iterator it = ops.iterator(); it.hasNext(); )
            {
                ZExp element = (ZExp) it.next();
                //System.out.println("   sub expression: " + element +
                //                   " of " + element.getClass());

                if (element instanceof ZExpression)
                {
                    ZExp zzz = translateWhere( (ZExpression) element, mapping,
                                              localSchema, remoteSchema);
                    // don't add null or empty expression
                    if (zzz instanceof ZExpression)
                    {
                        if ( ( (ZExpression) zzz).getOperands() == null)
                        {
                            continue;
                        }
                        if ( ( (ZExpression) zzz).getOperands().size() == 0)
                        {
                            continue;
                        }
                    }
                    newExp.addOperand(zzz);
                }
                else
                {
                    newExp.addOperand(element);
                }
            }

            // if it is an OR/AND clause and has only one oprand, return the only
            // oprand
            return SQLQueryBuilder.removeOrphanAndOr(newExp);
        }
    }

    /**
     * translateAtomWhere - Translate an atom where clause
     *
     * The atomic clause can be of the following form: X op Y (consisting of 3 parts)
     *
     * Here, X refers to a column name (1st operand).
     *
     * Here, op refers to one of the operators (=,<=,<,>,>=,!=).
     *
     * Here, Y refers to a value (2nd operand)
     * which can be either String, Number , AVH or even a column name.
     *
     *
     * @param where ZExpression
     * @param mapping DataSourceMapping
     * @return ZExp
     * @author Jie Bao
     * @since 2005-03-21
     */
    public ZExpression translateAtomWhere(ZExpression where,
                                          DataSourceMapping mapping,
                                          Schema localSchema,
                                          Schema remoteSchema)
    {
        //System.out.println(where);
        //Debug.trace(where);
        //System.out.println(where.getOperand(0).getClass());
        //System.out.println(where.getOperand(1).getClass());

        // Let us divide the WHERE clause (i.e., X op Y) into 3 parts
        String operator = (String) where.getOperator();

        // There exists only 2 operands in this vector.
        String localAttributeName = ( (ZConstantEx) (where.getOperand(0))).
            getValue();
        //System.out.println("operandName1 = "+operandName1);

        String localValue = ( (ZConstantEx) (where.getOperand(1))).getValue();

        // Firstly, change the column name of the 1st operand (into a column
        // name of the remote data source).
        SchemaMapping s_mapping = mapping.schemaMapping;
        //System.out.println(s_mapping);
        BridgeRule r = s_mapping.findCompatibleOrEqual(localAttributeName);
        //System.out.println("BridgeRule = " + r);

        if (r == null)
        {
            // can't find a peer remote attribute,
            return new ZExpression("AND");
        }
        String remoteAttributeName = r.toTerm;

        // This is the type of Operand2
        ZExp operand2 = where.getOperand(1);
        ZConstantEx operand2Type = (ZConstantEx) operand2;

        if (operand2Type.getType() == ZConstantEx.STRING)
        {
            // If it is a string, simply do nothing.
            return SQLQueryBuilder.buildAttributeValuePair(remoteAttributeName,
                operator, localValue, ZConstantEx.STRING);
        }
        else if (operand2Type.getType() == ZConstantEx.NUMBER)
        {
            String newValue = localValue;
            // if there is a numberical bridge rule between localAttributeName and
            // remoteAttributeName
            if (r.connector instanceof NumericConnector)
            {
                newValue = ( (NumericConnector) r.connector).eval(localValue);
            }

            return SQLQueryBuilder.buildAttributeValuePair(remoteAttributeName,
                operator, newValue, ZConstantEx.NUMBER);
        }
        else if (operand2Type.getType() == ZConstantEx.COLUMNNAME)
        {
            // If it is a column name, change the name in terms of the data source schema.
            r = s_mapping.findEqual(localValue);
            String remoteOperandName2 = r.toTerm;

            return SQLQueryBuilder.buildAttributeValuePair(remoteAttributeName,
                operator, remoteOperandName2, ZConstantEx.COLUMNNAME);
        }
        else if (operand2Type.getType() == ZConstantEx.AVH) // This is for AVH Only
        {
            // find the right AVH mapping for given local and remote attribute

            // first , find the type of given attributes
            //Debug.trace(localSchema);
            //Debug.trace(remoteSchema);
            String localType = localSchema.getType(localAttributeName);
            String remoteType = remoteSchema.getType(remoteAttributeName);

            // second, find the right AVH mapping in the DS mapping
            //System.out.println(localType+","+remoteType);
            OntologyMapping avhMapping = mapping.findAVHMapping(localType,
                remoteType);

            return translateAVHAtomWhere(where, remoteAttributeName, avhMapping);
        }
        return new ZExpression("AND");
    }

    /**
     * translateAVHAtomWhere
     *
     * @param where ZExpression
     * @param mapping DataSourceMapping
     * @since 2005-03-21
     */
    public ZExpression translateAVHAtomWhere(ZExpression where,
                                             String remoteColName,
                                             OntologyMapping mapping)
    {
        //System.out.println(where + " is AVH Atom Where");
        String localOperator = (String) where.getOperator();
        //String localColName = ( (ZConstantEx) (where.getOperand(0))).getValue();
        String localValueName = ( (ZConstantEx) (where.getOperand(1))).getValue();

        //System.out.println("localOperator = "+ localOperator);
        //System.out.println("localValueName = "+ localValueName);

        // if there is a EQU rule, just apply it, we don't need to change operator
        //System.out.println(mapping);
        BridgeRule rule = mapping.findEqual(localValueName);
        //System.out.println(localValueName + ": " +rule);

        if (rule != null)
        {
            //System.out.println("    Find EQU rule " + rule);
            String remoteValueName = rule.toTerm;
            ZExpression translated =  SQLQueryBuilder.buildAttributeValuePair(remoteColName,
                localOperator, remoteValueName, ZConstantEx.AVH);
            System.out.println("translated = "+translated);
            return translated;
        }

        else
        {
            // find applicable bridge rules
            Vector applicableRules = mapping.findMapped(localValueName);
            ZExpression modifiedWhere = new ZExpression("AND");

            for (Iterator it = applicableRules.iterator(); it.hasNext(); )
            {
                BridgeRule aRule = (BridgeRule) it.next();
                //System.out.println("Find a rule " + aRule);

                if (aRule.connector.equals(SimpleConnector.ONTO)) // >=
                {
                    if (localOperator.equals("<=") || localOperator.equals("<"))
                    {
                        String remoteValueName = aRule.toTerm;
                        ZExpression clause = SQLQueryBuilder.
                            buildAttributeValuePair(
                                remoteColName, localOperator, remoteValueName,
                                ZConstantEx.AVH);
                        modifiedWhere.addOperand(clause);
                    }
                }
                else if (aRule.connector.equals(SimpleConnector.INTO)) // <=
                {
                    if (localOperator.equals(">=") || localOperator.equals(">"))
                    {
                        String remoteValueName = aRule.toTerm;
                        ZExpression clause = SQLQueryBuilder.
                            buildAttributeValuePair(
                                remoteColName, localOperator, remoteValueName,
                                ZConstantEx.AVH);
                        modifiedWhere.addOperand(clause);
                    }
                }
                else if (aRule.connector.equals(SimpleConnector.UNEQU)) // !=
                {
                    if (localOperator.equals("=") || localOperator.equals("<") ||
                        localOperator.equals("<="))
                    {
                        String remoteValueName = aRule.toTerm;
                        ZExpression clause = SQLQueryBuilder.
                            buildAttributeValuePair(
                                remoteColName, "!=", remoteValueName,
                                ZConstantEx.AVH);
                        modifiedWhere.addOperand(clause);
                    }
                }
            }
            // apply the mapping rules

            // if there is only one clause, like  (AND (a = 1)), we just return the
            // the only clause, eg (a=1)
            ZExpression translated = (ZExpression) SQLQueryBuilder.removeOrphanAndOr(
                    modifiedWhere);
            //System.out.println("translated = "+translated);
            return translated;
        }
    }

    /** an expression is an atom where expression if it
     *   1. has two oprand
     *   2. the first oprand is a column name (ZConstantEx)
     *   3. the second oprand is a column name, AVH, string or number (ZConstantEx)
     *   4. the opration is one of  =,!=>,>=,<,<=
     * @param where ZExp
     * @return boolean
     */
    private boolean isAtomWhere(ZExpression where)
    {
        // condition 1, 4
        if (where.getOperands().size() != 2 ||
            !SQLQueryBuilder.isAvhOp(where.getOperator()))
        {
            return false;
        }

        // condition 2,3 part 1
        ZExp oprand1 = where.getOperand(0);
        ZExp oprand2 = where.getOperand(1);

        if (! (oprand1 instanceof ZConstantEx) &&
            ! (oprand2 instanceof ZConstantEx))
        {
            return false;
        }

        // condition 2,3 part 2
        ZConstantEx op1 = (ZConstantEx) oprand1;
        ZConstantEx op2 = (ZConstantEx) oprand2;

        if (op1.getType() != ZConstantEx.COLUMNNAME ||
            (op2.getType() != ZConstantEx.STRING &&
             op2.getType() != ZConstantEx.NUMBER &&
             op2.getType() != ZConstantEx.COLUMNNAME &&
             op2.getType() != ZConstantEx.AVH))
        {
            return false;
        }
        return true;
    }

    /**
     * The core function: do everything together
     * @param localQuery ZQuery
     * @param remoteDataSourceName String
     * @param dsMapping DataSourceMapping
     * @param localAttributeToAVH Map
     * @param remoteAttributeToAVH Map
     * @return ZQuery
     * @since 2005-03-22
     */
    public ZQuery doTranslate(ZQuery localQuery,
                              String remoteDataSourceName,
                              DataSourceMapping dsMapping,
                              Map localAttributeToAVH,
                              Map remoteAttributeToAVH,
                              Schema localSchema,
                              Schema remoteSchema,
                              boolean inLocalTerm)
    {
        // do the query rewriting wrt local AVHs
        System.out.println("Original: " + localQuery);

        ZQuery q = this.rewriteWithAVH(localQuery, localAttributeToAVH, true, true);
        System.out.println("Local rewriting: " + q);

        // add the local world limitation
        ZExp where = q.getWhere();
        if (where instanceof ZExpression)
        {
            q.addWhere(addLocalWorldLimitation( (ZExpression) where,
                                               localAttributeToAVH));
        }
        System.out.println("Added Local World Limitation: " + q);

        // remove all IN and NOT IN
        where = q.getWhere();
        if (where instanceof ZExpression)
        {
            where = removeIN( (ZExpression) where);
            q.addWhere(where);
        }
        System.out.println("removed IN: " + q);

        // translate the query to remote query wrt mapping between the local
        // data source and the remote data source
        // also do numerica mapping back into user value
        q = translateQuery(q, remoteDataSourceName, dsMapping,
                           localSchema, remoteSchema, inLocalTerm);
        System.out.println("Translated: " + q);

        // rewriting the query wrt remote AVHs
        q = rewriteWithAVH(q, remoteAttributeToAVH, false, false);
        System.out.println("Final: " + q);

        return q;
    }

    /**
     * Add the local world limiation to the query , eg if produceAt is a Location
     * AVH with root Iowa, then the returned query will be the AND of orginal query and
     * Location <= 'Iowa'
     *
     * @param where ZExpression
     * @param localAttributeToAVH Map
     * @return ZExpression
     * @author Jie Bao
     * @since 2005-03-22
     */
    public ZExpression addLocalWorldLimitation(ZExpression where,
                                               Map attributeToAVH)
    {
        ZExpression ze = new ZExpression("AND");
        ze.addOperand(where);

        Set usedColumn = getUsedColumn(where, attributeToAVH);

        for (Iterator it = usedColumn.iterator(); it.hasNext(); )
        {
            String attribute = (String) it.next();
            AVH avhTree = (AVH) attributeToAVH.get(attribute);
            TypedTree ontology = avhTree.getTreeAVH();
            String rootConcept = ontology.getTop().getUserObject().toString();
            ZExpression limitation = SQLQueryBuilder.buildAttributeValuePair(
                attribute, "<=", rootConcept, ZConstantEx.AVH);
            ze.addOperand(limitation);
        }
        return ze;
    }

    /**
     *
     * @param where ZExpression
     * @param attributeToAVH Map
     * @return Vector
     * @since 2005-03-22
     */
    public Set getUsedColumn(ZExpression where, Map attributeToAVH)
    {
        Set result = new HashSet();
        //System.out.println(where);

        if (isAtomWhere(where))
        {
            ZConstantEx op1 = (ZConstantEx) where.getOperand(0);
            ZConstantEx op2 = (ZConstantEx) where.getOperand(1);
            String name1 = op1.getValue();
            String name2 = op2.getValue();

            if (op1.getType() == ZConstantEx.COLUMNNAME &&
                attributeToAVH.get(name1) != null)
            {
                result.add(name1);
            }
            if (op2.getType() == ZConstantEx.COLUMNNAME &&
                attributeToAVH.get(name2) != null)
            {
                result.add(name2);
            }

        }
        else
        {
            Vector ops = where.getOperands();
            if (ops == null || ops.size() == 0)
            {
                return result;
            }

            for (Iterator it = ops.iterator(); it.hasNext(); )
            {
                ZExp element = (ZExp) it.next();
                if (element instanceof ZExpression)
                {
                    result.addAll(getUsedColumn( (ZExpression) element,
                                                attributeToAVH));
                }
                else
                {
                    return result;
                }

            }
        }
        return result;
    }

    public static void main(String[] args)
    {
        SQLQueryTranslatorTest.testDoQuery();
    }
}
