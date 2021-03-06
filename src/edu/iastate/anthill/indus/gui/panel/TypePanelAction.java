package edu.iastate.anthill.indus.gui.panel;

import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Arrays;
import java.util.Vector;

import javax.swing.ImageIcon;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.w3c.dom.Text;

import edu.iastate.anthill.indus.IndusConstants;
import edu.iastate.anthill.indus.agent.InfoReader;
import edu.iastate.anthill.indus.agent.InfoWriter;
import edu.iastate.anthill.indus.datasource.type.AVH;
import edu.iastate.anthill.indus.datasource.type.DAG;
import edu.iastate.anthill.indus.datasource.type.DataType;
import edu.iastate.anthill.indus.datasource.type.DbAVH;
import edu.iastate.anthill.indus.datasource.type.SimpleDataType;
import edu.iastate.anthill.indus.gui.IndusBasis;
import edu.iastate.anthill.indus.iterator.DB2TreeFactory;
import edu.iastate.utils.Debug;
import edu.iastate.utils.io.FileUtils;
import edu.iastate.utils.lang.MessageHandler;
import edu.iastate.utils.lang.MessageMap;
import edu.iastate.utils.string.Zip;

/**
 *
 * <p>@author Jie Bao , baojie@cs.iastate.edu</p>
 * <p>@since 2005-03-29</p>
 */
