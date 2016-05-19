package se.melent.closebitconandroid.bluetooth.bci_sorters;

import java.util.Comparator;

import se.melent.closebitconandroid.bluetooth.BluetoothConnectionInfo;

/**
 * Created by EnderCrypt on 19/05/16.
 */
public abstract class AbstrComparator implements Comparator<BluetoothConnectionInfo>
{
	private Order order;

	public AbstrComparator(Order order)
	{
		this.order = order;
	}

	@Override
	public final int compare(BluetoothConnectionInfo bci_1, BluetoothConnectionInfo bci_2)
	{
		int value = compareConnections(bci_1, bci_2);
		if (order == Order.DESCENDING)
		{
			value = -value;
		}
		return value;
	}

	public abstract int compareConnections(BluetoothConnectionInfo bci_1, BluetoothConnectionInfo bci_2);
}
