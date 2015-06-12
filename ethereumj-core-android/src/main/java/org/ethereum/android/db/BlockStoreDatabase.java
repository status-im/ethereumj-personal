package org.ethereum.android.db;

import org.ethereum.core.Block;
import org.ethereum.core.TransactionReceipt;

import java.math.BigInteger;
import java.util.List;

public interface BlockStoreDatabase {

    public List<BlockVO> getByNumber(Long number);

    public List<BlockVO> getByHash(byte[] hash);

    public List<byte[]> getHashListByNumberLimit(Long from, Long to);

    public void deleteBlocksSince(long number);

    public void save(BlockVO block);

    public BigInteger getTotalDifficultySince(long number);

    public BigInteger getTotalDifficulty();

    public Block getBestBlock();

    public List<Block> getAllBlocks();

    public void reset();

    public void save(TransactionReceiptVO transactionReceiptVO);

    public TransactionReceipt getTransactionReceiptByHash(byte[] hash);

    public boolean flush(List<Block> blocks);
}
