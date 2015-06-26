package org.ethereum.android.db;


import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "block_transaction")
public class BlockTransactionVO {

    @DatabaseField(index = true, persisterClass = HashPersister.class)
    byte[] blockHash;

    @DatabaseField(index = true, persisterClass = HashPersister.class)
    byte[] transactionHash;

    @DatabaseField(dataType = DataType.INTEGER)
    int transactionIndex;

    public BlockTransactionVO() {

    }

    public BlockTransactionVO(byte[] blockHash, byte[] transactionHash, int transactionIndex) {

        this.blockHash = blockHash;
        this.transactionHash = transactionHash;
        this.transactionIndex = transactionIndex;
    }

    public byte[] getBlockHash() {
        return blockHash;
    }

    public void setBlockHash(byte[] blockHash) {
        this.blockHash = blockHash;
    }

    public byte[] getTransactionHash() {
        return transactionHash;
    }

    public void setTransactionHash(byte[] transactionHash) {
        this.transactionHash = transactionHash;
    }

    public int getTransactionIndex() {
        return transactionIndex;
    }

    public void setTransactionIndex(int transactionIndex) {
        this.transactionIndex = transactionIndex;
    }
}
