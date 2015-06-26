package org.ethereum.android.interop;


import android.os.Parcel;
import android.os.Parcelable;

public class Transaction extends org.ethereum.core.Transaction implements Parcelable{


    public Transaction(org.ethereum.core.Transaction transaction) {

        super(transaction.getEncoded());
    }

    @Override
    public int describeContents() {

        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {

        parcel.writeByteArray(getEncoded());
    }

    public static final Parcelable.Creator<Transaction> CREATOR = new Parcelable.Creator<Transaction>() {

        public Transaction createFromParcel(Parcel in) {

            return new Transaction(in);
        }

        public Transaction[] newArray(int size) {

            return new Transaction[size];
        }
    };

    private Transaction(Parcel in) {

        super(in.createByteArray());
    }
}