public abstract class TypePanelAction extends TypePanelGUI implements
        MessageHandler
{
    public TypePanelAction()
    {
        super();
        try
        {
            jbInit2();
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }

    JMenuItem        menuDelete   = new JMenuItem("Delete");
    JMenuItem        menuCopyType = new JMenuItem("Copy Type");
    final JPopupMenu menu         = new JPopupMenu();

    void jbInit2() throws Exception
    {
        messageMap();

        menu.add(menuDelete);
        menu.add(menuCopyType);
    }

    public void messageMap()
    {
        try
        {
            MessageMap.mapAction(this.btnNewType, this, "onCreateNewType");
            MessageMap.mapAction(this.btnSave, this, "onSave");
            MessageMap.mapAction(this.btnExportXML, this, "onExport");
            MessageMap.mapAction(this.menuDelete, this, "onDeleteType");
            MessageMap.mapAction(this.menuCopyType, this, "onCopyType");
            MessageMap.mapAction(this.btnExportText, this, "onExportText");
            MessageMap.mapAction(this.btnReload, this, "onReload");
            MessageMap.mapAction(this.btnImportText, this, "onImportText");
            MessageMap.mapAction(this.btnImportXML, this, "onImportXML");

            listAllTypes.addListSelectionListener(new ListSelectionListener() {
                public void valueChanged(ListSelectionEvent e)
                {
                    onSelectedTypeChanged(e);
                }
            });
            // Set the component to show the popup menu
            listAllTypes.addMouseListener(new MouseAdapter() {
                public void mousePressed(MouseEvent evt)
                {
                    int row = listAllTypes.locationToIndex(evt.getPoint());
                    listAllTypes.setSelectedIndex(row);

                    if (evt.isPopupTrigger())
                    {
                        menu.show(evt.getComponent(), evt.getX(), evt.getY());
                    }
                }

                public void mouseReleased(MouseEvent evt)
                {
                    if (evt.isPopupTrigger())
                    {
                        menu.show(evt.getComponent(), evt.getX(), evt.getY());
                    }
                }
            });

        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }

    public void onSelectedTypeChanged(ListSelectionEvent evt)
    {
        // When the user release the mouse button and completes the selection,
        // getValueIsAdjusting() becomes false
        if (!evt.getValueIsAdjusting())
        {
            JList list = (JList) evt.getSource();
            promptSave();
            final String currentSelectedType = (String) list.getSelectedValue();

            // Get all selected items
            if (currentSelectedType != null)
            {
                Thread t = new Thread() {
                    public void run()
                    {
                        //load it
                        loadType(currentSelectedType, false);

                        // enable / disenable "deletetype" button
                        // deleting of predefined type is not allowed

                        btnSave.setEnabled(!AVH
                                .isPredefinedType(currentSelectedType));

                    }
                };
                t.start();
            }
        }
    }

    /**
     * If the type is AVT, create a tree for it
     *
     * @param selected String
     * @version 2004-09-30
     */
    protected void loadType(String selected, boolean forceReload)
    {

        int pb = parent.statusBar.addProgressBar(true, 0, 0);
        parent.statusBar.updateProgressBar(pb, "Loading type " + selected);

        DataType dt = null;
        try
        {
            dt = InfoReader.readDataType(selected, forceReload);
        }
        catch (Exception e)
        {
            Debug.trace("Error in loading " + selected + "\n\n"
                    + e.getMessage());
            e.printStackTrace();
        }
        parent.statusBar.removeProgressBar(pb);
        loadType(dt);

    }

    protected void loadType(DataType dt)
    {
        String selected = dt.getName();

        if (dt != null)
        {
            currentType = dt;
            String info = "Selected Type : '" + (String) selected + "', "
                    + currentType.getInformation();
            labelSelectedType.setText(info);

            String supertype = currentType.getSupertype();
            ImageIcon icon = IndusConstants.iconDatatype;

            if (AVH.isAVH(selected, supertype))
            {
                icon = IndusConstants.iconTreetype;
            }
            else if (DataType.isNumber(selected, supertype))
            {
                icon = IndusConstants.iconNumber;
            }
            else if (DataType.isString(selected, supertype))
            {
                icon = IndusConstants.iconString;
            }
            typeIcon.put(selected, icon);

            //Debug.trace(currentType.getClass());

            jScrollPaneTree.getViewport().removeAll();
            jScrollPaneTree.getViewport()
                    .add(currentType.getEditorPane(), null);
            currentType.modified = false;
        }
        else
        {
            String info = "Type definition is not available for '" + selected
                    + "'";
            JOptionPane.showMessageDialog(this, info);
        }
    }

    DataType currentType = null;

    public void onCreateNewType(ActionEvent e)
    {
        // ==== 1. ask for name =======
        Object used[] = InfoReader.getAllType();
        String name = askForName(used);

        if (name == null) { return; }

        // ======== 2. select father type =======
        String data[] = DataType.getPredefinedTypes();
        String fatherType = (String) JOptionPane.showInputDialog(this,
                "Choose one", "Input", JOptionPane.INFORMATION_MESSAGE, null,
                data, data[0]);

        if (fatherType == null) { return; }

        // ======== 3. create new type ===========
        String treetype = null;
        //         === 3.1 AVH ====
        if (fatherType.equals("AVH"))
        {
            // select AVH tree type eg. partof, isa
            while (true)
            {
                treetype = JOptionPane.showInputDialog(
                        "Please give the type of the tree", "ISA");
                if (treetype == null) { return; }

                // validate the name
                if (!AVH.isLegalName(treetype))
                {
                    JOptionPane.showMessageDialog(this, "Name is not legal!");
                }
                else
                {
                    break;
                }
            }

            // select template
            String temp[] = DB2TreeFactory.allTemplete;
            String template = (String) JOptionPane
                    .showInputDialog(
                            this,
                            "Choose one tree template, or click cancel if you don't need any template"
                                    + "\n\n A template is a tree predefined by INDUS. If you selected a template to\n"
                                    + " the new AVH, you can copy the whole or part of the template into your new AVH",
                            "Input", JOptionPane.INFORMATION_MESSAGE, null,
                            temp, temp[0]);

            if (template == null)
            {
                currentType = new AVH(name, treetype);
            }
            else
            {
                currentType = new DbAVH(name, treetype, template);
            }
        }
        //  === 3.1 DAG ====
        else if (fatherType.equals("DAG"))
        {
            currentType = new DAG(name);
        }
        else
        {
            currentType = new SimpleDataType(name, fatherType);
        }

        // ======== 4. submit the new type to server and update GUI ========
        if (currentType != null && save())
        {
            // update UI
            updateTypesList();
            listAllTypes.setSelectedValue(name, true);
        }
        currentType.modified = false;

    }

    public void onSave(ActionEvent e)
    {
        save();
    }

    // export AVT to xml
    boolean save()
    {
        // Debug.trace(currentType.print());
        String typeName = null;
        try
        {
            if (currentType != null)
            {
                if (currentType.readOnly)
                {

                    JOptionPane.showMessageDialog(this,
                            "This is a read only type, cannot be updated");
                    return true;
                }

                typeName = currentType.getName();
                //Debug.trace(typeName);
                if (typeName != null)
                {
                    boolean suc = InfoWriter.writeType(currentType);
                    if (suc)
                    {
                        currentType.modified = false;

                        // 2006-06-15, update the data type cache
                        InfoReader.updateCache(typeName,
                                InfoReader.dataTypeCache, currentType);

                        JOptionPane.showMessageDialog(this, "Type '" + typeName
                                + "' saved successfully");
                        return true;
                    }
                    else
                    {
                        JOptionPane
                                .showMessageDialog(this,
                                        "Cannot communicate with server, saving failed!");
                        return false;
                    }
                }
            }
            JOptionPane.showMessageDialog(this,
                    "No current type! saving failed");
            return false;

        }
        catch (Exception ex)
        {
            JOptionPane.showMessageDialog(this, "Type '" + typeName
                    + "' can't be updated");
            ex.printStackTrace();
            System.out.println(currentType.print());
        }
        return false;
    }

    /**
     * Copy a type
     * @param e ActionEvent
     * @since 2005-03-29
     */
    public void onCopyType(ActionEvent e)
    {
        String typeName = (String) listAllTypes.getSelectedValue();
        // predefined types cannot be copied
        if (DataType.isPredefinedType(typeName))
        {
            String info = "Predefined type cannot be copied";
            JOptionPane.showMessageDialog(this, info);
            return;
        }

        Object used[] = InfoReader.getAllType();
        String name = askForName(used);

        if (name != null)
        {
            DataType dt = InfoReader.readDataType(typeName, false);
            dt.setName(name);
            if (InfoWriter.writeType(dt))
            {
                updateTypesList();
                String info = "Type " + name + " is copied from " + typeName
                        + " sucessfully";
                JOptionPane.showMessageDialog(this, info);
            }
        }

    }

    /**
     * Delete a datatype
     * predefined type is not allowed to be deleted
     *
     * @param e ActionEvent
     */
    public void onDeleteType(ActionEvent e)
    {
        try
        {
            // get the selected type
            String typeName = (String) listAllTypes.getSelectedValue();
            if (!AVH.isPredefinedType(typeName))
            {

                // Modal dialog with yes/no button
                int answer = JOptionPane.showConfirmDialog(this,
                        "Are you sure to delete type '" + typeName
                                + "'? The deletion can't be undone");
                if (answer != JOptionPane.YES_OPTION) { return; }

                boolean suc = InfoWriter.deleteType(typeName);
                if (suc)
                {
                    JOptionPane.showMessageDialog(this, "Type '" + typeName
                            + "' is deleted successfully");
                    currentType.modified = false;

                    //{{ 2006-06-24 Jie Bao 
                    currentType = null;
                    jScrollPaneTree.getViewport().removeAll();
                    jScrollPaneTree.repaint();
                    labelSelectedType.setText(null);
                    model.removeElement(typeName);
                    //}}
                    //updateTypesList();
                }
                else
                {
                    JOptionPane.showMessageDialog(this, "Delete type '"
                            + typeName + "' failed! --");
                }
            }
            else
            {
                JOptionPane.showMessageDialog(this, "Predefined type '"
                        + typeName + "' cannot be deleted");
            }
        }
        catch (Exception ex)
        {}
    }

    /**
     * readRegisteredType
     */
    protected void updateTypesList()
    {
        Object types[] = InfoReader.getAllType();
        Vector dbTypes = new Vector(Arrays.asList(types));
        //dbTypes.add("MIPS");
        //dbTypes.add("SCOP");
        //dbTypes.add("GO");
        types = dbTypes.toArray();

        if (types != null)
        {
            //final ProgressBarWin win = new ProgressBarWin(new JFrame(),
            //    "Updating available types:", 100, false, 0, types.length);
            //win.start();
            
            model.removeAllElements();

            final Object types1[] = types;
            for (int i = 0; i < types.length; i++)
            {
                model.addElement(types[i]);
            }
            //listAllTypes.setListData(types);

            setTypeIcon(types1);

            //win.stop();

            //listAllTypes.validate();
        }
    }

    private void setTypeIcon(Object[] types)
    {
        // set icons, 2004-10-15
        typeIcon.entrySet().clear();
        // Debug.trace(this, typeIcon);
        for (int i = 0; i < types.length; i++)
        {
            ImageIcon icon = IndusConstants.iconDatatype;

            //					    //win.step();
            //		    String supertype = IndusHttpClient.
            //		        getTopSuperType(types[i].toString());
            //		    //Debug.systrace(null, types[i] + ":" + supertype);
            //
            //		    if (AVH.isAVH( (String) types[i], supertype))
            //		    {
            //		        icon = IndusConstants.iconTreetype;
            //		    }
            //		    else if (DataType.isNumber( (String) types[i], supertype))
            //		    {
            //		        icon = IndusConstants.iconNumber;
            //		    }
            //		    else if (DataType.isString( (String) types[i], supertype))
            //		    {
            //		        icon = IndusConstants.iconString;
            //		    }
            typeIcon.put(types[i], icon);
        }
    }

    /**
     * Export the XML for the type
     * @param e ActionEvent
     * @author Jie Bao
     * @since 2004-10-08
     */
    public void onExport(ActionEvent e)
    {
        if (this.currentType != null)
        {
            String xml = currentType.toXML();
            String url = IndusConstants.typeBasisURL + currentType.getName()
                    + ".xml";
            //Debug.trace("This XML file is also available from " + url);
            IndusBasis.showXML(xml);
        }
        else
        {
            JOptionPane.showMessageDialog(this, "No type is defined");
        }
    }

    /**
     * Export the ontology as plain text
     * 
     * @author baojie
     * @since 2006-06-21
     * @param e
     */
    public void onExportText(ActionEvent e)
    {
        if (this.currentType != null)
        {
            String text = currentType.toText();
            // save as 
            final String title = "Export to plain text";
            final String extension = "txt";
            final String description = "Text Documents";

            String fileName = getFileName(title, extension, description, true);
            // get the ontology from the database
            if (fileName != null)
            {
                try
                {
                    FileUtils.writeFile(fileName, text);
                }
                catch (Exception e1)
                {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }
            }
        }
        else
        {
            JOptionPane.showMessageDialog(this, "No type is defined");
        }
    }

    public void onImportXML(ActionEvent e)
    {
        final String title = "Import from XML";
        final String extension = "xml";
        final String description = "XML Documents";
        
        importFile(title, extension, description, true);

    }

    public void onImportText(ActionEvent e)
    {
        // save as 
        final String title = "Import from plain text";
        final String extension = "txt";
        final String description = "Text Documents";

        importFile(title, extension, description, false);

    }

    private void importFile(final String title, final String extension,
            final String description, boolean isXML)
    {
        //save the current
        promptSave();

        String fileName = getFileName(title, extension, description, false);
        // get the ontology from the database
        if (fileName != null)
        {
            String text;
            try
            {
                text = FileUtils.readFile(fileName);
                
                // if it starts with a number, is a zipped text
                if (Character.isDigit(text.charAt(0)))
                {
                    text = Zip.decode(text);
                }
                
                //System.out.println(text);
                
                DataType t = null;
                
                if (isXML)
                    t = InfoReader.readDataTypeXML("newType", text);
                else
                    t =InfoReader.readDataTypeText("newType", text);
                
                if (t== null)
                {
                    Debug.trace("File format error");
                    return;
                }

                // add to list
                currentType = t;
                // check if the name is used
                Object used[] = InfoReader.getAllType();
                Vector v = new Vector(Arrays.asList(used));

                String newName = t.getName();
                int count = 1;
                while (v.contains(newName))
                {
                    newName = t.getName() + "_" + (count++);
                }
                t.setName(newName);

                model.addElement(t.getName());
                listAllTypes.setModel(model);
                //listAllTypes.updateUI();
                listAllTypes.setSelectedValue(t.getName(), true);

                loadType(t);
                InfoReader
                        .updateCache(t.getName(), InfoReader.dataTypeCache, t);
                listAllTypes.repaint();
            }
            catch (Exception e1)
            {
                e1.printStackTrace();
                Debug.trace("Error in reading the file");
            }
        }
    }

    public void onReload(ActionEvent e)
    {
        String typeName = (String) listAllTypes.getSelectedValue();
        // predefined types do not need reloading
        if (!DataType.isPredefinedType(typeName))
        {
            loadType(typeName, true);
        }
    }
}
