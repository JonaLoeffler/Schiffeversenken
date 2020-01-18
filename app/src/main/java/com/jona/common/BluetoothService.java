package com.jona.common;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.jona.schiffeversenken.Constants;

public class BluetoothService implements Constants {

	// debugging
	private static final String TAG = "BluetoothService";
	// name for the SDP record when creating server socket
	private static final String NAME_SECURE = "BluetoothGameSecure";
	// generierte UUID für diese App
	private static final UUID MY_UUID = UUID.fromString("b9e0a882-6af0-4839-bbad-cc536f187e83");
	// attribute
	private final BluetoothAdapter mAdapter;
	private final Handler mHandler;
	private AcceptThread mAcceptThread;
	private ConnectThread mConnectThread;
	private ConnectedThread mConnectedThread;
	private int mState;

	// TODO AcceptThread
	private class AcceptThread extends Thread {

		// local server socket
		private final BluetoothServerSocket mmServerSocket;

		public AcceptThread() {
			BluetoothServerSocket tmp = null;
			try {
				tmp = mAdapter.listenUsingRfcommWithServiceRecord(NAME_SECURE, MY_UUID);
			} catch (IOException e) {
				e.printStackTrace();
			}

			if (tmp == null)
				Log.d(TAG, "mmServersocket = null");
			mmServerSocket = tmp;
		}

