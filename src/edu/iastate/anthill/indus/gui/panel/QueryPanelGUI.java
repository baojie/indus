package edu.iastate.anthill.indus.gui.panel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;

import edu.iastate.anthill.indus.IndusConstants;

import edu.iastate.utils.sql.DBPanel;
import edu.iastate.utils.sql.SQLPanel;

public class QueryPanelGUI
    extends IndusPane
{
    JButton btnRun = new JButton("Run");
    JButton btnLoad = new JButton("Load");
    JButton btnSave = new JButton("Save");

    JSplitPane jSplitPane1;
    DBPanel dbPanel = new DBPanel(
        IndusConstants.dbCacheURL,
        IndusConstants.dbUsr,
        IndusConstants.dbPwd, false, IndusConstants.dbDriver, false);

    SQLPanel sqlInputArea = new SQLPanel();
    JPanel leftPane = new JPanel();
    JPanel guidePane = new JPanel();
    GridLayout gridLayout1 = new GridLayout();

    JButton btnCreateSQL = new JButton();

    Border border1 = BorderFactory.createEtchedBorder(EtchedBorder.RAISED,
        Color.white, new Color(148, 145, 140));
    JButton btnTranslate = new JButton();
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

    private void jbInit() throws Exception
    {

        // 1  left panel

        // 1.1 left-upper
        btnCreateSQL.setText("Create Query");

        guidePane.setBorder(border1);
        btnTranslate.setText(" To Local Term");
        //guidePane.add(btnTranslate);
        guidePane.add(btnCreateSQL);

        // 1.2 left-bottom
        sqlInputArea.buttonPanel.add(btnRun, null);
        sqlInputArea.buttonPanel.add(btnLoad, null);
        sqlInputArea.buttonPanel.add(btnSave, null);
        sqlInputArea.sqlInput.setEditable(true);
        sqlInputArea.jButtonCopy.setVisible(false);
        sqlInputArea.jButtonPaste.setVisible(false);

        // left assembly

        leftPane.setLayout(new BorderLayout());
        leftPane.add(sqlInputArea, BorderLayout.CENTER);
        leftPane.add(guidePane, BorderLayout.NORTH);

        // 2 right panel
        // just DBPanel dbPanel

        // 3 total assembly
        jSplitPane1 = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPane,
                                     dbPanel);
        add(jSplitPane1, BorderLayout.CENTER);
    }

    public void promptSave()
    {
    }

    public void showDefault(String toSelect)
    {
    }

}
