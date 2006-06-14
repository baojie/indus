package edu.iastate.anthill.indus.reasoner;

import edu.iastate.anthill.indus.datasource.Configable;
import edu.iastate.anthill.indus.datasource.mapping.BridgeRule;
import edu.iastate.anthill.indus.datasource.mapping.Connector;

// 2004-10-17
public class ShortBridgeRule
{
    String term1, term2;
    Connector connector;
    public ShortBridgeRule(BridgeRule brdg)
    {
        term1 = brdg.fromTerminology + ":" + brdg.fromTerm;
        term2 = brdg.toTerminology + ":" + brdg.toTerm;
        connector = brdg.connector;
    }

    public ShortBridgeRule(String term1, Connector connector, String term2)
    {
        this.term1 = term1;
        this.connector = connector;
        this.term2 = term2;
    }

    public String toString()
    {
        return "[" + term1 + "]" + "[" + connector + "]" + "[" + term2 + "]";
    }

}
