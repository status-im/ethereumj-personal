package org.ethereum.android.interop;


import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

public class AdminInfo extends org.ethereum.manager.AdminInfo implements Parcelable {

    public AdminInfo(org.ethereum.manager.AdminInfo adminInfo) {

        startupTimeStamp = adminInfo.getStartupTimeStamp();
        consensus = adminInfo.isConsensus();
        blockExecTime = adminInfo.getBlockExecTime();
    }

    @Override
    public int describeContents() {

        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {

        parcel.writeLong(getStartupTimeStamp());
        parcel.writeByte((byte) (isConsensus() ? 1 : 0));
        parcel.writeLongArray(listToArray(getBlockExecTime()));
    }

    private long[] listToArray(List<Long> list) {

        int length = list.size();
        int arrayLength = 0;
        for (int i = 0; i < length; i++) {
            if (list.get(i) != null) {
                arrayLength++;
            }
        }
        long[] array = new long[arrayLength];
        int arrayIndex = 0;
        for (int i = 0; i < length; i++) {
            Long item = list.get(i);
            if (item != null) {
                array[arrayIndex] = item;
                arrayIndex++;
            }
        }
        return array;
    }

    private List<Long> arrayToList(long[] array) {

        ArrayList<Long> list = new ArrayList<>(array.length);
        for (long item : array) {
            list.add(item);
        }
        return list;
    }

    public static final Parcelable.Creator<AdminInfo> CREATOR = new Parcelable.Creator<AdminInfo>() {

        public AdminInfo createFromParcel(Parcel in) {

            return new AdminInfo(in);
        }

        public AdminInfo[] newArray(int size) {

            return new AdminInfo[size];
        }
    };

    private AdminInfo(Parcel in) {

        startupTimeStamp = in.readLong();
        consensus = in.readByte() == 1 ? true : false;
        blockExecTime = arrayToList(in.createLongArray());
    }
}
