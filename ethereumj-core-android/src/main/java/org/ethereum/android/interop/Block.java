package org.ethereum.android.interop;

import android.os.Parcel;
import android.os.Parcelable;

public class Block extends org.ethereum.core.Block implements Parcelable {

    public Block(byte[] rawData) {

        super(rawData);
    }

    public Block(org.ethereum.core.Block block) {

        super(block.getEncoded());
    }

    public Block(Parcel in) {

        super(new byte[0]);
        rlpEncoded = new byte[in.readInt()];
        in.readByteArray(rlpEncoded);
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {

        byte[] data = getEncoded();
        parcel.writeInt(data.length);
        parcel.writeByteArray(data);
    }

    @Override
    public int describeContents() {

        return 0;
    }

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {

        public Block createFromParcel(Parcel in) {

            return new Block(in);
        }

        public Block[] newArray(int size) {

            return new Block[size];
        }

    };
}
