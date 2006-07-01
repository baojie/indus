package edu.iastate.anthill.indus.gui.panel;

import java.sql.Connection;

import java.awt.event.ActionEvent;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

import edu.iastate.anthill.indus.agent.InfoReader;
import edu.iastate.anthill.indus.agent.InfoWriter;
import edu.iastate.anthill.indus.datasource.IndusDataSource;
import edu.iastate.anthill.indus.datasource.schema.Schema;

import edu.iastate.utils.gui.GUIUtils;
import edu.iastate.utils.lang.MessageHandler;
import edu.iastate.utils.lang.MessageMap;
import edu.iastate.utils.sql.JDBCConfigPanel;

/**
 * @author Jie Bao
 * @since 1.0 2005-03-18
 */
public class DataRDBMSPanel
    extends JDBCConfigPanel implements MessageHandler
{
    Connection systemDB;
    public DataRDBMSPanel(Connection infoDB)
    {
        try
        {
            this.systemDB = infoDB;
            jbInit();
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }

    JComboBox schemaList = new JComboBox();
    JTextField DBName = new JTextField();

    static String NULL_SCHEMA = "[NULL]";
    JButton btnViewData = new JButton();


    protected void jbInit() throws Exception
    {
        messageMap();

        myContentPane.addItem("Table Name", DBName);
        DBName.setEditable(false);

        Object data[] = InfoReader.getAllSchema();
        schemaList = new JComboBox(data);
        schemaList.insertItemAt(NULL_SCHEMA, 0);
        schemaList.setSelectedItem(NULL_SCHEMA);
        myContentPane.addItem("Schema Definition", schemaList);

        btnViewData.setText("View Data");
        paneButton.add(btnViewData);

        super.jbInit();
    }

    /**
     * @since 2005-03-27
     * @return IndusDataSource
     */
    IndusDataSource fromGUI()
    {
        IndusDataSource ds = new IndusDataSource();

        ds.setName(DBName.getText());
        ds.setSchemaName(getSelectedSchema());
        ds.setUrl(dbMachineURL.getText());
        ds.setUser(dbUserID.getText());
        ds.setPassword(new String(dbUserPwd.getPassword()));
        ds.setDriver(dbJdbcDriver.getText());

        return ds;
    }

    /**
     * @since 2005-03-27
     * @return String
     */
    String getSelectedSchema()
    {
        String str = (String) schemaList.getSelectedItem();
        if (str != null)
        {
            if (!str.equals(this.NULL_SCHEMA))
            {
                return str;
            }
        }
        return null;
    }

    /**
     * @since 2005-03-27
     * @param ds IndusDataSource
     */
    void toGUI(IndusDataSource ds)
    {
        if (ds != null)
        {
            DBName.setText(ds.getName());

            String schemaName = ds.getSchemaName();
            schemaList.setEnabled(false);
            if (schemaName == null || schemaName.trim().length() == 0)
            {
                schemaName = this.NULL_SCHEMA;
                schemaList.setEnabled(true);
            }
            schemaList.setSelectedItem(schemaName);

            dbMachineURL.setText(ds.getUrl());
            if (ds.getUrl() != null)
            {
                for (int i = 0; i < items.length; i++)
                {
                    if (ds.getUrl().toUpperCase().indexOf(items[i]) > -1)
                    {
                        dbType.setSelectedItem(items[i]);
                    }
                }
            }
            dbUserID.setText(ds.getUser());
            dbUserPwd.setText(ds.getPassword());
            dbJdbcDriver.setText(ds.getDriver());
        }
    }

    /**
     * delete
     * @since 2005-03-27
     */
    public void delete()
    {
        IndusDataSource ds = fromGUI();

        // if the data source is in indus local repository, delete it
        if (ds.isLocal())
        {
            // ask for confirmation
            String info =
                "Do you also want to delete the real data of this data source ?" +
                "\n\nData cannot be restored if deleted." +
                "\n\nData Source information:\n" +
                ds.toString();
            int answer = JOptionPane.showConfirmDialog(this, info);
            if (answer == JOptionPane.YES_OPTION)
            {
                ds.deleteRealDataSource();
            }

        }

        ds.deleteDataSourceRegistraion(systemDB);

        // clear UI
        ds = new IndusDataSource();
        toGUI(ds);
    }

    Schema extractRelationalSchema(IndusDataSource ds)
    {
        if (ds.getSchemaName() == null ||
            ds.getSchemaName().trim().length() == 0)
        {
            String info =
                "The data source has no associated schema, do you" +
                "want to create one from its relational defintion?";
            int answer = JOptionPane.showConfirmDialog(this, info);
            if (answer == JOptionPane.YES_OPTION)
            {
                if (!ds.connect())
                {
                    info = "Cannot create connect with given information";
                    JOptionPane.showMessageDialog(this, info);
                    return null;
                }
                Schema schema = Schema.buildFromDBTable(ds.db, ds.getName(),
                    ds.getName() + "_Schema");
                if (schema != null)
                {
                    // add to schema list
                    schemaList.addItem(schema.getName());
                    schemaList.setSelectedItem(schema.getName());

                    info = "New schema will be created as: \n" +
                        schema.toString() +
                        "\n\n You can edit it in schema editor";
                    JOptionPane.showMessageDialog(this, info);
                    InfoWriter.writeSchema(schema);
                    ds.disconnect();
                    return schema;
                }
                else
                {
                    info = "Can not extract the schema from database table";
                    JOptionPane.showMessageDialog(this, info);
                }
            }
        }
        return null;
    }

    /**
     * save
     * @since 2005-03-27
     */
    public void save()
    {
        IndusDataSource ds = fromGUI();

        if (ds.getName() != null && ds.getName().trim().length() > 0)
        {
            if (ds.connect())
            {
                extractRelationalSchema(ds);
            }
            boolean suc = InfoWriter.writeDataSource(systemDB, ds);
            String info = suc ? "is registered successfully" :
                "can not be registered";

            JOptionPane.showMessageDialog(
                this, "Data Source '" + DBName.getText() + "' " + info);
            ds.disconnect();
        }
    }

    /**
     * @since 2005-03-27
     */
    public void messageMap()
    {
        try
        {
            MessageMap.mapAction(this.btnTest, this, "onTest");
            MessageMap.mapAction(this.btnViewData, this, "onViewData");
        }
        catch (Exception ex)
        {
        }
    }

    /**
     * View Data of given data source
     * @param evt ActionEvent
     * @since 2005-03-27
     */
    public void onViewData(ActionEvent evt)
    {
        JFrame frame = GUIUtils.getRootFrame(this);
        IndusDataSource ds = fromGUI();

        if (frame != null)
        {
            if (!ds.connect())
            {
                String bad = "Given data source is NOT connectable";
                JOptionPane.showMessageDialog(this, bad);

                return;
            }
            DataAVHViewer viewer = new DataAVHViewer(frame, ds);
            viewer.setSQL("SELECT * FROM " + ds.getName() /*+ " WHERE true;"*/);
            viewer.setVisible(true);
        }
    }

}
