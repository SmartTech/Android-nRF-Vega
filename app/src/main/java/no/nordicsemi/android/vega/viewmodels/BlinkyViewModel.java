/*
 * Copyright (c) 2015, Nordic Semiconductor
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *  Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 *
 *  Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 *  Neither the name of copyright holder nor the names of its
 *   contributors may be used to endorse or promote products derived from
 *   this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package no.nordicsemi.android.vega.viewmodels;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.bluetooth.BluetoothDevice;
import android.support.annotation.NonNull;
import android.util.Log;

import java.util.ArrayList;

import butterknife.internal.Utils;
import no.nordicsemi.android.vega.LoraItem;
import no.nordicsemi.android.vega.R;
import no.nordicsemi.android.vega.adapter.ExtendedBluetoothDevice;
import no.nordicsemi.android.vega.profile.BlinkyManager;
import no.nordicsemi.android.vega.profile.BlinkyManagerCallbacks;
import no.nordicsemi.android.log.LogSession;
import no.nordicsemi.android.log.Logger;

public class BlinkyViewModel extends AndroidViewModel implements BlinkyManagerCallbacks {
	private final BlinkyManager mBlinkyManager;

	// Connection states Connecting, Connected, Disconnecting, Disconnected etc.
	private final MutableLiveData<String> mConnectionState = new MutableLiveData<>();

	// Flag to determine if the device is connected
	private final MutableLiveData<Boolean> mIsConnected = new MutableLiveData<>();

	// Flag to determine if the device is ready
	private final MutableLiveData<Void> mOnDeviceReady = new MutableLiveData<>();

	// Flag that holds the on off state of the LED. On is true, Off is False
	private final MutableLiveData<Boolean> mLEDState = new MutableLiveData<>();
	private final MutableLiveData<Integer> mArmState = new MutableLiveData<>();
    private final MutableLiveData<byte[]> mSerialNumber = new MutableLiveData<>();
    private final MutableLiveData<Integer> mTemperature = new MutableLiveData<>();
	private final MutableLiveData<byte[]> mLoraState = new MutableLiveData<>();
	private final MutableLiveData<byte[]> mStatusState = new MutableLiveData<>();
    private final ArrayList<LoraItem> mLoraItemsValues = new ArrayList<LoraItem>();
    private final MutableLiveData<ArrayList<LoraItem>> mLoraItems = new MutableLiveData<>();
//	private int mLoraCount = 0;

	// Flag that holds the pressed released state of the button on the devkit. Pressed is true, Released is False
	private final MutableLiveData<Boolean> mButtonState = new MutableLiveData<>();

	public LiveData<Void> isDeviceReady() {
		return mOnDeviceReady;
	}

	public LiveData<String> getConnectionState() {
		return mConnectionState;
	}

	public LiveData<Boolean> isConnected() {
		return mIsConnected;
	}

	public LiveData<Boolean> getButtonState() {
		return mButtonState;
	}

	public LiveData<Boolean> getLEDState() {
		return mLEDState;
	}

	public LiveData<Integer> getArmState() {
		return mArmState;
	}
	public MutableLiveData<byte[]> getLoraState() {
		return mLoraState;
	}
	public LiveData<byte[]> getStatusState() {
		return mStatusState;
	}

    public LiveData<byte[]> getSerialNumber() {
        return mSerialNumber;
    }

    public LiveData<Integer> getTemperature() {
        return mTemperature;
    }
	public MutableLiveData<ArrayList<LoraItem>> getLoraItems() {
		return mLoraItems;
	}

    public BlinkyViewModel(@NonNull final Application application) {
		super(application);

		// Initialize the manager
		mBlinkyManager = new BlinkyManager(getApplication());
		mBlinkyManager.setGattCallbacks(this);
		mLoraItems.setValue(mLoraItemsValues);
	}

	/**
	 * Connect to peripheral
	 */
	public void connect(final ExtendedBluetoothDevice device) {
		final LogSession logSession = Logger.newSession(getApplication(), null, device.getAddress(), device.getName());
		mBlinkyManager.setLogger(logSession);
		mBlinkyManager.connect(device.getDevice());
	}

	/**
	 * Disconnect from peripheral
	 */
	private void disconnect() {
		mBlinkyManager.disconnect();
	}

	public void toggleLED(final boolean onOff) {
		mBlinkyManager.send((byte) (onOff?1:0));
		mLEDState.setValue(onOff);
	}

	public void requestInfo() {
        final byte[] command = {2};
		mBlinkyManager.write(command);
		Log.e("ble", "requestInfo " );
	}

	public void prearm() {
        final byte[] command = {0, 0};
		mBlinkyManager.write(command);
	}

	public void armConfirm() {
        final byte[] command = {0, 1};
		mBlinkyManager.write(command);
	}
	public void armDiscard() {
        final byte[] command = {0, 2};
		mBlinkyManager.write(command);
	}
	public void disarm() {
        final byte[] command = {0, 3};
		mBlinkyManager.write(command);
	}


	@Override
	protected void onCleared() {
		super.onCleared();
		if (mBlinkyManager.isConnected()) {
			disconnect();
		}
	}

	@Override
	public void onDataReceived(final byte[] data) {

		Log.e("onDataReceived", "cmd = "+data[0]);

		if ( data.length > 0) {
		switch(data[1]) {
			// CHAR_LORA_COUNT
//			case 0 : {
//				if(mLoraCount>0) {
//					mLoraItems.getValue().clear();
//				}
//				mLoraCount = data[2];

//				Log.e("mLORA_count", String.valueOf(mLoraCount));
//			} break;
			// CHAR_LORA_ADDR
			case 1 : {
				Log.e("mLORA_addr", String.valueOf(data[2]));
//				if(data[2]<mLoraItems.getValue().size()) {
					StringBuffer addr = new StringBuffer();
					for(int i=0; i<5; i++) {
						int intVal = data[3+i] & 0xff;
						if (intVal < 0x10) addr.append("0");
						addr.append(Integer.toHexString(intVal));
					}
					mLoraItems.getValue().add(new LoraItem(addr.toString()));
					Log.e("add lora addr", String.valueOf(data[2]));
//				}
				mLoraItems.postValue(mLoraItemsValues);
			} break;
			// CHAR_LORA_DATA
			case 2 : {

			} break;
			// CHAR_LORA_GET
			case 3 : {

			} break;
			// CHAR_LORA_ADD
			case 4 : {

			} break;
			// CHAR_LORA_DEL
			case 5 : {

			} break;
			default: break;
		};

		// CHAR_CMD_ARM
			if (data[0] == 0) {
				mArmState.postValue((int) data[1]);
			}
			// CHAR_CMD_LORA
			else if (data[0] == 1) {
				mLoraState.postValue(data);
				Log.e("onDataReceivedPostData", "cmd = "+data[0]);

			} else if (data[0] == 3) {
				mStatusState.postValue( data);
			}
		}
		//mButtonState.postValue(state);

	}

	@Override
	public void onDataSent(final boolean state) {
		mLEDState.postValue(state);
	}

    @Override
    public void onRevisionReceived(String revision) {

    }

    @Override
    public void onSerialReceived(byte[] serial) {
        mSerialNumber.setValue(serial);
    }

    @Override
    public void onTemperatureReceived(Integer temp) {
        mTemperature.setValue(temp);
    }

    @Override
	public void onDeviceConnecting(final BluetoothDevice device) {
		mConnectionState.postValue(getApplication().getString(R.string.state_connecting));
	}

	@Override
	public void onDeviceConnected(final BluetoothDevice device) {
		mIsConnected.postValue(true);
		mConnectionState.postValue(getApplication().getString(R.string.state_discovering_services));
	}

	@Override
	public void onDeviceDisconnecting(final BluetoothDevice device) {
		mIsConnected.postValue(false);
	}

	@Override
	public void onDeviceDisconnected(final BluetoothDevice device) {
		mIsConnected.postValue(false);
	}

	@Override
	public void onLinkLossOccurred(BluetoothDevice device) {

	}

	//@Override
	//public void onLinklossOccur(final BluetoothDevice device) {
	//	mIsConnected.postValue(false);
	//}

	@Override
	public void onServicesDiscovered(final BluetoothDevice device, final boolean optionalServicesFound) {
		mConnectionState.postValue(getApplication().getString(R.string.state_initializing));

	}

	@Override
	public void onDeviceReady(final BluetoothDevice device) {
		mConnectionState.postValue(getApplication().getString(R.string.state_discovering_services_completed, device.getName()));
		mOnDeviceReady.postValue(null);
	}

	@Override
	public boolean shouldEnableBatteryLevelNotifications(final BluetoothDevice device) {
		// Blinky doesn't have Battery Service
		return false;
	}

	@Override
	public void onBatteryValueReceived(final BluetoothDevice device, final int value) {
		// Blinky doesn't have Battery Service
	}

	@Override
	public void onBondingRequired(final BluetoothDevice device) {
		// Blinky does not require bonding
	}

	@Override
	public void onBonded(final BluetoothDevice device) {
		// Blinky does not require bonding
	}

	@Override
	public void onBondingFailed(BluetoothDevice device) {

	}

	@Override
	public void onError(final BluetoothDevice device, final String message, final int errorCode) {
		// TODO implement
	}

	@Override
	public void onDeviceNotSupported(final BluetoothDevice device) {
		// TODO implement
	}

//    public void addLora(byte[] addr) {
//		final byte[] command = {1, 4, addr[0], addr[1], addr[2], addr[3], addr[4]};
//		mBlinkyManager.write(command);
//
	//}
public void getLora(int index) {
	Log.e("write", "getLora "+index);
	final byte[] command = {1, 3, (byte) index};
	mBlinkyManager.write(command);
}
	public void addLora(byte[] addr) {
		Log.e("write", "addLora "+addr[0]+" "+addr[1]+" "+addr[2]+" "+addr[3]+" "+addr[4]);
		final byte[] command = {1, 4, addr[0], addr[1], addr[2], addr[3], addr[4]};
		mBlinkyManager.write(command);
	}
	public void deleteLoraNum(int index) {
		if(index>=0) {
			final byte[] command = {1, 5, (byte) index};
			mBlinkyManager.write(command);
		}
	}
	public void deleteLora(byte[] addr) {
		Log.e("write", "deleteLora "+addr[0]+" "+addr[1]+" "+addr[2]+" "+addr[3]+" "+addr[4]);
		final byte[] command = {1, 5, addr[0], addr[1], addr[2], addr[3], addr[4]};
		mBlinkyManager.write(command);
	}
}
