package edu.iastate.anthill.indus.reasoner;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import edu.iastate.anthill.indus.datasource.mapping.SimpleConnector;
import java.util.TreeMap;

// 2004-10-17 1.0
// 2005-04-07 2.0
public class ShortBridgeReasoner
{
    HashSet ruleSet = new HashSet(); // Set of ShortBridgeRule
    Map term2EqualClass = new TreeMap(); // String -> EqualClass
    Map term2CompClass = new TreeMap(); // String -> CompClass

    EqualClassGraph ECG = new EqualClassGraph();

    boolean isBuilt = false;

    public Object clone()
    {
        try
        {
            ShortBridgeReasoner newR = new ShortBridgeReasoner();
            // clone rule set
            newR.ruleSet.addAll(this.ruleSet);
            // clone EC and ECF
            Map oldEC2newEC = new HashMap();
            newR.ECG = (EqualClassGraph)this.ECG.clone(oldEC2newEC);

            // make  term2EqualClass mapping
            Iterator ecIt = term2EqualClass.keySet().iterator();
            while (ecIt.hasNext())
            {
                String term = (String) ecIt.next();
                EqualClass one = (EqualClass) term2EqualClass.get(term);
                EqualClass newEC= (EqualClass)  oldEC2newEC.get(one);
                newR.term2EqualClass.put(term,newEC);
            }

            // clone CC and ICC
            Iterator it = this.term2CompClass.keySet().iterator();
            while (it.hasNext())
            {
                String term = (String) it.next();
                CompClass CC = getCompClass(term);
                CompClass newCC = (CompClass) CC.clone();
                newR.term2CompClass.put(term, newCC);
                //System.out.println(newCC);

                for (Iterator cc = CC.compatible.iterator(); cc.hasNext(); )
                {
                    EqualClass one = (EqualClass) cc.next();
                    EqualClass newOne = (EqualClass) oldEC2newEC.get(one);
                    if (newOne != null)
                    {
                        newCC.removeCompatible(one);
                        newCC.addCompatible(newOne);
                    }
                }
                for (Iterator icc = CC.incompatible.iterator(); icc.hasNext(); )
                {
                    EqualClass one = (EqualClass) icc.next();
                    EqualClass newOne = (EqualClass) oldEC2newEC.get(one);
                    if (newOne != null)
                    {
                        newCC.removeIncompatible(one);
                        newCC.addIncompatible(newOne);
                    }
                }
            }

            newR.isBuilt = this.isBuilt;
            return newR;
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            return null;
        }
    }

    public void buildReasoner()
    {
        term2EqualClass.clear();
        term2CompClass.clear();
        ECG.clear();

        buildEqualClass(); // build equal class and ECF
        buildCCFromEC(); // build compatible class inferred from equal class
        buildCompClass(); // build comp clss from rule set

        isBuilt = true;
    }

    public boolean isConsistent()
    {
        // check A=B and A<>B
        Iterator it = ruleSet.iterator();
        while (it.hasNext())
        {
            // the rule
            ShortBridgeRule element = (ShortBridgeRule) it.next();
            if (element.connector.equals(SimpleConnector.UNEQU))
            {
                String t1 = element.term1;
                EqualClass ec1 = getEqualClass(t1);
                String t2 = element.term2;
                EqualClass ec2 = getEqualClass(t2);
                if (ec1 == ec2)
                {
                    badRuleInformation = t1 + " is both equal and unequal to " +
                        t2;
                    return false;
                }
            }
        }

        // check A comp B and A incomp B
        it = this.term2CompClass.keySet().iterator();
        while (it.hasNext())
        {
            String term = (String) it.next();
            CompClass CC = getCompClass(term);
            //System.out.println(CC);
            if (!CC.isConsistent())
            {
                badRuleInformation = CC.information;
                return false;
            }
        }

        return true;
    }

    public String badRuleInformation = "";

    /**
     * Query if term1 is <= (INTO) term2
     * @param term1 String
     * @param term2 String
     * @return boolean
     * @auhtor Jie Bao
     * @since 2005-04-12
     */
    public boolean isInto(String term1, String term2)
    {
        EqualClass ec1 = getEqualClass(term1);
        EqualClass ec2 = getEqualClass(term2);
        if (ec1.equals(ec2) || ec1.isOffspring(ec2))
        {
            return true;
        }
        else
        {
            return false;
        }
    }

