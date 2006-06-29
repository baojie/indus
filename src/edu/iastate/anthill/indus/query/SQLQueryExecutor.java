/**
 * 
 */
package edu.iastate.anthill.indus.query;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * @author baojie
 * @since 2006-06-28
 */
public class SQLQueryExecutor
{
    /**
     * Execute the query
     * 
     * @param db
     *            Connection
     * @param qeury
     *            ZQuery
     * @return ResultSet
     * @author Jie Bao
     * @since 2005-03-21
     */
    public static ResultSet executeNativeQuery(Connection db, String strQuery)
    {
        try
        {
            Statement stmt = db.createStatement();
            ResultSet rs = stmt.executeQuery(strQuery);
            
            return rs;
        }
        catch (SQLException ex)
        {
            ex.printStackTrace();
            return null;
        }
    }
}
