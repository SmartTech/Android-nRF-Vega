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
import android.widget.EditText;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;

import no.nordicsemi.android.log.LogSession;
import no.nordicsemi.android.log.Logger;
import no.nordicsemi.android.vega.LoraItem;
import no.nordicsemi.android.vega.R;
import no.nordicsemi.android.vega.adapter.ExtendedBluetoothDevice;
import no.nordicsemi.android.vega.profile.BlinkyManager;
import no.nordicsemi.android.vega.profile.BlinkyManagerCallbacks;

import static java.lang.Integer.valueOf;

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
	private final MutableLiveData<Integer> mGSM = new MutableLiveData<>();
    private final MutableLiveData<byte[]> mSerialNumber = new MutableLiveData<>();
	private final MutableLiveData<String> mVersionNumber = new MutableLiveData<>();
    private final MutableLiveData<Integer> mTemperature = new MutableLiveData<>();
	private final MutableLiveData<byte[]> mLoraState = new MutableLiveData<>();
	private final MutableLiveData<Integer> mWakeState = new MutableLiveData<>();
	private final MutableLiveData<Integer> mChargeState = new MutableLiveData<>();
    private final MutableLiveData<Integer> mBootCauseState = new MutableLiveData<>();
	//private final MutableLiveData<byte[]> mStatusState = new MutableLiveData<>();
	private final MutableLiveData<Integer> mStatusOID = new MutableLiveData<>();
	private final MutableLiveData<Float> mStatusTemp = new MutableLiveData<>();
	private final MutableLiveData<Float> mStatusCurrent = new MutableLiveData<>();
	private final MutableLiveData<Integer> mStatusBat = new MutableLiveData<>();

	private final ArrayList<LoraItem> mLoraItemsValues = new ArrayList<LoraItem>();
    private final MutableLiveData<ArrayList<LoraItem>> mLoraItems = new MutableLiveData<>();
