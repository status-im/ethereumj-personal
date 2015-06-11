package org.ethereum.android.db;

import org.ethereum.core.Block;
import org.ethereum.core.TransactionReceipt;
import org.ethereum.db.BlockStore;
import org.ethereum.util.ByteUtil;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

public class BlockStoreImpl implements BlockStore {

    private BlockStoreDatabase database;

    public BlockStoreImpl(BlockStoreDatabase database) {

        this.database = database;
    }

    public byte[] getBlockHashByNumber(long blockNumber) {

        Block block = getBlockByNumber(blockNumber);
        if (block != null) return block.getHash();
        return ByteUtil.EMPTY_BYTE_ARRAY;
    }


    public Block getBlockByNumber(long blockNumber) {


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

    public List<byte[]> getListOfHashesStartFrom(byte[] hash, int qty) {

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

    public void saveBlock(Block block, List<TransactionReceipt> receipts) {

        BlockVO blockVO = new BlockVO(block.getNumber(), block.getHash(),
                block.getEncoded(), block.getCumulativeDifficulty());

        for (TransactionReceipt receipt : receipts) {

            byte[] hash = receipt.getTransaction().getHash();
            byte[] rlp = receipt.getEncoded();

            TransactionReceiptVO transactionReceiptVO = new TransactionReceiptVO(hash, rlp);
            database.save(transactionReceiptVO);
        }

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
}
