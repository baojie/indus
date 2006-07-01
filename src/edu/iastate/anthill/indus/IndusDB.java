package edu.iastate.anthill.indus;

import edu.iastate.utils.sql.LocalDBConnection;

/**
 * @author Jie Bao
 * @since 1.0
 */
public class IndusDB
    extends LocalDBConnection
{
    public IndusDB()
    {
    }

    /**
     * 2005-02-17
     */
    public boolean connect(String url)
    {
        return super.connect(IndusConstants.dbDriver, url,
                      IndusConstants.dbUsr,
                      IndusConstants.dbPwd);
    }

}
