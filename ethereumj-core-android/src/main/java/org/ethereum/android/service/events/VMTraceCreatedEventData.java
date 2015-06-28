package org.ethereum.android.service.events;


import android.os.Parcel;
import android.os.Parcelable;

public class VMTraceCreatedEventData extends EventData {

    public String transactionHash;
    public String trace;

    public VMTraceCreatedEventData(String transactionHash, String trace) {

        super();
        this.transactionHash = transactionHash;
        this.trace = trace;
    }

    @Override
    public int describeContents() {

        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {

        super.writeToParcel(parcel, i);
        parcel.writeString(transactionHash);
        parcel.writeString(trace);
    }

    public static final Parcelable.Creator<VMTraceCreatedEventData> CREATOR = new Parcelable.Creator<VMTraceCreatedEventData>() {

        public VMTraceCreatedEventData createFromParcel(Parcel in) {

            return new VMTraceCreatedEventData(in);
        }

        public VMTraceCreatedEventData[] newArray(int size) {

            return new VMTraceCreatedEventData[size];
        }
    };

    private VMTraceCreatedEventData(Parcel in) {

        super(in);
        transactionHash = in.readString();
        trace = in.readString();
    }
}
