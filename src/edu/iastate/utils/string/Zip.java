/**
 * 
 */
package edu.iastate.utils.string;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringBufferInputStream;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;

import edu.iastate.utils.io.FileUtils;

/**
 * Zip and unzip
 * 
 * @author baojie
 * @since 2006-06-21
 *
 */
public class Zip 
{

    static final int BUFFER = 2048;

    public static String encode(String str)
    {
        return StringUtils.encodeHex(encodeByte(str));
    }
    
    public static byte[] encodeByte(String str)
    {
        try
        {
            BufferedInputStream origin = null;
            ByteArrayOutputStream dest = new ByteArrayOutputStream();

            Deflater compresser = new Deflater();
            compresser.setLevel(Deflater.BEST_SPEED);
            DeflaterOutputStream out = new DeflaterOutputStream(
                    new BufferedOutputStream(dest), compresser);

            byte data[] = new byte[BUFFER];

            StringBufferInputStream fi = new StringBufferInputStream(str);
            origin = new BufferedInputStream(fi, BUFFER);
            int count;
            while ((count = origin.read(data, 0, BUFFER)) != -1)
            {
                out.write(data, 0, count);
            }
            origin.close();
            out.close();

            // get result
            //System.out.println(dest.size());
            return dest.toByteArray();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return null;
    }

    public static String decode(String encoded)
    {
        byte[] result = decodeByte(encoded);
        String outputString = new String(result, 0, result.length);
        return outputString;        
    }
    
    public static byte[] decodeByte(String encoded)
    {
        try
        {
            byte[] source = StringUtils.decodeHex(encoded);

            ByteArrayInputStream fis = new ByteArrayInputStream(source);
            InflaterInputStream zis = new InflaterInputStream(
                    new BufferedInputStream(fis));

            BufferedOutputStream dest = null;

            int count;
            byte data[] = new byte[BUFFER];
            // write the files to the disk
            ByteArrayOutputStream fos = new ByteArrayOutputStream();
            dest = new BufferedOutputStream(fos, BUFFER);
            while ((count = zis.read(data, 0, BUFFER)) != -1)
            {
                dest.write(data, 0, count);
            }
            dest.flush();
            dest.close();
            
            byte[] result = fos.toByteArray();
            return result;

        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return null;
    }

    public static void main(String[] args) throws FileNotFoundException,
            SecurityException, IOException
    {
        String source = FileUtils.readFile("D:\\test.txt");

        //String source = "11";
        System.out.println(source.length());

        String encoded = encode(source);
        //System.out.println(encoded);
        System.out.println(encoded.length());

        String restored = decode(encoded);
        System.out.println(restored.length());

    }
}
