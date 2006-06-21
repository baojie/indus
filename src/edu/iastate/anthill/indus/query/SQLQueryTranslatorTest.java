package edu.iastate.anthill.indus.query;

import java.util.Map;

import edu.iastate.anthill.indus.datasource.mapping.DataSourceMapping;
import edu.iastate.anthill.indus.datasource.mapping.OntologyMapping;
import edu.iastate.anthill.indus.datasource.schema.Schema;

import Zql.ZExp;
import Zql.ZExpression;
import Zql.ZQuery;

/**
 * test class for QueryEngine
 * @author Jie Bao
 * @since 1.0 2005-03-20
 */
public class SQLQueryTranslatorTest
    extends SQLQuerySampleBuilder
{
    /**
     * @author Jie Bao
     * @since 2005-03-20
     */
    public static void testReWriteWhere()
    {
        Map attributeToAVH = buildSampleLocalAttributeToAVHMapping();

        // prepare the where expression
        // producedAt <= 'Iowa' AND distance <= 50
        ZExpression part1 = SQLQueryBuilder.buildAttributeValuePair(
            "producedAt",
            "<=", "USA", ZConstantEx.AVH);
        ZExpression part2 = SQLQueryBuilder.buildAttributeValuePair("distance",
            "<=", "50", ZConstantEx.NUMBER);
        ZExpression part3 = SQLQueryBuilder.buildAttributeValuePair("soldAt",
            ">=", "Iowa", ZConstantEx.AVH);
        ZExpression andClause = new ZExpression("AND", part1, part2);
        ZExpression where = new ZExpression("OR", andClause, part3);
        System.out.print("Old query: ");
        System.out.println(where);

        // test it
        SQLQueryTranslator qe = new SQLQueryTranslator();
        ZExpression newExp = qe.reWriteWhere(where, attributeToAVH, true, false);
        System.out.print("New query: ");
        System.out.println(newExp);
    }

    /**
     * @author Jie Bao
     * @since 2005-03-20
     */
    public static void testIsAVHExpression()
    {
        Map attributeToAVH = buildSampleLocalAttributeToAVHMapping();

        // prepare the where expression
        // producedAt <= 'Iowa' AND distance <= 50
        String value = "Iowa";
        ZExpression part1 = SQLQueryBuilder.buildAttributeValuePair(
            "producedAt",
            "<=", value, ZConstantEx.AVH);
        ZExpression part2 = SQLQueryBuilder.buildAttributeValuePair("distance",
            "<=", "50", ZConstantEx.NUMBER);

        // test it
        SQLQueryTranslator qe = new SQLQueryTranslator();
        boolean result = qe.isAtomAVHExpression(part1, attributeToAVH);
        System.out.println(part1 + " is AVH Expression: " + result);
        result = qe.isAtomAVHExpression(part2, attributeToAVH);
        System.out.println(part2 + " is AVH Expression: " + result);
    }

    /**
     * test translateAtomWhere
     * @since 2005-03-21
     */
    public static void testTranslateAVHAtomWhere()
    {
        String remoteColName = "madeIn";
        OntologyMapping mapping = buildSampleOntologyMapping();

        SQLQueryTranslator qe = new SQLQueryTranslator();

        String ops[] = SQLQueryBuilder.AVH_OP;
        String location[] = new String[]
            {
            "Iowa", "Virginia", "Ames", "Richmond"};
        for (int j = 0; j < location.length; j++)
        {
            for (int i = 0; i < ops.length; i++)
            {
                ZExpression q = SQLQueryBuilder.buildAttributeValuePair(
                    "producedAt",
                    ops[i], location[j], ZConstantEx.AVH);
                System.out.println("Old: " + q);

                ZExp ze = qe.translateAVHAtomWhere(q, remoteColName,
                    mapping);
                System.out.println("New: " + ze + "\n");
            }
            System.out.println("==========");
        }
    }

    /**
     * @since 2005-03-21
     */
    public static void testTranslateWhere()
    {
        DataSourceMapping mapping = buildSampleDataSourceMapping();
        Schema fromSchema = buildSampleLocalSchema();
        Schema toSchema = buildSampleRemoteSchema();

        SQLQueryTranslator qe = new SQLQueryTranslator();

        ZExpression test[] = new ZExpression[4];
        test[0] = SQLQueryBuilder.buildAttributeValuePair(
            "soldAt", "=", "producedAt", ZConstantEx.COLUMNNAME);
        test[1] = SQLQueryBuilder.buildAttributeValuePair(
            "id", "=", "1", ZConstantEx.NUMBER);
        test[2] = SQLQueryBuilder.buildAttributeValuePair(
            "name", "=", "iPod", ZConstantEx.STRING);
        test[3] = SQLQueryBuilder.buildAttributeValuePair(
            "producedAt", ">=", "Ames", ZConstantEx.AVH);

        ZExpression t1 = new ZExpression("AND", test[0], test[1]);
        ZExpression t2 = new ZExpression("AND", test[2], test[3]);
        ZExpression all = new ZExpression("OR", t1, t2);

        System.out.println("Old: " + all);
        ZExp ze = qe.translateWhere(all, mapping, fromSchema, toSchema);
        System.out.println("New: " + ze + "\n");
    }

    /**
     * @since 2005-03-21
     */
    public static void testTranslateAtomWhere()
    {
        Schema fromSchema = buildSampleLocalSchema();
        Schema toSchema = buildSampleRemoteSchema();
        DataSourceMapping mapping = buildSampleDataSourceMapping();

        SQLQueryTranslator qe = new SQLQueryTranslator();

        ZExpression test[] = new ZExpression[4];
        test[0] = SQLQueryBuilder.buildAttributeValuePair(
            "soldAt", "=", "producedAt", ZConstantEx.COLUMNNAME);
        test[1] = SQLQueryBuilder.buildAttributeValuePair(
            "id", "=", "1", ZConstantEx.NUMBER);
        test[2] = SQLQueryBuilder.buildAttributeValuePair(
            "name", "=", "iPod", ZConstantEx.STRING);
        test[3] = SQLQueryBuilder.buildAttributeValuePair(
            "producedAt", ">=", "Ames", ZConstantEx.AVH);

        for (int j = 0; j < test.length; j++)
        {
            System.out.println("Old: " + test[j]);
            ZExp ze = qe.translateAtomWhere(test[j], mapping, fromSchema,
                                            toSchema);
            System.out.println("New: " + ze + "\n");
        }
    }

    /**
     * @since 2005-03-21
     */
    public static void testTranslateQuery()
    {
        DataSourceMapping mapping = buildSampleDataSourceMapping();
        Schema fromSchema = buildSampleLocalSchema();
        Schema toSchema = buildSampleRemoteSchema();

        SQLQueryTranslator qe = new SQLQueryTranslator();
        ZQuery localQuery = buildSampleLocalQuery();
        System.out.println("Old: " + localQuery + "\n");
        ZQuery remoteQuery = qe.translateQuery(localQuery, "remoteTable",
                                               mapping, fromSchema, toSchema, true);
        System.out.println("New: " + remoteQuery + "\n");
    }

    /**
     * @since 2005-03-22
     */
    public static void testRewriteIN()
    {
        SQLQueryTranslator qe = new SQLQueryTranslator();

        ZExpression clause = buildSampleINClause(false);
        System.out.println("Old : " + clause);
        System.out.println("New  : " + qe.rewriteIN(clause));

        clause = buildSampleINClause(true);
        System.out.println("Old : " + clause);
        System.out.println("New  : " + qe.rewriteIN(clause));
    }

    /**
     * @since 2005-03-22
     */
    public static void testRemoveIn()
    {
        SQLQueryTranslator qe = new SQLQueryTranslator();

        ZExpression z1 = buildSampleINClause(false);
        ZExpression z2 = SQLQueryBuilder.buildAttributeValuePair("soldAt", "<=",
            "Iowa", ZConstantEx.AVH);
        ZExpression z3 = buildSampleINClause(true);
        ZExpression p1 = new ZExpression("OR", z1, z2);
        ZExpression clause = new ZExpression("AND", p1, z3);

        System.out.println("Old : " + clause);
        System.out.println("New  : " + qe.removeIN(clause));
    }

    /**
     * @since 2005-03-22
     */
    public static void testDoQuery()
    {
        DataSourceMapping mapping = buildSampleDataSourceMapping();
        Schema fromSchema = buildSampleLocalSchema();
        Schema toSchema = buildSampleRemoteSchema();

        SQLQueryTranslator qe = new SQLQueryTranslator();
        ZQuery localQuery = buildSampleLocalQuery();

        Map localAtt2avh = buildSampleLocalAttributeToAVHMapping();
        Map remoteAtt2avh = buildSampleRemoteAttributeToAVHMapping();

        ZQuery remoteQuery = qe.doTranslate(localQuery, "remoteTable",
                                            mapping, localAtt2avh,
                                            remoteAtt2avh,
                                            fromSchema, toSchema, true);
    }

    /**
     * @since 2005-03-22
     */
    public static void testGetUsedColumn()
    {
        SQLQueryTranslator qe = new SQLQueryTranslator();
        ZQuery localQuery = buildSampleLocalQuery();
        ZExpression ze = (ZExpression) localQuery.getWhere();
        Map localAtt2avh = buildSampleLocalAttributeToAVHMapping();

        System.out.println(ze);
        System.out.println(qe.getUsedColumn(ze, localAtt2avh));

    }

    public static void main(String[] args)
    {
        SQLQueryTranslatorTest queryenginetest = new SQLQueryTranslatorTest();
    }
}
