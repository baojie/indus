/**
 * Copyright by Cleverlance 2001
 * Contact: development@cleverlance.com
 * Website: www.cleverlance.com
 */

/**
 * Interface for column classes.
 *
 * @author	Jan Seda
 * @version	0.1.4
 */
package edu.iastate.anthill.indus.panel.query;

public interface IColumn
{

    /**
     * Method returns full column name.
     *
     *@return		<B>String</B>
     */
    public String getFullname();

    /**
     * Method returns table name for that column.
     *
     *@return		<B>String</B>
     */
    public String getTable();
}
