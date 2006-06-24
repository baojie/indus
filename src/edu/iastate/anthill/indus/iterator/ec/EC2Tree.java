package edu.iastate.anthill.indus.iterator.ec;

import java.sql.Connection;

import edu.iastate.anthill.indus.iterator.DB2Tree;
import java.util.Vector;

/**
 * @author Jie Bao , baojie@cs.iastate.edu
 * @since 2005-03-31
 *

 CREATE TABLE ec
 (
 id char(20) NOT NULL,
 name varchar(256),
 parent varchar,
 CONSTRAINT "EC_pkey" PRIMARY KEY (id)
 )


 */
public class EC2Tree extends DB2Tree
{

    public EC2Tree(Connection db)
    {
        super(db);
    }

    protected String findComments(String id)
    {
        return defaultFindComments("ec", "id", "name", id);
    }

    public String getRootId()
    {
        return ""; // '';
    }

    protected Vector getChildren(String from_id)
    {
        return defaultGetChildren("ec", "id", "parent", from_id, null, null);
    }

    protected Vector getParent(String from_id)
    {
        return defaultGetParent("ec", "id", "parent", from_id, null, null);
    }

    @Override
    public Vector<String[]> getChildrenFast(String cacheTable)
    {
        return defaultGetChildren("ec", "id", "parent", null, null, "ec", "id",
                "name", cacheTable);
    }
}
