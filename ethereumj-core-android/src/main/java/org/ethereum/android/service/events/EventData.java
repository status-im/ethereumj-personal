package org.ethereum.android.service.events;


import android.os.Parcel;
import android.os.Parcelable;

public class EventData implements Parcelable {

    public long registeredTime;

    public EventData() {

        registeredTime = System.currentTimeMillis();
    }

    @Override
    public int describeContents() {

        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {

        parcel.writeLong(registeredTime);
    }

    public static final Parcelable.Creator<EventData> CREATOR = new Parcelable.Creator<EventData>() {

        public EventData createFromParcel(Parcel in) {

            return new EventData(in);
        }

        public EventData[] newArray(int size) {

            return new EventData[size];
        }
    };

    protected EventData(Parcel in) {

        registeredTime = in.readLong();
    }

}
