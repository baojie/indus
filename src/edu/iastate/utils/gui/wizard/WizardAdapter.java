/*
 * $Id: WizardAdapter.java,v 1.1 2006/06/14 22:35:48 baojie Exp $
 *
 * $Log: WizardAdapter.java,v $
 * Revision 1.1  2006/06/14 22:35:48  baojie
 * *** empty log message ***
 *
 */

package edu.iastate.utils.gui.wizard;

/** This class provides means of abreviating work when using the WizardListener
 * allowing the developer to implement only the needed methods
 *
 * @author  rodrigomalara@users.sourceforge.net
 */
public abstract class WizardAdapter
    implements WizardListener
{

    /** Creates a new instance of WizardAdapter */
    public WizardAdapter()
    {}

    /** Called when the wizard is cancelled.
     * @param wizard the wizard that was cancelled.
     */
    public void wizardCancelled(Wizard wizard)
    {}

    /** Called when the wizard finishes.
     * @param wizard the wizard that finished.
     */
    public void wizardFinished(Wizard wizard)
    {}

    /** Called when a new panel has been displayed in the wizard.
     * @param wizard the wizard that was updated
     */
    public void wizardPanelChanged(Wizard wizard)
    {}

}
