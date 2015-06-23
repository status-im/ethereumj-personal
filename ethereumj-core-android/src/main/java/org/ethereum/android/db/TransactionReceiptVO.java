package org.ethereum.android.db;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;

/**
 * @author Roman Mandeleil
 * @since 14.11.2014
 */
@DatabaseTable(tableName = "transaction_receipt")
public class TransactionReceiptVO {

    @DatabaseField(index = true, persisterClass = HashPersister.class)
    byte[] hash;

    @DatabaseField(dataType = DataType.BYTE_ARRAY)
    byte[] rlp;

    public TransactionReceiptVO() {
    }

    public TransactionReceiptVO(byte[] hash, byte[] rlp) {
        this.hash = hash;
        this.rlp = rlp;
    }

    public byte[] getHash() {
        return hash;
    }

    public void setHash(byte[] hash) {
        this.hash = hash;
    }

    public byte[] getRlp() {
        return rlp;
    }

    public void setRlp(byte[] rlp) {
        this.rlp = rlp;
    }

}
