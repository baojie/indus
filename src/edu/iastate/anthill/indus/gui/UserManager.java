package edu.iastate.anthill.indus.gui;

import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import edu.iastate.utils.sql.JDBCUtils;

/**
 * <p>@author Jie Bao</p>
 * <p>@since 2005-07-20 </p>
 *
 CREATE TABLE users
 (
 id varchar(255) NOT NULL,
 role varchar(32),
 name varchar(255),
 institution varchar(255),
 email varchar(32),
 pass varchar(32),
 create_date varchar(32),
 CONSTRAINT user_pkey PRIMARY KEY (id)
 )

 CREATE TABLE privilege
 (
 package_oid varchar(32),
 user_id varchar(255),
 rights varchar(8)
 )

 CREATE TABLE online
 (
 user_id varchar(256) NOT NULL,
 host varchar(32) NOT NULL,
 port varchar(8) NOT NULL,
 login_time varchar(32) NOT NULL
 )
 WITHOUT OIDS;


 */
public class UserManager
{
    static public boolean rightUser(Connection db, String user, String pass)
    {
        String sql = "SELECT pass FROM users WHERE id = '" + user + "'";
        String pass_db = JDBCUtils.getFirstValue(db, sql);
        if (pass == null)
        {
            return (pass_db == null);
        }
        else
        {
            return pass.equals(pass_db);
        }
    }

    static public boolean beginEditing(Connection db, String pkg_oid,
        String user)
    {

        Map<String, String> fields = new HashMap();
        fields.put("package", pkg_oid);
        fields.put("usr", user);

        return JDBCUtils.insertOrUpdateDatabase(db, "editing", fields,
            "package");
    }

    /**
     * If the given user id exists on the ontology server
     * @param db Connection
     * @param user String
     * @return boolean
     *
     * @author Jie Bao
     * @since 2005-08-19
     */
    static public boolean ifUserExist(Connection db, String user)
    {
        // SELECT id FROM users WHERE id = 'user'
        String sql = "SELECT id FROM users WHERE id = '" + user + "'";
        String id = JDBCUtils.getFirstValue(db, sql);
        return (id != null);
    }

    /**
     * Apply a user name with given ontology connection
     * @param db Connection
     * @param user String
     * @param password String
     * @param email String
     * @param name String
     * @param institution String
     * @return boolean - if successful, return true, otherwise false
     *
     * @author Jie Bao
     * @since 2005-08-19
     */
    static public boolean applyForID(Connection db, String user,
        String password, String email, String name, String institution,
        String role)
    {
        //INSERT INTO users(id, name, institution, email, pass, create_date, role)
        //    VALUES('user','name','institution', 'email','password', '2005-08-13', 'role');
        String date = getTime();
        String sql = "INSERT INTO users(id, name, institution, email, pass, create_date, role) "
            + "VALUES('"
            + user
            + "','"
            + name
            + "','"
            + institution
            + "', '"
            + email + "','" + password + "','" + date + "' , '" + role + "')";
        return JDBCUtils.updateDatabase(db, sql);
    }

    public static String getTime()
    {
        SimpleDateFormat dateFormat = new SimpleDateFormat(
            "yyyy-MM-dd HH:mm:ss z");
        return dateFormat.format(new Date());
    }

    // 2005-08-27
    static public Vector<String> getAllUsers(Connection db, String role)
    {
        // SELECT id FROM users [WHERE role = 'role'] ORDER BY id;
        String sql = "SELECT id FROM users ";
        if (role != null)
        {
            sql += " WHERE role = '" + role + "'";
        }
        sql += " ORDER BY id";
        return JDBCUtils.getValues(db, sql);
    }
}
