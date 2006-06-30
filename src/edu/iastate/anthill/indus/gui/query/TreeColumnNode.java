package edu.iastate.anthill.indus.gui.query;

import javax.swing.tree.DefaultMutableTreeNode;

/**
 * Class for tree column node.
 *
 * @author	Jan Seda
 * @version	0.2.9
 */
public class TreeColumnNode
    extends DefaultMutableTreeNode implements Comparable, IColumn
{
    /** Reference to parent table node. */
    private DefaultMutableTreeNode _parent;
    String _column;

    /**
     * Class constructor.
     *
     *@param		<B>table</B> parent node reference
     *@param		<B>column</B> column name
     */
    public TreeColumnNode(DefaultMutableTreeNode table, String column)
    {
        super(column);
        _column = column;
        _parent = table;
    }

    /**
     * Method returns node's parent from tree.
     *
     *@return		<B>DefaultMutableTreeNode</B> parent node
     */
    public DefaultMutableTreeNode getColParent()
    {
        return _parent;
    }

    /**
     * Method returns string representing this object.
     *
     *@return		<B>String</B>
     */
    public String toString()
    {
        return _column;
    }

    /**
     * Interface comparable implementation.
     *
     *@return		<B>int</B>
     */
    public int compareTo(Object obj)
    {
        return this.toString().compareTo(obj.toString());
    }

    /**
     * Method returns full column name.
     *
     *@return		<B>String</B>
     */
    public String getFullname()
    {
        return _parent.toString() + "." + _column;
    }

    /**
     * Method returns table name for that column.
     *
     *@return		<B>String</B>
     */
    public String getTable()
    {
        return _parent.toString();
    }
}
