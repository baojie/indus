package edu.iastate.anthill.indus.panel.query;

import javax.swing.tree.DefaultMutableTreeNode;

/**
 * Class for tree table node.
 *
 * @author	Jan Seda
 * @version	0.1.9
 */
public class DBTableNode
    extends DefaultMutableTreeNode
{
    /** Description of the table. */
    private String _desc;
    String _table;

    /**
     * Class constructor.
     *
     *@param		<B>table</B> table name
     *@param		<B>table</B> description of the table from database
     */
    public DBTableNode(String table, String tabDesc)
    {
        super(table);
        _table = table;
        if (tabDesc == null)
        {
            _desc = "";
        }
        else
        {
            _desc = tabDesc;
        }
    }

    /**
     * Method returns string representing this object.
     *
     *@return		<B>String</B>
     */
    public String toString()
    {
        return _table;
    }
}
