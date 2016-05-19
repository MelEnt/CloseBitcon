package se.melent.closebitconandroid.bluetooth;

import java.util.Comparator;

import se.melent.closebitconandroid.bluetooth.BluetoothConnectionInfo;

/**
 * Created by EnderCrypt on 18/05/16.
 */
public class RssiComparator implements Comparator<BluetoothConnectionInfo>
{
	@Override
	public int compare(BluetoothConnectionInfo bci_1, BluetoothConnectionInfo bci_2)
	{
		return bci_2.getRssi() - bci_1.getRssi();
	}
}
