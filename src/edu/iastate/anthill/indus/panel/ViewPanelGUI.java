package edu.iastate.anthill.indus.panel;

import java.awt.BorderLayout;
import java.awt.event.InputEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;

import com.borland.jbcl.layout.VerticalFlowLayout;

abstract public class ViewPanelGUI
    extends IndusPane
{
    public ViewPanelGUI()
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

    final JPopupMenu menu = new JPopupMenu();
    JMenuItem menuEditSchema = new JMenuItem("Edit Local Schema...");

    private void jbInit() throws Exception
    {
        //viewEditPane.addItem("My Local Schema", cbLocalSchema);

        btnAddDataSource.setText("Add A Source");
        jLabel1.setText("Registered Views: ");
        btnUpdateList.setText("Update View List");
        btnDelete.setText("Delete View");
        btnSave.setText("SaveView");
        btnCreateView.setText("Create New");
        btnExport.setText("ExportXML");
        viewEditPane.setLayout(verticalFlowLayout1);

        this.add(viewEditPane, BorderLayout.CENTER);
        this.add(controlPane, BorderLayout.SOUTH);
        controlPane.add(jLabel1);
        controlPane.add(viewList);
        controlPane.add(btnCreateView);
        controlPane.add(btnUpdateList);
        this.add(northPane, java.awt.BorderLayout.NORTH);
        northPane.add(btnSave);
        northPane.add(btnDelete);
        northPane.add(btnAddDataSource);
        northPane.add(btnExport);

        menu.add(menuEditSchema);
        popup();

    }

    void popup()
    {
        // Set the component to show the popup menu
// 2005-03-26
        this.addMouseListener(new MouseAdapter()
        {
            boolean isPopupTrigger(MouseEvent evt)
            {
                return (evt.getModifiers() & InputEvent.BUTTON3_MASK) != 0 &&
                    evt.getClickCount() == 1;
            }

            public void mousePressed(MouseEvent evt)
            {
                String name = (String) cbLocalSchema.getSelectedItem();
                if (isPopupTrigger(evt) && name != null)
                {
                    menu.show(evt.getComponent(), evt.getX(), evt.getY());
                }
            }

            public void mouseReleased(MouseEvent evt)
            {
                String name = (String) cbLocalSchema.getSelectedItem();
                if (isPopupTrigger(evt) && name != null)
                {
                    menu.show(evt.getComponent(), evt.getX(), evt.getY());
                }
            }
        });
    }

    //LabelledItemPanel viewEditPane = new LabelledItemPanel();
    JPanel viewEditPane = new JPanel();

    JPanel controlPane = new JPanel();
    JComboBox cbLocalSchema = new JComboBox();
    JButton btnCreateView = new JButton();

    JComboBox viewList = new JComboBox();

    public boolean modified = false;
    JLabel jLabel1 = new JLabel();
    JButton btnUpdateList = new JButton();
    JPanel northPane = new JPanel();

    JButton btnSave = new JButton();
    JButton btnExport = new JButton();
    JButton btnDelete = new JButton();
    JButton btnAddDataSource = new JButton();
    VerticalFlowLayout verticalFlowLayout1 = new VerticalFlowLayout();

}
