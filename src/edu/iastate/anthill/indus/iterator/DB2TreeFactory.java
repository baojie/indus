package edu.iastate.anthill.indus.iterator;

import java.sql.Connection;

import edu.iastate.anthill.indus.IndusConstants;
import edu.iastate.anthill.indus.IndusDB;
import edu.iastate.anthill.indus.iterator.ec.EC2Tree;
import edu.iastate.anthill.indus.iterator.go.Go2Tree;
import edu.iastate.anthill.indus.iterator.mips.MIPS2Tree;

/**
 * <p>@author Jie Bao , baojie@cs.iastate.edu</p>
 * <p>@since 2005-03-31</p>
 */
public class DB2TreeFactory
{
    public static String[] allTemplete =
        {
        "mips", "scop", "go","ec"};

    public static DB2Tree buildFromName(String name, Connection db)
    {
        Connection mydb = db;
        if (mydb == null)
        {
            IndusDB d = new IndusDB();
            d.connect(IndusConstants.dbURL);
            mydb = d.db;
        }

        if (name.equals("mips"))
        {
            return new MIPS2Tree(mydb);
        }
        else if (name.equals("go"))
        {
            return new Go2Tree(mydb);
        }
        else if (name.equals("ec"))
        {
            return new EC2Tree(mydb);
        }
        else
        {
            return null;
        }
    }
}
