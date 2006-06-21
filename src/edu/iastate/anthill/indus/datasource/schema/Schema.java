package edu.iastate.anthill.indus.datasource.schema;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import javax.swing.table.DefaultTableModel;

import edu.iastate.anthill.indus.datasource.Configable;

import edu.iastate.utils.string.SimpleXMLParser;
import java.sql.ResultSet;
import java.sql.DatabaseMetaData;
import java.sql.*;
import edu.iastate.utils.*;
import edu.iastate.utils.sql.JDBCUtils;

/**
 * @author Jie Bao
 * @since 1.0 2004-10-10
 *        1.1 2005-03-25 : add attDBTypeList, toSQL(), modify related functions
 */

public class Schema
    implements Configable
{

    protected String name = null;
    Map attTypeList = new HashMap(); // hash table - type of the attribute
    //  eg temperature -> Farenheit, Weather -> Outlook (AVH)
    Map attDBTypeList = new HashMap(); // hash table - database understandable type
    //   eg temperatur -> real,  Weather -> varchar(20)

    /* The following types (or spellings thereof) are specified by SQL:
     bit, bit varying, boolean, char, character varying, character, varchar,
     date, double precision, integer, interval, numeric, decimal, real, smallint,
     time (with or without time zone), timestamp (with or without time zone). */

    /**
     * Generate a table creation SQL sentence
     * @return String
     * @author Jie Bao
     * @since 2005-03-25
     */
    public String toSQL(String tableName, Vector pk, Map additionalColumn)
    {
        /*
            CREATE TABLE distributors (
                 did     integer,
                 name    varchar(40) );
         */
        StringBuffer buf = new StringBuffer();
        buf.append("CREATE TABLE " + tableName + "( \n");
        for (Iterator it = attDBTypeList.keySet().iterator(); it.hasNext(); )
        {
            String attribute = (String) it.next();
            String dbtype = (String) attDBTypeList.get(attribute);
            buf.append("    " + attribute + "   " + dbtype + ",\n");
        }

        if (additionalColumn != null) // 2005-03-28
        {
            for (Iterator it = additionalColumn.keySet().iterator(); it.hasNext(); )
            {
                String attribute = (String) it.next();
                String dbtype = (String) additionalColumn.get(attribute);
                buf.append("    " + attribute + "   " + dbtype + ",\n");
            }
        }

        /*
          CREATE TABLE distributors (
              did     integer CHECK (did > 100),
              name    varchar(40),
              CONSTRAINT distributors_pkey PRIMARY KEY (did,name)
          );
         */

        if (pk != null)
        {
            buf.append("CONSTRAINT " + tableName + "_pkey PRIMARY KEY (");
            for (int i = 0; i < pk.size() - 1; i++)
            {
                buf.append(pk.elementAt(i) + ",");
            }
            buf.append(pk.elementAt(pk.size() - 1));

            buf.append("),\n");
        }

        String sql = buf.toString();
        sql = sql.substring(0, sql.length() - 2); // filter out the last ",\n"
        sql = sql + ");";
        return sql;
    }

    public String toString()
    {
        StringBuffer buf = new StringBuffer();
        buf.append("Name = " + name + "\n");
        for (Iterator it = attTypeList.keySet().iterator(); it.hasNext(); )
        {
            String attribute = (String) it.next();
            String type = (String) attTypeList.get(attribute);
            String dbtype = (String) attDBTypeList.get(attribute);
            buf.append("    " + attribute + " : " + type +
                       ",  database type " + dbtype + "\n");
        }
        return buf.toString();
    }

    public boolean addAttribute(String name, String type, String dbtype)
    {
        if (attTypeList.containsKey(name))
        {
            return false;
        }
        else
        {
            attTypeList.put(name, type);
            attDBTypeList.put(name, dbtype);
            return true;
        }
    }

    public boolean deleteAttribute(String name)
    {
        if (attTypeList.containsKey(name))
        {
            attTypeList.remove(name);
            attDBTypeList.remove(name);
            return true;
        }
        else
        {
            return false;
        }
    }

    public String getType(String name)
    {
        if (attTypeList.containsKey(name))
        {
            return (String) attTypeList.get(name);
        }
        else
        {
            return null;
        }
    }

    /**
     * Get the database type
     * @param name String
     * @return String
     * @since 2005-03-25
     */
    public String getDBType(String name)
    {
        if (attDBTypeList.containsKey(name))
        {
            return (String) attDBTypeList.get(name);
        }
        else
        {
            return null;
        }
    }

    /**
     * Get the set of all types
     * @return Set
     * @since 2004-10-11
     */
    public Set getTypeSet()
    {
        Set set = new HashSet();

        Iterator it = attTypeList.keySet().iterator();
        while (it.hasNext())
        {
            // Get key
            Object attrName = it.next();
            Object type = attTypeList.get(attrName);
            set.add(type);
        }
        return set;
    }

    public int getAttibuteCount()
    {
        return attTypeList.size();
    }

    public String toText() {
        return toXML();
    }

    public void fromText(String text) {
        fromXML(text);        
    }

    
    public String toXML()
    {
        StringBuffer buf = new StringBuffer();
        buf.append(
            "<?xml version =\"1.0\" encoding=\"ISO-8859-1\"?><schema>");
        if (name != null)
        {
            buf.append("<name>" + name + "</name>");

        }
        Iterator it = attTypeList.keySet().iterator();
        while (it.hasNext())
        {
            // Get key
            Object attrName = it.next();
            Object type = attTypeList.get(attrName);
            Object dbtype = attDBTypeList.get(attrName);
            buf.append("<attribute>" +
                       "<attrname>" + attrName + "</attrname>" +
                       "<attrtype>" + type + "</attrtype>" +
                       "<dbtype>" + dbtype + "</dbtype>" +
                       "</attribute>");
        }
        buf.append("</schema>");
        return buf.toString();
    }

    public void clear()
    {
        // delete old ones
        attTypeList.clear();
        attDBTypeList.clear();

        name = null;
    }

    public void fromXML(String datatypeinXML)
    {
        if (datatypeinXML != null)
        {
            clear();

            Vector vec = SimpleXMLParser.getNestedBlock("name", datatypeinXML, false);
            if (vec.size() > 0)
            {
                name = (String) vec.elementAt(0);
            }

            vec = SimpleXMLParser.getNestedBlock("attribute", datatypeinXML, false);

            for (int i = 0; i < vec.size(); i++)
            {
                String attribute = (String) vec.elementAt(i);
                Vector attrname = SimpleXMLParser.getNestedBlock("attrname",
                    attribute, false);
                Vector attrtype = SimpleXMLParser.getNestedBlock("attrtype",
                    attribute, false);
                Vector dbtype = SimpleXMLParser.getNestedBlock("dbtype",
                    attribute, false);
                // add a new attribute row
                String dbT = (dbtype == null || dbtype.size() == 0) ?
                    "varchar(128)" : (String) dbtype.elementAt(0);
                addAttribute( (String) attrname.elementAt(0),
                             (String) attrtype.elementAt(0),
                             dbT);
            }
        }
    }

    /**
     *
     * @param name String
     * @param model DefaultTableModel
     *         model.addColumn("AttributeName");
               model.addColumn("Type");
               model.addColumn("DatabaseType");
     *
     *
     */

    public void fromGUI(String name, DefaultTableModel model)
    {
        if (model != null)
        {
            clear();
            this.name = name;

            for (int i = 0; i < model.getRowCount(); i++)
            {
                //Debug.trace(this, typeIndex +  " " +attIndex);

                String attrName = (String) model.getValueAt(i, 0);
                String type = (String) model.getValueAt(i, 1);
                String dbtype = (String) model.getValueAt(i, 2);
                addAttribute(attrName, type, dbtype);
            }
        }
    }

    public void toTable(DefaultTableModel model)
    {
        while (model.getRowCount() > 0)
        {
            model.removeRow(0);
        }

        Iterator it = attTypeList.keySet().iterator();
        while (it.hasNext())
        {
            // Get key
            Object attrName = it.next();
            Object type = attTypeList.get(attrName);
            Object dbtype = attDBTypeList.get(attrName);
            model.addRow(new Object[]
                         {attrName, type, dbtype});
        }
    }

    /**
     * This method returns a resultset containing the schema information for
     * a particular table.
     *
     * @param Name the name of the table.
     * @return the resultset.
     * @throws Exception
     *
     * Since - 1:37:15 PM, Mar 23, 2005
     */

    public static Schema buildFromDBTable(Connection conn, String tableName,
                                          String newSchemaName)
    {
        try
        {
            DatabaseMetaData DBMeta = conn.getMetaData();
            ResultSet rset = DBMeta.getColumns(null, null,
                                               tableName, null);

            Schema schema = new Schema(newSchemaName);

            /*
             * Actually, this code is not needed if we return the
             * result set. But, I am just doing it for the sake!
             */
            while (rset.next())
            {
                String columnName = rset
                    .getString("COLUMN_NAME");
                // Get the java.sql.Types type to which this database-specific type is mapped
                short dataType = rset.getShort("DATA_TYPE");
                // Get the name of the java.sql.Types value.
                String columnType = JDBCUtils.getJdbcTypeName(dataType);

                schema.addAttribute(columnName, columnType, columnType);
            }
            return schema;
        }

        catch (Exception e)
        {
            System.out.println(" Cannot retrieve the schema  : "
                               + e.toString());
            e.printStackTrace();
            return null;
        }
    }

    public Schema(String name)
    {
        this.name = name;
    }

    public Schema()
    {
        this.name = name;
    }

    public Map getAttList()
    {
        return attTypeList;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }
}
