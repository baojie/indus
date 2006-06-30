/**
 * Copyright by Cleverlance 2001
 * Contact: development@cleverlance.com
 * Website: www.cleverlance.com
 *
 * Modified by Jie Bao 2005-03-22
 */

package edu.iastate.anthill.indus.gui.query;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.JFrame;

import edu.iastate.anthill.indus.IndusConstants;

public class SQLBuilderPaneTest
    extends JFrame implements ISQLBuilder
{
    private static Connection con;
    public SQLBuilderPaneTest(String title)
    {
        super(title);
    }

    public static void main(String[] args)
    {
        SQLBuilderPaneTest frame = new SQLBuilderPaneTest("Testing dialog");

        try
        {
            Class.forName(IndusConstants.dbDriver);
            con = DriverManager.getConnection(IndusConstants.dbCacheURL,
                                              IndusConstants.dbUsr,
                                              IndusConstants.dbPwd);
            //frame.getContentPane().add(new JSQLBuilder(frame, (Frame)null, con, true));
        }
        catch (SQLException sqle)
        {
            sqle.printStackTrace();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        frame.addWindowListener(new WindowAdapter()
        {
            public void windowClosing(WindowEvent e)
            {
                try
                {
                    con.close();
                }
                catch (SQLException sqle)
                {}
                System.exit(0);
            }
        });

        frame.setBounds(30, 30, 580, 489);
        frame.setVisible(true);
    }

// implementation of interface of ISQLBuilder
    public void created(boolean init)
    {
    }

    public void cancel()
    {
    }

    public void finish(Object returnValue)
    {
    }
}
