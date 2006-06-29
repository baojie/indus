package edu.iastate.anthill.indus.panel.query;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.util.Enumeration;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import Zql.ZExpression;
import edu.iastate.anthill.indus.agent.InfoReader;
import edu.iastate.anthill.indus.datasource.schema.Schema;
import edu.iastate.anthill.indus.datasource.view.View;
import edu.iastate.anthill.indus.query.SQLQueryOptimizer;
import edu.iastate.anthill.indus.tree.TypedNode;
import edu.iastate.anthill.indus.tree.TypedTree;
import edu.iastate.utils.gui.GUIUtils;
import edu.iastate.utils.lang.MessageHandler;
import edu.iastate.utils.lang.MessageMap;

/**
 * @since 2005-03-24
 * @author Jie Bao
 */
public class WhereTreePane
    extends JPanel implements MessageHandler
{

    short LOGIC = 0, EXPRESSION = 1;
    SQLBuilderPane builder;
    public WhereTreePane(SQLBuilderPane builder)
    {
        try
        {
            this.builder = builder;
            jbInit();
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }

    TypedNode newLogicNode(String operator)
    {
        return new TypedNode(operator, LOGIC, null);
    }

    TypedNode newExpressionNode(ZExpression exp)
    {
        return new TypedNode(exp, EXPRESSION, null);
    }

    public void createTree()
    {
        TypedNode top = newLogicNode("AND");
        jTree1.setTop(top);

        jTree1.addTreeSelectionListener(new TreeSelectionListener()
        {
            public void valueChanged(TreeSelectionEvent evt)
            {
                // Get all nodes whose selection status has changed
                TreePath[] paths = evt.getPaths();
                if (evt.isAddedPath(0))
                {
                    // This node has been selected
                    TypedNode theNode = (TypedNode) paths[0].
                        getLastPathComponent();
                    //Debug.trace(theNode);
                    if (theNode.getUserObject() instanceof ZExpression)
                    {
                        atomExpression.setExp( (ZExpression) theNode.
                                              getUserObject());
                    }

                }
            }
        });
    }

    private void jbInit() throws Exception
    {
        messageMap();

        atomExpression = new WhereAtomPane();

        this.setLayout(borderLayout1);
        btnAddLogic.setText("Add Logic Clause");
        btnDelete.setText("Delete");
        btnAddExp.setText("Add Expression");
        jPanel1.add(btnAddExp);
        jPanel1.add(btnAddLogic);
        jPanel1.add(btnDelete);
        this.add(jPanel1, java.awt.BorderLayout.SOUTH);

        this.add(jScrollPane1, java.awt.BorderLayout.CENTER);

        this.add(atomExpression, java.awt.BorderLayout.NORTH);
        jScrollPane1.getViewport().add(jTree1);
        jTree1.setEditable(false);
        jTree1.setShowsRootHandles(true);
        jTree1.getSelectionModel().setSelectionMode
            (TreeSelectionModel.SINGLE_TREE_SELECTION);
        model = (  jTree1.getModel());

        createTree();
    }

    DefaultTreeModel model;
    BorderLayout borderLayout1 = new BorderLayout();
    JPanel jPanel1 = new JPanel();
    JButton btnAddLogic = new JButton();
    TypedTree jTree1 = new TypedTree();
    WhereAtomPane atomExpression;
    JButton btnDelete = new JButton();
    JScrollPane jScrollPane1 = new JScrollPane();
    JButton btnAddExp = new JButton();

    public void messageMap()
    {
        try
        {
            MessageMap.mapAction(this.btnAddLogic, this, "onAddLogic");
            MessageMap.mapAction(this.btnAddExp, this, "onAddExp");
            MessageMap.mapAction(this.btnDelete, this, "onDelete");
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }

    }

    public void onAddLogic(ActionEvent e)
    {
        TypedNode theNode = getSelectedNode();
        // Debug.trace(theNode);

        String data[] = new String[]
            {
            "AND", "OR", "NOT"};
        String type = (String) JOptionPane.showInputDialog(null,
            "Choose one", "Input",
            JOptionPane.INFORMATION_MESSAGE, null,
            data, data[0]);
        if (type != null)
        {
            if (theNode != null)
            {
                if (theNode.getUserObject().equals("NOT") &&
                    theNode.getType() == LOGIC && theNode.getChildCount() >= 1)
                {
                    return;
                }

                //Debug.trace(theNode.getParent().getClass());
                TypedNode parent = (TypedNode) theNode.getParent();
                TypedNode newNode = this.newLogicNode(type);

                if (parent != null)
                {
                    model.removeNodeFromParent(theNode);
                    model.insertNodeInto(theNode, newNode,
                                         newNode.getChildCount());

                    //newNode.add(theNode);
                    model.insertNodeInto(newNode, parent, parent.getChildCount());
                    parent.add(newNode);
                    System.out.print(jTree1);

                }
                else
                {
                    model.insertNodeInto(theNode, newNode,
                                         newNode.getChildCount());
                    //newNode.add(theNode);
                    jTree1.setTop(newNode);
                }

            }
            else // create a new root
            {
                TypedNode newNode = this.newLogicNode(type);
                jTree1.setTop(newNode);
            }
            jTree1.revalidate();
            jTree1.repaint();
            builder.sqlDlg.setSQL(builder.generateZQuery().toString());
        }

    }

    public void onAddExp(ActionEvent e)
    {
        TypedNode theNode = getSelectedNode();
        if (theNode != null && theNode.getType() == LOGIC)
        {
            if (theNode.getUserObject().equals("NOT") &&
                theNode.getType() == LOGIC && theNode.getChildCount() >= 1)
            {
                return;
            }
            ZExpression exp = atomExpression.getExp();
            if (exp != null)
            {
                TypedNode newNode = newExpressionNode(exp);
                model.insertNodeInto(newNode, theNode,
                                     theNode.getChildCount());
                jTree1.setSelectionPath(jTree1.getPath(newNode));
                jTree1.revalidate();
                jTree1.repaint();
                builder.sqlDlg.setSQL(builder.generateZQuery().toString());
            }

        }

    }

    TypedNode getSelectedNode()
    {
        TreePath path = jTree1.getSelectionPath();

        if (path != null)
        {
            TypedNode theNode = (TypedNode) path.getLastPathComponent();
            //Debug.trace(theNode);
            return theNode;
        }
        return null;

    }

    /**
     * @since 2005-03-24
     * @param e ActionEvent
     */
    public void onDelete(ActionEvent e)
    {
        TypedNode theNode = getSelectedNode();
        if (theNode != null)
        {
            TypedNode parent = (TypedNode) theNode.getParent();
            if (parent != null)
            {
                jTree1.getModel().removeNodeFromParent( theNode);
            }

            builder.sqlDlg.setSQL(builder.generateZQuery().toString());
            jTree1.repaint();
        }
    }

    public ZExpression generateWhere()
    {
        return toZExpression( (TypedNode) jTree1.getTop());
    }

    ZExpression toZExpression(TypedNode node)
    {
        if (node.getType() == LOGIC)
        {
            ZExpression exp = new ZExpression( (String) node.getUserObject());
            if (node.getChildCount() >= 0)
            {
                for (Enumeration e = node.children(); e.hasMoreElements(); )
                {
                    TypedNode n = (TypedNode) e.nextElement();
                    ZExpression subExp = toZExpression(n);
                    if (subExp != null)
                    {
                        subExp = (ZExpression) SQLQueryOptimizer.removeOrphanAndOr(
                            subExp);
                        exp.addOperand(subExp);
                    }
                }
            }
            exp = (ZExpression) SQLQueryOptimizer.removeOrphanAndOr(exp);
            return exp;
        }
        else if (node.getType() == EXPRESSION)
        {
            return (ZExpression) node.getUserObject();
        }
        return null;
    }

    /**
     * Method initializes the second pane with chosen columns.
     *
     *@param		<B>model</B> reference to DefaultListModel of the JList on FirstPane
     *@return		<B>void</B>
     *
     * Modified by Jie Bao 2005-03-26
     */
    protected void update(DefaultListModel model)
    {
        Object[] objs = model.toArray(); // of TreeColumnNode
        GUIUtils.updateComboBox(atomExpression.field, objs);

        // get the view name [ all column should be from a single view]
        TreeColumnNode col = (TreeColumnNode) objs[0];
        String viewName = col.getTable();
        // get the View object
        View view = InfoReader.readView(viewName);
        if (view != null)
        {
            // get the schema of the view
            String schemaName = view.getLocalSchemaName();
            Schema schema = InfoReader.readSchema(schemaName);

            if (schema != null)
            {
                // get the attribute to avh mapping of the schema
                atomExpression.setAtt2avh(InfoReader.findAttributeToAVHMapping(
                    schema));
                atomExpression.setAtt2type(InfoReader.findAttributeSupertypeMapping(
                    schema));

//System.out.println(atomExpression.att2avh);

            }
        }
    }
}
