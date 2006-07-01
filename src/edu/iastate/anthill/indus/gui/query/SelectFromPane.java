/**
 * Copyright by Cleverlance 2001
 * Contact: development@cleverlance.com
 * Website: www.cleverlance.com
 */

package edu.iastate.anthill.indus.gui.query;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.TreeSet;
import java.util.Vector;

import javax.swing.JTree;
import javax.swing.border.EtchedBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeSelectionModel;

import Zql.ZFromItem;
import Zql.ZQuery;
import Zql.ZSelectItem;
import edu.iastate.anthill.indus.agent.InfoReader;
import edu.iastate.anthill.indus.datasource.view.View;
import edu.iastate.utils.Debug;
import edu.iastate.utils.lang.MessageHandler;

/**
 * Class with first panel of query builder.<BR>
 *
 * @author	Jan Seda
 * @version	0.7.20
 */
public class SelectFromPane
    extends SelectFromPaneGUI implements TreeSelectionListener,
    ListSelectionListener, ActionListener, MessageHandler
{
    public SelectFromPane(SQLBuilderPane builder)
    {
        super();
        try
        {
            this._builder = builder;
            createTree(_builder._conn, false);

            messageMap();
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }

    /** Reference to JSQLBuilder. */
    public SQLBuilderPane _builder;

    /** Types of tables which will be included in treeview. Default are user tables. */
    public String[] tabTypes =
        {
        "TABLE"
        /*, "VIEW", "SYNONYM"*/};

    /**
     * Method generates SQL for first pane.
     *
     *@return		<B>String</B> list of columns and tables
     */
    protected String generateSQL()
    {
        String sql = "";
        int size = listModel.size();
        TreeSet set = new TreeSet();

        // generate column list
        for (int i = 0; i < size; i++)
        {
            TreeColumnNode column = (TreeColumnNode) listModel.elementAt(i);
            // create list of columns in final sql
            sql += column.getFullname();
            if (i < (size - 1))
            {
                sql += ", ";
            }
            // create set of tables
            set.add(column.getTable());
        }

        // generate table list
        sql += " FROM ";
        Object[] tabs = set.toArray();
        for (int i = 0; i < tabs.length; i++)
        {
            // create list of tables in final sql
            sql += (String) tabs[i];
            if (i < (tabs.length - 1))
            {
                sql += ", ";
            }
        }

        return sql;
    }

    /**
     * Generate select ... from ....
     *
     * @since 2005-03-23
     * @return ZQuery
     */
    ZQuery generateZQuery()
    {
        ZQuery q = new ZQuery();

        int size = listModel.size();
        TreeSet set = new TreeSet();

        Vector select = new Vector();
        // generate column list
        for (int i = 0; i < size; i++)
        {
            TreeColumnNode column = (TreeColumnNode) listModel.elementAt(i);
            // create list of columns in final sql
            ZSelectItem s = new ZSelectItem(column._column);

            // if a data source can have multiple table, use this
            //ZSelectItem s = new ZSelectItem(column.getFullname());
            select.add(s);

            // create set of tables
            set.add(column.getTable());
        }

        q.addSelect(select);

        // generate table list
        Vector from = new Vector();

        Object[] tabs = set.toArray();
        for (int i = 0; i < tabs.length; i++)
        {
            // create list of tables in final sql
            ZFromItem f = new ZFromItem( (String) tabs[i]);
            from.add(f);
        }
        q.addFrom(from);

        return q;
    }

    /**
     * Method creates tree with table names and coumns names.
     *
     *@param		<B>con</B> Connection object
     *@param		<B>update</B> is it update of the tree?
     *@return		<B>void</B>
     */
    public void createTree(Connection con, boolean update)
    {
        try
        {
            Object[] allview = InfoReader.getAllView();
            Vector v = new Vector(Arrays.asList(allview));
            
            DatabaseMetaData dtmt = con.getMetaData();
            DefaultMutableTreeNode top = new DefaultMutableTreeNode(dtmt.getURL());
            treeModel = new DefaultTreeModel(top);

            // JTree tree = new JTree(top);
            if (update && sqlTree != null)
            {
                sqlTree.setModel(treeModel);
            }
            else
            {
                sqlTree = new JTree();
                sqlTree.setModel(treeModel);
                sqlTree.getSelectionModel().setSelectionMode(TreeSelectionModel.
                    SINGLE_TREE_SELECTION);
                sqlTree.addTreeSelectionListener(this);
                sqlTree.putClientProperty("JTree.lineStyle", "Angled");
                sqlTree.setShowsRootHandles(true);
                sqlTree.setBorder(new EtchedBorder(EtchedBorder.RAISED));
                // sqlTree.setVisibleRowCount(8);
            }

            ResultSet rs = dtmt.getTables(null, null, null, tabTypes);
            // name of the table retrieved from Database
            String tabName = "";
            while (rs.next())
            {
                tabName = rs.getString("TABLE_NAME");
                
                // only show tables that belong to the current user
                // note that those cache tables have same names to corresponding views
                if (!v.contains(tabName)) continue;
                
                DBTableNode table = new DBTableNode(tabName,
                    rs.getString("REMARKS"));
                table.setAllowsChildren(true);

                ResultSet cols = dtmt.getColumns(null, null, tabName, null);
                while (cols.next())
                {
                    // don't show indus metadata column
                    String columnName = cols.getString("COLUMN_NAME");// 2005-03-28
                    if (!columnName.equals(View.FROM_DATA_SOURCE ))
                    {
                        // add it
                        TreeColumnNode column = new TreeColumnNode(table,
                            columnName);
                        column.setAllowsChildren(false);
                        // table.add(column);
                        treeModel.insertNodeInto(column, table, 0);
                    }
                }
                cols.close();

                // addColNode(table, tabName, dtmt);
                // top.add(table);
                treeModel.insertNodeInto(table, top, 0);
            }
            rs.close();

            // expand list of tables
            sqlTree.expandRow(0);
            jScrollPane2.getViewport().setView(sqlTree);

        }
        catch (SQLException sqle)
        {
            sqle.printStackTrace();
        }
    }

    /**
     * Method disables all buttons for movement.
     *
     *@param		<B>type</B> type of action<BR>
     * 0 - disable all buttons<BR>
     * 1 - tree events<BR>
     * 2 - list events<BR>
     *@return		<B>void</B>
     */
    private void setButtons(int type)
    {
        switch (type)
        {
            case 0:
                btnAdd.setEnabled(false);
                btnRemove.setEnabled(false);
                btnRemoveAll.setEnabled(false);
                break;
            case 1:
                DefaultMutableTreeNode item = (DefaultMutableTreeNode) sqlTree.
                    getLastSelectedPathComponent();

                // if (sqlTree.getLastSelectedPathComponent() instanceof TreeColumn) {
                // if (listModel.getSize() == 0) jButton6.setEnabled(false);
                if (item instanceof TreeColumnNode)
                {
                    btnAdd.setEnabled(true);
                }
                else if (item instanceof DBTableNode)
                {
                    if (item.getChildCount() > 0)
                    {
                        btnAdd.setEnabled(true);
                    }
                    else
                    {
                        btnAdd.setEnabled(false);
                    }
                }
                else
                {
                    btnAdd.setEnabled(false);
                }
                break;
            case 2:
                btnAdd.setEnabled(false);
                if (colList.isSelectionEmpty())
                {
                    btnRemove.setEnabled(false);
                }
                else
                {
                    btnRemove.setEnabled(true);
                }
                break;
        }

        if (listModel.getSize() > 0)
        {
            btnRemoveAll.setEnabled(true);
        }
        else
        {
            btnRemoveAll.setEnabled(false);
        }
    }

    /**
     * Method moves columns in list back to the tree.
     *
     *@param		<B>all</B> move all columns from list?
     *@return		<B>void</B>
     */
    private void moveColFromList(boolean all)
    {
        if (all)
        {
            int size = listModel.getSize();
            for (int i = 0; i < size; i++)
            {
                TreeColumnNode col = (TreeColumnNode) listModel.getElementAt(0);
                treeModel.insertNodeInto(col, col.getColParent(), 0);
                listModel.removeElementAt(0);
            }
            // disable next button
            _builder.buttonPane.next.setEnabled(false);
            btnRemove.setEnabled(false);
            btnRemoveAll.setEnabled(false);
        }
        else
        {
            int[] index = colList.getSelectedIndices();
            int last = 0;
            if (index.length > 0)
            {
                for (int i = 0; i < index.length; i++)
                {
                    TreeColumnNode col = (TreeColumnNode) listModel.
                        getElementAt(index[
                                     i]);
                    if (col != null)
                    {
                        treeModel.insertNodeInto(col, col.getColParent(), 0);
                        listModel.removeElementAt(index[i]);
                        last = index[i];
                    }
                }
            }
            int size = listModel.getSize();
            if (size == 0)
            {
                btnRemove.setEnabled(false);
                // disable next button
                _builder.buttonPane.next.setEnabled(false);
                btnRemoveAll.setEnabled(false);
            }
            else
            {
                if (size > last)
                {
                    colList.setSelectedIndex(last);
                }
                else
                {
                    colList.setSelectedIndex(size - 1);
                }
            }
        }
        setButtons(1);
    }

    /**
     * Method is called when option dialog should be shown.
     *
     *@return		<B>void</B>
     */
    protected void options()
    {
        if (optDialog == null)
        {
            optDialog = new OptionDialog(this);
            optDialog.pack();
            optDialog.setVisible(true);
        }
        else
        {
            optDialog.setVisible(true);
        }
    }

// ----------------------------------  actions -------------------------------
    public void actionPerformed(ActionEvent e)
    {
        DefaultMutableTreeNode item = (DefaultMutableTreeNode) sqlTree.
            getLastSelectedPathComponent();
        if (e.getActionCommand().equals(this.ADD_CMD))
        {
            // read slected items in list
            String fromTable = null;
            if (!listModel.isEmpty())
            {
                TreeColumnNode column = (TreeColumnNode) listModel.elementAt(0);
                fromTable = column.getTable();
            }

            if (item instanceof TreeColumnNode)
            {
                if ( (fromTable != null) &&
                    ( (TreeColumnNode) item).getTable().compareToIgnoreCase(
                        fromTable) != 0)
                {
                    Debug.trace("Columns should be from the same local schema");
                    return;
                }

                // remove column from tree
                treeModel.removeNodeFromParent(item);
                // add column to the list
                listModel.addElement(item);
                // enable next button
                _builder.buttonPane.next.setEnabled(true);
            }
            else if (item instanceof DBTableNode)
            {
                if ( (fromTable != null) &&
                    ( (DBTableNode) item).toString().compareToIgnoreCase(
                        fromTable) != 0)
                {
                    Debug.trace("Columns should be from the same local schema");
                    return;
                }

                int index = item.getChildCount();
                for (int i = 0; i < index; i++)
                {
                    DefaultMutableTreeNode child = (DefaultMutableTreeNode)
                        treeModel.getChild(item, 0);
                    treeModel.removeNodeFromParent(child);
                    listModel.addElement(child);
                }
                // enable next button
                _builder.buttonPane.next.setEnabled(true);
                setButtons(0);
            }
        }
        else if (e.getActionCommand().equals(this.REMOVE_ALL_CMD))
        {
            // add all columns from table
            moveColFromList(true);
            // disable next button
            _builder.buttonPane.next.setEnabled(false);
        }
        else if (e.getActionCommand().equals(this.REMOVE_CMD))
        {
            moveColFromList(false);
        }

    }

    public void valueChanged(TreeSelectionEvent e)
    {
        setButtons(1);
    }

    public void valueChanged(ListSelectionEvent e)
    {
        if (e.getValueIsAdjusting())
        {
            return;
        }
        setButtons(2);
    }

    /**
     * @since 2005-03-25
     * @author Jie Bao
     */
    public void messageMap()
    {
        btnAdd.addActionListener(this);
        btnRemove.addActionListener(this);
        btnRemoveAll.addActionListener(this);
        colList.addListSelectionListener(this);
    }
}
