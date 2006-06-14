package edu.iastate.anthill.indus.datasource.mapping;

import edu.iastate.anthill.indus.datasource.Configable;

public abstract class Connector
    implements Configable
{
    public String name;
    Connector(String newname)
    {
        name = newname;
    }

    public String toString()
    {
        return name;
    }

    /**
     * Export to XML
     * @return String
     * @author Jie Bao
     * @since 2004-10-16
     */
    abstract public String toXML();

    /**
     * Get mirror connector, eg. INTO -> ONTO
     * @return Connector
     * @since 2004-10-17
     */
    abstract public Connector getMirror();

    /**
     * read from XML
     * @param textXML String
     * @author Jie Bao
     * @since 2004-10-16
     */
    abstract public void fromXML(String textXML);

    /**
     * If two connectors are the same
     * @param c2 Connector
     * @return boolean
     * @author Jie Bao
     * @since 2004-10-15
     */
    public boolean equals(Connector c2)
    {
        if (name == null)
        {
            return false;
        }
        return name.equals(c2.name);
    }

}
