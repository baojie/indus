package edu.iastate.anthill.indus.datasource;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import edu.iastate.anthill.indus.IndusConstants;
import edu.iastate.anthill.indus.gui.IndusBasis;
import edu.iastate.utils.sql.JDBCUtils;
import edu.iastate.utils.sql.LocalDBConnection;

/**
 * The class for data source
 * 
 * @author Jie Bao , baojie@cs.iastate.edu
 * @since 2005-03-24
 */
public class IndusDataSource extends LocalDBConnection implements Configable
{
    String schemaName;

    String name;      // the table name of this data source

    public String getSchemaName()
    {
        return schemaName;
    }

    public String getName()
    {
        return name;
    }

    public void fromXML(String xml)
    {}

    public String toXML()
    {
        return "";
    }

    /**
     * CREATE TABLE ds2schema ( datasource varchar(256) NOT NULL, dsschema
     * varchar(256), url varchar(256), user" varchar(256), password"
     * varchar(256), jdbc_driver varchar(256), CONSTRAINT ds2schema_pkey PRIMARY
     * KEY (datasource) )
     * 
     * @param readfrom
     *            Connection - where the information is stored NOTE: it's not
     *            the connection of the data source itself!!!! you need to
     *            create the connection with connection() method
     * @param name
     *            String
     * @since 2005-03-25
     * @author Jie Bao
     */
    public boolean fromDB(Connection readfrom, String fromname)
    {
        try
        {
            String itemName = (fromname == null) ? name : fromname;
            String sql = "SELECT * FROM ds2schema WHERE datasource='"
                + itemName + "'";
            Statement stmt = readfrom.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            if (rs.next())
            {
                this.name = rs.getString("datasource");
                this.schemaName = rs.getString("dsschema");
                this.url = rs.getString("url");
                this.user = rs.getString("user_name");
                this.password = rs.getString("user_password");
                this.driver = rs.getString("jdbc_driver");
            }
            return name != null;
        }
        catch (SQLException ex)
        {
            ex.printStackTrace();
            return false;
        }
    }

    /**
     * @param readfrom
     *            Connection
     * @return boolean
     * @since 2005-03-25
     * @author Jie Bao
     */
    public boolean toDB(Connection readfrom)
    {
        Map<String, String> values = new HashMap<String, String>();

        values.put("datasource", name);
        values.put("dsschema", schemaName);
        values.put("url", url);
        values.put("user_name", user);
        values.put("user_password", password);
        values.put("jdbc_driver", driver);
        values.put("space", IndusBasis.user.name);

        return JDBCUtils.insertOrUpdateDatabase(readfrom, "ds2schema", values,
            "datasource");
    }

    /**
     * Delete the data source registration from INDUS, but the real data is kept
     * To delete the real data, use deleteRealDataSource()
     * 
     * @param readfrom
     *            Connection
     * @return boolean
     * @since 2005-03-27
     * @author Jie Bao
     */
    public boolean deleteDataSourceRegistraion(Connection readfrom)
    {
        String sql = "DELETE FROM ds2schema WHERE datasource = '" + name + "';";
        boolean suc = JDBCUtils.updateDatabase(readfrom, sql);
        if (suc)
        {
            name = null;
        }
        return suc;

    }

    /**
     * if the data source is from indus local repository
     * 
     * @return boolean
     * @since 2005-03-28
     * @author Jie Bao
     */
    public boolean isLocal()
    {
        return url.equalsIgnoreCase(IndusConstants.dbLocalURL);
    }

    /**
     * Delete the real data of this data source
     * 
     * @return boolean
     * @since 2005-03-28
     * @author Jie Bao
     */
    public boolean deleteRealDataSource()
    {
        if (connect())
        {
            String sql = "DROP TABLE " + this.name;
            boolean suc = JDBCUtils.updateDatabase(this.db, sql);
            return suc;
        }
        return false;
    }

    /**
     * Read all data source name list
     * 
     * @param readfrom
     *            Connection
     * @return Vector
     * @since 2005-03-27
     */
    public static Vector getAllDataSource(Connection readfrom)
    {
        try
        {
            String sql = "SELECT datasource FROM ds2schema WHERE space = '"
                + IndusBasis.user.name + "' ORDER BY datasource";
            Statement stmt = readfrom.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            Vector allDS = new Vector();
            while (rs.next())
            {
                String sss = rs.getString("datasource");
                allDS.add(sss);
            }
            return allDS;
        }
        catch (SQLException ex)
        {
            ex.printStackTrace();
            return null;
        }
    }

    public String toString()
    {
        return "Data Source Name: " + this.name + "\nSchema: "
            + this.schemaName + "\nURL: " + this.url + "\nUser: " + this.user
            + "\nPassword: " + "[hidden]" + "\nJDBC Driver: " + this.driver;
    }

    /**
     * @author Jie Bao
     * @since 2005-03-25
     */
    public static void test()
    {
        IndusDataSource ds = new IndusDataSource();

        ds.name = "testds";
        ds.schemaName = "someSchema";
        ds.url = "http://somweehre";
        ds.user = "auser";
        ds.password = "i_hate_the_war_of BUllSHit";
        ds.driver = "org.jdbc.something.someclass";

        IndusBasis basis = new IndusBasis();
        ds.toDB(basis.indusSystemDB.db);
        System.out.println(ds);

        IndusDataSource new_ds = new IndusDataSource();
        boolean suc = new_ds.fromDB(basis.indusSystemDB.db, ds.name);
        System.out.println("Success = " + suc + "\n" + new_ds);

        IndusDataSource noSuchThing = new IndusDataSource();
        suc = noSuchThing.fromDB(basis.indusSystemDB.db, "No_SUCH_ThinG");
        System.out.println("Success = " + suc + "\n" + noSuchThing);
    }

    public static void main(String[] args)
    {
    //
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public void setSchemaName(String schemaName)
    {
        this.schemaName = schemaName;
    }

    public String toText()
    {
        return toXML();
    }

    public void fromText(String text)
    {
        fromXML(text);
    }
}
