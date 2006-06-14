package edu.iastate.anthill.indus.panel;

import java.awt.BorderLayout;
import javax.swing.JPanel;

import edu.iastate.anthill.indus.IndusGUI;
import edu.iastate.anthill.indus.datasource.type.DataType;
import javax.swing.JOptionPane;

/**
 *
 * @author Jie Bao
 * @since 1.0 2005-03-11
 */
public abstract class IndusPane
    extends JPanel
{

    public IndusPane()
    {
        try
        {
            jbInit();
        }
        catch (Exception exception)
        {
            exception.printStackTrace();
        }
    }

    private void jbInit() throws Exception
    {
        this.setLayout(new BorderLayout());
    }

    /**
     * Ask for a name
     * @param exist String[]
     * @return String
     * @since 2005-03-30
     */
    protected String askForName(String exist[])
    {
        while (true)
        {
            // ask for a name
            String typeName = JOptionPane.showInputDialog(this,
                "Please select a name");
            if (typeName == null) // user cancelled
            {
                return null;
            }

            // validate the name
            if (!typeName.matches("[\\w\\-._]+"))
            {
                JOptionPane.showMessageDialog(this, "Name is not legal!");
                continue; // ask it again
            }

            //Check if the name is used
            if (exist != null)
            {
                boolean used = false;
                for (int i = 0; i < exist.length; i++)
                {
                    if (exist[i].equalsIgnoreCase(typeName))
                    {
                        JOptionPane.showMessageDialog(this,
                            "The name '" + typeName + "' already exists");
                        used = true;
                        break;
                    }
                }
                if (!used)
                {
                    return typeName; // return it
                }
                else
                {
                    // ask it again
                    continue;
                }
            }
            else
            {
                return typeName;
            }
        }
    }

    public boolean changed;
    public IndusGUI parent;

    abstract public void promptSave();

    abstract public void showDefault(String toSelect);
}
