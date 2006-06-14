package edu.iastate.anthill.indus.reasoner;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.tree.TreeNode;

import edu.iastate.anthill.indus.tree.TypedNode;

/**
 * @author Jie Bao
 * @since 2005-04-08
 */
public class EqualClass
{
    private Set termSet = new TreeSet();
    private Set superClass = new HashSet(); // of EqualClass
    private Set subClass = new HashSet(); // of EqualClass

    public Object clone()
    {
        EqualClass newEC = new EqualClass();
        newEC.termSet.addAll(this.termSet);
        newEC.superClass.addAll(this.superClass);
        newEC.subClass.addAll(this.subClass);
        return newEC;
    }

    /**
     * @since 2005-04-08
     * @return String
     */
    public String toString()
    {
        String res = termSet.toString();
        /*res += "\n     Superclass: ";
                 for (Iterator it = superClass.iterator(); it.hasNext(); )
                 {
            EqualClass superOne = (EqualClass) it.next();
            res += superOne.toString();
                 }*/
        /*Iterator it = subClass.iterator();
                 if (it.hasNext())
                 {
            res += " => ";
            while (it.hasNext())
            {
                EqualClass subOne = (EqualClass) it.next();
                res += subOne.termSet.toString();
            }
                 }*/

        return res;
    }

    public void addTerm(String term)
    {
        termSet.add(term);
    }

    /**
     * @since 2005-04-17
     * @param term String
     */
    public void removeTerm(String term)
    {
        termSet.remove(term);
    }

    public void merge(EqualClass ec, Map term2EqualClass)
    {
        if (this == ec)
        {
            return;
        }

        System.out.print("merge: ");
        System.out.print(this +" + " + ec + " -> ");

        termSet.addAll(ec.termSet);

        //System.out.println("SuperClass");
        superClass.addAll(ec.superClass);
        // remove ec from its super classes' subclass,  and add this
        for (Iterator it = ec.superClass.iterator(); it.hasNext(); )
        {
            EqualClass superOne = (EqualClass) it.next();
            superOne.subClass.remove(ec);
            superOne.subClass.add(this);
            superOne.subClass.remove(superOne);
        }

        //System.out.println("SubClass");
        subClass.addAll(ec.subClass);
        // remove ec from its subclasses' super class,  and add this
        for (Iterator it = ec.subClass.iterator(); it.hasNext(); )
        {
            EqualClass subOne = (EqualClass) it.next();
            subOne.superClass.remove(ec);
            subOne.superClass.add(this);
            subOne.superClass.remove(subOne);
        }

        //System.out.println("Remap");
        // remap terms in ec to this
        if (term2EqualClass != null)
        {
            for (Iterator it = ec.termSet.iterator(); it.hasNext(); )
            {
                String term = (String) it.next();
                EqualClass termEC = (EqualClass) term2EqualClass.get(term);
                term2EqualClass.put(term, this);
                if (termEC != this)
                {
                    merge(termEC,term2EqualClass);
                }
            }
        }

        // remove self-circle, if any
        subClass.remove(this);
        superClass.remove(this);

        System.out.println(this);
    }

    // 2005-04-09
    public void merge(Set allClass, Map term2EqualClass)
    {
        for (Iterator it = allClass.iterator(); it.hasNext(); )
        {
            EqualClass oneEC = (EqualClass) it.next();
            this.merge(oneEC, term2EqualClass);
        }
    }

    // 2005-04-09
    /**
     * merge all loop contains this node
     * @param term2EqualClass Map
     * @return Set  - All merged Set, expect this EC, null if not loop is found
     *             Set of EqualClass
     */
    public Set mergeAllLoops(Map term2EqualClass)
    {
        Set allLoop = new HashSet();
        Set loopSet = findOneLoop();
        while (loopSet != null)
        {
            //System.out.println("Merge Loop");
            allLoop.addAll(loopSet);
            merge(loopSet, term2EqualClass);
            loopSet = findOneLoop();
        }
        allLoop.remove(this);
        return loopSet;
    }

    // 2005-04-09
    public Set findOneLoop()
    {
        Set visited = new HashSet();
        return findLoop(this, visited);
    }

