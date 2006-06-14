package edu.iastate.anthill.indus.panel;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;

import edu.iastate.anthill.indus.agent.InfoReader;

import edu.iastate.utils.gui.GUIUtils;
import edu.iastate.utils.gui.MyComboBoxEditor;
import edu.iastate.utils.gui.MyComboBoxRenderer;
import java.awt.event.ActionEvent;

public abstract class SchemaPanelGUI
    extends IndusPane
{
    public SchemaPanelGUI()
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

    JPanel tablePanel = new JPanel();
    JPanel northPanel = new JPanel();
    JPanel southPanel = new JPanel();

    JButton btnNewAttr = new JButton();
    JButton btnDeleteAttr = new JButton();
    JButton btnSave = new JButton();

    DefaultTableModel model = new DefaultTableModel();
    JTable schemaTable = new JTable(model);
    JComboBox schemaList = new JComboBox();
    JLabel jLabel1 = new JLabel();
    JButton btnNew = new JButton();

    JButton updateTypeList = new JButton();
    int typeIndex;
    int attIndex;
    int dbTypeIndex;

    void jbInit() throws Exception
    {
        createTable();
        this.setLayout(new BorderLayout());

        btnNewAttr.setText("New Attribute");
        btnDeleteAttr.setText("Delete Attribute");
        btnSave.setText("Save Schema");

        southPanel.setLayout(new GridLayout());
        jLabel1.setHorizontalAlignment(SwingConstants.RIGHT);
        jLabel1.setText("Saved: ");
        btnNew.setText("New Schema");

        updateTypeList.setText("Update Type List");
        btnExport.setText("Export Schema");

        nameLabel.setText("Schema:");
        btnDelete.setText("Delete Schema");
        btnSchemaList.setText("Update ");

        northPanel.add(nameLabel, null);
        northPanel.add(btnNewAttr, null);
        northPanel.add(btnDeleteAttr, null);
        northPanel.add(updateTypeList, null);

        this.add(southPanel, BorderLayout.SOUTH);
        southPanel.add(btnSave, null);
        southPanel.add(btnNew, null);
        southPanel.add(btnExport, null);
        southPanel.add(btnDelete, null);
        southPanel.add(jLabel1, null);
        southPanel.add(schemaList, null);
        southPanel.add(btnSchemaList);
        northPanel.setLayout(new GridLayout());

        tablePanel.setLayout(new BorderLayout());
        this.add(tablePanel, BorderLayout.CENTER);
        tablePanel.add(new JScrollPane(schemaTable), null);
        this.add(northPanel, BorderLayout.NORTH);
    }

    JMenuItem menuEditType = new JMenuItem("Edit Type...");
    JButton btnExport = new JButton();
    JLabel nameLabel = new JLabel();
    JButton btnDelete = new JButton();

    private void createTable()
    {
        // Create 3 columns
        model.addColumn("AttributeName");
        model.addColumn("Type");
        model.addColumn("DatabaseType");

        typeIndex = schemaTable.getColumn("Type").getModelIndex();
        attIndex = schemaTable.getColumn("AttributeName").getModelIndex();
        dbTypeIndex = schemaTable.getColumn("DatabaseType").getModelIndex();

        schemaTable.setRowHeight(24);
        schemaTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        schemaTable.setRowSelectionAllowed(true);

        // Set the combobox editor to "Type"
        onUpdateTypeList(null);

        final JPopupMenu menu = new JPopupMenu();

// Create and add a menu item

        menu.add(menuEditType);

// Set the component to show the popup menu
        schemaTable.addMouseListener(new MouseAdapter()
        {
            public void mousePressed(MouseEvent evt)
            {
                // left click to select row
                int row = schemaTable.rowAtPoint(evt.getPoint());
                schemaTable.setRowSelectionInterval(row, row);

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

        model.addTableModelListener(new MyTableModelListener(schemaTable));
    }

    public boolean modified = false;

    public void onUpdateTypeList(ActionEvent evt)
    {
        String types[] = InfoReader.getAllType();
        if (types == null)
        {
            types = new String[]
                {
                "string"};
        }

        GUIUtils.updateComboBox(typeList, types);
        typeList.setSelectedIndex(0);

        schemaTable.getColumn("Type").setCellEditor(new MyComboBoxEditor(
            typeList));
        schemaTable.getColumn("Type").setCellRenderer(new MyComboBoxRenderer(
            types));
        //schemaTable.repaint();
    }

    JComboBox typeList = new JComboBox();
    JButton btnSchemaList = new JButton();

    public class MyTableModelListener
        implements TableModelListener
    {
        JTable table;

        // It is necessary to keep the table since it is not possible
        // to determine the table from the event's source
        MyTableModelListener(JTable table)
        {
            this.table = table;
        }

        public void tableChanged(TableModelEvent e)
        {
            //Debug.systrace(this,"Table Changed");
            modified = true;
        }
    }

}