//	private int mLoraCount = 0;

	private final MutableLiveData<Float> mGpsLat = new MutableLiveData<>();
	private final MutableLiveData<Float> mGpsLon = new MutableLiveData<>();
	private final MutableLiveData<Float> mGpsAlt = new MutableLiveData<>();
	private final MutableLiveData<Float> mGpsSpd = new MutableLiveData<>();
	private final MutableLiveData<Integer> mGpsSat = new MutableLiveData<>();
	private final MutableLiveData<Integer> mGpsTime = new MutableLiveData<>();

	private final MutableLiveData<Integer> mGsmReady = new MutableLiveData<>();
	private final MutableLiveData<Integer> mGsmRegistered = new MutableLiveData<>();

	// Flag that holds the pressed released state of the button on the devkit. Pressed is true, Released is False
	private final MutableLiveData<Boolean> mButtonState = new MutableLiveData<>();

	//----------------------------------------------------------------------------------------------

	private final MutableLiveData<String>  mConfigPhone       = new MutableLiveData<>();
	private final MutableLiveData<String>  mConfigID          = new MutableLiveData<>();
	private final MutableLiveData<Integer> mConfigOID         = new MutableLiveData<>();
	private final MutableLiveData<Integer> mConfigSleepIdle   = new MutableLiveData<>();
	private final MutableLiveData<Integer> mConfigSleepArm    = new MutableLiveData<>();
	private final MutableLiveData<Integer> mConfigAccel       = new MutableLiveData<>();
	private final MutableLiveData<Integer> mConfigHall        = new MutableLiveData<>();
	private final MutableLiveData<Integer> mConfigWaitRope    = new MutableLiveData<>();
	private final MutableLiveData<Integer> mConfigTimeGSM     = new MutableLiveData<>();
	private final MutableLiveData<Integer> mConfigTimeSMS     = new MutableLiveData<>();
	private final MutableLiveData<Integer> mConfigTimeEGTS    = new MutableLiveData<>();
	private final MutableLiveData<Integer> mConfigSmsGps      = new MutableLiveData<>();
	private final MutableLiveData<Integer> mConfigSmsAlert    = new MutableLiveData<>();
	private final MutableLiveData<Integer> mConfigSmsWake     = new MutableLiveData<>();
	private final MutableLiveData<Integer> mConfigWialonUsage = new MutableLiveData<>();
	private final MutableLiveData<Integer> mConfigWialonAddr  = new MutableLiveData<>();
	private final MutableLiveData<Integer> mConfigGlosavAddr  = new MutableLiveData<>();
	private final MutableLiveData<Integer> mConfigEgtsWake    = new MutableLiveData<>();
	private final MutableLiveData<Integer> mConfigLoraUsage   = new MutableLiveData<>();
	private final MutableLiveData<Integer> mConfigLoraP       = new MutableLiveData<>();
	private final MutableLiveData<Integer> mConfigLoraT       = new MutableLiveData<>();
	private final MutableLiveData<Integer> mConfigLoraD       = new MutableLiveData<>();
	private final MutableLiveData<Integer> mConfigAlertFT     = new MutableLiveData<>();
	private final MutableLiveData<Integer> mConfigAlertCL     = new MutableLiveData<>();
	private final MutableLiveData<Integer> mConfigAlertAL     = new MutableLiveData<>();
	private final MutableLiveData<Integer> mConfigGpsTFIX     = new MutableLiveData<>();
	private final MutableLiveData<Integer> mConfigGpsTPOS     = new MutableLiveData<>();
	private final MutableLiveData<Integer> mConfigGpsFNEAR    = new MutableLiveData<>();
	private final MutableLiveData<Integer> mConfigGpsFSTOP    = new MutableLiveData<>();
	private final MutableLiveData<Integer> mConfigGpsFSPD     = new MutableLiveData<>();
	private final MutableLiveData<Integer> mConfigGpsFSKIP    = new MutableLiveData<>();
	private final MutableLiveData<Integer> mConfigGpsFSAT     = new MutableLiveData<>();
	private final MutableLiveData<Integer> mConfigGpsOSI      = new MutableLiveData<>();

	public LiveData<String>  getConfigPhone()        { return mConfigPhone;         }
	public LiveData<String>  getConfigID()           { return mConfigID;	        }
	public LiveData<Integer> getConfigOID()          { return mConfigOID;	        }
	public LiveData<Integer> getConfigSleepIdle()    { return mConfigSleepIdle;	    }
	public LiveData<Integer> getConfigSleepArm()     { return mConfigSleepArm;	    }
	public LiveData<Integer> getConfigAccel()        { return mConfigAccel;	        }
	public LiveData<Integer> getConfigHall()         { return mConfigHall;   	    }
	public LiveData<Integer> getConfigWaitRope()     { return mConfigWaitRope;	    }
	public LiveData<Integer> getConfigTimeGSM()      { return mConfigTimeGSM;	    }
	public LiveData<Integer> getConfigTimeSMS()      { return mConfigTimeSMS;	    }
	public LiveData<Integer> getConfigTimeEGTS()     { return mConfigTimeEGTS;	    }
	public LiveData<Integer> getConfigSmsGps()       { return mConfigSmsGps;	    }
	public LiveData<Integer> getConfigSmsAlert()     { return mConfigSmsAlert;	    }
	public LiveData<Integer> getConfigSmsWake()      { return mConfigSmsWake;	    }
	public LiveData<Integer> getConfigWialonUsage()  { return mConfigWialonUsage;	}
	public LiveData<Integer> getConfigWialonAddr()   { return mConfigWialonAddr;	}
	public LiveData<Integer> getConfigGlosavAddr()   { return mConfigGlosavAddr;	}
	public LiveData<Integer> getConfigEgtsWake()     { return mConfigEgtsWake;	    }
	public LiveData<Integer> getConfigLoraUsage()    { return mConfigLoraUsage;  	}
	public LiveData<Integer> getConfigLoraP()        { return mConfigLoraP;	        }
	public LiveData<Integer> getConfigLoraT()        { return mConfigLoraT;     	}
	public LiveData<Integer> getConfigLoraD()        { return mConfigLoraD;         }
	public LiveData<Integer> getConfigAlertFT()      { return mConfigAlertFT;       }
	public LiveData<Integer> getConfigAlertCL()      { return mConfigAlertCL;       }
	public LiveData<Integer> getConfigAlertAL()      { return mConfigAlertAL;       }
	public LiveData<Integer> getConfigGpsTFIX()      { return mConfigGpsTFIX;       }
	public LiveData<Integer> getConfigGpsTPOS()      { return mConfigGpsTPOS;       }
	public LiveData<Integer> getConfigGpsFNEAR()     { return mConfigGpsFNEAR;      }
	public LiveData<Integer> getConfigGpsFSTOP()     { return mConfigGpsFSTOP;      }
	public LiveData<Integer> getConfigGpsFSPD()      { return mConfigGpsFSPD;       }
	public LiveData<Integer> getConfigGpsFSKIP()     { return mConfigGpsFSKIP;      }
	public LiveData<Integer> getConfigGpsFSAT()      { return mConfigGpsFSAT;       }
	public LiveData<Integer> getConfigGpsOSI()       { return mConfigGpsOSI;        }

	//private final MutableLiveData<byte[]> mConfigSeal = new MutableLiveData<>();

	//public MutableLiveData<byte[]> getConfigSeal()   { return mConfigSeal;   	    }

	//----------------------------------------------------------------------------------------------


	public LiveData<Void> isDeviceReady() {
		return mOnDeviceReady;
	}

	public LiveData<String> getConnectionState() {
		return mConnectionState;
	}

	public LiveData<Boolean> isConnected() {
		return mIsConnected;
	}

	public LiveData<Integer> getStatusOID() {return mStatusOID; }
	public LiveData<Float> getStatusTemp() {return mStatusTemp; }
	public LiveData<Float> getStatusCurrent() {return mStatusCurrent; }
	public LiveData<Integer> getStatusBat()  {return mStatusBat; }

	public LiveData<Boolean> getButtonState() {
		return mButtonState;
	}

	public LiveData<Boolean> getLEDState() {
		return mLEDState;
	}

	public LiveData<Integer> getArmState() {
		return mArmState;
	}

	public LiveData<Float> getGpsLat() { return mGpsLat; }
	public LiveData<Float> getGpsLon() { return mGpsLon; }
	public LiveData<Float> getGpsAlt() { return mGpsAlt; }
	public LiveData<Float> getGpsSpd() { return mGpsSpd; }
	public LiveData<Integer> getGpsSat() { return mGpsSat; }
	public LiveData<Integer> getGpsTime() { return mGpsTime; }

	public LiveData<Integer> getGsmReady() { return mGsmReady; }
	public LiveData<Integer> getGsmRegistered() { return mGsmRegistered; }

	public MutableLiveData<byte[]> getLoraState() {
		return mLoraState;
	}
	public MutableLiveData<Integer> getWakeState() { return mWakeState;	}
	public MutableLiveData<Integer> getChargeState() { return mChargeState;	}
    public MutableLiveData<Integer> getBootCauseState() { return mBootCauseState; }
