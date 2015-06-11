package org.ethereum.android.db;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.math.BigInteger;

@DatabaseTable(tableName = "block")
public class BlockVO {

    @DatabaseField(index = true, persisterClass = HashPersister.class)
    byte[] hash;

    @DatabaseField(index = true, dataType = DataType.LONG_OBJ)
    Long number;

    @DatabaseField(columnName = "cumulativedifficulty", dataType = DataType.BIG_INTEGER)
    BigInteger cumulativeDifficulty;

    @DatabaseField(dataType = DataType.BYTE_ARRAY)
    byte[] rlp;

    public BlockVO() {
    }

    public BlockVO(Long number, byte[] hash, byte[] rlp, BigInteger cumulativeDifficulty) {
        this.number = number;
        this.hash = hash;
        this.rlp = rlp;
        this.cumulativeDifficulty = cumulativeDifficulty;
    }

    public byte[] getHash() {
        return hash;
    }

    public void setHash(byte[] hash) {
        this.hash = hash;
    }

    public Long getIndex() {
        return number;
    }

    public void setIndex(Long number) {
        this.number = number;
    }

    public byte[] getRlp() {
        return rlp;
    }

    public void setRlp(byte[] rlp) {
        this.rlp = rlp;
    }

    public BigInteger getCumulativeDifficulty() {
        return cumulativeDifficulty;
    }

    public void setCumulativeDifficulty(BigInteger cumulativeDifficulty) {
        this.cumulativeDifficulty = cumulativeDifficulty;
    }

}
