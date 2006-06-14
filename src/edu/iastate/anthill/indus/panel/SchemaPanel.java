package edu.iastate.anthill.indus.panel;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import javax.swing.JOptionPane;

import edu.iastate.anthill.indus.IndusBasis;
import edu.iastate.anthill.indus.IndusConstants;
import edu.iastate.anthill.indus.IndusGUI;
import edu.iastate.anthill.indus.agent.InfoReader;
import edu.iastate.anthill.indus.agent.InfoWriter;
import edu.iastate.anthill.indus.datasource.schema.Schema;

import edu.iastate.utils.Debug;
import edu.iastate.utils.gui.GUIUtils;
import edu.iastate.utils.lang.MessageHandler;
import edu.iastate.utils.lang.MessageMap;

/**
 * Data Schema Definition Panel
 * @author Jie Bao
 * @since 1.0 2004-09-23
 */

public class SchemaPanel
    extends SchemaPanelGUI implements MessageHandler
{

    Schema currentSchema = new Schema("");

    Map defaultDBType = new HashMap();

    public SchemaPanel(IndusGUI parent, String defaultToShow)
    {
        try
        {
            this.parent = parent;

            defaultDBType.put("integer", "integer");
            defaultDBType.put("boolean", "boolean");
            defaultDBType.put("float", "real");
            defaultDBType.put("real", "real");
            defaultDBType.put("date", "date");
            defaultDBType.put("string", "varchar(128)");
            defaultDBType.put("AVH", "varchar(128)");
            defaultDBType.put("DAG", "varchar(128)");

            localInit();
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
    public void showDefault(String defaultToShow)
    {
        // look it up
        if (defaultToShow != null && defaultToShow.length() > 0)
        {
            for (int i = 0; i < schemaList.getItemCount(); i++)
            {
                if (schemaList.getItemAt(i).toString().equals(defaultToShow))
                {
                    // show it
                    schemaList.setSelectedIndex(i);
                    loadSchema(defaultToShow);
                    break;
                }
            }
        }
    }

    void localInit() throws Exception
    {
        messageMap();

        schemaList.addItemListener(new MySchemaListener());
        typeList.addItemListener(new MyTypeListener());
        readRegisteredSchema(null);
    }

    void readRegisteredSchema(Object defaultSelected)
    {
        String oldSelected = (String) schemaList.getSelectedItem();

        // get the list of all registered type
        String data[] = InfoReader.getAllSchema();

        if (data != null)
        {
            GUIUtils.updateComboBox(schemaList, data);

            if (defaultSelected != null)
            {
                schemaList.setSelectedItem(defaultSelected);
            }
            else if (oldSelected != null)
            {
                schemaList.setSelectedItem(oldSelected);
            }
            else if (schemaList.getItemCount() > 0)
            {
                schemaList.setSelectedIndex(0);
            }
        }
    }

    public void onNewAttr(ActionEvent e)
    {
        String name = JOptionPane.showInputDialog(this,
                                                  "The name of new attribute");
        if (name != null) // User clicked OK
        {
            // valiate "text"
            if (!name.matches("[\\w\\-._]+"))
            {
                JOptionPane.showMessageDialog(this,
                                              "Attribute name is not legal!");
                return;
            }

            // find if name is used
            if (currentSchema.getType(name) != null)
            {
                JOptionPane.showMessageDialog(this, "Attribute name is used!");
                return;
            }

            model.addRow(new Object[]
                         {name, "string", "varchar(128)"});
            currentSchema.addAttribute(name, "string", "varchar(128)");
            modified = true;
        }

    }

    public void onDeleteAtt(ActionEvent e)
    {

        int rowIndex = schemaTable.getSelectedRow();
        if (rowIndex != -1)
        {
            String attrName = (String) model.getValueAt(rowIndex, attIndex);

            int answer = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete attribute '" + attrName + "'?");
            if (answer == JOptionPane.YES_OPTION)
            {
                currentSchema.deleteAttribute(attrName);
                model.removeRow(rowIndex);
                modified = true;
            }
        }
    }

    public void onSave(ActionEvent e)
    {
        save();
    }

    public void save()
    {
        String name = currentSchema.getName();
        //validate
        if (!name.matches("[\\w\\-._]+"))
        {
            JOptionPane.showMessageDialog(this, "Name is not legal!");
            return;
        }

        // read from UI
        currentSchema.fromGUI(name, model);

        boolean suc = InfoWriter.writeSchema(currentSchema);

        if (suc)
        {
            JOptionPane.showMessageDialog(this,
                                          "Schema " + name +
                                          " is successfully saved",
                                          "Schema", JOptionPane.PLAIN_MESSAGE);
            modified = false;
            // look if the type is in the list
            for (int i = 0; i < schemaList.getItemCount(); i++)
            {
                String item = (String) schemaList.getItemAt(i);
                if (item.equals(name))
                {
                    return;
                }
            }
            // if a new type , update schema list
            // readRegisteredSchema(name);
            schemaList.addItem(name);
            schemaList.setSelectedItem(name);
        }
        else
        {
            JOptionPane.showMessageDialog(this,
                                          "Schema " + name + " can't be saved",
                                          "Schema", JOptionPane.ERROR_MESSAGE);
        }

    }

    class MyTypeListener
        implements ItemListener

    {
        public void itemStateChanged(ItemEvent e)
        {
            Object item = e.getItem();

            if (e.getStateChange() == ItemEvent.SELECTED)
            {
                int row = schemaTable.getSelectedRow();
                if (row != -1)
                {
                    Object dbtype = defaultDBType.get(item);
                    if (dbtype == null)
                    {
                        dbtype = "varchar(128)";
                    }

                    Object oldValue = model.getValueAt(row, dbTypeIndex);
                    //if (oldValue == null || oldValue.equals(""))
                    {
                        model.setValueAt(dbtype, row, dbTypeIndex);
                    }
                }
            }
        }
    }

    class MySchemaListener
        implements ItemListener
    {
        // This method is called only if a new item has been selected.
        public void itemStateChanged(ItemEvent evt)
        {
            // Get the affected item
            Object item = evt.getItem();

            if (evt.getStateChange() == ItemEvent.SELECTED)
            {
                // Item was just selected
                // prompt for save
                promptSave();
                modified = false;

                //Debug.trace(this,item.toString());
                if (!loadSchema(item.toString()))
                {
                    // load fails, restore the privious one unless it's the same to be loaded
                    if (currentSchema != null && currentSchema.getName() != null)
                    {
                        if (!currentSchema.getName().equals( (String) item))
                        {
                            schemaList.setSelectedItem(currentSchema.getName());
                        }
                    }
                }
            }
        }
    }

    public void onEditType(ActionEvent evt)
    {
        // show the selection
        int rowIndex = schemaTable.getSelectedRow();

        // goto type edit panel
        String type = model.getValueAt(rowIndex, typeIndex).toString();
        parent.switchToPane(parent.paneOntology, type);
    }

    /**
     * loadSchema
     *
     * @param string String
     * @version 2004-09-30
     */
    private boolean loadSchema(String name)
    {
        if (name == null || name.length() == 0)
        {
            return false;
        }

        Schema newSchema = InfoReader.readSchema(name);

        if (newSchema != null)
        {
            // find missing types
            Set used = newSchema.getTypeSet(); // types used in this schema
            String types[] = InfoReader.getAllType(); // types in the system
            Set available = new HashSet();
            for (int i = 0; i < types.length; i++)
            {
                available.add(types[i]);
            }
            if (!available.containsAll(used))
            {
                // some types are missing
                used.removeAll(available);
                Debug.trace(null, "Types " + used +
                            " are missing! Loading could results in unexpected outcome" +
                            ",\nplease create them or change your schema defintion");
            }

            // update UI
            currentSchema = newSchema;
            currentSchema.toTable(model);
            this.nameLabel.setText("Schema: " + name);
            schemaList.setSelectedItem(name);
            modified = false;
        }
        else
        {
            Debug.trace(this,
                        "Schema :" + name + " information is not available");
        }
        return true;
    }

    public void onNew(ActionEvent e)
    {
        String used[] = InfoReader.getAllSchema();
        String name = askForName(used);

        if (name == null)
        {
            return;
        }

        // remove all attribute rows
        while (model.getRowCount() > 0)
        {
            model.removeRow(0);
        }
        currentSchema.clear();
        currentSchema.setName(name);
        this.nameLabel.setText("Schema: " + name);

        save();
        modified = false;
    }

    /**
     * @param e ActionEvent
     * @since 2004-10-08
     */
    public void onExport(ActionEvent e)
    {
        if (currentSchema != null)
        {
            currentSchema.fromGUI(currentSchema.getName(), model);
            String xml = currentSchema.toXML();

            if (xml != null)
            {
                String url = IndusConstants.schemaBasisURL +
                    currentSchema.getName() + ".xml";
                //Debug.trace("This XML file is also available from " + url);

                //Debug.trace(currentSchema.toSQL("localTable"));

                IndusBasis.showXML(xml);
            }
            else
            {
                JOptionPane.showMessageDialog(
                    this, "No schema is defined");
            }
        }
    }

    /**
     * @param e ActionEvent
     * @since 2004-10-11
     */
    public void onDelete(ActionEvent e)
    {
        try
        {
            // get the selected schema
            String schemaName = currentSchema.getName();

            // Modal dialog with yes/no button
            int answer = JOptionPane.showConfirmDialog(this,
                "Are you sure to delete schema '" + schemaName +
                "'? The deletion can't be undone");
            if (answer != JOptionPane.YES_OPTION)
            {
                return;
            }

            boolean suc = InfoWriter.deleteSchema(schemaName);
            if (suc)
            {
                JOptionPane.showMessageDialog(this, "Schema '" + schemaName +
                                              "' is deleted successfully");
                currentSchema.clear();
                modified = false;
                readRegisteredSchema(null);
            }
            else
            {
                JOptionPane.showMessageDialog(
                    this, "Delete schema '" + schemaName + "' failed!");
            }
        }
        catch (Exception ex)
        {
        }
    }

    public void onSchemaList(ActionEvent e)
    {
        readRegisteredSchema(null);

    }

    /**
     * prompt to save changes when exit
     * @author Jie Bao
     * @since 2004-10-12
     */
    public void promptSave()
    {
        // prompt for save
        if (modified && currentSchema != null)
        {
            int answer = JOptionPane.showConfirmDialog(null,
                "Schema '" + currentSchema.getName() +
                "' is changed, do you want to update it? ");
            if (answer == JOptionPane.YES_OPTION)
            {
                save();
            }
        }
    }

    /**
     * @since 2005-03-25
     */
    public void messageMap()
    {
        try
        {
            MessageMap.mapAction(this.btnNewAttr, this, "onNewAttr");
            MessageMap.mapAction(this.btnDeleteAttr, this, "onDeleteAtt");
            MessageMap.mapAction(this.btnSave, this, "onSave");
            MessageMap.mapAction(this.btnNew, this, "onNew");
            MessageMap.mapAction(this.updateTypeList, this, "onUpdateTypeList");
            MessageMap.mapAction(this.btnExport, this, "onExport");
            MessageMap.mapAction(this.btnDelete, this, "onDelete");
            MessageMap.mapAction(this.menuEditType, this, "onEditType");
            MessageMap.mapAction(this.btnSchemaList, this, "onSchemaList");
        }
        catch (Exception ex)
        {
        }
    }

}
