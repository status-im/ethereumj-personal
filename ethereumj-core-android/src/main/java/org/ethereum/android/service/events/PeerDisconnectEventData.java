package org.ethereum.android.service.events;


import android.os.Parcel;
import android.os.Parcelable;

public class PeerDisconnectEventData extends EventData {

    public String host;
    public long port;

    public PeerDisconnectEventData(String host, long port) {

        super();
        this.host = host;
        this.port = port;
    }

    @Override
    public int describeContents() {

        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {

        super.writeToParcel(parcel, i);
        parcel.writeString(host);
        parcel.writeLong(port);
    }

    public static final Parcelable.Creator<PeerDisconnectEventData> CREATOR = new Parcelable.Creator<PeerDisconnectEventData>() {

        public PeerDisconnectEventData createFromParcel(Parcel in) {

            return new PeerDisconnectEventData(in);
        }

        public PeerDisconnectEventData[] newArray(int size) {

            return new PeerDisconnectEventData[size];
        }
    };

    private PeerDisconnectEventData(Parcel in) {

        super(in);
        host = in.readString();
        port = in.readLong();
    }
}
