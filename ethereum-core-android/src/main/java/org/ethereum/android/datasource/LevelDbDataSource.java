package org.ethereum.android.datasource;

import org.ethereum.config.SystemProperties;

import org.ethereum.datasource.KeyValueDataSource;
import org.iq80.leveldb.CompressionType;
import org.iq80.leveldb.DB;
import org.iq80.leveldb.DBIterator;
import org.iq80.leveldb.Options;
import org.iq80.leveldb.WriteBatch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.iq80.leveldb.impl.Iq80DBFactory.factory;
import android.content.Context;

/**
 * @author Roman Mandeleil
 * @since 18.01.2015
 */
public class LevelDbDataSource implements KeyValueDataSource {

    private static final Logger logger = LoggerFactory.getLogger("db");

    String name;
    private DB db;
    private Context context;

    public LevelDbDataSource() {
    }

    public LevelDbDataSource(String name) {
        this.name = name;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    @Override
    public void init() {

        if (name == null) throw new NullPointerException("no name set to the db");

        Options options = new Options();
        options.createIfMissing(true);
        options.compressionType(CompressionType.NONE);
        org.iq80.leveldb.Logger logger1 = new org.iq80.leveldb.Logger() {
            public void log(String message) {
                logger.debug(message);
            }
        };
        options.logger(logger1);
        try {
            logger.debug("Opening database");
            File dbLocation = context.getDir(SystemProperties.CONFIG.databaseDir(), 0);
            File fileLocation = new File(dbLocation, name);

            if (SystemProperties.CONFIG.databaseReset()) {
                destroyDB(fileLocation);
            }

            logger.debug("Initializing new or existing database: '{}'", fileLocation.getAbsolutePath());
            db = Iq80DBFactory.factory.open(fileLocation, options);

        } catch (IOException ioe) {
            logger.error(ioe.getMessage(), ioe);
            throw new RuntimeException("Can't initialize database");
        }
    }


    public void destroyDB(File fileLocation) {
        logger.debug("Destroying existing database");
        Options options = new Options();
        try {
            Iq80DBFactory.factory.destroy(fileLocation, options);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
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
        db.put(key, value);
        return value;
    }

    @Override
    public void delete(byte[] key) {
        db.delete(key);
    }

    @Override
    public Set<byte[]> keys() {

        DBIterator dbIterator = db.iterator();
        Set<byte[]> keys = new HashSet<>();
        while (dbIterator.hasNext()) {

            Map.Entry<byte[], byte[]> entry = dbIterator.next();
            keys.add(entry.getKey());
        }
        return keys;
    }

    @Override
    public void updateBatch(Map<byte[], byte[]> rows) {

        WriteBatch batch = db.createWriteBatch();

        for (Map.Entry<byte[], byte[]> row : rows.entrySet())
            batch.put(row.getKey(), row.getValue());

        db.write(batch);
    }

    @Override
    public void close() {
        try {
            logger.info("Close db: {}", name);
            db.close();
        } catch (IOException e) {
            logger.error("Failed to find the db file on the close: {} ", name);
        }
    }
}