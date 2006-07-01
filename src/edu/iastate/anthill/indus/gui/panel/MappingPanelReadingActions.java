package edu.iastate.anthill.indus.gui.panel;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import edu.iastate.anthill.indus.agent.InfoReader;
import edu.iastate.anthill.indus.datasource.DataSourceNode;
import edu.iastate.anthill.indus.datasource.mapping.BridgeRule;
import edu.iastate.anthill.indus.datasource.mapping.Connector;
import edu.iastate.anthill.indus.datasource.mapping.DataSourceMapping;
import edu.iastate.anthill.indus.datasource.mapping.InMemoryOntologyMapping;
import edu.iastate.anthill.indus.datasource.mapping.SimpleConnector;
import edu.iastate.anthill.indus.datasource.schema.Schema;
import edu.iastate.anthill.indus.datasource.type.AVH;
import edu.iastate.anthill.indus.datasource.type.DataType;
import edu.iastate.anthill.indus.gui.IndusGUI;
import edu.iastate.anthill.indus.reasoner.MappingReasoner;
import edu.iastate.anthill.indus.tree.TypedNode;
import edu.iastate.anthill.indus.tree.TypedTree;
import edu.iastate.utils.Debug;
import edu.iastate.utils.gui.GUIUtils;
import edu.iastate.utils.lang.MessageHandler;
import edu.iastate.utils.lang.MessageMap;
import edu.iastate.utils.lang.Serialization;

/**
 * Actions in the mapping panel that involve only reading  
 * @author baojie
 * @since 2006-06-25
 *
 */
