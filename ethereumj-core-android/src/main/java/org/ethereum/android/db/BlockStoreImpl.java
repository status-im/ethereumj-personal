package org.ethereum.android.db;

import org.ethereum.core.Block;
import org.ethereum.core.BlockHeader;
import org.ethereum.core.TransactionReceipt;
import org.ethereum.db.BlockStore;
import org.ethereum.util.ByteUtil;
import org.hibernate.SessionFactory;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

public class BlockStoreImpl implements BlockStore {

    private BlockStoreDatabase database;

    public BlockStoreImpl(BlockStoreDatabase database) {

        this.database = database;
    }

    @Override
    public long getMaxNumber() {
        return this.database.getMaxNumber();
    }

    //TODO: implement new functions

    @Override
    public List<Block> getListBlocksEndWith(byte[] hash, long qty) {
        return null;
    }

    @Override
    public List<BlockHeader> getListHeadersEndWith(byte[] hash, long qty) {
        return null;
    }

    @Override
    public boolean isBlockExist(byte[] hash) {
        return false;
    }

    @Override
    public void reBranch(Block forkBlock) {

    }

    @Override
    public BigInteger getTotalDifficultyForHash(byte[] hash) {
        return null;
    }

    public byte[] getBlockHashByNumber(long blockNumber) {

        Block block = getChainBlockByNumber(blockNumber);
        if (block != null) return block.getHash();
        return ByteUtil.EMPTY_BYTE_ARRAY;
    }

    @Override
    public Block getChainBlockByNumber(long blockNumber) {

        List result = database.getByNumber(blockNumber);
        if (result.size() == 0) return null;
        BlockVO vo = (BlockVO) result.get(0);

        return new Block(vo.rlp);
    }

    public Block getBlockByHash(byte[] hash) {

        List result = database.getByHash(hash);

        if (result.size() == 0) return null;
        BlockVO vo = (BlockVO) result.get(0);

        return new Block(vo.rlp);
    }

    public List<byte[]> getListHashesEndWith(byte[] hash, long qty) {

        List<byte[]> hashes = new ArrayList<byte[]>();

        // find block number of that block hash
        Block block = getBlockByHash(hash);
        if (block == null) return hashes;

        hashes = database.getHashListByNumberLimit(block.getNumber(), block.getNumber() - qty);

        return hashes;
    }

    public void deleteBlocksSince(long number) {

        database.deleteBlocksSince(number);
    }

    public void saveBlock(Block block, BigInteger cummDifficulty, boolean mainChain) {

        byte[] blockHash = block.getHash();
        BlockVO blockVO = new BlockVO(block.getNumber(), blockHash,
                block.getEncoded(), block.getCumulativeDifficulty());

        database.save(blockVO);
    }

    public BigInteger getTotalDifficultySince(long number) {

        return database.getTotalDifficultySince(number);
    }

    public BigInteger getTotalDifficulty() {

        return database.getTotalDifficulty();
    }

    public Block getBestBlock() {

        return database.getBestBlock();
    }

    public List<Block> getAllBlocks() {

        return database.getAllBlocks();
    }

    public void reset() {

        database.reset();
    }

    public TransactionReceipt getTransactionReceiptByHash(byte[] hash) {

        return database.getTransactionReceiptByHash(hash);
    }

    @Override
    public void load() {

    }

    @Override
    public void flush() {

    }

    @Override
    public void setSessionFactory(SessionFactory sessionFactory) {

    }
}
