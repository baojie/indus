package edu.iastate.anthill.indus.gui.panel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;

import edu.iastate.anthill.indus.IndusConstants;
import edu.iastate.utils.gui.JTableEx;
import edu.iastate.utils.sql.DBPanel;
import edu.iastate.utils.sql.SQLPanel;

public class QueryPanelGUI extends IndusPane
{
    JButton           btnCreateSQL = new JButton("Create Query");
    JButton           btnLoad      = new JButton("Load");
    JButton           btnRun       = new JButton("Run");
    JButton           btnSave      = new JButton("Save");

    GridLayout        gridLayout1  = new GridLayout();
    JSplitPane        jSplitPane1;

    SQLPanel          localSQL     = new SQLPanel();
    DefaultTableModel model        = new DefaultTableModel();
    JPanel            queryPane    = new JPanel();

    JTableEx          remoteSQL    = new JTableEx(model);
    DBPanel           resultPane   = new DBPanel(IndusConstants.dbCacheURL,
                                           IndusConstants.dbUsr,
                                           IndusConstants.dbPwd, false,
                                           IndusConstants.dbDriver, false);

    int               typeIndex, attIndex;

    public QueryPanelGUI()
    {

        super();
        try
        {
            jbInit();
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }

    //2006-06-30 Jie Bao
    private void createTable()
    {
        // Create 3 columns
        model.addColumn("Data Source");
        model.addColumn("Native SQL");

        typeIndex = remoteSQL.getColumn("Data Source").getModelIndex();
        attIndex = remoteSQL.getColumn("Native SQL").getModelIndex();

        remoteSQL.setRowHeight(24);
        remoteSQL.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        remoteSQL.setRowSelectionAllowed(true);

        //model.addTableModelListener(new MyTableModelListener(remoteSQL));
    }

    private void jbInit() throws Exception
    {

        // 1  bottom panel

        // 1.1 left-bottom
        localSQL.buttonPanel.add(btnCreateSQL, null);
        localSQL.buttonPanel.add(btnRun, null);
        localSQL.buttonPanel.add(btnLoad, null);
        localSQL.buttonPanel.add(btnSave, null);

        localSQL.sqlInput.setEditable(false);
        localSQL.sqlInput.setBackground(Color.YELLOW);
        localSQL.setInfo("Query in local ontologies");
        localSQL.btnCopy.setVisible(true);
        localSQL.btnPaste.setVisible(false);

        // 1.2 bottom assembly
        queryPane.setLayout(new GridLayout(1, 2));
        queryPane.add(localSQL);

        createTable();
        queryPane.add(new JScrollPane(remoteSQL));
        //queryPane.add(guidePane, BorderLayout.NORTH);

        // 2 right panel
        // just DBPanel resultPane

        // 3 total assembly
        jSplitPane1 = new JSplitPane(JSplitPane.VERTICAL_SPLIT, resultPane,
                queryPane);
        add(jSplitPane1, BorderLayout.CENTER);
    }

    public void promptSave()
    {}

    // 2006-06-30 Jie Bao
    public void resetPanel()
    {
        jSplitPane1.setDividerLocation(0.8);
    }

    public void showDefault(String toSelect)
    {}

}
