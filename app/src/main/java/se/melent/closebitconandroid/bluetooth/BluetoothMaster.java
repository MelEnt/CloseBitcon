package se.melent.closebitconandroid.bluetooth;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.pm.PackageManager;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by EnderCrypt on 18/05/16.
 */
public class BluetoothMaster
{
	private final String TAG = BluetoothMaster.class.getSimpleName();

	private Context context;
	private boolean bluetoothSupported = false;

	private boolean isScanning = false;

	private Timer pingTimer = new Timer();
	private Set<OnConnectionsChanged> onChangeListeners = new HashSet<>();

	private List<BluetoothConnectionInfo> devices = new ArrayList<>();
	private BluetoothAdapter bluetoothAdapter;

	private ScanCallbackClass scanCallbackClass = new ScanCallbackClass();

	public BluetoothMaster(Context context)
	{
		this.context = context;
		Activity activity = (Activity) context;

		bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		bluetoothSupported = activity.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE);
	}

	// STATUS METHODS //

	public boolean bluetoothSupported()
	{
		return bluetoothSupported;
	}

	public boolean isEnabled()
	{
		return bluetoothAdapter.isEnabled();
	}

	public void enable(boolean enable)
	{
		if (enable)
		{
			bluetoothAdapter.startLeScan(scanCallbackClass);
		} else
		{
			bluetoothAdapter.stopLeScan(scanCallbackClass);
		}
	}

	// GETTERS //

	public List<BluetoothConnectionInfo> getBluetoothConnections()
	{
		return devices;
	}

	// PING RELATED //

	public void togglePingService(boolean state)
	{
		pingTimer.purge();
		if (state)
		{
			pingTimer.schedule(new TimerTask()
			{
				@Override
				public void run()
				{
					pingCheckAll();
				}
			}, 0, 1000);
		}
	}

	public void pingCheckAll()
	{
		boolean changeOccured = false;
		synchronized (devices)
		{
			Iterator<BluetoothConnectionInfo> iter = devices.iterator();
			while (iter.hasNext())
			{
				BluetoothConnectionInfo bci = iter.next();
				if (bci.pingDeadCheck())
				{ // PING TIMEOUT
					iter.remove();
				}
			}
		}
		if (changeOccured)
		{
			triggerListeners();
		}
	}

	// CALLBACKS //

	public void addChangeListener(OnConnectionsChanged occ)
	{
		onChangeListeners.add(occ);
	}

	private void triggerListeners()
	{
		for (OnConnectionsChanged occ : onChangeListeners)
		{
			occ.changed(getBluetoothConnections());
		}
	}

	// CALLBACK CLASS //

	private class ScanCallbackClass implements BluetoothAdapter.LeScanCallback
	{
		private BluetoothConnectionInfo getConnection(String address)
		{
			for (BluetoothConnectionInfo bci : devices)
			{
				if (bci.getAddress() == address)
				{
					return bci;
				}
			}
			return null;
		}

		@Override
		public void onLeScan(BluetoothDevice device, int rssi, byte[] bytes)
		{
			BluetoothConnectionInfo bci = new BluetoothConnectionInfo(device, rssi, bytes, 3);
			synchronized (devices)
			{
				BluetoothConnectionInfo active_bci = getConnection(device.getAddress());
				if (active_bci == null)
				{
					Log.d(TAG, "Found new device: " + bci.getAddress());
					devices.add(bci);
					triggerListeners();
				}
				else
				{
					active_bci.ping(rssi, bytes);
				}
			}
		}
	}
}
