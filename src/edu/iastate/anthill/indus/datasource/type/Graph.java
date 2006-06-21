package edu.iastate.anthill.indus.datasource.type;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.StringTokenizer;
import java.util.Vector;

import java.awt.Point;
import javax.swing.JPanel;

import edu.iastate.anthill.indus.datasource.Configable;

import edu.iastate.utils.string.TaggedText;

class Node
{
    double x;
    double y;

    double dx;
    double dy;

    boolean fixed; // fixed position

    String lbl; // label

    Node()
    {}

    Node(double x, double y, String lbl, boolean fixed)
    {
        this.x = x;
        this.y = y;
        this.lbl = lbl;
        this.fixed = fixed;
    }

    boolean equals(Node n)
    {
        return lbl.equals(n.lbl);
    }

    Point getPosition()
    {
        return new Point( (int) x, (int) y);
    }

}

class Edge
{
    Node from;
    Node to;
    String lbl = "";

    double len;
}

public class Graph
    extends DataType implements Configable
{
    Vector nodes = new Vector();
    Vector edges = new Vector();

    /**
     * @author Jie Bao
     * @since 1.0 2004-10-18
     */
    public Graph(String name)
    {
        this.name = name;
        this.supertype = null;
    }

    public Graph()
    {
        this.supertype = null;
    }

    protected Node getNode(int i)
    {
        return (Node) nodes.elementAt(i);
    }

    protected Edge getEdge(int i)
    {
        return (Edge) edges.elementAt(i);
    }

    private Node addNode(String lbl)
    {
        Node n = new Node();
        n.x = 10 + 380 * Math.random();
        n.y = 10 + 380 * Math.random();
        n.lbl = lbl;
        nodes.add(n);
        //Debug.trace("Add Node "+lbl);
        return n;
    }

    void addNode(Node n)
    {
        nodes.add(n);
    }

    protected Node findOrCreateNode(String lbl)
    {
        for (int i = 0; i < nodes.size(); i++)
        {
            if (getNode(i).lbl.equals(lbl))
            {
                return getNode(i);
            }
        }
        return addNode(lbl);
    }

    protected Edge findEdge(String from, String to)
    {
        Iterator i = edges.iterator();
        while (i.hasNext())
        {
            Edge e = (Edge) i.next();
            if (e.from.lbl.equals(from) && e.to.lbl.equals(to))
            {
                return e;
            }
        }
        return null;
    }

    /**
     * Add a edge. If node is not in node set yet, it will be added into the set
     * @param from String
     * @param to String
     * @param len int
     * @param label String
     * @author Jie Bao
     * @since 1.0 2004-10-18
     */
    protected void addEdge(String from, String to, int len, String label)
    {
        if (findEdge(from, to) == null && !from.equals(to))
        {
            Edge e = new Edge();
            e.from = findOrCreateNode(from);
            e.to = findOrCreateNode(to);
            e.len = len;
            e.lbl = label;
            edges.add(e);
        }
    }

    protected void addEdge(String from, String to)
    {
        addEdge(from, to, 50, "isa");
    }

    public void renameEdge(Edge e, String newlabel)
    {
        e.lbl = newlabel;
    }

    protected Edge deleteEdge(String from, String to)
    {
        Iterator i = edges.iterator();
        while (i.hasNext())
        {
            Edge e = (Edge) i.next();
            if (e.from.lbl.equals(from) && e.to.lbl.equals(to))
            {
                i.remove();
                return e;
            }
        }
        return null;
    }

    protected Edge deleteEdge(Edge e)
    {
        return deleteEdge(e.from.lbl, e.to.lbl);
    }

    protected Node findNode(String lbl)
    {
        for (int i = 0; i < nodes.size(); i++)
        {
            if (getNode(i).lbl.equals(lbl))
            {
                return getNode(i);
            }
        }
        return null;
    }

    protected Node deleteNode(String lbl)
    {
        Node n = findNode(lbl);
        if (n != null)
        {
            // delete the node and associated edges
            Iterator i = edges.iterator();
            while (i.hasNext())
            {
                Edge e = (Edge) i.next();
                if (e.from.equals(n) || e.to.equals(n))
                {
                    i.remove();
                }
            }
            nodes.remove(n);
        }

        return n;
    }

    protected Node deleteNode(Node n)
    {
        return deleteNode(n.lbl);
    }

    public void renameNode(Node n, String newlabel)
    {
        n.lbl = newlabel;
    }

    public void fromXML(String datatypeinXML)
    {
        try
        {
            NumberFormat nf = NumberFormat.getInstance();

            TaggedText root = new TaggedText();
            root.fromXML(datatypeinXML);

            Vector allChildren = root.getAllChildren();

            for (int i = 0; i < allChildren.size(); i++)
            {

                TaggedText t = (TaggedText) allChildren.elementAt(i);
                //System.out.println(i + "(" + allChildren.size() + "): " +  t.getTag());

                if (t.getTag().equals("typename"))
                {
                    name = t.getContent().elementAt(0).toString();
                }
                else if (t.getTag().equals("subTypeOf"))
                {
                    supertype = t.getContent().elementAt(0).toString();
                }
                else if (t.getTag().equals("allnodes"))
                {
                    Vector allNodes = t.getAllChildren();
                    //System.out.println(allNodes);

                    for (int j = 0; j < allNodes.size(); j++)
                    {
                        TaggedText thisNode = (TaggedText) allNodes.elementAt(j);
                        Vector nodeSetting = thisNode.getAllChildren();
                        //System.out.println(nodeSetting);
                        String x = "", y = "", lbl = "", fixed = "";
                        for (int k = 0; k < nodeSetting.size(); k++)
                        {
                            TaggedText para = (TaggedText) nodeSetting.
                                elementAt(k);

                            if (para.getTag().equals("x"))
                            {
                                x = para.getContent().elementAt(0).toString();
                            }
                            else if (para.getTag().equals("y"))
                            {
                                y = para.getContent().elementAt(0).toString();
                            }
                            else if (para.getTag().equals("label"))
                            {
                                lbl = para.getContent().elementAt(0).toString();
                            }
                            else if (para.getTag().equals("fixed"))
                            {
                                fixed = para.getContent().elementAt(0).toString();
                            }
                        } // if k
                        Node newNode = new Node(nf.parse(x).doubleValue(),
                                                nf.parse(y).doubleValue(),
                                                lbl,
                                                fixed.equalsIgnoreCase("true"));
                        this.addNode(newNode);

                    } // if j
                } // if allnoes
                else if (t.getTag().equals("alledges"))
                {
                    Vector allEdges = t.getAllChildren();
                    //System.out.println(allEdges);
                    for (int j = 0; j < allEdges.size(); j++)
                    {
                        TaggedText thisEdge = (TaggedText) allEdges.elementAt(j);
                        Vector edgeSetting = thisEdge.getAllChildren();
                        String from = "", to = "", lbl = "", len = "";
                        for (int k = 0; k < edgeSetting.size(); k++)
                        {
                            TaggedText para = (TaggedText) edgeSetting.
                                elementAt(k);

                            if (para.getTag().equals("from"))
                            {
                                from = para.getContent().elementAt(0).toString();
                            }
                            else if (para.getTag().equals("to"))
                            {
                                to = para.getContent().elementAt(0).toString();
                            }
                            else if (para.getTag().equals("label"))
                            {
                                lbl = para.getContent().elementAt(0).toString();
                            }
                            else if (para.getTag().equals("len"))
                            {
                                len = para.getContent().elementAt(0).toString();
                            }
                        } // for k
                        this.addEdge(from, to, nf.parse(len).intValue(), lbl);
                    } // for j
                } // if all edges
            } // for allchilren
        }
        catch (NumberFormatException ex)
        {
            ex.printStackTrace();
        }
        catch (ParseException ex)
        {
            ex.printStackTrace();
        }
    }

    public String toXML()
    {
        return getTaggedText().toXML();
    }

    public String toText() {
        return toXML();
    }

    public void fromText(String text) {
        fromXML(text);        
    }

    
    /**
     * @author Jie Bao
     * @since 1.0 2004-10-18
     */
    TaggedText getTaggedText()
    {
        NumberFormat nf = NumberFormat.getInstance();

        TaggedText root = new TaggedText("type", null);
        root.addChild("typename", name);
        root.addChild("subTypeOf", supertype);

        TaggedText nodeRoot = new TaggedText("allnodes", null);
        for (int i = 0; i < nodes.size(); i++)
        {
            Node n = getNode(i);
            TaggedText thisNode = new TaggedText("node", null);
            thisNode.addChild("x", nf.format(n.x));
            thisNode.addChild("y", nf.format(n.y));
            thisNode.addChild("label", n.lbl);
            thisNode.addChild("fixed", n.fixed + "");
            nodeRoot.addChild(thisNode);
        }
        root.addChild(nodeRoot);

        TaggedText edgeRoot = new TaggedText("alledges", null);
        for (int i = 0; i < edges.size(); i++)
        {
            Edge e = getEdge(i);
            TaggedText thisEdge = new TaggedText("edge", null);
            thisEdge.addChild("from", e.from.lbl);
            thisEdge.addChild("to", e.to.lbl);
            thisEdge.addChild("label", e.lbl);
            thisEdge.addChild("len", nf.format(e.len));
            edgeRoot.addChild(thisEdge);
        }
        root.addChild(edgeRoot);
        return root;
    }

    public JPanel getEditorPane()
    {
        //return new GraphEditor(this);
        return null;
    }

    public String print()
    {
        StringBuffer buf = new StringBuffer();
        Iterator i = edges.iterator();
        while (i.hasNext())
        {
            Edge e = (Edge) i.next();
            buf.append(e.from.lbl + "->" + e.to.lbl + ", ");
        }
        return buf.toString();
    }

    /**
     * @author Jie Bao
     * @since 1.0 2004-10-18
     */
    public String toString()
    {
        return print();
    }

    public String getInformation()
    {
        String info = "";
        if (getSupertype() != null)
        {
            info += "is subtype of '" + getSupertype() + "'";
        }
        return info;
    }

    //String edges = "x-y,y-z"; //getParameter("edges");
    //String center = "x"; //"a1"; //getParameter("center");
    private void readGraph(String edges, String center) throws
        NumberFormatException
    {
        for (StringTokenizer t = new StringTokenizer(edges, ",");
             t.hasMoreTokens(); )
        {
            String str = t.nextToken();
            int i = str.indexOf('-');
            if (i > 0)
            {
                int len = 50;
                int j = str.indexOf('/');
                if (j > 0)
                {
                    len = Integer.valueOf(str.substring(j + 1)).intValue();
                    str = str.substring(0, j);
                }
                addEdge(str.substring(0, i), str.substring(i + 1), len,
                        "isa");
            }
        }

        if (center != null)
        {
            Node n = findOrCreateNode(center);
            n.fixed = true;
        }
    }

    /**
     * return nodes with no parent
     * @return HashSet
     * @author Jie Bao
     * @since 1.0 2004-10-18     */
    public HashSet getRoots()
    {
        HashSet rootSet = new HashSet();
        Iterator it = nodes.iterator();
        while (it.hasNext())
        {
            Node key = (Node) it.next();
            HashSet s = getParent(key);
            if (s.isEmpty())
            {
                rootSet.add(key);
            }
        }
        return rootSet;
    }

    /**
     * Return parent nodes of a node
     * @param n Node
     * @return HashSet
     * @since 2004-11-01
     */
    public HashSet getParent(Node n)
    {
        HashSet parentSet = new HashSet();
        Iterator it = edges.iterator();
        while (it.hasNext())
        {
            Edge e = (Edge) it.next();
            if (e.to == n)
            {
                parentSet.add(e.from);
            }
        }
        return parentSet;
    }

    /**
     * Return child nodes of a node
     * @param n Node
     * @return HashSet
     * @since 2004-11-01
     */
    public HashSet getChild(Node n)
    {
        HashSet parentSet = new HashSet();
        Iterator it = edges.iterator();
        while (it.hasNext())
        {
            Edge e = (Edge) it.next();
            if (e.from == n)
            {
                parentSet.add(e.to);
            }
        }
        return parentSet;
    }

    public HashSet getAllAncestor(Node n)
    {
        HashSet parentSet = getParent(n);
        int newSize = parentSet.size();
        int oldSize = 0;

        while (newSize != oldSize)
        {
            Iterator it = parentSet.iterator();
            oldSize = parentSet.size();
            while (it.hasNext())
            {
                Node ancestor = (Node) it.next();
                parentSet.add(getParent(ancestor));
            }
            newSize = parentSet.size();
        }
        return parentSet;
    }

    /**
     * return nodes with no child
     * @return HashSet
     * @author Jie Bao
     * @since 1.0 2004-10-18
     */
    public HashSet getLeaves()
    {
        HashSet leafSet = new HashSet();
        Iterator it = nodes.iterator();
        while (it.hasNext())
        {
            Node key = (Node) it.next();
            HashSet s = getChild(key);
            if (s.isEmpty())
            {
                leafSet.add(key);
            }
        }
        return leafSet;
    }

    /**
     * @author Jie Bao
     * @since 1.0 2004-10-18
     */
    public static void test()
    {
        Graph dag = new Graph("TestDAG");

        dag.addEdge("A", "B");
        dag.addEdge("C", "A");

        Graph dag1 = new Graph("AAA");
        dag1.fromXML(dag.toXML());
        System.out.println(dag);
        System.out.println(dag.toXML());
        System.out.println(dag1);
        System.out.println(dag1.toXML());
    }


}
