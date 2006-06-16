package edu.iastate.anthill.indus.agent;

import java.sql.Connection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

import edu.iastate.anthill.indus.datasource.Configable;
import edu.iastate.anthill.indus.datasource.IndusDataSource;
import edu.iastate.anthill.indus.datasource.mapping.DataSourceMapping;
import edu.iastate.anthill.indus.datasource.schema.Schema;
import edu.iastate.anthill.indus.datasource.type.AVH;
import edu.iastate.anthill.indus.datasource.type.DAG;
import edu.iastate.anthill.indus.datasource.type.DataType;
import edu.iastate.anthill.indus.datasource.type.SimpleDataType;
import edu.iastate.anthill.indus.datasource.view.View;
import edu.iastate.anthill.indus.datasource.type.*;

/**
 * Read information stored on the server
 *
 * <p>@author Jie Bao , baojie@cs.iastate.edu</p>
 * <p>@since 2005-03-26</p>
 */
public class InfoReader
    implements IndusCommand
{
    public InfoReader()
    {
    }

    static protected boolean read(String command, String name,
                                  Configable newObj)
    {
        String xml = IndusHttpClient.getDetails(command, name);
        //Debug.trace(xml);

        if (xml != null)
        {
            newObj.fromXML(xml);
            //Debug.trace(newObj.toXML());
            return true;
        }
        return false;
    }

    public static DataSourceMapping readMapping(String name)
    {
        DataSourceMapping newObj = new DataSourceMapping();
        read(CMD_GET_MAPPING_DETAILS, name, newObj);
        return newObj;
    }

    public static View readView(String name)
    {
        View newObj = new View();
        read(CMD_GET_VIEW_DETAILS, name, newObj);
        return newObj;
    }

    public static Schema readSchema(String name)
    {
        Schema newObj = new Schema(name);
        read(CMD_GET_SCHEMA_DETAILS, name, newObj);
        return newObj;
    }
    
    static HashMap<String,DataType> dataTypeCache = new HashMap<String,DataType>(); 

    /**
     * read/store datatype in cache 
     * @author baojie
     * @since 2006-06-15
     * @param name
     * @return
     */
    public static DataType readDataType(String name)
    {	
    	DataType d= dataTypeCache.get(name);
    	if (d == null)
    	{
    		d = readDataTypeNative(name);
    		dataTypeCache.put(name,d);
    	}
    	return d;
    }
    
    /**
     * Update datatype cache
     * @author baojie
     * @since 2006-06-15
     * @param name
     * @param d
     */
    public static void updateDataTypeCache(String name, DataType d)
    {
    	dataTypeCache.put(name,d);
    }
    
    
    /**
     * read the data type given its XML
     * @author baojie
     * @since 2006-06-15
     * @param name
     * @param datatypeinXML
     * @return
     */
    public static DataType readDataTypeNative(String name, String datatypeinXML)
    {
    	DataType newType = null;
        if (datatypeinXML != null)
        {
            String supertype = DataType.parseSupertype(datatypeinXML).trim();
            //System.out.println(name+" , "+supertype);

            if ("AVH".equals(supertype))
            {
                //System.out.println("Build AVH data type");

                String template = AVH.parseTemplate(datatypeinXML);
                if (template != null)
                {
                    newType = new DbAVH(name, null, template);
                }
                else
                {
                    newType = new AVH(name, null);
                }
            }
            else if ("DAG".equals(supertype))
            {
                //System.out.println("Build DAG data type");

                newType = new DAG(name);
            }
            else
            {
                //System.out.println("Build simple data type");
                newType = new SimpleDataType(name, supertype);
            }

            if (!DataType.isPredefinedType(name))
            {
                newType.fromXML(datatypeinXML);
            }

            if (newType.getName() == null)
            {
                newType.setName(name);
            }
        }
        if (newType!= null)
        	dataTypeCache.put(name,newType);
        return  newType;
        //System.out.println(newType.getClass());
    }
    
    public static DataType readDataTypeNative(String name)
    {
        try
        {
            if (DataType.isPredefinedType(name))
            {
                return new SimpleDataType(name, null);
            }

            //System.out.println("readDataType - " + name);
            String datatypeinXML = IndusHttpClient.getDetails(CMD_GET_TYPE_DETAILS, name);
            DataType newType = readDataTypeNative(name,datatypeinXML);
            return newType;
        }
        catch (Exception ex)
        {
            return null;
        }
    }

    /**
     * findAttributeToAVHMapping : given a schema, find all AVH type and create
     *    a mapping from column name to AVH
     *
     * @param remoteSchema Schema
     * @return Map - String -> AVH
     * @since 2005-03-25
     */
    public static Map findAttributeToAVHMapping(Schema remoteSchema)
    {
        Map attributeToAVH = new HashMap();
        Map attList = remoteSchema.getAttList();
        for (Iterator it = attList.keySet().iterator(); it.hasNext(); )
        {
            String attribute = (String) it.next();
            String type = (String) attList.get(attribute);
            DataType dt = readDataType(type);
            //Debug.trace(dt.getClass());
            if (dt != null)
            {
                if ("AVH".equalsIgnoreCase(dt.getSupertype()))
                {
                    // attribute is change to lower case, because db system use
                    // lower case as column name
                    attributeToAVH.put(attribute.toLowerCase(), dt);
                }
            }
            else
            {
                //Debug.trace(type + " type is not available");
            }
        }
        return attributeToAVH;
    }

    /**
     * Find attiribute to super type (eg. AVH, integer) mapping
     * @param schema Schema
     * @return Map
     * @author Jie Bao
     * @since 2005-03-28
     */
    public static Map findAttributeSupertypeMapping(Schema schema)
    {
        Map attributeToSupertype = new HashMap();
        Map attList = schema.getAttList();
        for (Iterator it = attList.keySet().iterator(); it.hasNext(); )
        {
            String attribute = (String) it.next();
            String type = (String) attList.get(attribute);
            DataType dt = readDataType(type);
            //Debug.trace(dt.getClass());
            if (dt != null)
            {
                if (dt.getSupertype() != null)
                {
                    attributeToSupertype.put(attribute.toLowerCase(),
                                             dt.getSupertype());
                }
                else // no super type
                {
                    attributeToSupertype.put(attribute.toLowerCase(),
                                             dt.getName());
                }
            }
            else
            {
                attributeToSupertype.put(attribute.toLowerCase(), type);
            }

        }
        return attributeToSupertype;
    }

    // 2005-03-25
    public static IndusDataSource readDataSource(Connection cacheDB,
                                                 String name)
    {
        IndusDataSource ds = new IndusDataSource();
        ds.fromDB(cacheDB, name);
        return ds;
    }

    public static String[] getAllType()
    {
        return getList(CMD_GET_ALL_TYPE);
    }

    public static String[] getAllView()
    {
        return getList(CMD_GET_ALL_VIEW);
    }

    public static String[] getAllMapping()
    {
        return getList(CMD_GET_ALL_MAPPING);
    }

    public static String[] getAllSchema()
    {
        return getList(CMD_GET_ALL_SCHEMA);
    }

    public static Object[] getAllDataSource(Connection db)
    {
        return IndusDataSource.getAllDataSource(db).toArray();
    }

    /**
     *
     * @param cmd String
     * @return String[]
     * @since 2004-10-13
     */
    static String[] getList(String cmd)
    {
        IndusHttpClient client = new IndusHttpClient();
        String res = client.sendCmd(cmd);
        if (res != null)
        {
            if (!res.equals(RES_GENERAL_ERROR))
            {
                String items[] = res.split(";");
                if (items != null && items.length == 1 &&
                    items[0].trim().length() == 0)
                {
                    return null;
                }
                else
                {
                    return items;
                }
            }
        }
        return null;
    }

}
