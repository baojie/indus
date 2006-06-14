package edu.iastate.anthill.indus.agent;

import java.sql.Connection;

import edu.iastate.anthill.indus.datasource.Configable;
import edu.iastate.anthill.indus.datasource.IndusDataSource;
import edu.iastate.anthill.indus.datasource.mapping.DataSourceMapping;
import edu.iastate.anthill.indus.datasource.schema.Schema;
import edu.iastate.anthill.indus.datasource.type.DataType;
import edu.iastate.anthill.indus.datasource.view.View;
import javax.swing.JOptionPane;
import edu.iastate.anthill.indus.IndusConstants;

/**
 * <p>@author Jie Bao , baojie@cs.iastate.edu</p>
 * <p>@since 2005-03-27</p>
 */
public class InfoWriter
    implements IndusCommand
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
        return write(CMD_UPDATE_TYPE, type.getName(), type);
    }

    public static boolean writeMapping(DataSourceMapping mapping)
    {
        return write(CMD_UPDATE_MAPPING, mapping.getName(),
                     mapping);
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
    private static boolean write(String command, String name,
                                 Configable toWrite)
    {
        // general XML
        String xml = toWrite.toXML();

        // send to server
        IndusHttpClient client = new IndusHttpClient();
        String res = client.sendCmd(
            command + ";name=" + name + ";value=" + xml);
        return res.equals(RES_OK);

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

    public static boolean deleteType(String name)
    {
        return delete(CMD_DELETE_TYPE, name);
    }

    public static boolean deleteSchema(String name)
    {
        return delete(CMD_DELETE_SCHEMA, name);
    }

    public static boolean deleteMapping(String name)
    {
        return delete(CMD_DELETE_MAPPING, name);
    }

    public static boolean deleteView(String name)
    {
        return delete(CMD_DELETE_VIEW, name);
    }

}
