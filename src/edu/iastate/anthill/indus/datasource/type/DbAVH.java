package edu.iastate.anthill.indus.datasource.type;

import edu.iastate.anthill.indus.iterator.DB2Tree;
import edu.iastate.anthill.indus.iterator.DB2TreeFactory;

/**
 * <p>@author Jie Bao , baojie@cs.iastate.edu</p>
 * <p>@since 2005-03-30</p>
 */
public class DbAVH
    extends AVH
{
    public DB2Tree templateTree;

    private DbAVH()
    {}

    public String getInformation()
    {
        return super.getInformation() + ", Template: " + template;
    }

    public void buildEditor()
    {
        editor= new DbAVHEditor(null, this);
    }

    public DbAVH(String name, String treetype, String template)
    {
        super(name, treetype);
        this.template = template;
        templateTree = DB2TreeFactory.buildFromName(template, null);
    }

    public DbAVH(String name, String treetype, String template, DB2Tree db2tree)
    {
        super(name, treetype);
        this.template = template;
        this.templateTree = db2tree;
    }

    public static void main(String[] args)
    {
        DbAVH dbavh = new DbAVH();
        System.out.println(dbavh instanceof DataType);
    }
}