//	public LiveData<byte[]> getStatusState() {
//		return mStatusState;
//	}

    public LiveData<byte[]> getSerialNumber() {
        return mSerialNumber;
    }

	public LiveData<String> getVersionNumber() {
		return mVersionNumber;
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

	// Пробудить пломбу
	public void wake() {
		final byte[] command = {5};
		mBlinkyManager.write(command);
		Log.e("ble", "requestInfo " );
	}
	// Запрос всей информации
	public void requestInfo() {
        final byte[] command = {2};
		mBlinkyManager.write(command);
		Log.e("ble", "requestInfo " );
	}

	// Запрос информации o GPS
	public void requestGPS() {
		final byte[] command = {6};
		mBlinkyManager.write(command);
		Log.e("ble", "requestGPS " );
	}

	// Запрос информации o GPS
	public void requestConfig() {
		final byte[] command = {10};
		mBlinkyManager.write(command);
		Log.e("ble", "requestConfig " );
	}

	// Запрос информации o GSM
	public void requestGSM() {
		final byte[] command = {7};
		mBlinkyManager.write(command);
		Log.e("ble", "requestGSM " );
	}

	// Подготовка к охране
	public void prearm() {
        final byte[] command = {0, 0};
		mBlinkyManager.write(command);
	}
	// Подтвердить постановку на охрану
	public void armConfirm() {
        final byte[] command = {0, 1};
		mBlinkyManager.write(command);
	}
	// Отменить постановку на охрану
	public void armDiscard() {
        final byte[] command = {0, 2};
		mBlinkyManager.write(command);
	}
	// Снять с охраны
	public void disarm() {
        final byte[] command = {0, 3};
		mBlinkyManager.write(command);
	}
	// Получить данные LoRa атчика по индексу
	public void getLora(int index) {
		Log.e("write", "getLora "+index);
		final byte[] command = {1, 3, (byte) index};
		mBlinkyManager.write(command);
	}
	// Добавить LoRa датчик
	public void addLora(byte[] addr) {
		Log.e("write", "addLora "+addr[0]+" "+addr[1]+" "+addr[2]+" "+addr[3]+" "+addr[4]);
		final byte[] command = {1, 4, addr[0], addr[1], addr[2], addr[3], addr[4]};
		mBlinkyManager.write(command);
	}
	// Удалить LoRa датчик по индексу
	public void deleteLoraNum(int index) {
		if(index>=0) {
			final byte[] command = {1, 5, (byte) index};
			mBlinkyManager.write(command);
		}
	}
	// Удалить LoRa датчик по адресу
	public void deleteLora(byte[] addr) {
		Log.e("write", "deleteLora "+addr[0]+" "+addr[1]+" "+addr[2]+" "+addr[3]+" "+addr[4]);
		final byte[] command = {1, 5, addr[0], addr[1], addr[2], addr[3], addr[4]};
		mBlinkyManager.write(command);
	}

	@Override
	protected void onCleared() {
		super.onCleared();
		if (mBlinkyManager.isConnected()) {
			disconnect();
		}
	}

	int mLoraCount = 0;
	void onCmdArm(int subCmd) {
		switch(subCmd) {
			default: {
				Log.e("onCmdArm", "Unknown subCmd - " + subCmd);
				break;
			}
		}
	}

	void onCmdGPS(int subCmd, final byte[] data) {

		byte valdata[] = new byte[]{data[2], data[3], data[4], data[5]};

		//
		switch(subCmd) {
			// CHAR_GPS_LAT
			case 0: {
				float value = ByteBuffer.wrap(valdata).order(ByteOrder.LITTLE_ENDIAN).getFloat();
				mGpsLat.postValue(value);
				String str = String.format("%.01f", value) + "°";
				Log.e("CHAR_GPS_LAT", str);
			}
			break;
			// CHAR_GPS_LON
			case 1: {
				float value = ByteBuffer.wrap(valdata).order(ByteOrder.LITTLE_ENDIAN).getFloat();
				mGpsLon.postValue(value);
				String str = String.format("%.01f", value) + "°";
				Log.e("CHAR_GPS_LON", str);
			}
			break;
			// CHAR_GPS_ALT
			case 2: {
				float value = ByteBuffer.wrap(valdata).order(ByteOrder.LITTLE_ENDIAN).getFloat();
				mGpsAlt.postValue(value);
				String str = String.format("%.01f", value) + "°";
				Log.e("CHAR_GPS_ALT", str);
			}
			break;
			// CHAR_GPS_SPD
			case 3: {
				float value = ByteBuffer.wrap(valdata).order(ByteOrder.LITTLE_ENDIAN).getFloat();
				mGpsSpd.postValue(value);
				String str = String.format("%.01f", value) + "°";
				Log.e("CHAR_GPS_SPD", str);
			}
			break;
			// CHAR_GPS_SAT
			case 4: {
				int value = ByteBuffer.wrap(valdata).order(ByteOrder.LITTLE_ENDIAN).getInt();
				mGpsSat.postValue(value);
				Log.e("CHAR_GPS_SAT", String.valueOf(value));
			}
			break;
			// CHAR_GPS_UTC
			case 5: {
				int value = ByteBuffer.wrap(valdata).order(ByteOrder.LITTLE_ENDIAN).getInt();
				mGpsTime.postValue(value);
				Log.e("CHAR_GPS_UTC", String.valueOf(value));
			}
			break;
			default:
				break;
		}
	}

	void onCmdGSM(int subCmd, final byte[] data) {

		byte valdata[] = new byte[]{data[2], data[3], data[4], data[5]};

		//
		switch(subCmd) {
			// CHAR_GSM_READY
			case 0: {
				int value = ByteBuffer.wrap(valdata).order(ByteOrder.LITTLE_ENDIAN).getInt();
				mGsmReady.postValue(value);
				Log.e("CHAR_GSM_READY", String.valueOf(value));
			}
			break;
			// CHAR_GSM_REGISTERED
			case 1: {
				int value = ByteBuffer.wrap(valdata).order(ByteOrder.LITTLE_ENDIAN).getInt();
				mGsmRegistered.postValue(value);
				Log.e("CHAR_GSM_REGISTERED", String.valueOf(value));
			}
			break;
			default:
				break;
		}
	}

	void onCmdLora(int subCmd, final byte[] data) {
		Log.e("onCmdLora", "subCmd = " + subCmd);
		switch(subCmd) {
			// CHAR_LORA_COUNT
			case 0 : {
				if(mLoraCount>0) {
					mLoraItems.getValue().clear();
				}
				mLoraCount = data[2];
				Log.e("onCmdLora", "mLoraCount = " + mLoraCount);
			} break;
			// CHAR_LORA_ADDR
			case 1 : {
				int loraIndex = data[2];
				StringBuffer addr = new StringBuffer();
				for(int i=0; i<5; i++) {
					int intVal = data[3+i] & 0xff;
					if (intVal < 0x10) addr.append("0");
					addr.append(Integer.toHexString(intVal));
				}
				mLoraItems.getValue().add(new LoraItem(addr.toString()));
				Log.e("onCmdLora", "add addr[" + loraIndex + "] " + addr.toString());
				mLoraItems.postValue(mLoraItemsValues);
			} break;
			// CHAR_LORA_DATA
			case 2 : {
				mLoraState.postValue(data);
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
			default: {
				Log.e("onCmdLora", "Unknown subCmd");
				break;
			}
		};
	}

	void onCmdInfo(int subCmd, final byte[] data) {
		Log.e("onCmdLora", "subCmd = " + subCmd);

	}

	@Override
	public void onDataReceived(final byte[] data) {

		if ( data.length < 2) return;

		Log.e("onDataReceived", "cmd = " + data[0]);
		int cmd    = data[0];
		int subCmd = data[1];

		switch(cmd) {
			// CHAR_CMD_ARM
			case 0 : {
				//onCmdArm(subCmd);
				mArmState.postValue(subCmd);
			} break;
			// CHAR_CMD_LORA
			case 1 : {
				onCmdLora(subCmd, data);
			} break;
			// CHAR_CMD_INFO
			case 3 : {
//				mStatusState.postValue(data);
				byte valdata[] = new byte[]{data[2], data[3], data[4], data[5]};
				// CHAR_CMD_INFO events
				switch(data[1]) {
					// CHAR_INFO_OID
					case 0 : {
						int oid = ByteBuffer.wrap(valdata).order(ByteOrder.LITTLE_ENDIAN).getInt();
						mStatusOID.postValue(oid);
						Log.e("CHAR_INFO_OID", String.valueOf(oid));
					} break;
					// CHAR_INFO_BAT
					case 1 : {
						int bat = ByteBuffer.wrap(valdata).order(ByteOrder.LITTLE_ENDIAN).getInt();
						//mStatusBat.postValue(bat);
						Log.e("CHAR_INFO_BAT", String.valueOf(bat));
					} break;
					// CHAR_INFO_TEMP
					case 2 : {
						float temp = ByteBuffer.wrap(valdata).order(ByteOrder.LITTLE_ENDIAN).getFloat();
						mStatusTemp.postValue(temp);
						String str = String.format("%.01f", temp) + "°C";
						Log.e("CHAR_INFO_TEMP", str);
					} break;
					// CHAR_INFO_CURRENT
					case 3 : {
						float current = ByteBuffer.wrap(valdata).order(ByteOrder.LITTLE_ENDIAN).getFloat();
						mStatusCurrent.postValue(current);
						String str = String.format("%.02f", current) + "mAh";
						Log.e("CHAR_INFO_CURRENT", str);
					} break;
					default: break;
				};
			} break;
			// CHAR_CMD_WAKE
			case 5 : {
				mWakeState.postValue(subCmd);
			} break;
			// CHAR_CMD_GPS
			case 6 : {
				onCmdGPS(subCmd, data);
			} break;
			// CHAR_CMD_GSM
			case 7 : {
				onCmdGSM(subCmd, data);
			} break;
			// CHAR_CMD_CHARGE
			case 8 : {
				mChargeState.postValue(subCmd);
			} break;
            // CHAR_CMD_BOOT_CAUSE
            case 9 : {
                mBootCauseState.postValue(subCmd);
            } break;
			// CHAR_CMD_CONFIG_REQUEST
			case 10 : {
//				mConfigSeal.postValue(data);
				switch(data[1]) {
					// CHAR_CONFIG_PHONE
					case 0: {
						//EditText seal_param = mParametersView.findViewById(R.id.seal_config_value_phone);
					}
					break;
					// CHAR_CONFIG_ID
					case 1: {
						String value = String.valueOf(ByteBuffer.wrap(new byte[]{data[2], data[3], data[4], data[5]}).order(ByteOrder.LITTLE_ENDIAN).getInt());
						mConfigID.postValue(value);
					}
					break;
					// CHAR_CONFIG_OID
					case 2: {
						Integer value = ByteBuffer.wrap(new byte[]{data[2], data[3], data[4], data[5]}).order(ByteOrder.LITTLE_ENDIAN).getInt();
						mConfigOID.postValue(value);
					}
					break;
					// CHAR_CONFIG_SLEEP_IDLE
					case 3: {
						Integer value = ByteBuffer.wrap(new byte[]{data[2], data[3], data[4], data[5]}).order(ByteOrder.LITTLE_ENDIAN).getInt();
						mConfigSleepIdle.postValue(value);
					}
					break;
					/////
					/** И т.д. */
				}
			} break;
			default: {
				Log.e("onDataReceived", "Unknown cmd");
			} break;
		}

	}

	@Override
	public void onDataSent(final boolean state) {
		mLEDState.postValue(state);
	}

    @Override
    public void onRevisionReceived(String revision) {
		mVersionNumber.postValue(revision);
    }

    @Override
    public void onSerialReceived(byte[] serial) {
        mSerialNumber.postValue(serial);
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
	public void onLinklossOccur(final BluetoothDevice device) {
		mIsConnected.postValue(false);
	}

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
		return true;
	}

	@Override
	public void onBatteryValueReceived(final BluetoothDevice device, final int value) {
		Log.e("Test", "Battery = " + value);
        mStatusBat.postValue(value);
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
	public void onError(final BluetoothDevice device, final String message, final int errorCode) {
		// TODO implement
	}

	@Override
	public void onDeviceNotSupported(final BluetoothDevice device) {
		// TODO implement
	}
}
