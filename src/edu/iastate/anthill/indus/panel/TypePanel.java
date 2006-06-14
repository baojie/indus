package edu.iastate.anthill.indus.panel;

import javax.swing.JOptionPane;

import edu.iastate.anthill.indus.IndusGUI;

/**
 * The Data Type Definition Panel
 * @author Jie Bao
 * @since 1.0 2004-10
 */

public class TypePanel
    extends TypePanelAction
{
    public TypePanel(IndusGUI parent, String defaultToShow)
    {
        super();
        try
        {
            this.parent = parent;
            jbInit1();
            showDefault(defaultToShow);
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }

    /**
     * showDefault
     *
     * @param defaultToShow String
     */
    public void showDefault(final String defaultToShow)
    {
        // look it up
        if (defaultToShow != null)
        {
            Thread t = new Thread()
            {
                public void run()
                {
                    for (int i = 0; i < listAllTypes.getModel().getSize(); i++)
                    {
                        if (listAllTypes.getModel().getElementAt(i).toString().
                            equals(defaultToShow))
                        {
                            // show it
                            listAllTypes.setSelectedIndex(i);
                            loadType(defaultToShow);
                            break;
                        }
                    }
                }
            };
            t.start();

        }
    }

    /**
     * prompt to save changes when exit
     * @author Jie Bao
     * @since 2004-10-12
     */
    public void promptSave()
    {
        // prompt for save
        if (currentType != null)
        {
            String currentSelectedType = currentType.getName();
            // prompt for save
            if (currentType.modified && currentSelectedType != null)
            {
                int answer = JOptionPane.showConfirmDialog(this,
                    "Type '" + currentSelectedType +
                    "' is changed, do you want to update it? ");
                if (answer == JOptionPane.YES_OPTION)
                {
                    onUpdateType(null);
                }
            }
        }
    }

    void jbInit1() throws Exception
    {
        updateTypesList();
    }
}
