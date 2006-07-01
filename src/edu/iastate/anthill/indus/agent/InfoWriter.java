package edu.iastate.anthill.indus.agent;

import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

import edu.iastate.anthill.indus.datasource.Configable;
import edu.iastate.anthill.indus.datasource.IndusDataSource;
import edu.iastate.anthill.indus.datasource.mapping.DataSourceMapping;
import edu.iastate.anthill.indus.datasource.schema.Schema;
import edu.iastate.anthill.indus.datasource.type.DataType;
import edu.iastate.anthill.indus.datasource.view.View;
import edu.iastate.anthill.indus.gui.IndusBasis;
import edu.iastate.utils.Debug;
import edu.iastate.utils.sql.JDBCUtils;
import edu.iastate.utils.string.Zip;

/**
 * <p>@author Jie Bao , baojie@cs.iastate.edu</p>
 * <p>@since 2005-03-27</p>
 */
public class InfoWriter implements IndusCommand
{
    /**
     * Send schema to the server
     * @param schema Schema
     * @return boolean
     * @since 2005-03-27
     */
    public static boolean writeSchema(Schema schema)
    {
        return write(CMD_UPDATE_SCHEMA, schema.getName(), schema);
    }

    public static boolean writeType(DataType type)
    {
        //writeTypeOld(type);

        // save to database
        String value = Zip.encode(type.toText());
        String name = type.getName();
        String space = "public";

        Map<String,String> values = new HashMap<String,String>();

        values.put("name", name);
        values.put("value", value);
        values.put("space", space);

        Connection db = IndusBasis.indusSystemDB.db;
        return JDBCUtils.insertOrUpdateDatabase(db, "types", values, "name");

    }

    // save to the indus server
    public static boolean writeTypeOld(DataType type)
    {
        // the old XML-based storage
        //return write(CMD_UPDATE_TYPE, type.getName(), type);

        // 2006-06-20 compressed plain text
        // @see InfoReader
        String text = Zip.encode(type.toText());
        //Debug.trace("after encoding: "+text.length());
        // send to server
        IndusHttpClient client = new IndusHttpClient();
        String res = client.sendCmd(CMD_UPDATE_TYPE + ";name=" + type.getName()
                + ";value=" + text);
        return res.equals(RES_OK);
    }

    /**
     * @deprecated
     * @param mapping
     * @return
     */
    public static boolean writeMappingOld(DataSourceMapping mapping)
    {
        return write(CMD_UPDATE_MAPPING, mapping.getName(), mapping);
    }

    public static boolean writeMapping(DataSourceMapping mapping)
    {
        //writeMappingOld(type);

        // save to database
        String value = Zip.encode(mapping.toText());
        String name = mapping.getName();
        String space = "public";
        String format = "XML1.0";        

        Map<String,String> values = new HashMap<String,String>();

        values.put("name", name);
        values.put("value", value);
        values.put("space", space);
        values.put("ont1",mapping.schemaMapping.from);
        values.put("ont2",mapping.schemaMapping.to);
        values.put("format", format);
        
        Connection db = IndusBasis.indusSystemDB.db;
        return JDBCUtils.insertOrUpdateDatabase(db, "mappings", values, "name");
    }

    public static boolean writeView(View view)
    {
        return write(CMD_UPDATE_VIEW, view.getName(), view);
    }

    public static boolean writeDataSource(Connection systemDB,
            IndusDataSource ds)
    {
        return ds.toDB(systemDB);
    }

    /**
     * Generic writer
     * @param command String
     * @param name String
     * @param toWrite Configable
     * @return boolean
     * @since 2005-03-27
     *
     */
    private static boolean write(String command, String name, Configable toWrite)
    {
        // general XML
        String xml = toWrite.toText();

        // send to server
        IndusHttpClient client = new IndusHttpClient();
        String res = client
                .sendCmd(command + ";name=" + name + ";value=" + xml);
        return RES_OK.equals(res);
    }

    /**
     * Generic delete
     * @param command String
     * @param name String
     * @return boolean
     * @since 2005-03-27
     */
    private static boolean delete(String command, String name)
    {
        IndusHttpClient client = new IndusHttpClient();
        String res = client.sendCmd(command + ";name=" + name);
        return res.equals(RES_OK);
    }

    public static boolean deleteTypeOld(String name)
    {
        return delete(CMD_DELETE_TYPE, name);
    }

    // 2006-06-22
    public static boolean deleteType(String name)
    {
        String sql = "DELETE FROM types WHERE name = '" + name + "'";
        Connection db = IndusBasis.indusSystemDB.db;
        return JDBCUtils.updateDatabase(db, sql);
    }

    public static boolean deleteSchema(String name)
    {
        return delete(CMD_DELETE_SCHEMA, name);
    }

    public static boolean deleteMappingOld(String name)
    {
        return delete(CMD_DELETE_MAPPING, name);
    }

    // 2006-06-27
    public static boolean deleteMapping(String name)
    {
        String sql = "DELETE FROM mappings WHERE name = '" + name + "'";
        Connection db = IndusBasis.indusSystemDB.db;
        return JDBCUtils.updateDatabase(db, sql);
    }

    public static boolean deleteView(String name)
    {
        return delete(CMD_DELETE_VIEW, name);
    }

}
