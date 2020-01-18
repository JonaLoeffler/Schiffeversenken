package com.jona.common;

import java.util.Set;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.jona.schiffeversenken.Constants;
import com.jona.schiffeversenken.R;

public class DeviceListActivity extends Activity implements Constants {

	private static final String TAG = "DeviceListActivity";

	private BluetoothAdapter mBtAdapter;
	int REQUEST_ENABLE_BT = 1;

	private ArrayAdapter<String> mNewDevicesArrayAdapter;

	private TextView tvPaired;
	private View sep2;
	private ProgressBar pbDiscovery;

	// the onclicklistener for all devices in the listviews
	// gibt MAC adresse des ausgewählten geräts weiter
	private AdapterView.OnItemClickListener mDeviceClickListener = new AdapterView.OnItemClickListener() {
		public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3) {
			// cancel discovery because its costly and were about to connect
			mBtAdapter.cancelDiscovery();

			// get the device MAC address which is the last 17 chars in the
			// clicked view
			String info = ((TextView) v).getText().toString();
			String address = info.substring(info.length() - 17);

			Log.d(TAG, "Found BT-Address " + address);
			// create the result intent and include the mac address
			Intent intent = new Intent();
			intent.putExtra(EXTRA_DEVICE_ADDRESS, address);

			// set result and finish activity
			setResult(Activity.RESULT_OK, intent);
			finish();
		}
	};

	// The BroadcastReceiver that listens for discovered devices and changes the
	// title when discovery is finished
	private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			// When discovery finds a device
			if (BluetoothDevice.ACTION_FOUND.equals(action)) {
				// Get the BluetoothDevice object from the Intent
				BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
				// If it's already paired, skip it, because it's been listed
				// already
				if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
					mNewDevicesArrayAdapter.add(device.getName() + "\n" + device.getAddress());
				}
				// When discovery is finished, change the Activity title
			} else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
				Log.d(TAG, "Discovery finished");
				pbDiscovery.setVisibility(View.GONE);
				setTitle(R.string.select_device);
				if (mNewDevicesArrayAdapter.getCount() == 0) {
					String noDevices = getResources().getText(R.string.none_found).toString();
					mNewDevicesArrayAdapter.add(noDevices);
				}
			}
		}
	};

	// start device discovery
	private void doDiscovery() {
		// indicate scanning
		setTitle(getString(R.string.title_scanning));
		pbDiscovery.setVisibility(View.VISIBLE);
		// tvNew.setVisibility(View.VISIBLE);
		// sep1.setVisibility(View.VISIBLE);

		if (!mBtAdapter.isEnabled()) {
			Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
		}

		// stop possibly running discovery
		if (mBtAdapter.isDiscovering()) {
			mBtAdapter.cancelDiscovery();
		}

		// start new discovery
		mBtAdapter.startDiscovery();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_device_list);
		setTitle(getString(R.string.select_device));
		setResult(Activity.RESULT_CANCELED);

		// set up two arrayadapters, one for bonded, one for new devices
		ArrayAdapter<String> pairedDevicesArrayAdapter = new ArrayAdapter<String>(this, R.layout.device_name);
		mNewDevicesArrayAdapter = new ArrayAdapter<String>(this, R.layout.device_name);

		// find views
		tvPaired = (TextView) findViewById(R.id.title_paired_devices);
		sep2 = (View) findViewById(R.id.sep2);
		pbDiscovery = (ProgressBar) findViewById(R.id.progressbar_discovery);

		// find and set up the listview for paired devices
		ListView pairedListView = (ListView) findViewById(R.id.paired_devices);
		pairedListView.setAdapter(pairedDevicesArrayAdapter);
		pairedListView.setOnItemClickListener(mDeviceClickListener);

		// find and set up the listview for newly discovered devices
		ListView newDevicesListView = (ListView) findViewById(R.id.new_devices);
		newDevicesListView.setVisibility(View.VISIBLE);
		newDevicesListView.setAdapter(mNewDevicesArrayAdapter);
		newDevicesListView.setOnItemClickListener(mDeviceClickListener);

		// register for broadcast when a device is discovered
		IntentFilter filter = new IntentFilter();
		filter.addAction(BluetoothDevice.ACTION_FOUND);
		filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
		this.registerReceiver(mReceiver, filter);

		// get the local bluetooth adapter
		mBtAdapter = BluetoothAdapter.getDefaultAdapter();

		// get a set of currently paired devices
		Set<BluetoothDevice> pairedDevices = mBtAdapter.getBondedDevices();

		// if there are paired devices, add each one to the arrayadapter
		if (pairedDevices.size() > 0) {
			findViewById(R.id.title_paired_devices).setVisibility(View.VISIBLE);
			for (BluetoothDevice device : pairedDevices) {
				pairedDevicesArrayAdapter.add(device.getName() + "\n" + device.getAddress());
			}

			tvPaired.setVisibility(View.VISIBLE);
			sep2.setVisibility(View.VISIBLE);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.device_list, menu);
		return true;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();

		// stop possible discovery
		if (mBtAdapter != null) {
			mBtAdapter.cancelDiscovery();
		}

		// unregister broadcast listeners
		this.unregisterReceiver(mReceiver);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		switch (item.getItemId()) {
		case R.id.action_refresh:
			doDiscovery();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
}