    /**
     * Find one loop that contains the checkPoint
     * @since 2005-04-09
     * @param checkPoint EqualClass
     * @return Set
     */
    private Set findLoop(EqualClass checkPoint, Set visited)
    {
        //Set s = new HashSet();
        for (Iterator it = subClass.iterator(); it.hasNext(); )
        {
            // walk to a child node
            EqualClass subOne = (EqualClass) it.next();

            // skip visited node
            if (visited.contains(subOne))
            {
                continue;
            }
            // make the child as visited
            visited.add(subOne);

            // walk back to the check point
            if (subOne == checkPoint)
            {
                Set path = new HashSet();
                path.add(subOne);
                return path;
            }
            else
            {
                // move ahead from the child node
                Set path = subOne.findLoop(checkPoint, visited);
                if (path != null)
                {
                    // the child find the checkpoint : a loop!
                    path.add(subOne);
                    return path;
                }
            }

        }
        // can't find the check point, return
        return null;
    }

    public void addSubClass(EqualClass ec)
    {
        subClass.add(ec);
        ec.superClass.add(this);
    }

    public void removeSubClass(EqualClass ec)
    {
        subClass.remove(ec);
        ec.superClass.remove(this);
    }

    public void addSuperClass(EqualClass ec)
    {
        superClass.add(ec);
        ec.subClass.add(this);
    }

    public void removeSuperClass(EqualClass ec)
    {
        superClass.remove(ec);
        ec.subClass.remove(this);
    }

    /**
     * Get all terms in this term set and all terms in ancenstors' term sets
     * @return HashSet - Set of String
     * @since 2005-04-09
     */
    HashSet getAllSuperTerms()
    {
        HashSet all = new HashSet(this.termSet);
        for (Iterator it = superClass.iterator(); it.hasNext(); )
        {
            EqualClass superOne = (EqualClass) it.next();
            all.addAll(superOne.getAllSuperTerms());
        }
        return all;
    }

    /**
     * Get all ancenstor ECs,  includes the EC in question itself
     * @return HashSet - Set of EqualClass
     * @since 2005-04-11
     */
    HashSet getAllSuperEqualClasses()
    {
        HashSet all = new HashSet();
        all.add(this);
        for (Iterator it = superClass.iterator(); it.hasNext(); )
        {
            EqualClass superOne = (EqualClass) it.next();
            all.addAll(superOne.getAllSuperEqualClasses());
        }
        return all;
    }

    /**
     * Get all offspring EC, includes the EC in question itself
     * @return HashSet
     * @since 2005-04-11
     */
    HashSet getAllSubEqualClasses()
    {
        HashSet all = new HashSet();
        all.add(this);
        for (Iterator it = subClass.iterator(); it.hasNext(); )
        {
            EqualClass subOne = (EqualClass) it.next();
            all.addAll(subOne.getAllSubEqualClasses());
        }
        return all;
    }

    public Set getSubClass()
    {
        return subClass;
    }

    public Set getSuperClass()
    {
        return superClass;
    }

    public Set getTermSet()
    {
        return termSet;
    }

    /**
     * Return a tree for the EC and all it offsprings
     * @since 2004-04-11
     * @return TreeNode
     */
    TreeNode visualize()
    {
        TypedNode root = new TypedNode(this);
        for (Iterator it = subClass.iterator(); it.hasNext(); )
        {
            EqualClass ec = (EqualClass) it.next();
            root.insert( (TypedNode) ec.visualize(), root.getChildCount());
        }
        return root;
    }

    /**
     * Query if this is a offspring of given ec.
     *    If this is in the Sub set of given ec, return true, else return false;
     * @param ec EqualClass
     * @return boolean
     * @since 2005-04-12
     */
    public boolean isOffspring(EqualClass ec)
    {
        Set sub = ec.getAllSubEqualClasses();
        return sub.contains(this);
    }

    /**
     * Query if this is an ancestor of given ec.
     *    If this is in the Super set of given ec, return true, else return false;
     * @param ec EqualClass
     * @return boolean
     * @since 2005-04-12
     */
    public boolean isAncestor(EqualClass ec)
    {
        Set superSet = ec.getAllSuperEqualClasses();
        return superSet.contains(this);
    }
}
