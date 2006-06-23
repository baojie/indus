/**
 * 
 */
package edu.iastate.anthill.indus.datasource.mapping;

/**
 * The mapping is saved in a relational database
 * 
 * @author baojie
 * @since 2006-06-22
 *
 */
public class DbOntologyMapping extends OntologyMapping
{
    public DbOntologyMapping(String from, String to)
    {
        super(from,to);        
    }

    /* (non-Javadoc)
     * @see edu.iastate.anthill.indus.datasource.Configable#fromXML(java.lang.String)
     */
    public void fromXML(String xml)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see edu.iastate.anthill.indus.datasource.Configable#toXML()
     */
    public String toXML()
    {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see edu.iastate.anthill.indus.datasource.Configable#toText()
     */
    public String toText()
    {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see edu.iastate.anthill.indus.datasource.Configable#fromText(java.lang.String)
     */
    public void fromText(String text)
    {
        // TODO Auto-generated method stub
        
    }
}
