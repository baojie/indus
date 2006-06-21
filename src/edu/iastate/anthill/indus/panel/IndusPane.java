package edu.iastate.anthill.indus.panel;

import java.awt.BorderLayout;

import javax.swing.JFileChooser;
import javax.swing.JPanel;

import edu.iastate.anthill.indus.IndusGUI;
import edu.iastate.anthill.indus.datasource.type.DataType;
import edu.iastate.utils.gui.FileFilterEx;
import edu.iastate.utils.io.FileUtils;

import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;

/**
 *
 * @author Jie Bao
 * @since 1.0 2005-03-11
 */
public abstract class IndusPane extends JPanel
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
            { return null; }

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
                        JOptionPane.showMessageDialog(this, "The name '"
                                + typeName + "' already exists");
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

    protected String getFileName(String title, String extension,
            String description, boolean isSave)
    {
        // ask for a place to save the file
        JFileChooser saveDialog = new JFileChooser();

        int mode = isSave ? JFileChooser.SAVE_DIALOG : JFileChooser.OPEN_DIALOG;
        saveDialog.setDialogType(mode);
        saveDialog.setDialogTitle(title);

        FileFilterEx firstFilter = new FileFilterEx(extension, description);
        saveDialog.addChoosableFileFilter(firstFilter);

        //The "All Files" file filter is added to the dialog
        //by default. Put it at the end of the list.
        FileFilter all = saveDialog.getAcceptAllFileFilter();
        saveDialog.removeChoosableFileFilter(all);
        saveDialog.addChoosableFileFilter(all);
        saveDialog.setFileFilter(firstFilter);

        int returnVal = saveDialog.showSaveDialog(null);
        if (returnVal == JFileChooser.APPROVE_OPTION)
        {
            String newfile = saveDialog.getSelectedFile().getPath();

            if (isSave)
            {
                // if the name have no extenstion, append extension like "owl"
                if (FileUtils.findExtension(newfile) == "")
                {
                    newfile += "." + extension;
                }
            }
            return newfile;
        }
        return null;
    }

    public boolean  changed;

    public IndusGUI parent;

    abstract public void promptSave();

    abstract public void showDefault(String toSelect);
}
