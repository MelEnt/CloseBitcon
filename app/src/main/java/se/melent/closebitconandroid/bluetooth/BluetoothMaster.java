package se.melent.closebitconandroid.bluetooth;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.pm.PackageManager;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import se.melent.closebitconandroid.extra.AutoLog;

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

	/**
	 * Checks if bluetooth is supported on this device
	 * @return true if supported
	 */
	public boolean bluetoothSupported()
	{
		return bluetoothSupported;
	}

	/**
	 * checks if this bluetooth is currently scanning for beacons and/or other devices
	 * @return true when scanning is currently active
	 */
	public boolean isEnabled()
	{
		return bluetoothAdapter.isEnabled();
	}

	/**
	 * tells the android to start/stop scanning for beacons and/or other devices
	 * @param enable true to start, false to stop
	 */
	public void enable(boolean enable)
	{
		if (enable)
		{
			AutoLog.info("Bluetooth scanning was enabled");
			bluetoothAdapter.startLeScan(scanCallbackClass);
		} else
		{
			AutoLog.info("Bluetooth scanning was disabled");
			bluetoothAdapter.stopLeScan(scanCallbackClass);
		}
	}

	// GETTERS //

	/**
	 * returns a list of nearby devices, this list WILL be immutable as it is a direct reference to the list used internally by this class
	 * @return the list of nearby devices
	 */
	public List<BluetoothConnectionInfo> getBluetoothConnections()
	{
		return Collections.unmodifiableList(devices);
	}

	// PING RELATED //

	/**
	 * toggles if the ping service in this class will be active
	 * @param state true will make any inactive connections get removed, false will make any connection stay indefinetly
	 */
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

	/**
	 * used internally to trigger all ping checks on all in memory connections available to bluetooth devices
	 * this will cause devices that arent nearby to get removed
	 *
	 * any changes (such as deletion of a timed out connection) will trigger the on change listeners
	 */
	private void pingCheckAll()
	{
		int devicesRemoved = 0;
		synchronized (devices)
		{
			Iterator<BluetoothConnectionInfo> iter = devices.iterator();
			while (iter.hasNext())
			{
				BluetoothConnectionInfo bci = iter.next();
				if (bci.pingDeadCheck())
				{ // PING TIMEOUT
					iter.remove();
					devicesRemoved++;
				}
			}
		}
		if (devicesRemoved > 0)
		{
			AutoLog.info(devicesRemoved+" device connections timed out by ping");
			triggerListeners();
		}
	}

	// CALLBACKS //

	/**
	 * adds a new OnConnectionsChanged listener to this class, the implementation will be callbacked whenever bluetooth detects a new beacon or device or when a bluetooth device is removed for having a timeout (ping)
	 * @param occ a implementation of OnConnectionsChanged
	 */
	public void addChangeListener(OnConnectionsChanged occ)
	{
		onChangeListeners.add(occ);
	}

	/**
	 * internal method to activate all the listeners from the outside
	 */
	private void triggerListeners()
	{
		List<BluetoothConnectionInfo> immutableDevicesList = getBluetoothConnections();
		for (OnConnectionsChanged occ : onChangeListeners)
		{
			occ.changed(immutableDevicesList);
		}
	}

	// CALLBACK CLASS //

	private class ScanCallbackClass implements BluetoothAdapter.LeScanCallback
	{
		private BluetoothConnectionInfo getConnection(String address)
		{
			for (BluetoothConnectionInfo bci : devices)
			{
				if (bci.getAddress().equals(address))
				{
					return bci;
				}
			}
			return null;
		}

		@Override
		public void onLeScan(BluetoothDevice device, int rssi, byte[] bytes)
		{
			BluetoothConnectionInfo bci = new BluetoothConnectionInfo(device, rssi, bytes);
			synchronized (devices)
			{
				BluetoothConnectionInfo active_bci = getConnection(device.getAddress());
				if (active_bci == null)
				{
					AutoLog.info("Found new device: " + bci.getAddress());
					devices.add(bci);
				}
				else
				{
					active_bci.ping(rssi, bytes);
				}
				triggerListeners();
			}
		}
	}
}
