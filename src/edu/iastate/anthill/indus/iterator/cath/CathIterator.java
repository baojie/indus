package edu.iastate.anthill.indus.iterator.cath;

import edu.iastate.anthill.indus.IndusDB;

/**
 * Iterator for CATH Protein Structure Classification
 *   http://www.biochem.ucl.ac.uk/bsm/cath/
 *
 * @author Jie Bao
 * @since 1.0 2005-03-03
 */
public class CathIterator
    extends IndusDB
{
    public CathIterator()
    {
    }

    public static void test()
    {
        CathIterator iterator = new CathIterator();

        iterator.connect();
        iterator.clearAllData();
        iterator.loadToDB();
        iterator.disconnect();
    }

    /**
     * loadToDB
     */
    private static void loadToDB()
    {
    }

    /**
     * clearAllData
     */
    private static void clearAllData()
    {
    }
}
