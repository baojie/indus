package edu.iastate.anthill.indus.reasoner;

import java.util.Vector;
import java.util.Map;
import java.util.*;

/**
 * @author Jie Bao
 * @since 2005-04-12
 */
public class DistributedReasoner
{
    ShortBridgeReasoner metaReasoner = new ShortBridgeReasoner();

    public ShortBridgeReasoner getReasoner()
    {
        return metaReasoner;
    }

    /**
     * Merge r2 into metaReasoner. r2 should be kept as it is [no change after
     *   the merge operation]
     * @param r2 ShortBridgeReasoner
     * @since 2005-04-12
     */
    public void mergeReasoner(ShortBridgeReasoner r)
    {
        System.out.println("\n*** Merge a reasoner ***");
        ShortBridgeReasoner localCopy = (ShortBridgeReasoner) r.clone();

        // step 0: copy rule set
        metaReasoner.ruleSet.addAll(localCopy.ruleSet);

        // step 1: merge ec
        Map term2EqualClass2 = localCopy.term2EqualClass;
        //System.out.println(term2EqualClass2);

        Set mergedEC = new HashSet();
        Set newTerms = term2EqualClass2.keySet();
        for (Iterator it = newTerms.iterator(); it.hasNext(); )
        {
            String term = (String) it.next();
            System.out.println("=====  " + term + "  =====");
            EqualClass EC1 = metaReasoner.getEqualClass(term);
            EqualClass EC2 = (EqualClass) term2EqualClass2.get(term);
            if (!mergedEC.contains(EC2))
            {
                // merge EC2 into EC1
                EC1.merge(EC2, metaReasoner.term2EqualClass);
                mergedEC.add(EC2);
                Set merged = EC1.mergeAllLoops(metaReasoner.term2EqualClass);
                if (merged != null)
                {
                    mergedEC.addAll(merged);
                }
            }
        }
        // step 1.5: build ECG
        metaReasoner.ECG.cleanNonRoot();

        // step 2: build CC from EC
        metaReasoner.buildCCFromEC();

        // step 3: add CC and ICC from given reasoner
        for (Iterator it = newTerms.iterator(); it.hasNext(); )
        {
            String term = (String) it.next();
            CompClass metaCC = metaReasoner.getCompClass(term);
            CompClass rCC = localCopy.getCompClass(term);
            Set compTerms = rCC.getAllCompatibleTerms();
            for (Iterator cc = compTerms.iterator(); cc.hasNext(); )
            {
                String ccterm = (String) cc.next();
                metaCC.addCompatible(metaReasoner.getEqualClass(ccterm));
            }
            Set incompTerms = rCC.getAllIncompatibleTerms();
            for (Iterator cc = incompTerms.iterator(); cc.hasNext(); )
            {
                String iccterm = (String) cc.next();
                metaCC.addIncompatible(metaReasoner.getEqualClass(iccterm));
            }
        }

    }

}
