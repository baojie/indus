package edu.iastate.anthill.indus.reasoner;

import java.util.HashSet;
import java.util.Set;
import java.util.Map;
import java.util.Iterator;

/**
 * Compatible / Incompatible class for a term
 *
 * @author Jie Bao
 * @since 2005-04-11
 */
public class CompClass
{
    String term;
    Set compatible = new HashSet(); // set of EqualClass;
    Set incompatible = new HashSet(); // set of EqualClass;

    CompClass(String term)
    {
        this.term = term;
    }

    // 2005-04-12
    public Object clone()
    {
        CompClass newCC = new CompClass(this.term);
        newCC.compatible.addAll(this.compatible);
        newCC.incompatible.addAll(this.incompatible);
        return newCC;
    }

    public void addCompatible(EqualClass ec)
    {
        compatible.add(ec);
    }

    public void removeCompatible(EqualClass ec)
    {
        compatible.remove(ec);
    }

    public void addCompatibleSet(Set ecSet)
    {
        compatible.addAll(ecSet);
    }

    public void addIncompatible(EqualClass ec)
    {
        incompatible.add(ec);
    }

    public void removeIncompatible(EqualClass ec)
    {
        incompatible.remove(ec);
    }

    public void addIncompatibleSet(Set ecSet)
    {
        incompatible.addAll(ecSet);
    }

    /**
     * If an EC is in both compatible and incompatible set
     * there is inconsistency
     *
     * @since 2005-04-11
     * @return boolean
     */
    public boolean isConsistent()
    {
        Set intersection = new HashSet(compatible);
        intersection.retainAll(incompatible);
        information = term + " is both compatible and imcompatible with " +
            intersection + "\nAll Compatible: " + compatible +
            "\nAll Imcompatible: " + incompatible;
        return intersection.isEmpty();
    }

    public String information;

    public String toString()
    {
        return this.term + " , COMP: " + this.compatible + " , INCOMP: " +
            this.incompatible;
    }

    // 2005-04-13
    public void merge(CompClass cc)
    {
        this.compatible.addAll(cc.compatible);
        this.incompatible.addAll(cc.incompatible);
    }

    // 2005-04-13
    Set getAllCompatibleTerms()
    {
        Set allTerms = new HashSet();
        for (Iterator it = compatible.iterator(); it.hasNext(); )
        {
            EqualClass ec = (EqualClass) it.next();
            allTerms.addAll(ec.getTermSet());
        }
        return allTerms;
    }

    // 2005-04-13
    Set getAllIncompatibleTerms()
    {
        Set allTerms = new HashSet();
        for (Iterator it = incompatible.iterator(); it.hasNext(); )
        {
            EqualClass ec = (EqualClass) it.next();
            allTerms.addAll(ec.getTermSet());
        }
        return allTerms;
    }

}
