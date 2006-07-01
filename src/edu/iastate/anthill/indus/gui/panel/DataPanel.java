package edu.iastate.anthill.indus.gui.panel;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.Map;
import java.util.Vector;

import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import javax.swing.JOptionPane;

import edu.iastate.anthill.indus.IndusConstants;
import edu.iastate.anthill.indus.agent.InfoReader;
import edu.iastate.anthill.indus.agent.InfoWriter;
import edu.iastate.anthill.indus.datasource.IndusDataSource;
import edu.iastate.anthill.indus.datasource.schema.Schema;
import edu.iastate.anthill.indus.gui.IndusGUI;

import edu.iastate.utils.Debug;
import edu.iastate.utils.gui.GUIUtils;
import edu.iastate.utils.lang.MessageHandler;
import edu.iastate.utils.lang.MessageMap;
import edu.iastate.utils.sql.JDBCUtils;

/**
 * @author Jie Bao
 * @since 1.0 2004-03-15
 */
public class DataPanel
    extends DataPanelGUI implements MessageHandler
{
    public DataPanel(IndusGUI parent)
    {
        super(parent);
        try
        {
            this.parent = parent;
            jbInit();
        }
        catch (Exception exception)
        {
            exception.printStackTrace();
        }
    }

    private void jbInit() throws Exception
    {
        messageMap();
        //Debug.trace("DataPanel.jbInit");
        datasourceList.addItemListener(new MySchemaListener());
        onUpdateDSList(null);
    }

    // 2005-03-27
    public void promptSave()
    {
        // prompt for save
        IndusDataSource ds = this.dsPanel.fromGUI();
        if (modified && ds.getName() != null &&
            ds.getName().trim().length() > 0)
        {
            int answer = JOptionPane.showConfirmDialog(null,
                "Data Source defintion '" + ds.getName() +
                "' is changed, do you want to update it? ");
            if (answer == JOptionPane.YES_OPTION)
            {
                onSave(null);
            }
        }
    }

    public void showDefault(String toSelect)
    {
        if (toSelect != null)
        {
            datasourceList.setSelectedItem(toSelect);
        }
    }

    /**
     * @since 2005-03-27
     * @param e ActionEvent
     */
    public void onSave(ActionEvent e)
    {
        this.dsPanel.save();
    }

    public void onDeleteDS(ActionEvent e)
    {
        // get currently selected ds
        String selected = (String) datasourceList.getSelectedItem();

        if (selected == null)
        {
            return;
        }

        int answer = JOptionPane.showConfirmDialog(this,
            "Are you sure to delete the data source registration '" + selected +
            "'?\n" +
            "The real data will be kept if it is not on INDUS server,\n" +
            "You may delete the data with your own database management tool");
        if (answer == JOptionPane.YES_OPTION)
        {
            this.dsPanel.delete();
            datasourceList.removeItem(selected);
            modified = false;
        }
    }

    /**
     * @since 2005-03-15
     */
    public void onCreateNewDS(ActionEvent e)
    {
        promptSave();

        String dsName = JOptionPane.showInputDialog(this,
            "Name of the new data source\n" +
            "The name MUST be the same of the table of this data source\n" +
            "Current version of INDUS only supports single-table data source");
        if (dsName == null) // User clicked OK
        {
            return;
        }
        // valiate "text"
        if (!dsName.matches("[\\w\\-._]+"))
        {
            JOptionPane.showMessageDialog(this, "Name is not legal!");
            return;
        }

        // get the list of all registered ds
        Vector allds = IndusDataSource.getAllDataSource(
            this.parent.indusSystemDB.db);
        if (allds.contains(dsName))
        {
            JOptionPane.showMessageDialog(
                this, "Data Source '" + dsName + "' already exists");
            return;
        }

        String data[] =
            {
            "Relational Database", "Create New From Schema", "Weka ARFF File",
            "CSV File"};
        String choice = (String) JOptionPane.showInputDialog(null,
            "Choose one", "Input",
            JOptionPane.INFORMATION_MESSAGE, null,
            data, data[0]);
        if (choice == null)
        {
            return;
        }
        if (choice.equals("Relational Database"))
        {
            registerRDB(dsName);
        }
        else if (choice.equals("Create New From Schema"))
        {
            createFromSchema(parent.indusLocalDB.db, dsName);
        }
        else
        {
            Debug.trace("Not available in this edition");
        }
    }

    /**
     * Register a relational database table
     * @param dsName String
     */
    private void registerRDB(String dsName)
    {
        IndusDataSource ds = new IndusDataSource();
        ds.setName(dsName);
        this.dsPanel.toGUI(ds);
        onSave(null);
        datasourceList.addItem(dsName);
        datasourceList.setSelectedItem(dsName);
    }

    public void messageMap()
    {
        try
        {
            MessageMap.mapAction(btnCreateNewDS, this, "onCreateNewDS");
            MessageMap.mapAction(btnDeleteDS, this, "onDeleteDS");
            MessageMap.mapAction(btnSave, this, "onSave");
            MessageMap.mapAction(this.btnUpdateDSList, this, "onUpdateDSList");

        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }

    /**
     *
     * @param storedTo Connection
     * @param dsName String
     * @since 2005-03-27
     */
    private void createFromSchema(Connection storedTo, String dsName)
    {
        // =========== 1. ask for schema =================
        Object data[] = InfoReader.getAllSchema();
        if (data == null && data.length == 0)
        {
            JOptionPane.showMessageDialog(this, "No available schema");
            return;
        }

        String schemaName = (String) JOptionPane.showInputDialog(this,
            "Choose one schema", "Input", JOptionPane.INFORMATION_MESSAGE,
            null, data, data[0]);
        if (schemaName == null)
        {
            return;
        }

        // load the schema information
        Schema schema = InfoReader.readSchema(schemaName);

        // =========== 2. create a local db table ===============

        // choose pk
        Map attList = schema.getAttList();
        Object keys[] = attList.keySet().toArray();
        String pk = (String) JOptionPane.showInputDialog(null,
            "You need to choose a primary key", "Input",
            JOptionPane.INFORMATION_MESSAGE, null,
            keys, keys[0]);
        if (pk == null)
        {
            return;
        }

        Vector pks = new Vector();
        pks.add(pk);

        // check if the table is alreay exists
        if (JDBCUtils.isTableExist(storedTo, dsName,false))
        {
            String info = "Table with the same name '" + dsName +
                "' already exists" +
                ", please choose anthor name and try again";
            JOptionPane.showMessageDialog(this, info);
            return;

        }

        String sql = schema.toSQL(dsName, pks, null);
        boolean suc = JDBCUtils.updateDatabase(storedTo, sql);

        if (!suc)
        {
            String info =
                "Creating local table failed, data source cannot be created";
            JOptionPane.showMessageDialog(this, info);
            return;
        }

        // =======  3. create and save a data source for this table ========
        IndusDataSource ds = new IndusDataSource();
        try
        {
            DatabaseMetaData dmd = storedTo.getMetaData();

            ds.setName(dsName);
            ds.setSchemaName(schemaName);
            ds.setUrl(dmd.getURL());
            ds.setUser(dmd.getUserName());
            ds.setPassword(IndusConstants.dbPwd);

            //Driver driver = DriverManager.getDriver(dmd.getURL());
            ds.setDriver(IndusConstants.dbDriver);
        }
        catch (SQLException ex)
        {
            String info =
                "Reading connection setting failed, data source cannot be created";
            JOptionPane.showMessageDialog(this, info);
            return;
        }

        InfoWriter.writeDataSource(parent.indusSystemDB.db, ds);
        // ===========      4. update gui  ================

        datasourceList.addItem(dsName);
        datasourceList.setSelectedItem(dsName);
        this.dsPanel.toGUI(ds);
    }

    /**
     * @since 2005-03-27
     * @param e ActionEvent
     */
    public void onUpdateDSList(ActionEvent e)
    {
        Vector v = IndusDataSource.getAllDataSource(parent.indusSystemDB.db);
        GUIUtils.updateComboBox(datasourceList, v.toArray());
    }

    class MySchemaListener
        implements ItemListener
    {
        // This method is called only if a new item has been selected.
        public void itemStateChanged(ItemEvent evt)
        {
            // Get the affected item
            Object item = evt.getItem();

            if (evt.getStateChange() == ItemEvent.SELECTED && item != null)
            {
                // Item was just selected
                // prompt for save
                promptSave();
                //Debug.trace(item);
                IndusDataSource ds = InfoReader.readDataSource(
                    parent.indusSystemDB.db, item.toString());
                dsPanel.toGUI(ds);
                modified = false;
            }
        }
    }

    protected boolean modified = false;

}
