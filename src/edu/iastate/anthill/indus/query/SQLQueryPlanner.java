package edu.iastate.anthill.indus.query;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import Zql.ZExp;
import Zql.ZExpression;
import Zql.ZQuery;
import edu.iastate.anthill.indus.IndusConstants;
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

    /*
     translation a query in local ontology and schema 
     to a query in remote ontology and schema 
     return the list of translation remote queries
     Jie Bao  2005-03-24
     2006-06-30 enable return value  
     */
    public Map<String, String> doQuery(ZQuery localQuery, String viewName,
            boolean inLocalTerm)
    {
        Map<String, String> queries = new HashMap<String, String>();

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
            try
            {
                String datasourceName = (String) it.next();
                String mapping = (String) datasourceMapping.get(datasourceName);

                IndusDataSource dataSource = InfoReader.readDataSource(
                        systemDB, datasourceName);
                //System.out.println("[dataSource] " + dataSource);
                DataSourceMapping dsMapping = InfoReader.readMapping(mapping);
                //System.out.println("[dsMapping] " + dsMapping);

                String schemaName = dataSource.getSchemaName();
                Schema remoteSchema = InfoReader.readSchema(schemaName);
                //System.out.println("[remoteSchema] " + remoteSchema);

                Map remoteAttributeToAVH = InfoReader
                        .findAttributeToAVHMapping(remoteSchema);
                //System.out.println("[remoteAttributeToAVH] " + remoteAttributeToAVH);

                dataSource.connect();
                SQLQueryOptimizer opt = new SQLQueryOptimizer(dataSource.db);

                StopWatch w = new StopWatch();
                w.start();
                ZQuery query = translateSingleQuery(localQuery,
                        datasourceName, dsMapping, localAttributeToAVH,
                        remoteAttributeToAVH, localSchema, remoteSchema,
                        inLocalTerm, opt);                
                
                queries.put(datasourceName, query.toString());
                
                String strQuery = opt.optimize(query,true);
                w.stop();
                
                System.out.println("Translation time used for "
                        + datasourceName + " : " + w.print());

                w.start();

                try
                {
                    ResultSet result = SQLQueryExecutor.executeNativeQuery(
                            dataSource.db, strQuery);
                    if (result != null)
                    {
                        appendResult(view.getName(), select, result,
                                datasourceName);
                        w.stop();
                        System.out.println("Retrieval time used for "
                                + datasourceName + " : " + w.print());
                    }
                    else
                    {
                        Debug.trace("Data Source " + datasourceName
                                + " returns no records.");
                    }

                }
                catch (Exception ex)
                {
                    ex.printStackTrace();
                }

                opt.close();

                dataSource.disconnect();
            }
            catch (Exception e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        return queries;
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
    public ZQuery translateSingleQuery(ZQuery localQuery, String remoteDSName,
            DataSourceMapping dsMapping, Map localAttributeToAVH,
            Map remoteAttributeToAVH, Schema localSchema, Schema remoteSchema,
            boolean inLocalTerm, SQLQueryOptimizer opt)
    {
        SQLQueryTranslator translator = new SQLQueryTranslator();
        ZQuery query = translator.doTranslate(localQuery, remoteDSName,
                dsMapping, localAttributeToAVH, remoteAttributeToAVH,
                localSchema, remoteSchema, inLocalTerm);

        return query;
    }

    public static void main(String[] args)
    {
    // QueryPlanner queryplanner = new QueryPlanner();
    }
}
