package edu.iastate.anthill.indus.query;

import java.util.Map;

import edu.iastate.anthill.indus.IndusBasis;
import edu.iastate.anthill.indus.agent.InfoReader;
import edu.iastate.anthill.indus.datasource.SampleBuilder;
import edu.iastate.anthill.indus.datasource.schema.Schema;

import Zql.ZQuery;

/**
 * <p>@author Jie Bao , baojie@cs.iastate.edu</p>
 * <p>@since 2005-03-24</p>
 */
public class SQLQueryPlannerTest
    extends SampleBuilder
{
    /**
     * @since 2005-03-25
     */
    public static void testFindAttributeToAVHMapping()
    {
        //SQLQueryPlanner planner = getPlannerInstance();
        Schema s = buildSampleRemoteSchema();
        Map m = InfoReader.findAttributeToAVHMapping(s);
        System.out.println(m);
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

    public static void testDoQuery()
    {
        SQLQueryPlanner planner = getPlannerInstance();
        ZQuery zq = buildSampleLocalQuery();
        planner.doQuery(zq, "test1", true);

    }

    public static void main(String[] args)
    {
        SQLQueryPlannerTest.testDoQuery();
    }
}
