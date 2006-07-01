package edu.iastate.anthill.indus.gui.panel;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.Vector;

import javax.swing.JOptionPane;

import edu.iastate.anthill.indus.agent.IndusHttpClient;
import edu.iastate.anthill.indus.agent.InfoReader;
import edu.iastate.anthill.indus.agent.InfoWriter;
import edu.iastate.anthill.indus.datasource.DataSourceNode;
import edu.iastate.anthill.indus.datasource.mapping.BridgeRule;
import edu.iastate.anthill.indus.datasource.mapping.Connector;
import edu.iastate.anthill.indus.datasource.mapping.DataSourceMapping;
import edu.iastate.anthill.indus.datasource.mapping.NumericConnector;
import edu.iastate.anthill.indus.datasource.type.AVH;
import edu.iastate.anthill.indus.datasource.type.DataType;
import edu.iastate.anthill.indus.gui.IndusBasis;
import edu.iastate.anthill.indus.gui.IndusGUI;
import edu.iastate.anthill.indus.tree.TypedNode;
import edu.iastate.utils.Debug;
import edu.iastate.utils.Utility;
import edu.iastate.utils.io.FileUtils;
import edu.iastate.utils.lang.MessageHandler;
import edu.iastate.utils.lang.MessageMap;

public abstract class MappingPanelWritingActions extends
        MappingPanelReadingActions implements MessageHandler
{
    public MappingPanelWritingActions(IndusGUI parent)
    {
        super(parent);
        try
        {
            localInit();
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }

    private void localInit()
    {
        messageMap();
    }

    /**
     * Create new mapping
     * 
     * @param e
     *            ActionEvent
     * @since 2004-10-04
     */
    public void onNewMapping(ActionEvent e)
    {
        // ask for the first schema
        // get the list of all registered schema
        Object[] data = InfoReader.getAllSchema();

        String s1 = (String) JOptionPane.showInputDialog(null,
            "Choose schema 1", "Input", JOptionPane.INFORMATION_MESSAGE, null,
            data, data[0]);
        if (s1 != null)
        {
            Vector data_without_s1 = Utility.Array2Vector(data);
            data_without_s1.remove(s1);

            if (data_without_s1.size() == 0)
            {
                JOptionPane
                        .showMessageDialog(this, "No more schema to select!");
                return;
            }

            String s2 = (String) JOptionPane.showInputDialog(null,
                "Choose schema 2", "Input", JOptionPane.INFORMATION_MESSAGE,
                null, data_without_s1.toArray(), data_without_s1.elementAt(0));
            if (s2 != null)
            {
                String mappingName = JOptionPane.showInputDialog(this,
                    "The name of new mapping", s1 + "-" + s2);
                if (mappingName != null) // User clicked OK
                {
                    // valiate "text"
                    if (!mappingName.matches("[\\w\\-]+"))
                    {
                        JOptionPane.showMessageDialog(this,
                            "Name is not legal!");
                        return;
                    }
                }
                else
                {
                    return;
                }

                // Debug.trace(this,mappingName);

                Object[] allMappings = InfoReader.getAllMapping();
                if (allMappings != null)
                {
                    if (Arrays.asList(allMappings).contains(mappingName))
                    {
                        JOptionPane.showMessageDialog(this, "Mapping '"
                            + mappingName + "' already exists");
                        return;
                    }

                    schema1.setText(s1);
                    schema2.setText(s2);
                    createTreeFromSchema(s1, 1);
                    createTreeFromSchema(s2, 2);

                    // clear current mapping
                    clearMapping();
                    myMapping = new DataSourceMapping(s1, s2, mappingName);

                    // update ui
                    mappingFileList.addItem(myMapping.getName());
                    mappingFileList.setSelectedItem(myMapping.getName());

                    // save it
                    save();
                    this.setInfo("Mapping Rules (" + myMapping.size() + ")");
                    modified = false;
                }
                else
                {
                    JOptionPane.showMessageDialog(this,
                        "Cannot create new mapping - reading server fails!");
                }

            }
        }
    }

    /**
     * Add a mapping
     * 
     * @param e
     *            ActionEvent
     * @version 2004-10-02
     */
    public void onAddMapping(ActionEvent e)
    {
        //System.out.println("MappingPanelWritingActions.onAddMapping");

        TypedNode n1 = (TypedNode) tree1.getLastSelectedPathComponent();
        TypedNode n2 = (TypedNode) tree2.getLastSelectedPathComponent();
        Connector conn = (Connector) mappingConnectorsList.getSelectedValue();
        // Debug.trace(this, n1 + " " + conn + " " + n2);

        Connector sel = (Connector) mappingConnectorsList.getSelectedValue();

        // numeric mapping can be allowed only for numeric types
        if (sel instanceof NumericConnector)
        {
            if (n1.getType() == TypedNode.ATTRIBUTE)
            {
                String type1 = ((DataSourceNode) n1).getDatatype();
                String type2 = ((DataSourceNode) n2).getDatatype();
                String supertype1 = IndusHttpClient.getTopSuperType(type1);
                String supertype2 = IndusHttpClient.getTopSuperType(type2);
                if (!AVH.isNumber(type1, supertype1)
                    || !AVH.isNumber(type2, supertype2))
                {
                    JOptionPane.showMessageDialog(this, "Bridge " + sel
                        + "can only be applied on numeric attirbutes");
                    return;
                }
            }
        }

        if (n1 != null && n2 != null && conn != null)
        {
            if (n1.getType() == n2.getType())
            {
                // Debug.trace(this, "" + n1.getType());
                // schema to schema mapping
                if (n1.getType() == DataSourceNode.ATTRIBUTE)
                {
                    BridgeRule t = myMapping.addSchemaMappingItem(n1
                            .getUserObject().toString(), conn, n2
                            .getUserObject().toString());
                    t.type = BridgeRule.SCHEMA_COMMENT;

                    lstBridges.addElement(0, t);
                    //mappingRuleListModel.add(0, t);
                    modified = true;
                }
                else if (n1.getType() == DataSourceNode.AVH) // AVH to AVH
                // mapping
                {
                    String AVH1 = findNodeType(n1);
                    String AVH2 = findNodeType(n2);
                    // Debug.trace(this, AVH1);
                    // Debug.trace(this, AVH2);
                    if (AVH1 != null && AVH2 != null)
                    {
                        BridgeRule t = myMapping.addAVHMappingItem(AVH1, n1
                                .getUserObject().toString(), conn, AVH2, n2
                                .getUserObject().toString());
                        t.type = BridgeRule.AVH_COMMENT;
                        lstBridges.addElement(0, t);
                        //mappingRuleListModel.add(0, t);
                        modified = true;
                    }
                }
            }
        }
        this.setInfo("Mapping Rules (" + myMapping.size() + ")");
        //btnSaveMapping.setEnabled(mappingRuleListModel.getSize() > 0);
    }

    /**
     * prompt to save changes when exit
     * 
     * @author Jie Bao
     * @since 2004-10-12
     */
    public void promptSave()
    {
        // prompt for save
        if (modified && myMapping != null)
        {
            int answer = JOptionPane.showConfirmDialog(null, "Mapping '"
                + myMapping.getName()
                + "' is changed, do you want to update it? ");
            if (answer == JOptionPane.YES_OPTION)
            {
                save();
            }
        }
    }

    public void onSaveMapping(ActionEvent e)
    {
        save();
    }

    protected boolean save()
    {
        // if inverse mapping is not given for some connector, can't save
        // 2005-03-29
        int num = mappingConnectorListModel.getSize();
        for (int i = 0; i < num; i++)
        {
            Connector cc = (Connector) mappingConnectorListModel
                    .getElementAt(i);
            if (cc.getMirror() == null)
            {
                String info = "Connector " + cc.name
                    + " has no inverse mapping, please add it";
                JOptionPane.showMessageDialog(this, info);
                return false;
            }
        }

        if (myMapping != null && myMapping.getName() != null)
        {
            boolean suc = InfoWriter.writeMapping(myMapping);
            if (suc)
            {
                //String selected = (String) mappingFileList.getSelectedItem();
                String info = "Mapping " + myMapping.getName()
                    + " saved successfully";
                JOptionPane.showMessageDialog(this, info);
                modified = false;
                //readRegisteredMapping(selected);
                InfoReader.updateCache(myMapping.getName(),
                    InfoReader.mappingCache, myMapping);
                return true;
            }
        }
        JOptionPane.showMessageDialog(this, "Saving failed!");
        return false;
    }

    /**
     * Export the XML of this mapping
     * 
     * @param e
     *            ActionEvent
     * @since 2004-10-08
     */
    public void onExportMapping(ActionEvent e)
    {
        //String from = myMapping.schemaMapping.from;
        //String to = myMapping.schemaMapping.to;
        //String currentMapping = from + "-" + to;

        String xml = myMapping.toXML();

        if (xml != null)
        {
            //String url = IndusConstants.mappingBasisURL + currentMapping
            //        + ".xml";
            // Debug.trace("This XML file is also available from " + url);
            //System.out.println(xml);
            //IndusBasis.showXML(xml);
            //          save as 
            final String title = "Export to XML";
            final String extension = "xml";
            final String description = "XML Documents";

            String fileName = getFileName(title, extension, description, true);
            // get the ontology from the database
            if (fileName != null)
            {
                try
                {
                    FileUtils.writeFile(fileName, xml);
                }
                catch (Exception e1)
                {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }
            }
        }
        else
        {
            JOptionPane.showMessageDialog(this, "No mapping is defined");
        }
    }

    /**
     * Delete selected mapping
     * 
     * @param e
     *            ActionEvent
     * @since 2004-10-13
     */
    public void onDeleteMapping(ActionEvent e)
    {
        try
        {
            // get the selected mapping
            String mappingName = myMapping.getName();

            // Modal dialog with yes/no button
            int answer = JOptionPane.showConfirmDialog(this,
                "Are you sure to delete mapping '" + mappingName
                    + "'? The deletion can't be undone");
            if (answer != JOptionPane.YES_OPTION) { return; }

            boolean suc = InfoWriter.deleteMapping(mappingName);
            if (suc)
            {
                JOptionPane.showMessageDialog(this, "Mapping '" + mappingName
                    + "' is deleted successfully");
                myMapping.clear();
                this.lstBridges.setListData(new Vector());

                modified = false;
                //readRegisteredMapping(null);
                this.mappingFileList.removeItem(mappingName);
            }
            else
            {
                String info = "Delete Mapping '" + mappingName + "' failed!";
                JOptionPane.showMessageDialog(this, info);
            }
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }

    public void onEditConnector(ActionEvent e)
    {
        NumericConnector t = (NumericConnector) mappingConnectorsList
                .getSelectedValue();
        MappingRuleDialog dlg = new MappingRuleDialog(t);
        dlg.setSize(500, 300);
        dlg.nameField.setEnabled(false);
        // dlg.exprField.setEnabled(false);
        dlg.show();

        if (dlg.action == JOptionPane.YES_OPTION)
        {
            t.expression = dlg.expr;

            modified = true;
            btnSaveMapping.setEnabled(true);

        }
    }

    // 2004-10-16
    public void onNewConnector(ActionEvent e)
    {
        NumericConnector t = null;
        while (true)
        {
            MappingRuleDialog dlg = new MappingRuleDialog(t);
            dlg.setSize(500, 300);
            dlg.show();

            // Debug.trace(this, ""+(dlg.action == JOptionPane.YES_OPTION));
            if (dlg.action == JOptionPane.YES_OPTION)
            {
                t = new NumericConnector(dlg.exprname.trim(), dlg.expr);
                // if the name is used
                Vector existing = new Vector();
                for (int i = 0; i < mappingConnectorListModel.size(); i++)
                {
                    existing.add(mappingConnectorListModel.elementAt(i)
                            .toString());
                }
                // Debug.trace(existing);
                // Debug.trace(dlg.exprname);
                if (existing.contains(dlg.exprname))
                {
                    JOptionPane.showMessageDialog(this, "Name is used!");
                }
                else
                {
                    // Debug.trace(this, dlg.exprname + " " + dlg.expr);
                    mappingConnectorListModel.add(mappingConnectorListModel
                            .getSize(), t);

                    String info = "Connector "
                        + t.name
                        + " is created successfully"
                        + "\nHowever, it will be stored in the mapping file only if it is used by some mapping rules";
                    JOptionPane.showMessageDialog(this, info);
                    // modified = true;
                    return;
                }
            }
            else
            {
                return;
            }
        }
    }

    /**
     * Add inverse function of a numeric connector
     * 
     * @param e
     *            ActionEvent
     * @since 2005-03-28
     */
    public void onAddInverse(ActionEvent e)
    {
        NumericConnector t = (NumericConnector) mappingConnectorsList
                .getSelectedValue();
        NumericConnector inverse = new NumericConnector("Inverse-" + t.name,
            t.inverseExpression);

        MappingRuleDialog dlg = new MappingRuleDialog(inverse);

        dlg.setSize(500, 300);
        dlg.nameField.setEnabled(false);
        // dlg.exprField.setEnabled(false);
        dlg.setVisible(true);

        if (dlg.action == JOptionPane.YES_OPTION)
        {
            t.inverseExpression = dlg.expr;

            modified = true;
            btnSaveMapping.setEnabled(true);

        }
    }

    public void messageMap()
    {
        System.out.println("MappingPanelWritingActions.messageMap()");
        // button handler
        try
        {
            MessageMap.mapAction(this.btnSaveMapping, this, "onSaveMapping");
            MessageMap.mapAction(this.btnAddMapping, this, "onAddMapping");
            MessageMap.mapAction(this.btnNewMapping, this, "onNewMapping");
            MessageMap
                    .mapAction(this.btnDeleteMapping, this, "onDeleteMapping");
            MessageMap.mapAction(this.btnNewConnector, this, "onNewConnector");
            MessageMap.mapAction(this.itemEditConnector, this,
                "onEditConnector");
            MessageMap.mapAction(this.itemAddInverse, this, "onAddInverse");
            MessageMap.mapAction(this.btnExportXML, this, "onExportMapping");
            MessageMap.mapAction(this.btnImportXML, this, "onImportMapping");
            MessageMap.mapAction(this.btnImportText, this,
                "onImportMappingText");
        }
        catch (Exception ex)
        {}
    }

    /**
     * Import mapping from plain text
     * @param e
     * 
     * @author Jie Bao
     * @since 2006-07-01
     */
    public void onImportMappingText(ActionEvent e)
    {
        // save as 
        final String title = "Import from Text";
        final String extension = "txt";
        final String description = "Text Documents";

        importFile(title, extension, description, false);

    }

    /**
     * @author baojie
     * @since 2006-06-28
     * @param e
     */
    public void onImportMapping(ActionEvent e)
    {
        // save as 
        final String title = "Import from XML";
        final String extension = "xml";
        final String description = "XML Documents";

        importFile(title, extension, description, true);
    }

    private void importFile(final String title, final String extension,
        final String description, boolean isXML)
    {
        //      save the current
        promptSave();
        String fileName = getFileName(title, extension, description, false);
        // get the ontology from the database
        if (fileName != null)
        {
            try
            {
                String text = FileUtils.readFile(fileName);

                DataSourceMapping t = new DataSourceMapping();

                if (isXML)
                    t.fromXML(text);
                else t.fromText(text);

                // add to list
                myMapping = t;
                // check if the name is used
                Object used[] = InfoReader.getAllMapping();
                Vector v = new Vector(Arrays.asList(used));
                String newName = t.getName();
                int count = 1;
                while (v.contains(newName))
                {
                    newName = t.getName() + "_" + (count++);
                }
                t.name = newName;

                InfoWriter.writeMapping(t);
                readRegisteredMapping(null);
                loadMapping(t);
                InfoReader.updateCache(t.getName(), InfoReader.mappingCache, t);

            }
            catch (Exception e1)
            {
                e1.printStackTrace();
                Debug.trace("Error in reading the file");
            }
        }
    }

    class DeleteMappingRuleListener implements ActionListener
    {
        public void actionPerformed(ActionEvent evt)
        {
            BridgeRule t = (BridgeRule) lstBridges.getSelectedValue();
            // delete it from mapping
            //System.out.println(t.type);

            if (BridgeRule.SCHEMA_COMMENT.equals(t.type))
            {
                myMapping.deleteSchemaMappingItem(t.fromTerm, t.connector,
                    t.toTerm);
                modified = true;
            }
            else if (BridgeRule.AVH_COMMENT.equals(t.type))
            {
                myMapping.deleteAVHMappingItem(t.fromTerminology, t.fromTerm,
                    t.connector, t.toTerminology, t.toTerm);
                modified = true;
            }
            btnSaveMapping.setEnabled(true);
            lstBridges.removeElement(t);
            setInfo("Mapping Rules (" + myMapping.size() + ")");
        }
    }
}
