package org.ethereum.android.db;

import org.ethereum.core.Block;
import org.ethereum.core.TransactionReceipt;
import org.ethereum.db.BlockStore;
import org.ethereum.util.ByteUtil;

import java.math.BigInteger;
import java.util.List;

public class BlockStoreImpl implements BlockStore {

    private BlockDatabaseHelper blockDao;
    private TransactionDatabaseHelper transactionDao;

    public BlockStoreImpl(BlockDatabaseHelper blockDao, TransactionDatabaseHelper transactionDao) {

        this.blockDao = blockDao;
        this.transactionDao = transactionDao;
    }

    public byte[] getBlockHashByNumber(long blockNumber) {

        Block block = getBlockByNumber(blockNumber);
        if (block != null) return block.getHash();
        return ByteUtil.EMPTY_BYTE_ARRAY;
    }


    public Block getBlockByNumber(long blockNumber) {

        /*
        List result = sessionFactory.getCurrentSession().
                createQuery("from BlockVO where number = :number").
                setParameter("number", blockNumber).list();

        if (result.size() == 0) return null;
        BlockVO vo = (BlockVO) result.get(0);

        return new Block(vo.rlp);
        */
        return null;
    }

    public Block getBlockByHash(byte[] hash) {

        return null;
    }

    @SuppressWarnings("unchecked")
    public List<byte[]> getListOfHashesStartFrom(byte[] hash, int qty) {

        return null;
    }

    public void deleteBlocksSince(long number) {

    }

    public void saveBlock(Block block, List<TransactionReceipt> receipts) {

    }

    public BigInteger getTotalDifficultySince(long number) {

        return null;
    }

    public BigInteger getTotalDifficulty() {

        return null;
    }

    public Block getBestBlock() {

        return null;
    }

    @SuppressWarnings("unchecked")
    public List<Block> getAllBlocks() {

        return null;
    }

    public void reset() {

    }

    public TransactionReceipt getTransactionReceiptByHash(byte[] hash) {

        return null;
    }
}
