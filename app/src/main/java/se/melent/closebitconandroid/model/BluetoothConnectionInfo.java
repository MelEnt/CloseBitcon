package se.melent.closebitconandroid.model;

import android.bluetooth.BluetoothDevice;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by MelEnt on 2016-05-05.
 */
public class BluetoothConnectionInfo implements Parcelable
{
    private BluetoothDevice device;
    private int rssi;
    private byte[] bytes;
    private String address;

    public BluetoothConnectionInfo(BluetoothDevice device, int rssi, byte[] bytes)
    {
        this.device = device;
        this.rssi = rssi;
        this.bytes = bytes;
        address = device.getAddress();
    }

    protected BluetoothConnectionInfo(Parcel in) {
        device = in.readParcelable(BluetoothDevice.class.getClassLoader());
        rssi = in.readInt();
        bytes = in.createByteArray();
        address = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(device, flags);
        dest.writeInt(rssi);
        dest.writeByteArray(bytes);
        dest.writeString(address);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<BluetoothConnectionInfo> CREATOR = new Creator<BluetoothConnectionInfo>() {
        @Override
        public BluetoothConnectionInfo createFromParcel(Parcel in) {
            return new BluetoothConnectionInfo(in);
        }

        @Override
        public BluetoothConnectionInfo[] newArray(int size) {
            return new BluetoothConnectionInfo[size];
        }
    };

    public BluetoothDevice getDevice() {
        return device;
    }

    public int getRssi() {
        return rssi;
    }

    public byte[] getBytes() {
        return bytes;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BluetoothConnectionInfo that = (BluetoothConnectionInfo) o;

        return address.equals(that.address);

    }

    @Override
    public int hashCode()
    {
        return address.hashCode();
    }
}
