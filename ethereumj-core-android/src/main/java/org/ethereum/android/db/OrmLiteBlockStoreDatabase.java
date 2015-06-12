package org.ethereum.android.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;
import android.util.Log;

import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.GenericRawResults;
import com.j256.ormlite.misc.TransactionManager;
import com.j256.ormlite.stmt.DeleteBuilder;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

import org.ethereum.core.Block;
import org.ethereum.core.TransactionReceipt;

import java.io.File;
import java.math.BigInteger;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

public class OrmLiteBlockStoreDatabase extends OrmLiteSqliteOpenHelper implements BlockStoreDatabase {

    private static final String DATABASE_NAME = "blockchain.db";
    private static final int DATABASE_VERSION = 1;

    private Dao<BlockVO, Integer> blockDao = null;
    private Dao<TransactionReceiptVO, Integer> transactionDao = null;

    public OrmLiteBlockStoreDatabase(Context context) {
        super(context, Environment.getExternalStorageDirectory().getAbsolutePath()
                + File.separator + DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     * This is called when the database is first created. Usually you should call createTable statements here to create
     * the tables that will store your data.
     */
    @Override
    public void onCreate(SQLiteDatabase db, ConnectionSource connectionSource) {

        try {
            Log.i(OrmLiteBlockStoreDatabase.class.getName(), "onCreate");
            TableUtils.createTable(connectionSource, BlockVO.class);
            TableUtils.createTable(connectionSource, TransactionReceiptVO.class);
        } catch (SQLException e) {
            Log.e(OrmLiteBlockStoreDatabase.class.getName(), "Can't create database", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * This is called when your application is upgraded and it has a higher version number. This allows you to adjust
     * the various data to match the new version number.
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, ConnectionSource connectionSource, int oldVersion, int newVersion) {

        try {
            Log.i(OrmLiteBlockStoreDatabase.class.getName(), "onUpgrade");
            TableUtils.dropTable(connectionSource, BlockVO.class, true);
            TableUtils.dropTable(connectionSource, TransactionReceiptVO.class, true);
            // after we drop the old databases, we create the new ones
            onCreate(db, connectionSource);
        } catch (SQLException e) {
            Log.e(OrmLiteBlockStoreDatabase.class.getName(), "Can't drop databases", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * Returns the Database Access Object (DAO) for our SimpleData class. It will create it or just give the cached
     * value.
     */
    public Dao<BlockVO, Integer> getBlockDao() throws SQLException {

        if (blockDao == null) {
            blockDao = getDao(BlockVO.class);
        }
        return blockDao;
    }

    /**
     * Returns the Database Access Object (DAO) for our SimpleData class. It will create it or just give the cached
     * value.
     */
    public Dao<TransactionReceiptVO, Integer> getTransactionDao() throws SQLException {
        if (transactionDao == null) {
            transactionDao = getDao(TransactionReceiptVO.class);
        }
        return transactionDao;
    }

    /**
     * Close the database connections and clear any cached DAOs.
     */
    @Override
    public void close() {

        super.close();
        blockDao = null;
        transactionDao = null;
    }

    public List<BlockVO> getByNumber(Long number) {

        List<BlockVO> list = new ArrayList<BlockVO>();
        try {
            list = getBlockDao().queryForEq("number", number);
        } catch(java.sql.SQLException e) {
            Log.e(OrmLiteBlockStoreDatabase.class.getName(), "Error querying for number", e);
        }

        return list;
    }

    public List<BlockVO> getByHash(byte[] hash) {

        List<BlockVO> list = new ArrayList<BlockVO>();
        try {
            list = getBlockDao().queryForEq("hash", hash);
        } catch(java.sql.SQLException e) {
            Log.e(OrmLiteBlockStoreDatabase.class.getName(), "Error querying for hash", e);
        }

        return list;
    }

    public List<byte[]> getHashListByNumberLimit(Long from, Long to) {

        List<byte[]> results = new ArrayList<byte[]>();
        try {
            List<BlockVO> list = new ArrayList<BlockVO>();
            list = getBlockDao().queryBuilder().orderBy("number", false).limit(to - from).where().between("number", from, to).query();
            for (BlockVO block : list) {
                results.add(block.hash);
            }
        } catch(java.sql.SQLException e) {
            Log.e(OrmLiteBlockStoreDatabase.class.getName(), "Error querying for hash list", e);
        }

        return results;
    }

    public void deleteBlocksSince(long number) {

        try {
            DeleteBuilder<BlockVO, Integer> deleteBuilder = getBlockDao().deleteBuilder();
            deleteBuilder.where().gt("number", number);
            deleteBuilder.delete();
        } catch(java.sql.SQLException e) {
            Log.e(OrmLiteBlockStoreDatabase.class.getName(), "Error deleting blocks since", e);
        }
    }

    public void save(BlockVO block) {

        try {
            getBlockDao().create(block);
        } catch(java.sql.SQLException e) {
            Log.e(OrmLiteBlockStoreDatabase.class.getName(), "Error saving block", e);
        }
    }

    public BigInteger getTotalDifficultySince(long number) {

        try {
            GenericRawResults<String[]> rawResults = getBlockDao().queryRaw("select sum(cumulativedifficulty) from block where number > " + number);
            List<String[]> results = rawResults.getResults();
            return new BigInteger(results.get(0)[0]);
        } catch(java.sql.SQLException e) {
            Log.e(OrmLiteBlockStoreDatabase.class.getName(), "Error getting total difficulty since", e);
        }
        return null;
    }

    public BigInteger getTotalDifficulty() {

        try {
            GenericRawResults<String[]> rawResults = getBlockDao().queryRaw("select sum(cumulativedifficulty) from block");
            List<String[]> results = rawResults.getResults();
            return new BigInteger(results.get(0)[0]);
        } catch(java.sql.SQLException e) {
            Log.e(OrmLiteBlockStoreDatabase.class.getName(), "Error getting total difficulty", e);
        }
        return null;
    }

    public Block getBestBlock() {

        Long bestNumber = null;
        try {
            GenericRawResults<String[]> rawResults = getBlockDao().queryRaw("select max(number) from block");
            List<String[]> results = rawResults.getResults();
            if (results.size() > 0 && results.get(0).length > 0) {
                bestNumber = Long.valueOf(results.get(0)[0]);
            }
        } catch(java.sql.SQLException e) {
            Log.e(OrmLiteBlockStoreDatabase.class.getName(), "Sql Error getting best block", e);
        } catch (Exception e) {
            Log.e(OrmLiteBlockStoreDatabase.class.getName(), "Error getting best block", e);
        }

        if (bestNumber == null) return null;
        List result = getByNumber(bestNumber);

        if (result.isEmpty()) return null;
        BlockVO vo = (BlockVO) result.get(0);

        return new Block(vo.rlp);
    }

    public List<Block> getAllBlocks() {

        ArrayList<Block> blocks = new ArrayList<>();
        try {
            for (BlockVO blockVO : getBlockDao()) {
                blocks.add(new Block(blockVO.getRlp()));
            }
        } catch(java.sql.SQLException e) {
            Log.e(OrmLiteBlockStoreDatabase.class.getName(), "Error getting all blocks", e);
        }

        return blocks;
    }

    public void reset() {

        deleteBlocksSince(Long.valueOf(0));
    }

    public void save(TransactionReceiptVO transactionReceiptVO) {

        try {
            getTransactionDao().create(transactionReceiptVO);
        } catch(java.sql.SQLException e) {
            Log.e(OrmLiteBlockStoreDatabase.class.getName(), "Error saving transaction", e);
        }
    }

    public TransactionReceipt getTransactionReceiptByHash(byte[] hash) {

        List<TransactionReceiptVO> list = new ArrayList<TransactionReceiptVO>();
        try {
            list = getTransactionDao().queryForEq("hash", hash);
        } catch(java.sql.SQLException e) {
            Log.e(OrmLiteBlockStoreDatabase.class.getName(), "Error querying for hash", e);
        }

        if (list.size() == 0) return null;
        TransactionReceiptVO vo = list.get(0);

        return new TransactionReceipt(vo.rlp);

    }

    public boolean flush(final List<Block> blocks) {

        try {
            TransactionManager.callInTransaction(getBlockDao().getConnectionSource(),
                    new Callable<Void>() {
                        public Void call() throws Exception {
                            for (Block block : blocks) {
                                BlockVO blockVO = new BlockVO(block.getNumber(), block.getHash(), block.getEncoded(), block.getCumulativeDifficulty());
                                save(blockVO);
                            }
                            // you could pass back an object here
                            return null;
                        }
                    });


            return true;
        } catch(java.sql.SQLException e) {
            Log.e(OrmLiteBlockStoreDatabase.class.getName(), "Error querying for hash", e);
            return false;
        }
    }
}
