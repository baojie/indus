/**
 * 
 */
package edu.iastate.anthill.indus.datasource.mapping;

import edu.iastate.anthill.indus.datasource.Configable;

/**
 * @author baojie
 *
 */
abstract public class OntologyMapping implements Configable
{
    public String from, to;
    
    public OntologyMapping(String from, String to)
    {
        this.from = from;
        this.to = to;
    }
}
