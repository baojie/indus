package edu.iastate.anthill.indus.query;

import Zql.ZQuery;

/**
 * <p>@author Jie Bao , baojie@cs.iastate.edu</p>
 * <p>@since </p>
 */
public class SQLQueryOptimizer
{

    /**
     * [do nothing so far, just return the original query]
     * @param q ZQuery
     * @return ZQuery
     * @Jie Bao
     * @since 2005-03-24
     */
    public static ZQuery optimize(ZQuery q)
    {
        return q;
    }

    public static void main(String[] args)
    {
        SQLQueryOptimizer queryoptimizer = new SQLQueryOptimizer();
    }
}
