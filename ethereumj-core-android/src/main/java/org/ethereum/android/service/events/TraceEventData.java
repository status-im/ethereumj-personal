package org.ethereum.android.service.events;

import android.os.Parcel;
import android.os.Parcelable;

public class TraceEventData extends EventData {

    public String message;

    public TraceEventData(String message) {

        super();
        this.message = message;
    }

    @Override
    public int describeContents() {

        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {

        super.writeToParcel(parcel, i);
        parcel.writeString(message);
    }

    public static final Parcelable.Creator<TraceEventData> CREATOR = new Parcelable.Creator<TraceEventData>() {

        public TraceEventData createFromParcel(Parcel in) {

            return new TraceEventData(in);
        }

        public TraceEventData[] newArray(int size) {

            return new TraceEventData[size];
        }
    };

    private TraceEventData(Parcel in) {

        super(in);
        message = in.readString();
    }
}
