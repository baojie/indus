package edu.iastate.anthill.indus.datasource;

/**
 * <p>@author Jie Bao , baojie@cs.iastate.edu</p>
 *
 * <p>@since </p>
 *
 * <p> </p>
 *
 * <p> </p> not attributable
 */
public interface Configable
{
    public void fromXML(String xml);

    public String toXML();

    public String toString();
}
