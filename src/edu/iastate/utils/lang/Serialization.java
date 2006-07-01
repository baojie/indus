/**
 * 
 */
package edu.iastate.utils.lang;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;

import edu.iastate.utils.Utility;

/**
 * @author baojie
 * @since 2006-06-21
 *
 */
public class Serialization
{
    public static void main(String[] argv)
    {
        try
        {
            test();
        }
        catch (Exception e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    static public void test() throws IOException, ClassNotFoundException
    {
        String data = new String();
        //System.out.println(data.s);
        byte[] b = saveToByteArray(data);
        saveToFile(data, "D:\\object.dat");

        System.out.println(b.length + "(" + Utility.formatSize(b.length) + ")");
        data = (String) loadFromByteArray(b);
        //System.out.println(data); 
    }

    static public Object loadFromByteArray(byte[] array) throws IOException,
            ClassNotFoundException
    {
        ByteArrayInputStream fis = new ByteArrayInputStream(array);
        return load(fis);
    }

    static public Object loadFromFile(String fileName) throws IOException,
            ClassNotFoundException
    {
        FileInputStream fis = new FileInputStream(fileName);
        return load(fis);
    }

    static public Object load(InputStream fis) throws IOException,
            ClassNotFoundException
    {

        ObjectInputStream in = new ObjectInputStream(fis);
        Object data = in.readObject();
        in.close();

        return data;
    }

    static public byte[] saveToByteArray(Object data) throws IOException
    {
        ByteArrayOutputStream fos = new ByteArrayOutputStream();
        save(data, fos);
        return fos.toByteArray();
    }

    static public void saveToFile(Object data, String fileName)
            throws IOException
    {
        FileOutputStream fos = new FileOutputStream(fileName);
        save(data, fos);
    }

    static public void save(Object data, OutputStream fos) throws IOException
    {
        ObjectOutputStream out = new ObjectOutputStream(fos);
        out.writeObject(data);
        out.flush();
        out.close();
    }

    /**
     * Clone a object
     * @param obj
     * @return
     * @throws IOException
     * @throws ClassNotFoundException
     * 
     * @author Jie Bao
     * @since 2006-07-01
     */
    public static Object cloneObject(Object obj) 
    {
        try
        {
            byte[] b = saveToByteArray(obj);
            return loadFromByteArray(b);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return null;
    }
}