		public void cancel() {
			try {
				mmServerSocket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		public void run() {
			Log.d(TAG, "Starting AcceptThread");

			setName("AcceptThread");
			BluetoothSocket socket = null;

			// listens to server socket while not connected
			while (mState != STATE_CONNECTED) {
				try {
					// only returns when connection is successfull or an
					// exception has occurred
					socket = mmServerSocket.accept();
				} catch (IOException e) {
					e.printStackTrace();
					break;
				}

				// if a connection was accepted
				if (socket != null) {
					synchronized (BluetoothService.this) {
						switch (mState) {
						case STATE_LISTEN:
						case STATE_CONNECTING:
							// situation normal, start the connection thread
							connected(socket, socket.getRemoteDevice(), "Secure");
							break;
						case STATE_NONE:
						case STATE_CONNECTED:
							// either not ready or already connected. terminate
							// new socket.
							try {
								socket.close();
							} catch (IOException e) {
								e.printStackTrace();
							}
							break;
						}
					}
				}
			}
		}
	}

	/**
	 * der laufende Thread bei einer bestehenden verbindung. verwaltet
	 * eingehende und ausgehende daten
	 */
	// TODO ConnectedThread
	private class ConnectedThread extends Thread {
		private final BluetoothSocket mmSocket;
		private final InputStream mmInStream;
		private final OutputStream mmOutStream;

		public ConnectedThread(BluetoothSocket socket, String socketType) {
			mmSocket = socket;

			InputStream tmpIn = null;
			OutputStream tmpOut = null;

			try {
				tmpIn = socket.getInputStream();
				tmpOut = socket.getOutputStream();
			} catch (IOException e) {
				e.printStackTrace();
			}

			mmInStream = tmpIn;
			mmOutStream = tmpOut;
		}

		public void cancel() {
			try {
				mmSocket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		public void run() {
			Log.d(TAG, "Starting ConnectedThread");
			byte[] buffer = new byte[1024];
			int bytes;

			// keep listening to the inputstream while connected
			while (true) {
				try {
					// read from inputstream
					bytes = mmInStream.read(buffer);

					// send obtained bytes to UI activity
					mHandler.obtainMessage(Constants.MESSAGE_READ, bytes, -1, buffer).sendToTarget();
				} catch (IOException e) {
					e.printStackTrace();
					connectionLost();
					BluetoothService.this.start();
					break;
				}
			}
		}

		public void write(byte[] buffer) {
			try {
				Log.d(TAG, "Sending message to " + mmSocket.getRemoteDevice().getName());

				mmOutStream.write(buffer);

				// share the sent message back to the UI Activity
				mHandler.obtainMessage(Constants.MESSAGE_WRITE, -1, -1, buffer).sendToTarget();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	// TODO ConnectThread
	private class ConnectThread extends Thread {
		private final BluetoothSocket mmSocket;
		private final BluetoothDevice mmDevice;

		public ConnectThread(BluetoothDevice device, String socketType) {
			BluetoothSocket tmp = null;
			mmDevice = device;

			try {
				tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
			} catch (IOException e) {
				e.printStackTrace();
			}
			mmSocket = tmp;
		}

		public void cancel() {
			try {
				mmSocket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		public void run() {
			Log.d(TAG, "Starting ConnectThread");
			setName("ConnectThread");

			// cancelling the discovery
			mAdapter.cancelDiscovery();

			// make a connection to the bluetoothsocket
			try {
				// only returns when connection is successfull or an
				// exception has occurred
				mmSocket.connect();
			} catch (IOException e) {
				// close the socket
				try {
					mmSocket.close();
				} catch (IOException e2) {
					e.printStackTrace();
				}
				connectionFailed();
				return;
			}

			// reset the Connectthread because were done
			synchronized (BluetoothService.this) {
				mConnectThread = null;
			}

			// start the ConnectedThread
			connected(mmSocket, mmDevice, "Secure");
		}
	}

	// constructor for service, param handler sends messages back to the UI
	public BluetoothService(Context context, Handler handler) {
		mAdapter = BluetoothAdapter.getDefaultAdapter();
		mState = STATE_NONE;
		mHandler = handler;
	}

	// start the connectThread to initiate a connection to a remote device
	public synchronized void connect(BluetoothDevice device) {

		// cancel any thread attempting to make a connection
		if (mState == STATE_CONNECTING) {
			if (mConnectThread != null) {
				mConnectThread.cancel();
				mConnectThread = null;
			}
		}

		// cancel any thread currently running a connection
		if (mConnectedThread != null) {
			mConnectedThread.cancel();
			mConnectedThread = null;
		}

		// start the connectthread to connect with the new given device
		mConnectThread = new ConnectThread(device, "Secure");
		mConnectThread.start();
		setState(STATE_CONNECTING);
	}

	// start connectedThread to begin managing a bluetooth connection
	public synchronized void connected(BluetoothSocket socket, BluetoothDevice device, final String socketType) {

		// cancel the thread that completed the connection
		if (mConnectThread != null) {
			mConnectThread.cancel();
			mConnectThread = null;
		}

		// cancel any thread currently running a connection
		if (mConnectedThread != null) {
			mConnectedThread.cancel();
			mConnectedThread = null;
		}

		// cancel the acceptThread because we only want one connected device
		if (mAcceptThread != null) {
			mAcceptThread.cancel();
			mAcceptThread = null;
		}

		// start the connected thread to manage the connection and perform
		// transmissions
		mConnectedThread = new ConnectedThread(socket, "Secure");
		mConnectedThread.start();

		// send the name of the connected device back to the ui activity
		Message msg = mHandler.obtainMessage(Constants.MESSAGE_DEVICE_NAME);
		Bundle bundle = new Bundle();
		bundle.putString(Constants.DEVICE_NAME, device.getName());
		msg.setData(bundle);
		mHandler.sendMessage(msg);

		setState(STATE_CONNECTED);
	}

	// indicate that the connection attempt failed and notify the UI Activity
	private void connectionFailed() {
		// send a failure message back to the activity
		Message msg = mHandler.obtainMessage(Constants.MESSAGE_FAILURE);
		Bundle bundle = new Bundle();
		bundle.putString(Constants.FAILURE, "Unable to connect to device");
		msg.setData(bundle);
		mHandler.sendMessage(msg);

		// start the server over to restart listening mode
		BluetoothService.this.start();
	}

	// Indicate that the connection was lost and notify the UI Activity.
	private void connectionLost() {
		// Send a failure message back to the Activity
		Message msg = mHandler.obtainMessage(Constants.MESSAGE_FAILURE);
		Bundle bundle = new Bundle();
		bundle.putString(Constants.FAILURE, "Device connection was lost");
		msg.setData(bundle);
		mHandler.sendMessage(msg);

		// Start the service over to restart listening mode
		BluetoothService.this.start();
	}

	// gibt aktuellen status zurück
	public synchronized int getState() {
		return mState;
	}

	// setzt den aktuellen status
	private synchronized void setState(int aState) {
		mState = aState;

		// give the new state to the Handler so the UI activity can update
		mHandler.obtainMessage(Constants.MESSAGE_CONNECTION_STATE_CHANGE, aState, -1).sendToTarget();
	}

	// Start the chat service. Specifically start AcceptThread to begin a
	// session in listening (server) mode. Called by the Activity onResume()
	public synchronized void start() {

		// cancel any thread attempting to make a connection
		if (mConnectThread != null) {
			mConnectThread.cancel();
			mConnectThread = null;
		}

		// cancel any thread currently running a connection
		if (mConnectedThread != null) {
			mConnectedThread.cancel();
			mConnectedThread = null;
		}

		setState(STATE_LISTEN);

		// start the acceptthread to listen on a bluetoothserversocket
		if (mAcceptThread == null) {
			mAcceptThread = new AcceptThread();
			mAcceptThread.start();
		}
	}

	// stop all threads
	public synchronized void stop() {

		if (mConnectThread != null) {
			mConnectThread.cancel();
			mConnectThread = null;
		}

		if (mConnectedThread != null) {
			mConnectedThread.cancel();
			mConnectedThread = null;
		}

		if (mAcceptThread != null) {
			mAcceptThread.cancel();
			mAcceptThread = null;
		}

		setState(STATE_NONE);
	}

	// write to the connectedthread in an unsynchronized manner
	public void write(byte[] out) {
		// create temporary object
		ConnectedThread r;
		synchronized (this) {
			if (mState != STATE_CONNECTED)
				return;
			r = mConnectedThread;
		}
		// perform the write unsynchronized
		r.write(out);
	}
}
