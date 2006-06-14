package edu.iastate.anthill.indus.reasoner;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;

import edu.iastate.utils.gui.JTreeEx;
import java.util.Map;
import java.util.HashMap;
import edu.iastate.anthill.indus.tree.TypedNode;

/**
 * The set of EqualClass trees
 * @author Jie Bao
 * @since 2005-04-11
 */
public class EqualClassGraph
{
    Set forest = new HashSet(); // of EqualClass, for root set

    public void clear()
    {
        forest.clear();
    }

    void addRoot(EqualClass ec)
    {
        // check ec's offspring , and remove them from root set
        Set subSet = ec.getAllSubEqualClasses();
        forest.removeAll(subSet);
        // add the EC as root
        forest.add(ec);
    }

    void removeRoot(EqualClass ec)
    {
        // remove the EC and add all its direct children as root
        forest.addAll(ec.getSubClass());
        forest.remove(ec);
    }

    /**
     * Get all EC in this forest
     * @return Set
     */
    Set getAllEC()
    {
        Set allECSet = new HashSet();
        for (Iterator it = forest.iterator(); it.hasNext(); )
        {
            EqualClass ec = (EqualClass) it.next();
            allECSet.addAll(ec.getAllSubEqualClasses());
        }
        return allECSet;
    }

    public Object clone(Map oldEC2newEC)
    {
        Set allECSet = getAllEC();

        // make clone of all old ec, create a map from old ecs to new ecs
        oldEC2newEC.clear();
        EqualClassGraph newECF = new EqualClassGraph();
        for (Iterator it = allECSet.iterator(); it.hasNext(); )
        {
            EqualClass oldEC = (EqualClass) it.next();
            EqualClass newEC = (EqualClass) oldEC.clone();
            oldEC2newEC.put(oldEC, newEC);
            if (this.forest.contains(oldEC))
            {
                newECF.addRoot(newEC);
            }
        }

        // update super and sub class
        for (Iterator it = allECSet.iterator(); it.hasNext(); )
        {
            EqualClass oldEc = (EqualClass) it.next();
            EqualClass ec = (EqualClass) oldEC2newEC.get(oldEc);

            for (Iterator sup = oldEc.getSuperClass().iterator(); sup.hasNext(); )
            {
                EqualClass supOne = (EqualClass) sup.next();
                EqualClass newSupOne = (EqualClass) oldEC2newEC.get(supOne);
                if (newSupOne != null)
                {
                    ec.removeSuperClass(supOne);
                    ec.addSuperClass(newSupOne);
                }
            }
            for (Iterator sub = oldEc.getSubClass().iterator(); sub.hasNext(); )
            {
                EqualClass subOne = (EqualClass) sub.next();
                EqualClass newSubOne = (EqualClass) oldEC2newEC.get(subOne);
                if (newSubOne != null)
                {
                    ec.removeSubClass(subOne);
                    ec.addSubClass(newSubOne);
                }
            }
        }
        return newECF;
    }

    public Object clone()
    {
        Map oldEC2newEC = new HashMap();
        return clone(oldEC2newEC);
    }

    public JTreeEx visualize()
    {
        JTreeEx tree = new JTreeEx();
        DefaultTreeModel model = (DefaultTreeModel) tree.getModel();

        EqualClass top = new EqualClass();
        top.addTerm("Thing");
        TypedNode root = new TypedNode(top);
        model.setRoot(root);

        for (Iterator it = forest.iterator(); it.hasNext(); )
        {
            EqualClass ec = (EqualClass) it.next();
            model.insertNodeInto( (MutableTreeNode) ec.visualize(), root,
                                 root.getChildCount());
        }

        return tree;
    }

    /**
     * cleanNonRoot
     */
    public void cleanNonRoot()
    {
        for (Iterator it = forest.iterator(); it.hasNext(); )
        {
            EqualClass ec = (EqualClass) it.next();
            if (!ec.getSuperClass().isEmpty())
            {
                it.remove();
            }
        }
    }

    /**
     * @since 2005-04-13
     */
    public void removeLoop(Map term2EqualClass)
    {
        // start from any root
    }

}
