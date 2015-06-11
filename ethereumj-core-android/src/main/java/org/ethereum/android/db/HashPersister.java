package org.ethereum.android.db;

import android.text.TextUtils;

import com.j256.ormlite.field.FieldType;
import com.j256.ormlite.field.SqlType;
import com.j256.ormlite.field.types.StringType;

import android.util.Base64;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HashPersister extends StringType
{
    private static final String delimiter = ",";
    private static final HashPersister singleTon = new HashPersister();
    private static final Logger logger = LoggerFactory.getLogger("persister");

    private HashPersister()
    {
        super(SqlType.STRING, new Class<?>[]{ String[].class });
    }

    public static HashPersister getSingleton()
    {
        return singleTon;
    }

    @Override
    public Object javaToSqlArg(FieldType fieldType, Object javaObject)
    {
        byte[] array = (byte[]) javaObject;

        if (array == null)
        {
            return null;
        }
        else
        {
            String string = new String(Base64.encode(array, Base64.DEFAULT));
            //String string = new String(array);
            //logger.info("sqlArgToJava: " + string);
            return string;
        }
    }

    @Override
    public Object sqlArgToJava(FieldType fieldType, Object sqlArg, int columnPos)
    {

        String string = (String)sqlArg;
        //logger.info("sqlArgToJava: " + string);

        if ( string == null )
        {
            return null;
        }
        else
        {
            return Base64.decode(string, Base64.DEFAULT);
            //return string.getBytes();
        }
    }
}