package org.ethereum.android.service.events;


import android.os.Parcel;
import android.os.Parcelable;

import org.ethereum.core.Transaction;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class PendingTransactionsEventData extends EventData {

    public Set<Transaction> transactions;

    public PendingTransactionsEventData(Set<Transaction> transactions) {

        super();
        this.transactions = transactions;
    }

    @Override
    public int describeContents() {

        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {

        super.writeToParcel(parcel, i);
        org.ethereum.android.interop.Transaction[] txs = new org.ethereum.android.interop.Transaction[transactions.size()];
        int index = 0;
        for (Transaction transaction: transactions) {
            txs[index] = new org.ethereum.android.interop.Transaction(transaction);
        }
        parcel.writeParcelableArray(txs, i);
    }

    public static final Parcelable.Creator<PendingTransactionsEventData> CREATOR = new Parcelable.Creator<PendingTransactionsEventData>() {

        public PendingTransactionsEventData createFromParcel(Parcel in) {

            return new PendingTransactionsEventData(in);
        }

        public PendingTransactionsEventData[] newArray(int size) {

            return new PendingTransactionsEventData[size];
        }
    };

    private PendingTransactionsEventData(Parcel in) {

        super(in);
        transactions = new HashSet<Transaction>(Arrays.asList((Transaction[])in.readParcelableArray(org.ethereum.android.interop.Transaction.class.getClassLoader())));
    }

}