abstract public class MappingPanelReadingActions extends MappingPanelGUI
        implements MessageHandler
{
    static TypedNode    lastColored1, lastColored2;
    protected JMenuItem itemEditConnector = new JMenuItem("Edit...");
    protected JMenuItem itemAddInverse    = new JMenuItem(
                                              "Add Inverse Function...");

    protected boolean   modified          = false;

    /**
     * Find the type of node, for exmaple schema node of Weather1, Weather2,
     * type node of outlook, people,
     * 
     * @param node
     *            TypedNode
     * @return String
     */
    protected String findNodeType(TypedNode node)
    {
        // DB node
        if (node.getType() == DataSourceNode.DB)
        {
            return node.getLocalName();
        }
        // schema node
        else if ((node instanceof DataSourceNode)
            && (node.getType() == DataSourceNode.ATTRIBUTE))
        {
            TypedNode n = node;
            while (n != null)
            {
                if ((n instanceof DataSourceNode)
                    && (n.getType() == DataSourceNode.DB)) { return ((DataSourceNode) n)
                        .getLocalName(); }
                n = (TypedNode) n.getParent();
            }
        }
        else
        // AVH node
        {
            TypedNode n = node;
            while (n != null)
            {
                // Debug.trace(this, n + " " + (n instanceof DataSourceNode) + "
                // " + n.getType());
                if ((n instanceof DataSourceNode)
                    && (n.getType() == DataSourceNode.AVH))
                {
                    // Debug.trace(this, ((DataSourceNode) n).getDatatype());
                    return ((DataSourceNode) n).getDatatype();
                }
                n = (TypedNode) n.getParent();
            }
        }
        return null;
    }

    public MappingPanelReadingActions(IndusGUI parent)
    {
        super(parent);
        messageMap1(); // mapping reading actions        
    }

    protected void createTreeFromSchema(String schemaName, int whichTree)
    {
        DataSourceNode root = new DataSourceNode(schemaName, TypedNode.DB,
            "Schema");

        Schema schema = InfoReader.readSchema(schemaName);

        if (schema != null)
        {
            Map attList = schema.getAttList();

            for (Iterator it = attList.keySet().iterator(); it.hasNext();)
            {
                // read an attribute
                String attrname = (String) it.next();
                String typeName = (String) attList.get(attrname);

                DataSourceNode node = new DataSourceNode(attrname,
                    TypedNode.ATTRIBUTE, typeName);
                root.add(node);

                // read attribute value, build a tree
                DataType dt = InfoReader.readDataType(typeName, false);
                if (dt != null && dt instanceof AVH)
                {
                    // create a copy
                    dt = (DataType) Serialization.cloneObject(dt);
                    dt.readOnly = true;

                    TypedTree subtree = ((AVH) dt).getTreeAVH();
                    node.add(subtree.getTop());
                }
                else
                {
                    // node.add(new DefaultMutableTreeNode(typeName + typevec));
                }
            }
        }
        else
        {
            Debug.trace(this, "Schema information is not available");
        }

        if (whichTree == 1)
        {
            tree1 = createTree(root, jScrollPane1, 1);
        }
        else
        {
            tree2 = createTree(root, jScrollPane2, 2);
        }
    }

    public abstract TypedTree createTree(TypedNode root, JScrollPane pane,
        final int whichtree);

    protected void readRegisteredMapping(Object defaultSelected)
    {
        String oldSelected = (String) mappingFileList.getSelectedItem();

        Object data[] = InfoReader.getAllMapping();
        if (data == null) { return; }

        GUIUtils.updateComboBox(mappingFileList, data);

        if (defaultSelected != null)
        {
            mappingFileList.setSelectedItem(defaultSelected);
        }
        else if (oldSelected != null)
        {
            mappingFileList.setSelectedItem(oldSelected);
        }
        else if (mappingFileList.getItemCount() > 0)
        {
            mappingFileList.setSelectedIndex(0);
        }

    }

    /**
     * Update aviable mapping list from the server
     * 
     * @param e
     *            ActionEvent
     * @since 2004-10-13
     */
    public void onUpdateMappingList(ActionEvent e)
    {
        readRegisteredMapping(null);
    }

    /**
     * If the mapping is OK?
     * 
     * @param e
     *            ActionEvent
     * @since 2005-04-11
     */
    public void onValidate(ActionEvent e)
    {
        MappingReasoner reasoner = new MappingReasoner();

        boolean good = reasoner.isConsistent(this.myMapping);
        String ok = "The mapping is consistent";
        String bad = "The mapping is inconsistent";

        String info = (good ? ok : bad + "\nDetails: "
            + reasoner.badRuleInformation);
        JOptionPane.showMessageDialog(this, info);

    }

    /**
     * Uupdate the Mapping Rule List from myMapping
     * 
     * @since 2004-10-04
     * @param map
     *            Mapping
     */
    protected void updateMappingRuleList(DataSourceMapping map)
    {
        // Debug.traceWin(this, map.toXML());
        // clear current
        lstBridges.setListData(new Vector());
        // build new rule set
        InMemoryOntologyMapping schemaMap = map.schemaMapping;

        Vector<BridgeRule> all = new Vector<BridgeRule>();

        if (schemaMap != null)
        {
            all.addAll(schemaMap.mapList);
        }
        // Debug.trace(this, "" + map.avhMappingList.size());
        for (InMemoryOntologyMapping avhMap : map.avhMappingList)
        {
            all.addAll(avhMap.mapList);
        }

        lstBridges.setListData(all);
        //System.out.println(lstBridges.getModel().getClass());

        // 2006-06-15 Jie Bao: sort the loaded mapping rules		
        //GUIUtils.sortJList(mappingRuleList);
    }

    /**
     * loadMapping
     * 
     * @param item
     *            String
     * @since 2003-10-03
     */
    protected void loadMapping(String item)
    {
        // read information from the server
        DataSourceMapping m = InfoReader.readMapping(item);
        // Debug.traceWin(this,textXML);
        loadMapping(m);
    }

    protected void loadMapping(DataSourceMapping m)
    {
        if (m != null)
        {
            //System.out.println("Showing mapping " + item);

            //StopWatch w = new StopWatch();
            //w.start();

            clearMapping();
            //w.trace("clear old mapping", true);

            // 1 parse it
            myMapping = m;
            // Debug.traceWin(this, myMapping.toXML());
            // 2 update GUI

            // 2.1 update schema list
            // Debug.trace(this, myMapping.toString());
            if (myMapping.schemaMapping != null)
            {
                String from = myMapping.schemaMapping.from;
                String to = myMapping.schemaMapping.to;
                schema1.setText(from);
                schema2.setText(to);

                createTreeFromSchema(from, 1);
                createTreeFromSchema(to, 2);

                refreshBtn1.setEnabled(true);
                refreshBtn2.setEnabled(true);
                //w.trace("load schemas", true);
            }
            // 2.2 update mapping rule list
            updateMappingRuleList(myMapping);
            //w.trace("update mapping rule list", true);

            // 2.3 load user connectors
            updateConnectorList(myMapping);
            //w.trace("update connector list", true);

            // 2.4 update Info
            this.setInfo("Mapping Rules (" + myMapping.size() + ")");

            modified = false;
        }
        else
        {
            Debug.trace(this, "Mapping '" + m.name
                + "' information is not available");
        }
    }

    /**
     * upateConnectorList - load user defined connectors
     * 
     * @param myMapping
     *            Mapping
     * @since 2004-10-16
     */
    private void updateConnectorList(DataSourceMapping myMapping)
    {
        mappingConnectorListModel.removeAllElements();

        int len = myMapping.defaultConnectors.length;
        for (int i = 0; i < len; i++)
        {
            mappingConnectorListModel.add(i, myMapping.defaultConnectors[i]);
        }

        Vector vec = myMapping.getUserConnectors();
        // Debug.trace(this, vec);
        for (int j = len; j < len + vec.size(); j++)
        {
            mappingConnectorListModel.add(j, vec.elementAt(j - len));
        }
        mappingConnectorsList.setSelectedIndex(0);

        // popup menu

        final JPopupMenu menu = new JPopupMenu();
        menu.add(itemEditConnector);
        menu.add(this.itemAddInverse);

        // Set the component to show the popup menu
        mappingConnectorsList.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent evt)
            {
                showMenu(evt);
            }

            public void mouseReleased(MouseEvent evt)
            {
                showMenu(evt);
            }

            void showMenu(MouseEvent evt)
            {
                int row = mappingConnectorsList.locationToIndex(evt.getPoint());
                mappingConnectorsList.setSelectedIndex(row);

                if (evt.isPopupTrigger()
                    && !(mappingConnectorsList.getSelectedValue() instanceof SimpleConnector))
                {
                    menu.show(evt.getComponent(), evt.getX(), evt.getY());
                }
            }
        });
    }

    public void onRefresh1(ActionEvent e)
    {
        createTreeFromSchema(schema1.getText(), 1);
    }

    public void onRefresh2(ActionEvent e)
    {
        createTreeFromSchema(schema2.getText(), 2);
    }

    /**
     * Change selections on the two trees when selected a mapping rule
     * 
     * @author Jie Bao
     * @since 2006-06-14
     * @see onAddMapping
     * 
     * @param evt
     */
    public void onSelectedRuleChanged(ListSelectionEvent evt)
    {
        // When the user release the mouse button and completes the selection,
        // getValueIsAdjusting() becomes false
        if (!evt.getValueIsAdjusting())
        {
            JList list = (JList) evt.getSource();
            final BridgeRule currentSelected = (BridgeRule) list
                    .getSelectedValue();

            // Get selected item
            if (currentSelected != null)
            {
                // Debug.trace(currentSelected);

                // change the selected connector
                Connector c = currentSelected.connector;

                for (int i = 0; i < mappingConnectorListModel.size(); i++)
                {
                    Connector obj = (Connector) mappingConnectorListModel
                            .elementAt(i);
                    if (c.equals(obj))
                    {
                        //System.out.println(obj);
                        mappingConnectorsList.setSelectedValue(obj, true);
                        break;
                    }
                }

                // change the selected term on tree1
                String term1 = currentSelected.fromTerm;
                String type = currentSelected.fromTerminology;
                if (term1 != null && type != null)
                {
                    TypedNode marked1 = markNode(tree1, type, term1);
                    //Debug.systrace(this, "Tree1 marks " + marked1);
                    if (marked1 != null && lastColored1 != marked1)
                    {
                        if (lastColored1 != null)
                            lastColored1.setColor(Color.black);
                        lastColored1 = marked1;
                    }
                }

                // change the selected term on tree2
                String term2 = currentSelected.toTerm;
                String type2 = currentSelected.toTerminology;
                if (term2 != null && type2 != null)
                {
                    TypedNode marked2 = markNode(tree2, type2, term2);
                    //Debug.systrace(this, "Tree2 marks " + marked2);
                    if (marked2 != null && lastColored2 != marked2)
                    {
                        if (lastColored2 != null)
                            lastColored2.setColor(Color.black);
                        lastColored2 = marked2;
                    }
                }
            }
        }
    }

    /**
     * Mark node with blue
     * @param t Tree
     * @param node String
     * @return TypedNode - the node actually marked
     * @author Jie Bao
     * @since 2004-10-13
     */
    protected TypedNode markNode(TypedTree t, String dataType, String nodeToFind)
    {
        if (nodeToFind != null)
        {
            TypedNode n = (TypedNode) TypedTree.findFirst(t, nodeToFind);
            //System.out.println(n);
            String findType = dataType;

            if (n != null)
            {
                if (n.getType() == DataSourceNode.AVH)
                {
                    findType = findNodeType(n);
                    //System.out.println("AVH - "+findType);
                }
                if (dataType.equals(findType))
                {
                    n.setColor(Color.blue);
                    t.expandNode(n);
                    t.getModel().reload(n);
                }
                return n;
            }
        }
        return null;
    }

    public void messageMap1()
    {
        // button handler
        try
        {
            MessageMap.mapAction(this.btnUpdateMappingList, this,
                "onUpdateMappingList");
            MessageMap.mapAction(this.refreshBtn1, this, "onRefresh1");
            MessageMap.mapAction(this.refreshBtn2, this, "onRefresh2");
            MessageMap.mapAction(this.itemAddInverse, this, "onAddInverse");
            MessageMap.mapAction(this.btnValidate, this, "onValidate");

            // 2006-06-14 Jie Bao, change tree selections
            lstBridges.addListSelectionListener(new ListSelectionListener() {
                public void valueChanged(ListSelectionEvent e)
                {
                    onSelectedRuleChanged(e);
                }
            });
        }
        catch (Exception ex)
        {}
    }
}
