/*
 * Copyright (c) 2015, Nordic Semiconductor
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its contributors may be used to endorse or promote products derived from this
 * software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE
 * USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package no.nordicsemi.android.vega.profile;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.util.Log;

import java.io.UnsupportedEncodingException;
import java.util.Deque;
import java.util.LinkedList;
import java.util.UUID;

import no.nordicsemi.android.ble.BleManager;
import no.nordicsemi.android.ble.Request;
import no.nordicsemi.android.log.LogContract;
import no.nordicsemi.android.vega.utils.Utils;

public class BlinkyManager extends BleManager<BlinkyManagerCallbacks> {
	/**
	 * Nordic Blinky Service UUID
	 */
	public final static UUID LBS_UUID_SERVICE = UUID.fromString("9c201400-1c13-8b49-9236-040a580c61b8");
	public final static UUID DIS_UUID_SERVICE = UUID.fromString("0000180A-0000-1000-8000-00805F9B34FB");
	public final static UUID HTS_UUID_SERVICE = UUID.fromString("00001809-0000-1000-8000-00805F9B34FB");
	/**
	 * BUTTON characteristic UUID
	 */
	private final static UUID LBS_UUID_STATUS = UUID.fromString("9c201402-1c13-8b49-9236-040a580c61b8");
	//private final static UUID LBS_UUID_STATUS = UUID.fromString("00001524-1212-efde-1523-785feabcd123");
	/**
	 * LED characteristic UUID
	 */
	private final static UUID LBS_UUID_EVENT
			= UUID.fromString("9c201401-1c13-8b49-9236-040a580c61b8");
	//private final static UUID LBS_UUID_LED_CHAR = UUID.fromString("00001525-1212-efde-1523-785feabcd123");
	private final static UUID DIS_UUID_MODEL_NUMBER = UUID.fromString("00002A24-0000-1000-8000-00805F9B34FB");
	private final static UUID DIS_UUID_SERIAL = UUID.fromString("00002A23-0000-1000-8000-00805F9B34FB");

	private final static UUID HTS_UUID_TEMPERATURE = UUID.fromString("00002A1C-0000-1000-8000-00805F9B34FB");

	private BluetoothGattCharacteristic mStatusCharacteristic, mEventCharacteristic;
	private BluetoothGattCharacteristic mRevisionCharacteristic, mSerialCharacteristic;
	private BluetoothGattCharacteristic mTemperatureCharacteristic;

	public BlinkyManager(final Context context) {
		super(context);
	}

	@Override
	protected BleManagerGattCallback getGattCallback() {
		return mGattCallback;
	}

	@Override
	protected boolean shouldAutoConnect() {
		// If you want to connect to the device using autoConnect flag = true, return true here.
		// Read the documentation of this method.
		return super.shouldAutoConnect();
	}

	/**
	 * BluetoothGatt callbacks for connection/disconnection, service discovery, receiving indication, etc
	 */
	private final BleManagerGattCallback mGattCallback = new BleManagerGattCallback() {

		@Override
		protected Deque<Request> initGatt(final BluetoothGatt gatt) {
			Log.e("TEST", "initGatt");
			final LinkedList<Request> requests = new LinkedList<>();
			requests.push(Request.newReadRequest(mEventCharacteristic));
			requests.push(Request.newReadRequest(mStatusCharacteristic));
			requests.push(Request.newReadRequest(mRevisionCharacteristic));
			requests.push(Request.newReadRequest(mSerialCharacteristic));
			requests.push(Request.newEnableNotificationsRequest(mEventCharacteristic));
			requests.push(Request.newEnableNotificationsRequest(mStatusCharacteristic));
			return requests;
		}

		@Override
		public boolean isRequiredServiceSupported(final BluetoothGatt gatt) {
			final BluetoothGattService service = gatt.getService(LBS_UUID_SERVICE);
			final BluetoothGattService di_service =  gatt.getService(DIS_UUID_SERVICE);
			final BluetoothGattService ht_service =  gatt.getService(HTS_UUID_SERVICE);
			Log.e("TEST", "isRequiredServiceSupported");
			if (service != null) {
				Log.e("TEST", "LBS service  found");
				mStatusCharacteristic = service.getCharacteristic(LBS_UUID_STATUS);
				mEventCharacteristic = service.getCharacteristic(LBS_UUID_EVENT);
			} else {
				Log.e("TEST", "service == NULL");
			}

			if (di_service != null) {
				Log.e("TEST", "DIS service found ");
				mRevisionCharacteristic = di_service.getCharacteristic(DIS_UUID_MODEL_NUMBER);
				mSerialCharacteristic = di_service.getCharacteristic(DIS_UUID_SERIAL);
			} else {
				Log.e("TEST", "di_service == NULL");
			}

			if (ht_service != null) {
				Log.e("TEST", "HTS service found ");
				mTemperatureCharacteristic = ht_service.getCharacteristic(HTS_UUID_TEMPERATURE);
			} else {
				Log.e("TEST", "di_service == NULL");
			}

			boolean writeRequest = false;
			if (mEventCharacteristic != null) {
				Log.e("TEST", "mEventCharacteristic != NULL");
				final int rxProperties = mEventCharacteristic.getProperties();
				writeRequest = (rxProperties & BluetoothGattCharacteristic.PROPERTY_WRITE) > 0;
			} else {
				Log.e("TEST", "mEventCharacteristic == NULL");
			}

			return  mStatusCharacteristic != null && mEventCharacteristic != null && writeRequest;
		}

		@Override
		protected void onDeviceDisconnected() {
			mStatusCharacteristic = null;
			mEventCharacteristic = null;
			mRevisionCharacteristic = null;
			mSerialCharacteristic = null;
		}

		@Override
		protected void onCharacteristicRead(final BluetoothGatt gatt, final BluetoothGattCharacteristic characteristic) {
			//final int data = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT32, 0);
			final byte[] data = characteristic.getValue();
			if (characteristic == mEventCharacteristic) {
				//String str = Base64.encodeToString(data[0], Base64.DEFAULT);
				StringBuffer str = new StringBuffer();
				for (byte b : data) {
					int intVal = b & 0xff;
					if (intVal < 0x10) str.append("0");
					str.append(Integer.toHexString(intVal));
				}
				Log.e("TEST", "characteristic == mEventCharacteristic = " + str);
				final boolean ledOn = (data[0]==1);
				//mCallbacks.onDataSent(ledOn);
			} else if (characteristic == mStatusCharacteristic) {
				Log.e("TEST", "characteristic == mStatusCharacteristic");
				final boolean buttonPressed = (data[0]==1);
				mCallbacks.onDataReceived(data);
			} else if (characteristic == mRevisionCharacteristic) {
				Log.e("TEST", "characteristic == mRevisionCharacteristic");
				try {
					mCallbacks.onRevisionReceived(new String(data, "UTF-8"));
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}

			}  else if (characteristic == mSerialCharacteristic) {
				Log.e("TEST", "characteristic == mSerialCharacteristic");
				mCallbacks.onSerialReceived(data);

			}
		}

		@Override
		public void onCharacteristicWrite(final BluetoothGatt gatt, final BluetoothGattCharacteristic characteristic) {
			// This method is only called for LED characteristic
			final int data = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 0);
			final boolean ledOn = data == 0x01;
			log(LogContract.Log.Level.APPLICATION, "LED " + (ledOn ? "ON" : "OFF"));
			mCallbacks.onDataSent(ledOn);
		}

		@Override
		public void onCharacteristicNotified(final BluetoothGatt gatt, final BluetoothGattCharacteristic characteristic) {
			// This method is only called for Button characteristic
			final byte[] data = characteristic.getValue();
			StringBuffer str = new StringBuffer();
			for (byte b : data) {
				int intVal = b & 0xff;
				if (intVal < 0x10) str.append("0");
				str.append(Integer.toHexString(intVal));
			}
			if (characteristic == mEventCharacteristic) {
				Log.e("TEST", "characteristic == mEventCharacteristic = " + str);
				mCallbacks.onDataReceived(data);
			} else if (characteristic == mTemperatureCharacteristic) {
				mCallbacks.onTemperatureReceived(Utils.TemperatureFromCharacteristicValue(data));
			}
		}
	};

	public void send(final byte value) {
		// Are we connected?
		if (mEventCharacteristic == null)
			return;

		final byte[] command = new byte[] {(byte) 0xff, value};
		mEventCharacteristic.setValue(command);
		writeCharacteristic( mEventCharacteristic, command);
	}

	public void write(final byte[] cmd) {
		// Are we connected?
		if (mEventCharacteristic == null)
			return;

		mEventCharacteristic.setValue(cmd);
		writeCharacteristic(mEventCharacteristic, cmd);
	}
}
