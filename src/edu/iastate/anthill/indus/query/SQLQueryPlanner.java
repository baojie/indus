package edu.iastate.anthill.indus.query;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.Map;

import Zql.ZExp;
import Zql.ZExpression;
import Zql.ZQuery;
import edu.iastate.anthill.indus.IndusBasis;
import edu.iastate.anthill.indus.agent.InfoReader;
import edu.iastate.anthill.indus.datasource.IndusDataSource;
import edu.iastate.anthill.indus.datasource.mapping.DataSourceMapping;
import edu.iastate.anthill.indus.datasource.schema.Schema;
import edu.iastate.anthill.indus.datasource.view.View;
import edu.iastate.utils.Debug;
import edu.iastate.utils.lang.StopWatch;
import edu.iastate.utils.sql.JDBCUtils;

/**
 * @author Jie Bao , baojie@cs.iastate.edu
 * @since 2005-03-24
 * 
 * Given a local query and a view definition over a set of remote date sources,
 * query planner translates the local query to a set of remote queries (by calling)
 * QueryTranslator), create a SQL over remote DB, compose the query result(union)
 * and translates it backing into the local ontology
 */
public class SQLQueryPlanner
{
    Connection cacheDB, systemDB;

    public SQLQueryPlanner(Connection cacheDB, Connection systemDB)
    {
        this.cacheDB = cacheDB;
        this.systemDB = systemDB;
    }

    // result is Vector of Vector of String [2-dimensional table]
    public void doQuery(ZQuery localQuery, String viewName, boolean inLocalTerm)
    {
        String s[] = ZqlUtils.selectList(localQuery);
        String select[] = new String[s.length + 1]; // one more column to store data origin
        for (int i = 0; i < s.length; i++)
        {
            select[i] = s[i];
        }
        select[s.length] = View.FROM_DATA_SOURCE;

        // read view details
        View view = InfoReader.readView(viewName);
        //System.out.println("[View] " + view);

        // get the local schema
        String localSchemaName = view.getLocalSchemaName();
        Schema localSchema = InfoReader.readSchema(localSchemaName);
        //System.out.println("[localSchema] " + localSchema);

        Map localAttributeToAVH = InfoReader
                .findAttributeToAVHMapping(localSchema);
        //System.out.println("[localAttributeToAVH] " + localAttributeToAVH);

        // the local cache table should already been createed
        clearCache(viewName);

        // get the set of remote data source and the mapping
        Map datasourceMapping = view.getDatasourceMapping();
        //Vector allResult = new Vector();
        for (Iterator it = datasourceMapping.keySet().iterator(); it.hasNext();)
        {
            String datasourceName = (String) it.next();
            String mapping = (String) datasourceMapping.get(datasourceName);

            IndusDataSource dataSource = InfoReader.readDataSource(systemDB,
                    datasourceName);
            //System.out.println("[dataSource] " + dataSource);
            DataSourceMapping dsMapping = InfoReader.readMapping(mapping);
            //System.out.println("[dsMapping] " + dsMapping);

            String schemaName = dataSource.getSchemaName();
            Schema remoteSchema = InfoReader.readSchema(schemaName);
            //System.out.println("[remoteSchema] " + remoteSchema);

            Map remoteAttributeToAVH = InfoReader
                    .findAttributeToAVHMapping(remoteSchema);
            //System.out.println("[remoteAttributeToAVH] " + remoteAttributeToAVH);

            StopWatch w = new StopWatch();
            w.start();
            ResultSet r = doSingleQuery(localQuery, dsMapping,
                    localAttributeToAVH, remoteAttributeToAVH, dataSource,
                    localSchema, remoteSchema, inLocalTerm);
            w.stop();
            System.out.println("Translation time used for " + datasourceName
                    + " : " + w.print());

            if (r != null)
            {
                w.start();
                appendResult(view.getName(), select, r, datasourceName);
                w.stop();
                System.out.println("Retrieval time used for " + datasourceName
                        + " : " + w.print());
            }
            else
            {
                Debug.trace("Data Source " + datasourceName
                        + " returns no records.");
            }
        }
        //return allResult;
    }

    /**
     * clearCache
     */
    private void clearCache(String localCacheName)
    {
        JDBCUtils.clearTable(cacheDB, localCacheName);

    }

    /**
     * appendResult
     *
     * @param string String
     * @param r ResultSet
     * @author Jie Bao
     * @since 2005-03-25
     */
    private void appendResult(String localCacheName, String select[],
            ResultSet rs, String fromDS)
    {

        try
        {
            ResultSetMetaData rsmd = rs.getMetaData();
            int numberOfColumns = rsmd.getColumnCount();

            // make sure result has the same number of columns to the local query "select"
            if (numberOfColumns + 1 != select.length)
            {
                System.out.println("appendResult: wrong number of columns!");
                return;
            }

            String v[] = new String[numberOfColumns + 1];
            int count = 0;
            StringBuffer buf = new StringBuffer();
            while (rs.next())
            {
                //System.out.println(count);
                for (int i = 0; i < numberOfColumns; i++)
                {
                    v[i] = rs.getString(i + 1);
                }
                v[numberOfColumns] = fromDS;

                String sql = JDBCUtils.insertDatabaseS(localCacheName, select,
                        v);
                buf.append(sql + ";");
                count++;
            }

            JDBCUtils.updateDatabase(cacheDB, buf.toString());
            System.out.println("Data Source " + fromDS + " returns " + count
                    + " records");
        }
        catch (SQLException ex)
        {
            ex.printStackTrace();
        }
    }

    /**
     * Do query on one remote data source
     * @param localQuery ZQuery
     * @param remoteDataSourceName String
     * @param dsMapping DataSourceMapping
     * @param localAttributeToAVH Map
     * @param remoteAttributeToAVH Map
     * 
     * @since 2005-03-24
     * @author Jie Bao
     */
    public ResultSet doSingleQuery(ZQuery localQuery,
            DataSourceMapping dsMapping, Map localAttributeToAVH,
            Map remoteAttributeToAVH, IndusDataSource dataSource,
            Schema localSchema, Schema remoteSchema, boolean inLocalTerm)
    {
        SQLQueryTranslator translator = new SQLQueryTranslator();
        ZQuery query = translator.doTranslate(localQuery, dataSource.getName(),
                dsMapping, localAttributeToAVH, remoteAttributeToAVH,
                localSchema, remoteSchema, inLocalTerm);
        SQLQueryOptimizer opt = new SQLQueryOptimizer(IndusBasis.indusSystemDB.db);

        // {{ 2005-10-19 Jie Bao
        ZExp z = opt.optimize((ZExpression) query.getWhere());
        query.addWhere(z);// replace the old WHERE

        //String s = ZqlUtils.printZExpression((ZExpression) query.getWhere());
        //System.out.println(s);

        String strQuery = query.toString();
        strQuery = SQLQueryOptimizer.removeDupBrackets(strQuery.toCharArray());
        System.out.println("Native query : " + strQuery);
        // }} 2005-10-19        

        try
        {
            dataSource.connect();

            ResultSet result = SQLQueryExecutor.executeNativeQuery(
                    dataSource.db, strQuery);
            dataSource.disconnect();
            opt.close();
            return result;
        }
        catch (Exception ex)
        {
            return null;
        }
        
    }

    public static void main(String[] args)
    {
    // QueryPlanner queryplanner = new QueryPlanner();
    }
}
