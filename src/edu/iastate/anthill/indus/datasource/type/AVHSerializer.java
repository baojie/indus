package edu.iastate.anthill.indus.datasource.type;

import javax.swing.tree.TreeSelectionModel;

import org.apache.xpath.XPathAPI;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import edu.iastate.anthill.indus.datasource.DataSourceNode;
import edu.iastate.anthill.indus.tree.TypedNode;
import edu.iastate.anthill.indus.tree.TypedTree;

import edu.iastate.utils.log.Config;
import edu.iastate.anthill.indus.tree.SortTreeModel;

/**
 * @author Jie Bao
 * @since 2005-03-31
 */
class AVHSerializer
    extends Config
{
    public AVHSerializer()
    {
        super(null, false);
    }

    protected void createNew()
    {
        createBlankNew();
        Node root = addChildTag(configXML, "type");
    }

    protected void objToXML(Object obj)
    {
        AVH avh = (AVH) obj;

        createBlankNew();
        Element root = (Element) addChildTag(configXML, "type");

        if (avh.name != null)
        {
            setProperty(root, "typename", avh.name);
        }

        if (avh.supertype != null)
        {
            setProperty(root, "subTypeOf", avh.supertype);

            if (avh.supertype.equals("AVH"))
            {
                if (avh.nameOfOrder != null)
                {
                    setProperty(root, "ordername", avh.nameOfOrder);
                }
                if (avh.template != null)
                {
                    setProperty(root, "template", avh.template);
                }

                // write the tree
                Element tree = (Element) addChildTag(root, "tree");
                TypedNode rootNode = avh.treeAVT.getTop();
                Node2Doc(rootNode, tree);
            }
        }
    }

    /**
     *
     * @param node TypedNode
     * @param docParent Element
     * @return Element
     * @author Jie Bao
     * @since 2005-04-01
     */
    private Element Node2Doc(TypedNode node, Element docParent)
    {
        //System.out.println(node + "  <-  " + docParent);
        Element docNode = (Element) addChildTag(docParent, "node");
        setProperty(docNode, "name", node.getUserObject().toString());
        if (node.getComment() != null)
        {
            setProperty(docNode, "comment", node.getComment().toString());
        }

        if (node.getChildCount() > 0)
        {
            // add all its children
            Element childNode = (Element) addChildTag(docNode, "children");
            for (int i = 0; i < node.getChildCount(); i++)
            {
                TypedNode kid = (TypedNode) node.getChildAt(i);
                Node2Doc(kid, childNode);
            }
        }
        return docNode;
    }

    protected void xmlToObj(Object obj)
    {
        AVH avh = (AVH) obj;
        // get doc root
        Element root = findNode(null, "/type");
        // get jtree properties
        avh.name = getProperty(root, "typename");
        avh.supertype = getProperty(root, "subTypeOf");
        avh.nameOfOrder = getProperty(root, "ordername");
        avh.template = getProperty(root, "template");

        // create jtree
        avh.treeAVT = new TypedTree();

        // get doc tree top
        Element tree = findNode(null, "/type/tree/node");
        String rootName = this.getProperty(tree, "name");
        String rootComment = this.getProperty(tree, "comment");
        DataSourceNode rootNode = new DataSourceNode(rootName,
            DataSourceNode.AVH, avh.name, rootComment);

        // recursively build the jtree
        Doc2Node(tree, rootNode, avh.name);

        // set jtree GUI properties
        avh.treeAVT.setTop(rootNode);
        avh.treeAVT.setModel(new SortTreeModel(rootNode));
        avh.treeAVT.setEditable(false);
        avh.treeAVT.setShowsRootHandles(true);
        avh.treeAVT.getSelectionModel().setSelectionMode
            (TreeSelectionModel.SINGLE_TREE_SELECTION);
        avh.treeAVT.addDrageDropListener(new MyDropListener(avh));
        avh.treeAVT.setEnableDragDrop(true);
    }

    /**
     * Doc2Node
     *
     * @param tree Element
     * @return TypedNode
     * @since 2005-04-01
     * @author Jie Bao
     */
    private void Doc2Node(Element docParent, TypedNode treeParent,
                          String typeName)
    {
        try
        {
            NodeList nodelist = XPathAPI.selectNodeList(docParent,
                "children/node");

            for (int i = 0; i < nodelist.getLength(); i++)
            {
                Element elem = (Element) nodelist.item(i);
                String name = this.getProperty(elem, "name");
                String comment = this.getProperty(elem, "comment");
                //System.out.println(name + ":" + comment);
                DataSourceNode newNode = new DataSourceNode(name,
                    DataSourceNode.AVH, typeName, comment);
                treeParent.add(newNode);
                Doc2Node(elem, newNode, typeName);
            }
        }
        catch (javax.xml.transform.TransformerException e)
        {
        }
    }
}
