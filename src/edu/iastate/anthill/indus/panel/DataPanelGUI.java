package edu.iastate.anthill.indus.panel;

import java.awt.GridLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import edu.iastate.anthill.indus.IndusGUI;

abstract public class DataPanelGUI
    extends IndusPane
{

    JPanel northPanel = new JPanel();
    JComboBox datasourceList = new JComboBox();
    JButton btnCreateNewDS = new JButton();
    JLabel jLabel1 = new JLabel();
    GridLayout gridLayout1 = new GridLayout();
    JButton btnDeleteDS = new JButton();

    //DBPanel dbPanel;
    DataRDBMSPanel dsPanel;
    JButton btnSave = new JButton();
    JButton btnUpdateDSList = new JButton();
    JLabel jLabel2 = new JLabel();

    public DataPanelGUI(IndusGUI parent)
    {
        super();
        try
        {
            this.parent = parent;
            jbInit();
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }

    private void jbInit() throws Exception
    {
        dsPanel = new DataRDBMSPanel(parent.indusSystemDB.db);

        northPanel.setLayout(gridLayout1);
        btnCreateNewDS.setText("Create New DataSource");
        jLabel1.setText("Existing Data Sources ");
        //dbPanel.setEditable(true);
        btnDeleteDS.setText("Delete Datasource");
        btnSave.setText("Save ");
        btnUpdateDSList.setText("Update List");
        northPanel.add(jLabel1, null);
        northPanel.add(datasourceList, null);
        northPanel.add(btnUpdateDSList);
        northPanel.add(jLabel2);
        northPanel.add(btnDeleteDS);
        northPanel.add(btnCreateNewDS, null);
        northPanel.add(btnSave);

        //jTabbedPane1.add(dbPanel, "Edit existing Data Source");
        //jTabbedPane1.add(arffPanel, "Register Weka ARFF Data");
        //jTabbedPane1.add(rdbmsPanel, "Register Relation DB");

        this.add(dsPanel, java.awt.BorderLayout.CENTER);
        this.add(northPanel, java.awt.BorderLayout.NORTH);
    }
}
