package edu.iastate.anthill.indus.datasource;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import edu.iastate.anthill.indus.IndusBasis;
import edu.iastate.anthill.indus.IndusConstants;
import edu.iastate.anthill.indus.query.SQLQueryBuilder;
import edu.iastate.anthill.indus.query.SQLQueryPlanner;

import edu.iastate.utils.sql.JDBCUtils;
import edu.iastate.utils.sql.LocalDBConnection;

import Zql.ZExp;
import Zql.ZExpression;
import Zql.ZQuery;
import java.util.*;

/**
 * The class for data source
 * <p>
 * 
 * @author Jie Bao , baojie@cs.iastate.edu
 *         </p>
 *         <p>
 * @since 2005-03-24
 *        </p>
 */
public class IndusDataSource extends LocalDBConnection implements Configable {
	String schemaName;

	String name; // the table name of this data source

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
	public ResultSet executeNativeQuery(ZQuery query) {
		try {
			connect();
			Statement stmt = db.createStatement();

			// {{ 2005-10-19 Jie Bao
            ZExp z = SQLQueryBuilder.optimize((ZExpression)query.getWhere());
            query.addWhere(z);
            
            //String s = SQLQueryBuilder.printZExpression((ZExpression) query.getWhere());
			//System.out.println(s);
			
			String strQuery = query.toString();
			strQuery = optimzeQuery(strQuery.toCharArray());
			System.out.println("Native query : " + strQuery);
			// }} 2005-10-19

			ResultSet rs = stmt.executeQuery(strQuery);
			disconnect();
			return rs;
		} catch (SQLException ex) {
			ex.printStackTrace();
			return null;
		}
	}

	/**
	 * optimzeQuery
	 * 
	 * remove duplicated brackets, like ((a >1 )) will be (a>1)
	 * 
	 * @param strQuery
	 *            String
	 * @return String
	 * @author Jie Bao
	 * @since 2005-10-19
	 */
	private static String optimzeQuery(char[] strQuery) {
		// use a stack
		Stack<Integer> s = new Stack();
		Map<Integer, Integer> closing2opening = new HashMap();

		// detect all pairs of '(' and ')'
		for (int i = 0; i < strQuery.length; i++) {
			char c = strQuery[i];
			if (c == '(') {
				s.push(i); // find a opening, push the position into the stack
			}
			if (c == ')') {
				Integer open = s.pop();
				closing2opening.put(i, open);
			}
		}
		// remove redundant ones
		for (int i = 0; i < strQuery.length; i++) {
			char c = strQuery[i];
			if (c == ')') // for a char ')'
			{
				if (i > 0) {
					char last = strQuery[i - 1];
					if (last == ')') // if the last char is also ')'
					{
						int c_open = closing2opening.get(i);
						int last_open = closing2opening.get(i - 1);
						if (last_open - c_open == 1) // their opening '(' are
														// adjacent
						{
							// delete current ')' and its '('
							strQuery[i] = ' ';
							strQuery[c_open] = ' ';
						}
					}
				}
			}
		}

		return new String(strQuery);
	}

	public static void test1() {
		String str = "((((a=2)) OR (b=2)))";
		System.out.println(str);
		str = optimzeQuery(str.toCharArray());
		System.out.println(str);
	}

	public String getSchemaName() {
		return schemaName;
	}

	public String getName() {
		return name;
	}

	public void fromXML(String xml) {
	}

	public String toXML() {
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
	public boolean fromDB(Connection readfrom, String fromname) {
		try {
			String itemName = (fromname == null) ? name : fromname;
			String sql = "SELECT * FROM ds2schema WHERE datasource='"
					+ itemName + "'";
			Statement stmt = readfrom.createStatement();
			ResultSet rs = stmt.executeQuery(sql);
			if (rs.next()) {
				this.name = rs.getString("datasource");
				this.schemaName = rs.getString("dsschema");
				this.url = rs.getString("url");
				this.user = rs.getString("user_name");
				this.password = rs.getString("user_password");
				this.driver = rs.getString("jdbc_driver");
			}
			return name != null;
		} catch (SQLException ex) {
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
	public boolean toDB(Connection readfrom) {
		Map values = new HashMap();

		values.put("datasource", name);
		values.put("dsschema", schemaName);
		values.put("url", url);
		values.put("user_name", user);
		values.put("user_password", password);
		values.put("jdbc_driver", driver);

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
	public boolean deleteDataSourceRegistraion(Connection readfrom) {
		String sql = "DELETE FROM ds2schema WHERE datasource = '" + name + "';";
		boolean suc = JDBCUtils.updateDatabase(readfrom, sql);
		if (suc) {
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
	public boolean isLocal() {
		return url.equalsIgnoreCase(IndusConstants.dbLocalURL);
	}

	/**
	 * Delete the real data of this data source
	 * 
	 * @return boolean
	 * @since 2005-03-28
	 * @author Jie Bao
	 */
	public boolean deleteRealDataSource() {
		if (connect()) {
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
	public static Vector getAllDataSource(Connection readfrom) {
		try {
			String sql = "SELECT datasource FROM ds2schema";
			Statement stmt = readfrom.createStatement();
			ResultSet rs = stmt.executeQuery(sql);
			Vector allDS = new Vector();
			while (rs.next()) {
				String sss = rs.getString("datasource");
				allDS.add(sss);
			}
			return allDS;
		} catch (SQLException ex) {
			ex.printStackTrace();
			return null;
		}
	}

	public String toString() {
		return "Data Source Name: " + this.name + "\nSchema: "
				+ this.schemaName + "\nURL: " + this.url + "\nUser: "
				+ this.user + "\nPassword: " + "[hidden]" + "\nJDBC Driver: "
				+ this.driver;
	}

	/**
	 * @author Jie Bao
	 * @since 2005-03-25
	 */
	public static void test() {
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

	public static void main(String[] args) {
		test1();
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setSchemaName(String schemaName) {
		this.schemaName = schemaName;
	}

    public String toText() {
        return toXML();
    }

    public void fromText(String text) {
        fromXML(text);        
    }
}
