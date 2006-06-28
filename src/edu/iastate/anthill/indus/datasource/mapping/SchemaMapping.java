package edu.iastate.anthill.indus.datasource.mapping;

import edu.iastate.anthill.indus.datasource.Configable;
import edu.iastate.anthill.indus.reasoner.*;

/**
 * Mapping between two schema
 * @author Jie Bao
 * @since 1.0 2004-10-16
 */
public class SchemaMapping
    extends InMemoryOntologyMapping implements Configable
{
    public SchemaMapping(String from, String to)
    {
        super(from, to);
        type = BridgeRule.SCHEMA_COMMENT;
    }

    /**
     * Get the inverse mapping
     * @return SchemaMapping
     * @since 2005-03-29
     */
    public SchemaMapping getSchemaMirror()
    {
        SchemaMapping inverse = new SchemaMapping(this.to, this.from);
        for (int i = 0; i < mapList.size(); i++)
        {
            inverse.mapList.add( ( (BridgeRule) mapList.elementAt(i)).getMirror());
        }
        return inverse;
    }

}
