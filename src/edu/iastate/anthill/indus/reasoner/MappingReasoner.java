package edu.iastate.anthill.indus.reasoner;

import java.util.Enumeration;

import javax.swing.tree.DefaultMutableTreeNode;

import edu.iastate.anthill.indus.agent.InfoReader;
import edu.iastate.anthill.indus.datasource.mapping.BridgeRule;
import edu.iastate.anthill.indus.datasource.mapping.DataSourceMapping;
import edu.iastate.anthill.indus.datasource.mapping.OntologyMapping;
import edu.iastate.anthill.indus.datasource.mapping.SimpleConnector;
import edu.iastate.anthill.indus.datasource.type.AVH;

/**
 * @author Jie Bao
 * @since 2005-04-11
 */
public class MappingReasoner
    extends ShortBridgeReasoner
{
    /**
     * If the ontology mapping is consistent
     * Limitation: we only handle SimpleConnector, no numeric connectors
     * @return boolean
     * @author Jie Bao
     * @since 2004-10-17
     *        2005-04-11 complete
     */
    public boolean isConsistent(DataSourceMapping dsm)
    {
        addDataSourceMapping(dsm);
        buildReasoner();
        return isConsistent();
    }

    private void addDataSourceMapping(DataSourceMapping dsm)
    {
        if (dsm.schemaMapping != null)
        {
            addOntologyMapping(dsm.schemaMapping);
        }
        for (int i = 0; i < dsm.avhMappingList.size(); i++)
        {
            OntologyMapping om = (OntologyMapping) dsm.avhMappingList.elementAt(
                i);
            addOntologyMapping(om);

            AVH from = (AVH) InfoReader.readDataType(om.from,false);
            AVH to = (AVH) InfoReader.readDataType(om.to,false);
            addAVH(from);
            addAVH(to);
        }
    }

    void addOntologyMapping(OntologyMapping om)
    {
        for (int i = 0; i < om.mapList.size(); i++)
        {
            BridgeRule rule = (BridgeRule) om.mapList.elementAt(i);
            if (rule.connector instanceof SimpleConnector)
            {
                addRule(rule.getShort());
            }
        }
    }

    void addAVH(AVH ontology)
    {
        String name = ontology.getName();
        Enumeration e = ontology.getTreeAVH().getTop().preorderEnumeration();
        while (e.hasMoreElements())
        {
            DefaultMutableTreeNode nn = (DefaultMutableTreeNode) e.nextElement();
            if (nn.getParent() != null)
            {
                DefaultMutableTreeNode parent = (DefaultMutableTreeNode) nn.
                    getParent();
                String term = name + ":" + nn.getUserObject().toString();
                String termParent = name + ":" +
                    parent.getUserObject().toString();

                ShortBridgeRule rule = new ShortBridgeRule(term,
                    SimpleConnector.INTO, termParent);
                addRule(rule);
            }
        }
    }

}
