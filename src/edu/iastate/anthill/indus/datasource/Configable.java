package edu.iastate.anthill.indus.datasource;

import java.io.Serializable;

/**
 * @author Jie Bao , baojie@cs.iastate.edu
 * @since 
 */
public interface Configable extends Serializable
{
    public void fromXML(String xml);

    public String toXML();

    public String toString();

    public String toText();

    public void fromText(String text);
}
