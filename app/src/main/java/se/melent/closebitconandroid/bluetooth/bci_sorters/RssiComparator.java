package se.melent.closebitconandroid.bluetooth.bci_sorters;

import se.melent.closebitconandroid.bluetooth.BluetoothConnectionInfo;

/**
 * Created by EnderCrypt on 18/05/16.
 */
public class RssiComparator extends AbstrComparator
{
	public RssiComparator(boolean ascending)
	{
		super(ascending);
	}

	@Override
	public int compareConnections(BluetoothConnectionInfo bci_1, BluetoothConnectionInfo bci_2)
	{
		return bci_2.getRssi() - bci_1.getRssi();
	}
}
