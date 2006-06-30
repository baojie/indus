package edu.iastate.anthill.indus.gui.panel;

import java.awt.Color;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;

import edu.iastate.anthill.indus.datasource.DataSourceNode;
import edu.iastate.anthill.indus.datasource.mapping.BridgeRule;
import edu.iastate.anthill.indus.gui.IndusGUI;
import edu.iastate.anthill.indus.tree.TypedNode;
import edu.iastate.anthill.indus.tree.TypedTree;
import edu.iastate.anthill.indus.tree.TypedTreeRender;
import edu.iastate.utils.Debug;

/**
 * Mapping Definition Panel, actions
 * @author Jie Bao
 * @since 1.0 2004-09-23
 */

public class MappingPanel
    extends MappingPanelWritingActions
{

    public MappingPanel(IndusGUI parent)
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

    /**
     * localInit
     */
    private void localInit()
    {
        tree1 = createTree(null, jScrollPane1, 1);
        tree2 = createTree(null, jScrollPane2, 2);
        readRegisteredMapping(null);

        // add list message listeners
        mappingFileList.addItemListener(new MyMappingListListener());        

        // menu handler
        //btnSaveMapping.setEnabled(false);
        if (mappingFileList.getItemCount() > 0)
        {
            mappingFileList.setSelectedIndex(0);
            loadMapping( (String) mappingFileList.getSelectedItem());
        }

        createMappingRuleList();

    } // climb up and find the root of the AVH tree

    class MyMappingListListener
        implements ItemListener
    {
        MyMappingListListener()
        {
        }

        // This method is called only if a new item has been selected.
        public void itemStateChanged(ItemEvent evt)
        {
            if (evt.getStateChange() == ItemEvent.SELECTED)
            {
                String mappingName = evt.getItem().toString();
                // if the selected is different to current mapping
                if (mappingName != myMapping.getName())
                {
                    promptSave();
                    loadMapping(mappingName);
                    btnSaveMapping.setEnabled(true);
                }
            }            
        }       
    }


    class MyTreeSelectionListener
        implements TreeSelectionListener
    {
        int whichtree;

        MyTreeSelectionListener(int whichtree)
        {
            this.whichtree = whichtree;
        }

        /**
         * handleTree1
         */
        private void handleTree1(TypedNode n1)
        {            
            if (n1 != null)
            {
                //Debug.systrace(this, "Tree 1 selects " + n1.getLocalName());
                lastColored1 = n1;
                if (lastColored1 != null)
                    lastColored1.setColor(Color.black);
                if (lastColored2 != null)
                    lastColored2.setColor(Color.black);
                lstBridges.setSelectedValue(null,false);
                
                // find mapping rule for selected node
                String dataType = null;
                String mappedTo = null;
                BridgeRule b = null;
                if (n1.getType() == DataSourceNode.ATTRIBUTE)
                {
                    // find one mapped node
                    b = myMapping.findSchemaFirstMappedTo(n1.getLocalName());
                    dataType = n1.getLocalName();
                }
                else if (n1.getType() == DataSourceNode.AVH)
                {
                    String AVH1 = findNodeType(n1);
                    b = myMapping.findAVHFirstMappedTo(AVH1, n1.getLocalName());
                    if (b != null)
                    {
                        dataType = b.toTerminology;
                        mappedTo = b.toTerm;
                    }
                }
                
                if (b != null)
                {
                    lstBridges.setSelectedValue(b,true);
                }
                
                //Debug.trace(this, mappedTo);
                if (mappedTo != null && dataType != null)
                {
                    TypedNode marked2 = markNode(tree2, dataType, mappedTo);
                    //Debug.systrace(this, "Tree2 marks " + mappedTo);
                    if (marked2 != null)
                    {
                        lastColored2 = marked2;                        
                    }                    
                }
            }
        }
        
        /**
         * handleTree2
         */
        private void handleTree2(TypedNode n2)
        {            
            
            if (n2 != null)
            {
                //Debug.systrace(this, "Tree 2 selects " + n2.getLocalName());
                lastColored2 = n2;
                if (lastColored1 != null)
                    lastColored1.setColor(Color.black);
                if (lastColored2 != null)
                    lastColored2.setColor(Color.black);
                lstBridges.setSelectedValue(null,false);
                
                // find mapping rule for selected node
                String mappedFrom = null;
                String dataType = null;
                BridgeRule b = null;
                if (n2.getType() == DataSourceNode.ATTRIBUTE)
                {
                    // find one mapped node
                    b = myMapping.findSchemaFirstMappedFrom(n2.getLocalName());
                    dataType = n2.getLocalName();
                }
                else if (n2.getType() == DataSourceNode.AVH)
                {
                    String AVH2 = findNodeType(n2);
                    b = myMapping.findAVHFirstMappedFrom(AVH2, n2.getLocalName());
                    if (b != null)
                    {
                        dataType = b.fromTerminology;
                        mappedFrom = b.fromTerm;
                    }

                }

                if (b != null)
                {
                    lstBridges.setSelectedValue(b,true);
                }
                //Debug.trace(this, mappedTo);
                if (mappedFrom != null && dataType != null)
                {
                    TypedNode marked1 = markNode(tree1, dataType, mappedFrom);
                    if (marked1 != null)
                    {
                        lastColored1 = marked1;
                    }

                }
            }
        }

        /**
         * tree selection event
         *
         * @param e TreeSelectionEvent
         * @version 2004-10-02
         */
        public void valueChanged(TreeSelectionEvent e)
        {
            TypedNode n1 = (TypedNode) tree1.getLastSelectedPathComponent();
            TypedNode n2 = (TypedNode) tree2.getLastSelectedPathComponent();
            enableAddMappingRule(n1, n2);

            if (lastColored1 != null)
            {
                lastColored1.setColor(Color.black);
            }
            if (lastColored2 != null)
            {
                lastColored2.setColor(Color.black);
            }

            if (whichtree == 1)
            {
                handleTree1(n1);
            }
            else
            {
                handleTree2(n2);
            }
            tree1.repaint();
            tree2.repaint();
            //Debug.systrace(this, lastColored1 + ", " + lastColored2);
        }

        /**
         * Determine on what case should a mapping be legible
         *
         * @param n1 TypedNode
         * @param n2 TypedNode
         */
        void enableAddMappingRule(TypedNode n1, TypedNode n2)
        {
            boolean enableMapping = true;
            if (enableMapping)
            {
                // both have selected nodes
                enableMapping = (n1 != null && n2 != null);
            }
            if (enableMapping)
            {
                // nodes are of the same type: schema or AVH
                enableMapping = (n1.getType() == n2.getType());
            }
            if (enableMapping)
            {
                // nodes should not be the root node
                enableMapping = (n1.getType() != TypedNode.DB);
            }

            btnAddMapping.setEnabled(enableMapping);

        }
    }

//create the initial tree
    public TypedTree createTree(TypedNode root, JScrollPane pane,
                                final int whichtree)
    {
        if (root == null)
        {
            root = new DataSourceNode("Schema", TypedNode.DB,
                                      "Schema");
        }
        else
        {
            //root.removeAllChildren();
        }
        //DefaultTreeModel model = null;
        //model = new DefaultTreeModel(root);
        final TypedTree tree = new TypedTree(root);
        // set the tree attributes
        tree.setEditable(false);
        tree.setShowsRootHandles(false);
        tree.setRootVisible(true);
        tree.setCellRenderer(new TypedTreeRender());
        tree.addTreeSelectionListener(new MyTreeSelectionListener(whichtree));
        pane.getViewport().add(tree, null);

        tree.addMouseListener(new MappingPopupMenuListener(this, tree));

        return tree;
    }


    /**
     * createList
     */
    private void createMappingRuleList()
    {
        final JPopupMenu menu = new JPopupMenu();
        JMenuItem itemDelete = new JMenuItem("Delete");
        itemDelete.addActionListener(new DeleteMappingRuleListener());

        menu.add(itemDelete);

        // Set the component to show the popup menu
        lstBridges.addMouseListener(new MouseAdapter()
        {
            public void mousePressed(MouseEvent evt)
            {
                int row = lstBridges.locationToIndex(evt.getPoint());
                lstBridges.setSelectedIndex(row);

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
    }

    public void showDefault(String toSelect)
    {
    }

}
