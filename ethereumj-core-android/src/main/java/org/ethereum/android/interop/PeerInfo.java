package org.ethereum.android.interop;

import android.os.Parcel;
import android.os.Parcelable;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class PeerInfo extends org.ethereum.net.peerdiscovery.PeerInfo implements Parcelable {

    public PeerInfo(InetAddress ip, int port, String peerId) {

        super(ip, port, peerId);
    }

    public PeerInfo(org.ethereum.net.peerdiscovery.PeerInfo peerInfo) {

        super(peerInfo.getAddress(), peerInfo.getPort(), peerInfo.getPeerId());
    }

    @Override
    public int describeContents() {

        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {

        parcel.writeByteArray(address.getAddress());
        parcel.writeInt(port);
        parcel.writeString(peerId);
    }

    public static final Parcelable.Creator<PeerInfo> CREATOR = new Parcelable.Creator<PeerInfo>() {

        public PeerInfo createFromParcel(Parcel in) {

            return new PeerInfo(in);
        }

        public PeerInfo[] newArray(int size) {

            return new PeerInfo[size];
        }
    };

    private PeerInfo(Parcel in) {

        super(null, 0, "");
        InetAddress ip = null;
        try {
            address = InetAddress.getByAddress(in.createByteArray());
        } catch (UnknownHostException e) {

        }
        port = in.readInt();
        peerId = in.readString();
    }

}
