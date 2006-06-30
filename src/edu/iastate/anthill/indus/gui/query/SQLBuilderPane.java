/**
 * Copyright by Cleverlance 2001
 * Contact: development@cleverlance.com
 * Website: www.cleverlance.com
 */

package edu.iastate.anthill.indus.gui.query;

import java.sql.Connection;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.border.MatteBorder;

import edu.iastate.utils.sql.SQLDialog;

import Zql.ZExpression;
import Zql.ZQuery;

/**
 * Class is basic for SQL builder tool.<BR>
 *
 * @author	Jan Seda
 * @version	0.3.4
 */
public class SQLBuilderPane
    extends JPanel
{
    /** Parent of the query builder - holds it and recieves events. */
    private ISQLBuilder calledFrom;

    /** Connection object. */
    public Connection _conn;

    /** Minimum heigth of the components. */
    protected static int height = 350;
    /** Minimum width of the components. */
    protected static int width = 350;

    protected ButtonPanel buttonPane;
    protected SelectFromPane firstPane;
    protected WhereTreePane secondPane;
    //protected ThirdPane thirdPane;

    SQLDialog sqlDlg = new SQLDialog("");

    /**
     * Class constructor.
     *
     *@param		<B>parent</B> ISQLBuilder implementator
     *@param		<B>con</B> Connection object used to resolve all data
     */
    public SQLBuilderPane(ISQLBuilder parent, Connection con) throws Exception
    {
        if (parent == null)
        {
            throw new Exception(
                "Reference to implementation ISQLBuilder interface can not be null.");
        }
        if (con == null)
        {
            throw new Exception("Reference to connection object is null.");
        }

        calledFrom = parent;
        _conn = con;


        setVisible(false);
        setMinimumSize(new Dimension(SQLBuilderPane.width + 10,
                                     SQLBuilderPane.height + 10));

        buttonPane = new ButtonPanel(this, 2);
        firstPane = new SelectFromPane(this);
        secondPane = new WhereTreePane(this);
        //thirdPane = new ThirdPane(tooltips, this);

        // firstPane.setPreferredSize(new Dimension(JSQLBuilder.width, JSQLBuilder.height));
        firstPane.setMinimumSize(new Dimension(SQLBuilderPane.width,
                                               SQLBuilderPane.height));
        // secondPane.setPreferredSize(new Dimension(JSQLBuilder.width, JSQLBuilder.height));
        secondPane.setMinimumSize(new Dimension(SQLBuilderPane.width,
                                                SQLBuilderPane.height));
        // thirdPane.setPreferredSize(new Dimension(JSQLBuilder.width, JSQLBuilder.height));
        //thirdPane.setMinimumSize(new Dimension(JSQLBuilder.width, JSQLBuilder.height));

        setBorder(new MatteBorder(10, 10, 10, 10, Color.lightGray));

        // getContentPane().setLayout (new GridBagLayout ());
        setLayout(new GridBagLayout());
        // mPane.setMinimumSize(new Dimension(600, 350));
        GridBagConstraints gridBagConstraints1;

        addPane(firstPane);
        addPane(secondPane);
        //addPane(thirdPane);

        gridBagConstraints1 = new GridBagConstraints();
        gridBagConstraints1.gridx = 0;
        gridBagConstraints1.gridy = 9;
        gridBagConstraints1.weightx = 1.0;
        gridBagConstraints1.gridwidth = 4;
        gridBagConstraints1.gridheight = 1;
        gridBagConstraints1.fill = GridBagConstraints.BOTH;
        add(buttonPane, gridBagConstraints1);

        doLayout();

        //set visible pane
        setActivePane(0);

        setVisible(true);

        // Jie Bao
        sqlDlg.setModal(false);
        sqlDlg.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        sqlDlg.panel.buttonPanel.setVisible(false);
        sqlDlg.setSize(300, 200);
        sqlDlg.show();
    }

    /**
     * Method sets connection object used by builder.
     *
     *@param		<B>conn</B> connection object
     *@return		<B>void</B>
     */
    public void setConnection(Connection conn) throws Exception
    {
        if (conn == null)
        {
            throw new Exception("Connection parameter is null.");
        }
        // set instance variable
        _conn = conn;
    }

    /**
     * Method releases connection (sets connection object in builder to null).
     *
     *@return		<B>void</B>
     */
    public void releaseConnection()
    {
        _conn = null;
    }

    private void setActivePane(int cur)
    {
        switch (cur)
        {
            case 0:
                secondPane.setVisible(false);

                //thirdPane.setVisible(false);
                firstPane.setVisible(true);
                break;
            case 1:
                firstPane.setVisible(false);

                //thirdPane.setVisible(false);
                secondPane.setVisible(true);
                break;
                //case 2:
                //    firstPane.setVisible(false);
                //    secondPane.setVisible(false);
                //    thirdPane.setVisible(true);
                //    break;
        }
    }

    private void addPane(JPanel pane)
    {
        GridBagConstraints gridBagConstraints1;

        gridBagConstraints1 = new GridBagConstraints();
        gridBagConstraints1.gridx = 0;
        gridBagConstraints1.gridy = 0;
        gridBagConstraints1.weighty = 1.0;
        gridBagConstraints1.gridwidth = 4;
        gridBagConstraints1.gridheight = 8;
        // gridBagConstraints1.insets = new Insets (3, 3, 3, 3);
        gridBagConstraints1.fill = GridBagConstraints.BOTH;
        add(pane, gridBagConstraints1);
    }

    /**
     * Method generates SQL according to all parameters.
     *
     *@return		<B>String</B> generated SQL command
     */
    private String generateSQL()
    {
        return generateZQuery().toString();
    }

    /**
     * @since 2005-03-23
     * @return ZQuery
     */
    public ZQuery generateZQuery()
    {
        ZQuery q = firstPane.generateZQuery();
        ZExpression where = secondPane.generateWhere();
        if (where != null && where.getOperands() != null)
        {
            if (where.getOperands().size() > 0)
            {
                q.addWhere(where);
            }
        }
        return q;
    }

    public void cancel()
    {
        calledFrom.cancel();
        this.setVisible(false);
        sqlDlg.hide();
        sqlDlg.dispose();

    }

    protected void next(int cur)
    {
        setActivePane(cur);
    }

    protected void previous(int cur)
    {
        setActivePane(cur);
    }

    protected void finish()
    {
        calledFrom.finish(generateZQuery());
//        SQL = generateZQuery();

        sqlDlg.hide();
        sqlDlg.dispose();
        this.setVisible(false);
    }
}
