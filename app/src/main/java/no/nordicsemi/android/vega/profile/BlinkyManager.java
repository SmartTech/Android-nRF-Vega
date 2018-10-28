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

import java.util.Deque;
import java.util.LinkedList;
import java.util.UUID;

import no.nordicsemi.android.ble.BleManager;
import no.nordicsemi.android.ble.Request;
import no.nordicsemi.android.log.LogContract;

public class BlinkyManager extends BleManager<BlinkyManagerCallbacks> {
	/**
	 * Nordic Blinky Service UUID
	 */
	public final static UUID LBS_UUID_SERVICE = UUID.fromString("9c201400-1c13-8b49-9236-040a580c61b8");
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

	private BluetoothGattCharacteristic mStatusCharacteristic, mEventCharacteristic;

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
			requests.push(Request.newEnableNotificationsRequest(mEventCharacteristic));
			requests.push(Request.newEnableNotificationsRequest(mStatusCharacteristic));
			return requests;
		}

		@Override
		public boolean isRequiredServiceSupported(final BluetoothGatt gatt) {
			final BluetoothGattService service = gatt.getService(LBS_UUID_SERVICE);
			Log.e("TEST", "isRequiredServiceSupported");
			if (service != null) {
				Log.e("TEST", "service != NULL");
				mStatusCharacteristic = service.getCharacteristic(LBS_UUID_STATUS);
				mEventCharacteristic = service.getCharacteristic(LBS_UUID_EVENT);
			} else {
				Log.e("TEST", "service == NULL");
			}

			boolean writeRequest = false;
			if (mEventCharacteristic != null) {
				Log.e("TEST", "mEventCharacteristic != NULL");
				final int rxProperties = mEventCharacteristic.getProperties();
				writeRequest = (rxProperties & BluetoothGattCharacteristic.PROPERTY_WRITE) > 0;
			} else {
				Log.e("TEST", "mEventCharacteristic == NULL");
			}

			return mStatusCharacteristic != null && mEventCharacteristic != null && writeRequest;
		}

		@Override
		protected void onDeviceDisconnected() {
			mStatusCharacteristic = null;
			mEventCharacteristic = null;
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
			} else {
				Log.e("TEST", "characteristic == mStatusCharacteristic");
				final boolean buttonPressed = (data[0]==1);
				mCallbacks.onDataReceived(data);
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
			final byte[] data2 = characteristic.getValue();
			StringBuffer str = new StringBuffer();
			for (byte b : data2) {
				int intVal = b & 0xff;
				if (intVal < 0x10) str.append("0");
				str.append(Integer.toHexString(intVal));
			}
			Log.e("TEST", "characteristic == mEventCharacteristic = " + str);
			final int data = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT32, 0);
			final boolean buttonPressed = data == 0x01;
			log(LogContract.Log.Level.APPLICATION, "Button " + (buttonPressed ? "pressed" : "released"));
			mCallbacks.onDataReceived(data2);
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
