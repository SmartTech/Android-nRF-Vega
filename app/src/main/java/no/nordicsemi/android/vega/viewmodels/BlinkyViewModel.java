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
	private final MutableLiveData<byte[]> mLoraState = new MutableLiveData<byte[]>();
	private final MutableLiveData<Integer> mStatusState = new MutableLiveData<>();

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
	public LiveData<Integer> getStatusState() {
		return mStatusState;
	}

	public BlinkyViewModel(@NonNull final Application application) {
		super(application);

		// Initialize the manager
		mBlinkyManager = new BlinkyManager(getApplication());
		mBlinkyManager.setGattCallbacks(this);
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
		final byte[] command = {2, 1, 0, 0};
		mBlinkyManager.write(command);
		Log.e("ble", "requestInfo");
	}

	public void prearm() {
		final byte[] command = {0, 0, 0, 0};
		mBlinkyManager.write(command);
	}

	public void armConfirm() {
		final byte[] command = {0, 1, 0, 0};
		mBlinkyManager.write(command);
	}
	public void armDiscard() {
		final byte[] command = {0, 2, 0, 0};
		mBlinkyManager.write(command);
	}
	public void disarm() {
		final byte[] command = {0, 3, 0, 0};
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
		// CHAR_CMD_ARM
		if(data[0]==0) {
			mArmState.postValue((int) data[1]);
		}
		// CHAR_CMD_LORA
		else if(data[0]==1) {
			mLoraState.postValue(data);
		}
		else if(data[0]==4) {
			mStatusState.postValue((int) data[1]);
		}
		//mButtonState.postValue(state);

	}

	@Override
	public void onDataSent(final boolean state) {
		mLEDState.postValue(state);
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

    public void addLora(byte[] addr) {
		final byte[] command = {1, 4, addr[0], addr[1], addr[2], addr[3], addr[4]};
		mBlinkyManager.write(command);

	}
}
