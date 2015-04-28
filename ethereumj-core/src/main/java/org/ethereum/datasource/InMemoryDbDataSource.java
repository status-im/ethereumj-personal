package org.ethereum.datasource;

import android.content.Context;

import org.ethereum.config.SystemProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import static org.iq80.leveldb.impl.Iq80DBFactory.factory;

/**
 * @author Roman Mandeleil
 * @since 18.01.2015
 */
public class InMemoryDbDataSource implements KeyValueDataSource {

    private static final Logger logger = LoggerFactory.getLogger("db");

    String name;
    HashMap<ByteBuffer, byte[]> db;
    private Context context;

    public InMemoryDbDataSource() {
    }

    public InMemoryDbDataSource(String name) {
        this.name = name;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    @Override
    public void init() {

        db = new HashMap<ByteBuffer, byte[]>();
    }


    public void destroyDB(File fileLocation) {
        logger.debug("Destroying existing database");
        db = new HashMap<ByteBuffer, byte[]>();
    }


    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public byte[] get(byte[] key) {
        return db.get(key);
    }

    @Override
    public byte[] put(byte[] key, byte[] value) {
        ByteBuffer wrapped = ByteBuffer.wrap(key);
        db.put(wrapped, value);
        return value;
    }

    @Override
    public void delete(byte[] key) {
        db.remove(key);
    }

    @Override
    public Set<byte[]> keys() {

        Set<byte[]> keys = new HashSet<>();
        Set bufferKeys = db.keySet();

        for (Iterator i = bufferKeys.iterator(); i.hasNext();)
        {
            ByteBuffer key = (ByteBuffer) i.next();
            keys.add(key.array());
        }

        return keys;
    }

    @Override
    public void updateBatch(Map<byte[], byte[]> rows) {


        for (Map.Entry<byte[], byte[]> row : rows.entrySet()) {
            ByteBuffer wrapped = ByteBuffer.wrap(row.getKey());
            db.put(wrapped, row.getValue());
        }
    }

    @Override
    public void close() {
        try {
            logger.info("Close db: {}", name);
            init();
        } catch (Exception e) {
            logger.error("Failed to find the db file on the close: {} ", name);
        }
    }
}