    /**
     * @author Jie Bao
     * @since 2005-04-08
     */
    public void buildEqualClass()
    {
        Iterator it = ruleSet.iterator();
        while (it.hasNext())
        {
            // the rule
            ShortBridgeRule element = (ShortBridgeRule) it.next();
            //System.out.println(element);

            // equal classes of the two terms
            String t1 = element.term1;
            EqualClass ec1 = getEqualClass(t1);
            String t2 = element.term2;
            EqualClass ec2 = getEqualClass(t2);

            //System.out.println("Before");
            //System.out.println(ec1);
            //System.out.println(ec2);

            if (element.connector.equals(SimpleConnector.EQU))
            {
                ec1.merge(ec2, term2EqualClass);
                ec2 = null;
            }
            else if (element.connector.equals(SimpleConnector.INTO)) // t1<=t2
            {
                ec1.addSuperClass(ec2);
                ec2.addSubClass(ec1);
            }
            else if (element.connector.equals(SimpleConnector.ONTO)) // t1>=t2
            {
                ec1.addSubClass(ec2);
                ec2.addSuperClass(ec1);
            }

            // ckeck loops . we only need to check ec1
            ec1.mergeAllLoops(term2EqualClass);

            //System.out.println("After");
            //System.out.println(ec1);
            //System.out.println(ec2);
            //System.out.println(term2EqualClass);
        }

        // clean up loop
        //ECG.removeLoop(term2EqualClass);
        ECG.cleanNonRoot();
    }

    /**
     * Build Compatible class with equal class
     *  for each term, it is compatible to all its ancenstor equal elements and
     *  all its offspring elements
     *
     * @since 2005-04-11
     */
    void buildCCFromEC()
    {
        // clear old mapping
        // term2CompClass.clear();

        Iterator it = term2EqualClass.keySet().iterator();
        while (it.hasNext())
        {
            String term = (String) it.next();
            EqualClass EC = getEqualClass(term);
            CompClass CC = getCompClass(term);
            CC.addCompatibleSet(EC.getAllSuperEqualClasses());
            CC.addCompatibleSet(EC.getAllSubEqualClasses());
        }
    }

    void buildCompClass()
    {
        Iterator it = ruleSet.iterator();
        while (it.hasNext())
        {
            // the rule
            ShortBridgeRule element = (ShortBridgeRule) it.next();
            //System.out.println(element);

            // equal/compatible classes of the two terms
            String t1 = element.term1;
            EqualClass ec1 = getEqualClass(t1);
            CompClass cc1 = getCompClass(t1);
            String t2 = element.term2;
            EqualClass ec2 = getEqualClass(t2);
            CompClass cc2 = getCompClass(t2);

            if (element.connector.equals(SimpleConnector.COMP))
            {
                cc1.addCompatibleSet(ec2.getAllSuperEqualClasses());
                cc2.addCompatibleSet(ec1.getAllSuperEqualClasses());
            }
            else if (element.connector.equals(SimpleConnector.INCOMP))
            {
                cc1.addIncompatibleSet(ec2.getAllSubEqualClasses());
                cc2.addIncompatibleSet(ec1.getAllSubEqualClasses());
            }
        }
    }

    /**
     *
     * @param term String
     * @return EqualClass
     * @author Jie Bao
     * @since 2005-04-08
     */
    EqualClass getEqualClass(String term)
    {
        Object obj = term2EqualClass.get(term);
        if (obj != null)
        {
            // return a existing equal class
            return (EqualClass) obj;
        }
        else
        {
            // create a new single element equal class
            EqualClass newEc = new EqualClass();
            newEc.addTerm(term);
            // add a term -> ec mapping item
            term2EqualClass.put(term, newEc);
            // add the new ec as a EC forest root
            ECG.addRoot(newEc);

            return newEc;
        }
    }

    /**
     * Get compatible class
     * @param term String
     * @return CompClass
     * @since 2005-04-11
     */
    CompClass getCompClass(String term)
    {
        Object obj = term2CompClass.get(term);
        if (obj != null)
        {
            // return a existing equal class
            return (CompClass) obj;
        }
        else
        {
            // create a new single element equal class
            CompClass newCc = new CompClass(term);
            term2CompClass.put(term, newCc);
            return newCc;
        }
    }

    // 2004-10-17
    public String toString()
    {
        StringBuffer buf = new StringBuffer();
        Iterator it = ruleSet.iterator();
        while (it.hasNext())
        {
            ShortBridgeRule element = (ShortBridgeRule) it.next();
            buf.append(element.toString() + "\n");
        }
        return buf.toString();
    }

    /**
     * Find applicable rules for given term
     * @param term String
     * @return Set
     * @since 2005-04-12
     */
    Set getApplicableRuleSet(String term)
    {
        Set rs = new HashSet();
        Iterator it = ruleSet.iterator();
        while (it.hasNext())
        {
            ShortBridgeRule element = (ShortBridgeRule) it.next();
            if (element.term1.equals(term) || element.term2.equals(term))
            {
                rs.add(element);
            }
        }
        return rs;
    }

    // 2004-10-17
    public void addRule(ShortBridgeRule b)
    {
        ruleSet.add(b);
    }

}
