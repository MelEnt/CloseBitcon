package se.melent.closebitconandroid;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;

/**
 * Created by MelEnt on 2016-05-05.
 */
public class BluetoothConnectionInfo
{
    private BluetoothDevice device;
    private int rssi;
    private byte[] bytes;

    public BluetoothConnectionInfo(BluetoothDevice device, int rssi, byte[] bytes)
    {
        this.device = device;
        this.rssi = rssi;
        this.bytes = bytes;
    }

    public BluetoothDevice getDevice() {
        return device;
    }

    public int getRssi() {
        return rssi;
    }

    public byte[] getBytes() {
        return bytes;
    }


}
