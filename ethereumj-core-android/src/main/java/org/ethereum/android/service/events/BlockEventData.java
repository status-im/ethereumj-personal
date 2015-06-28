package org.ethereum.android.service.events;


import android.os.Parcel;
import android.os.Parcelable;

import org.ethereum.core.Block;
import org.ethereum.core.TransactionReceipt;

import java.util.Arrays;
import java.util.List;

public class BlockEventData extends EventData {

    public Block block;
    public List<TransactionReceipt> receipts;

    public BlockEventData(Block block, List<TransactionReceipt> receipts) {

        super();
        this.block = block;
        this.receipts = receipts;
    }

    @Override
    public int describeContents() {

        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {

        super.writeToParcel(parcel, i);
        parcel.writeParcelable(new org.ethereum.android.interop.Block(block), i);

        org.ethereum.android.interop.TransactionReceipt[] transactionReceipts = new org.ethereum.android.interop.TransactionReceipt[receipts.size()];
        int index = 0;
        for (TransactionReceipt receipt: receipts) {
            transactionReceipts[index] = new org.ethereum.android.interop.TransactionReceipt(receipt);
        }

        parcel.writeParcelableArray(transactionReceipts, i);
    }

    public static final Parcelable.Creator<BlockEventData> CREATOR = new Parcelable.Creator<BlockEventData>() {

        public BlockEventData createFromParcel(Parcel in) {

            return new BlockEventData(in);
        }

        public BlockEventData[] newArray(int size) {

            return new BlockEventData[size];
        }
    };

    protected BlockEventData(Parcel in) {

        super(in);
        block = in.readParcelable(org.ethereum.android.interop.Block.class.getClassLoader());
        receipts = Arrays.asList((TransactionReceipt[])in.readParcelableArray(TransactionReceipt.class.getClassLoader()));
    }
}
