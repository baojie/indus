/**
 * Copyright by Cleverlance 2001
 * Contact: development@cleverlance.com
 * Website: www.cleverlance.com
 */

/**
 * Interface is implemented to classed which wants to be called by SQL executing class (like from JDatabase).
 * This interface gives implementing class to get final SQL command with related properties and to work with them.
 * Also that interface can be used to debugging or watching how to optimize sql commands.
 *
 * @author	Jan Seda
 * @version	0.1.4
 */
package edu.iastate.anthill.indus.panel.query;

public interface ISQLBuilder
{

    /**
     * Event is called when dialog is created and correctly initialized.
     * @param	<B>init</B> true if initialization was ok
     * @return		<B>void</B>
     */
    public void created(boolean init);

    /**
     * Event is called when user pressed cancel button.
     * @return		<B>void</B>
     */
    public void cancel();

    /**
     * Event is called when user finishes working with builder.
     *
     * @param	<B>sql</B> string with SQL command create by builder
     * @return		<B>void</B>
     */
    public void finish(Object returnValue);
}
