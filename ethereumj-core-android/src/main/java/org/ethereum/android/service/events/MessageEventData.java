package org.ethereum.android.service.events;


import android.os.Parcel;
import android.os.Parcelable;

public class MessageEventData extends EventData {

    public Class messageClass;
    public byte[] message;

    public MessageEventData(Class messageClass, byte[] encodedMessage) {

        super();
        this.messageClass = messageClass;
        this.message = encodedMessage;
    }

    @Override
    public int describeContents() {

        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {

        super.writeToParcel(parcel, i);
        parcel.writeInt(message.length);
        parcel.writeByteArray(message);
        parcel.writeSerializable(messageClass);
    }

    public static final Parcelable.Creator<MessageEventData> CREATOR = new Parcelable.Creator<MessageEventData>() {

        public MessageEventData createFromParcel(Parcel in) {

            return new MessageEventData(in);
        }

        public MessageEventData[] newArray(int size) {

            return new MessageEventData[size];
        }
    };

    private MessageEventData(Parcel in) {

        super(in);
        message = new byte[in.readInt()];
        in.readByteArray(message);
        messageClass = (Class)in.readSerializable();
    }
}
