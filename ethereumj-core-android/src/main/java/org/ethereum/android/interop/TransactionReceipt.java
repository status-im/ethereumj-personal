package org.ethereum.android.interop;


import android.os.Parcel;
import android.os.Parcelable;

import org.ethereum.core.Bloom;
import org.ethereum.util.RLP;
import org.ethereum.util.RLPElement;
import org.ethereum.util.RLPItem;
import org.ethereum.util.RLPList;
import org.ethereum.vm.LogInfo;

import java.util.Arrays;

public class TransactionReceipt extends org.ethereum.core.TransactionReceipt implements Parcelable {

    public TransactionReceipt(org.ethereum.core.TransactionReceipt transactionReceipt) {

        super(transactionReceipt.getEncoded());
    }

    @Override
    public int describeContents() {

        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {

        byte[] rlp = getEncoded();
        parcel.writeInt(rlp.length);
        parcel.writeByteArray(rlp);
        parcel.writeParcelable(new Transaction(getTransaction()), i);
    }

    public static final Parcelable.Creator<TransactionReceipt> CREATOR = new Parcelable.Creator<TransactionReceipt>() {

        public TransactionReceipt createFromParcel(Parcel in) {

            return new TransactionReceipt(in);
        }

        public TransactionReceipt[] newArray(int size) {

            return new TransactionReceipt[size];
        }
    };

    private TransactionReceipt(Parcel in) {

        byte[] rlp;
        int length = in.readInt();
        rlp = new byte[length];
        in.readByteArray(rlp);
        parseRlp(rlp);
        setTransaction((org.ethereum.core.Transaction)in.readParcelable(Transaction.class.getClassLoader()));
    }

}
