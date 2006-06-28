package edu.iastate.anthill.indus.panel;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridLayout;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.ListSelectionModel;

import edu.iastate.anthill.indus.IndusConstants;
import edu.iastate.anthill.indus.IndusGUI;
import edu.iastate.anthill.indus.datasource.mapping.Connector;
import edu.iastate.anthill.indus.datasource.mapping.DataSourceMapping;
import edu.iastate.anthill.indus.datasource.mapping.SimpleConnector;
import edu.iastate.anthill.indus.tree.TypedTree;
import edu.iastate.utils.gui.JListEx;

/**
 * GUI definitions of mapping panel
 * @author Jie Bao
 * @since 1.0 2004-10-03
 */
abstract public class MappingPanelGUI extends IndusPane
{
    public MappingPanelGUI(IndusGUI parent)
    {
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

    JPanel            mappingPanel              = new JPanel();
    JPanel            commandPanel              = new JPanel();
    JButton           btnAddMapping             = new JButton();
    JButton           btnNewMapping             = new JButton();

    //AbstractListModel mappingRuleListModel      = new DefaultListModel();
    JListEx           lstBridges                = new JListEx(new Vector());

    JScrollPane       jScrollPane1              = new JScrollPane();
    JScrollPane       jScrollPane2              = new JScrollPane();
    JScrollPane       jScrollPane3              = new JScrollPane();
    JScrollPane       jScrollPane4              = new JScrollPane();

    JPanel            mapping_LeftLeft          = new JPanel();
    JPanel            mapping_LeftRight         = new JPanel();

    TypedTree         tree1                     = new TypedTree();
    TypedTree         tree2                     = new TypedTree();
    JPanel            mapping_botPanel          = new JPanel();
    JButton           btnSaveMapping            = new JButton();
    JComboBox         mappingFileList           = new JComboBox();
    JPanel            mapping_topPanel          = new JPanel();

    DataSourceMapping myMapping                 = new DataSourceMapping(
                                                        "Unknown1", "Unknown2",
                                                        null);                     // data structure for mapping

    DefaultListModel  mappingConnectorListModel = new DefaultListModel();
    JList             mappingConnectorsList     = new JList(
                                                        mappingConnectorListModel);

    JSplitPane        jSplitPane1               = new JSplitPane(
                                                        JSplitPane.VERTICAL_SPLIT);
    JButton           btnExportXML              = new JButton("Export XML");
    JButton           btnImportXML              = new JButton("Import XML");
    JLabel            schema1                   = new JLabel();
    JLabel            schema2                   = new JLabel();
    JButton           btnDeleteMapping          = new JButton();
    JButton           btnUpdateMappingList      = new JButton();
    JButton           refreshBtn1               = new JButton();
    JButton           refreshBtn2               = new JButton();
    JPanel            mapping_LeftMid           = new JPanel();
    JButton           btnNewConnector           = new JButton();
    JPanel            mapping_rightUpper        = new JPanel();
    JButton           btnValidate               = new JButton();

    void jbInit() throws Exception
    {
        this.setLayout(new BorderLayout());
        //setup the tree
        //initializes tree

        // 1- the treepane
        mappingPanel.setLayout(new BorderLayout());
        // 1.1.1 tree 1
        mapping_LeftLeft.setLayout(new BorderLayout());

        schema1.setText(" ");
        refreshBtn1.setText("Reload");
        refreshBtn1.setEnabled(false);
        mapping_rightUpper.setLayout(new BorderLayout());
        btnValidate.setText("Validate Mapping");

        mapping_LeftLeft.add(jScrollPane1, BorderLayout.CENTER);
        mapping_LeftLeft.add(schema1, java.awt.BorderLayout.NORTH);
        mapping_LeftLeft.add(refreshBtn1, java.awt.BorderLayout.SOUTH);

        jScrollPane4.getViewport().add(mappingConnectorsList, null);
        mapping_topPanel.add(mapping_LeftLeft, null);

        //1.1.2 the mapping choices
        mapping_LeftMid.setLayout(new BorderLayout());
        btnNewConnector.setText("New...");

        mapping_LeftMid.add(jScrollPane4, java.awt.BorderLayout.CENTER);
        mapping_topPanel.setLayout(new GridLayout());
        mappingConnectorsList
                .setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        mappingConnectorsList.setSelectedIndex(0);
        mappingConnectorsList.setCellRenderer(new ConnectorListCellRenderer());

        jScrollPane4.setBorder(BorderFactory.createTitledBorder("Constraints"));
        mapping_topPanel.add(mapping_LeftMid);

        //1.1.3 tree 2
        schema2.setText(" ");

        mapping_LeftRight.setLayout(new BorderLayout());
        mapping_LeftRight.add(jScrollPane2, BorderLayout.CENTER);
        mapping_LeftRight.add(schema2, java.awt.BorderLayout.NORTH);
        mapping_LeftRight.add(refreshBtn2, java.awt.BorderLayout.SOUTH);

        refreshBtn2.setText("Reload");
        refreshBtn2.setEnabled(false);
        mapping_topPanel.add(mapping_LeftRight, null);

        // 1.2 right panel
        // 1.2.1 mapping list
        mapping_botPanel.setLayout(new BorderLayout());
        jScrollPane3.getViewport().add(lstBridges, null);
        jScrollPane3.setBorder(BorderFactory
                .createTitledBorder("Mapping Rules"));

        //1.2.2 save button
        btnSaveMapping.setText("Save Mapping");
        mapping_botPanel.add(mapping_rightUpper, java.awt.BorderLayout.NORTH); // 2- the command panel - buttons
        btnAddMapping.setText("Add Mapping Rule");
        btnDeleteMapping.setText("Delete Mapping");
        btnUpdateMappingList.setText("Update List");
        btnNewMapping.setText("New Mapping");
        
        commandPanel.add(btnNewMapping, null);
        commandPanel.add(btnSaveMapping);
        commandPanel.add(btnDeleteMapping);
        commandPanel.add(btnValidate);
        commandPanel.add(btnExportXML, null);
        commandPanel.add(btnImportXML, null);
        
        jSplitPane1.setOneTouchExpandable(true);
        jSplitPane1.add(mapping_botPanel, JSplitPane.BOTTOM);
        jSplitPane1.add(mapping_topPanel, JSplitPane.TOP);
        mappingPanel.add(jSplitPane1, BorderLayout.CENTER);

        // 3 - put all together
        this.add(mappingPanel, BorderLayout.CENTER);
        this.add(commandPanel, BorderLayout.SOUTH);
        mapping_botPanel.add(jScrollPane3, java.awt.BorderLayout.CENTER);
        mapping_rightUpper.add(mappingFileList, java.awt.BorderLayout.CENTER);
        mapping_rightUpper
                .add(btnUpdateMappingList, java.awt.BorderLayout.EAST);
        mapping_LeftMid.add(btnNewConnector, java.awt.BorderLayout.NORTH);
        mapping_LeftMid.add(btnAddMapping, java.awt.BorderLayout.SOUTH);
        for (int i = 0; i < myMapping.defaultConnectors.length; i++)
        {
            mappingConnectorListModel.add(i, myMapping.defaultConnectors[i]);
        }
    }
    
    // 2006-06-28 Jie Bao
    void setInfo(String s )
    {
        jScrollPane3.setBorder(BorderFactory.createTitledBorder(s));
    }

    void clearMapping()
    {
        lstBridges.setListData(new Vector());
        myMapping.clear();
        btnAddMapping.setEnabled(false);
    }

    class ConnectorListCellRenderer extends DefaultListCellRenderer
    {
        public Component getListCellRendererComponent(JList list, Object value,
                int index, boolean isSelected, boolean cellHasFocus)
        {
            Component retValue = super.getListCellRendererComponent(list,
                    value, index, isSelected, cellHasFocus);
            Connector c = (Connector) value;
            if (c.equals(SimpleConnector.EQU))
            {
                setIcon(IndusConstants.iconEqu);
            }
            else if (c.equals(SimpleConnector.UNEQU))
            {
                setIcon(IndusConstants.iconUnequ);
            }
            else if (c.equals(SimpleConnector.INTO))
            {
                setIcon(IndusConstants.iconInto);
            }
            else if (c.equals(SimpleConnector.ONTO))
            {
                setIcon(IndusConstants.iconOnto);
            }
            else if (c.equals(SimpleConnector.COMP))
            {
                setIcon(IndusConstants.iconComp);
            }
            else if (c.equals(SimpleConnector.INCOMP))
            {
                setIcon(IndusConstants.iconIncomp);
            }
            else
            {
                setIcon(IndusConstants.iconUser);
            }

            return retValue;
        }
    }

    // 2006-06-24 Jie Bao
    public void resetPanel()
    {
        jSplitPane1.setDividerLocation(0.5);
    }

}
