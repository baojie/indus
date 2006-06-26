package edu.iastate.anthill.indus.datasource.type;

import javax.swing.JPanel;

import edu.iastate.anthill.indus.datasource.Configable;

/**
 * @author Jie Bao
 * @since 1.0
 */
public class DAG
    extends Graph implements Configable
{
    public DAG()
    {
        this.supertype = "DAG";
    }


    public DAG(String name)
    {
        super(name);

        this.name = name;
        this.supertype = "DAG";
    }

    public JPanel getEditorPane()
    {
        //return new GraphEditor(this);
        return null;
    }    
}
