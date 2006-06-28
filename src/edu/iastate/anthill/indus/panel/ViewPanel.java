package edu.iastate.anthill.indus.panel;

import java.sql.Connection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

import edu.iastate.anthill.indus.IndusBasis;
import edu.iastate.anthill.indus.IndusGUI;
import edu.iastate.anthill.indus.agent.InfoReader;
import edu.iastate.anthill.indus.agent.InfoWriter;
import edu.iastate.anthill.indus.datasource.schema.Schema;
import edu.iastate.anthill.indus.datasource.view.View;

import edu.iastate.utils.Debug;
import edu.iastate.utils.gui.GUIUtils;
import edu.iastate.utils.lang.MessageHandler;
import edu.iastate.utils.lang.MessageMap;
import edu.iastate.utils.sql.JDBCUtils;

/**
 * @author Jie Bao
 * @since 1.0 2005-03-15
 */
public class ViewPanel
    extends ViewPanelGUI implements MessageHandler
{
    public ViewPanel(IndusGUI parent)
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

    Vector<ViewAtomDateSource> datasource = new Vector<ViewAtomDateSource>();
    View currentView;

    public void promptSave()
    {
        // prompt for save
        if (modified && currentView != null)
        {
            int answer = JOptionPane.showConfirmDialog(null,
                "View '" + currentView.getName() +
                "' is changed, do you want to update it? ");
            if (answer == JOptionPane.YES_OPTION)
            {
                save(currentView);
            }
        }
    }

    public void showDefault(String toSelect)
    {
        // look it up
        if (toSelect != null && toSelect.length() > 0)
        {
            for (int i = 0; i < viewList.getItemCount(); i++)
            {
                if (viewList.getItemAt(i).toString().equals(toSelect))
                {
                    // show it
                    viewList.setSelectedIndex(i);
                    loadView(toSelect);
                    break;
                }
            }
        }

    }

    /**
     * loadView
     *
     * @param toSelect String
     */
    private boolean loadView(String name)
    {
        if (name == null || name.length() == 0)
        {
            return false;
        }

        View newView = InfoReader.readView(name);
        //Debug.trace("load "+datatypeinXML);

        if (newView != null)
        {

            // update UI
            currentView = newView;
            datasource.clear();
            toGUI(currentView);
            // this.nameLabel.setText("Schema: " + name);
            viewList.setSelectedItem(name);
            modified = false;
        }
        else
        {
            Debug.trace(this,
                        "Schema :" + name + " information is not available");
        }
        return true;

    }

    void toGUI(View toDisplay)
    {
        clearGUI();
        viewEditPane.add( new JLabel("My Local Schema: "));
        viewEditPane.add( cbLocalSchema);

        String data[] = InfoReader.getAllSchema();
        // get the list of all registered type
        if (data != null)
        {
            GUIUtils.updateComboBox(cbLocalSchema, data);
        }

        cbLocalSchema.setSelectedItem(toDisplay.getLocalSchemaName());
        viewEditPane.setBorder(BorderFactory.createTitledBorder(toDisplay.
            getName()));

        Map all = toDisplay.getDatasourceMapping();
        for (Iterator it = all.keySet().iterator();
             it.hasNext(); )
        {
            String ds = (String) it.next();
            String mapping = (String) all.get(ds);

            Object allDS[] = InfoReader.getAllDataSource(parent.indusSystemDB.
                db);
            Object allMapping[] = InfoReader.getAllMapping();

            ViewAtomDateSource p = new ViewAtomDateSource(this,allDS, allMapping);
            p.setDS(ds);
            p.setMapping(mapping);
            datasource.add(p);
            viewEditPane.add( p);
            this.validate();
        }
    }

    public void messageMap()
    {
        try
        {
            MessageMap.mapAction(this.btnAddDataSource, this, "onAddDataSource");
            MessageMap.mapAction(this.btnCreateView, this, "onCreateView");
            MessageMap.mapAction(this.btnUpdateList, this, "onUpdateList");
            MessageMap.mapAction(this.btnDelete, this, "onDelete");
            MessageMap.mapAction(this.btnSave, this, "onSave");
            MessageMap.mapAction(this.btnExport, this, "onExport");
            MessageMap.mapAction(this.menuEditSchema, this, "onEditSchema");

        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }

    // 2005-03-26
    public void onEditSchema(ActionEvent evt)
    {
        // goto schema editor panel
        String name = (String) cbLocalSchema.getSelectedItem();
        if (name != null)
        {
            parent.switchToPane(parent.paneSchema, name);
        }
    }

    public void onExport(ActionEvent e)
    {
        if (currentView != null)
        {
            if (currentView.getName() == null)
            {
                return;
            }
            fromGUI();
            String xml = currentView.toXML();

            if (xml != null)
            {
                IndusBasis.showXML(xml);
            }
            else
            {
                JOptionPane.showMessageDialog(
                    this, "No view is defined");
            }
        }
    }

    public void fromGUI()
    {
        currentView.setLocalSchemaName( (String) cbLocalSchema.getSelectedItem());

        currentView.getDatasourceMapping().clear();
        for (int i = 0; i < datasource.size(); i++)
        {
            ViewAtomDateSource p = (ViewAtomDateSource) datasource.elementAt(i);
            if (!p.deleted)
            {
                String ds = p.getDS();
                String mapping = p.getMapping();
                currentView.setMapping(ds, mapping);
            }
        }
    }

    public void onSave(ActionEvent e)
    {
        // read the changes from the GUI to current view, then save the view
        // to the server
        // 1. read gui
        ///Debug.trace(this.currentView.getName());
        if (currentView.getName() == null)
        {
            return;
        }
        fromGUI();

        //Debug.trace(this.currentView.getName());

        // 2. send to the server
        save(currentView);

        // 3. create a local cache
        createLocalCache(currentView);
        //onExport(e);
    }

    boolean createLocalCache(View view)
    {
        Connection cacheDB = parent.indusCacheDB.db;
        String tableName = view.getName();
        //Debug.trace("createLocalCache: " + tableName);

        // delete the table if already exists
        if (JDBCUtils.isTableExist(cacheDB, tableName,false))
        {
            return deleteLocalCache(view);
        }
        // create the table
        String schemaName = view.getLocalSchemaName();
        Schema schema = InfoReader.readSchema(schemaName);

        Map additionalCol = new HashMap();
        additionalCol.put(View.FROM_DATA_SOURCE, "varchar(128)");

        String sql = schema.toSQL(tableName, null, additionalCol);
        return JDBCUtils.updateDatabase(cacheDB, sql);
    }

    boolean deleteLocalCache(View view)
    {
        Connection cacheDB = parent.indusCacheDB.db;
        String tableName = view.getName();
        Debug.trace("deleteLocalCache: " + tableName);

        if (JDBCUtils.isTableExist(cacheDB, tableName,false))
        {
            String sql = "DROP TABLE " + tableName;
            //Debug.trace(sql);
            return JDBCUtils.updateDatabase(cacheDB, sql);
        }
        else
        {
            Debug.trace("No such table - " + tableName);
            return true;
        }
    }

    /**
     * @since 2005-03-23
     * @param e ActionEvent
     */
    public void onDelete(ActionEvent e)
    {
        if (currentView.getName() == null)
        {
            return;
        }
        try
        {
            // get the selected view
            String viewName = currentView.getName();

            // Modal dialog with yes/no button
            int answer = JOptionPane.showConfirmDialog(this,
                "Are you sure to delete view '" + viewName +
                "'? The deletion can't be undone");
            if (answer != JOptionPane.YES_OPTION)
            {
                return;
            }

            boolean suc = InfoWriter.deleteView(viewName);
            if (suc)
            {
                deleteLocalCache(currentView);
                JOptionPane.showMessageDialog(this, "View '" + viewName +
                                              "' is deleted successfully");
                currentView = new View();
                currentView.setName(null);
                modified = false;
                // clear GUI
                clearGUI();
                viewList.removeItem(viewName);

                readRegisteredView(null);
            }
            else
            {
                JOptionPane.showMessageDialog(
                    this, "Delete View '" + viewName + "' failed! --");
            }
        }
        catch (Exception ex)
        {
        }

    }

    void clearGUI()
    {
        datasource.removeAllElements();
        viewEditPane.removeAll();
        viewEditPane.setBorder(BorderFactory.createTitledBorder(""));
        viewEditPane.repaint();
        this.validate();
    }

    void enableButtons(boolean enable)
    {
        btnSave.setEnabled(enable);
        btnExport.setEnabled(enable);
        btnDelete.setEnabled(enable);
        btnAddDataSource.setEnabled(enable);
    }

    /**
     * @since 2005-03-23
     * @param e ActionEvent
     */
    public void onUpdateList(ActionEvent e)
    {
        readRegisteredView(null);
    }

    /**
     * @param defaultSelected Object
     * @since 2005-03-23
     */
    void readRegisteredView(Object defaultSelected)
    {
        String oldSelected = (String) viewList.getSelectedItem();

        String data[] = InfoReader.getAllView();
        // get the list of all registered view
        if (data != null)
        {
            GUIUtils.updateComboBox(viewList, data);
            //System.out.println(res);
            enableButtons(data.length != 0 && data[0].length() > 0);

            if (defaultSelected != null)
            {
                viewList.setSelectedItem(defaultSelected);
            }
            else if (oldSelected != null)
            {
                viewList.setSelectedItem(oldSelected);
            }
            else if (viewList.getItemCount() > 0)
            {
                viewList.setSelectedIndex(0);
            }
        }
    }

    void save(View toSave)
    {
        String name = toSave.getName();
        //validate
        if (!name.matches("[\\w\\-._]+"))
        {
            JOptionPane.showMessageDialog(this, "Name is not legal!");
            return;
        }

        boolean suc = InfoWriter.writeView(toSave);

        if (suc)
        {
            String info = "View " + name + " is successfully saved";
            JOptionPane.showMessageDialog(this, info,
                                          "View", JOptionPane.PLAIN_MESSAGE);
            modified = false;

            // look if the type is in the list
            for (int i = 0; i < viewList.getItemCount(); i++)
            {
                String item = (String) viewList.getItemAt(i);
                if (item.equals(name))
                {
                    return;
                }
            }
            // if a new type , update schema list
            //readRegisteredView(name);
            viewList.addItem(name);
            viewList.setSelectedItem(name);
        }
        else
        {
            JOptionPane.showMessageDialog(this,
                                          "View " + name + " can't be saved",
                                          "View", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void onCreateView(ActionEvent e)
    {
        View newView = new View();

        String used[] = InfoReader.getAllView();
        String name = askForName(used);

        if (name == null)
        {
            return;
        }

        newView.setName(name);
        this.currentView = newView;
        enableButtons(true);

        // save it
        save(currentView);
        createLocalCache(currentView);

        toGUI(newView);
        modified = true;
    }

    public void onAddDataSource(ActionEvent e)
    {
        if (currentView.getName() == null)
        {
            return;
        }
        try
        {
            //Debug.trace("onAddDataSource");
            Object allDS[] = InfoReader.getAllDataSource(
                    IndusBasis.indusSystemDB.db);
            Object allMapping[] = InfoReader.getAllMapping();

            ViewAtomDateSource p = new ViewAtomDateSource(this,allDS, allMapping);
            datasource.add(p);
            //Debug.trace("datasource.size :" + datasource.size());
            viewEditPane.add(p);
            modified = true;
            this.validate();
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }

    private void jbInit() throws Exception
    {
        messageMap();
        enableButtons(false);

        viewList.addItemListener(new MyViewListener());
        readRegisteredView(null);

    }

    class MyViewListener
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
                if (!loadView(item.toString()))
                {
                    // load fails, restore the privious one unless it's the same to be loaded
                    if (currentView != null && currentView.getName() != null)
                    {
                        if (!currentView.getName().equals( (String) item))
                        {
                            viewList.setSelectedItem(currentView.getName());
                        }
                    }
                }
            }
        }
    }

    public static void main(String[] args)
    {
        JFrame testFrame = new JFrame();
        testFrame.getContentPane().add(new ViewPanel(null));
        //testFrame.getContentPane().add(new ViewPane(new IndusGUI()));
        testFrame.setSize(800, 600);
        testFrame.show();

    }
}
