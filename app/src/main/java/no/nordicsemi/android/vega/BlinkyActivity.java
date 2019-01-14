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

package no.nordicsemi.android.vega;


//import android.app.Fragment;
//import android.app.FragmentTransaction;

import android.arch.lifecycle.ViewModelProviders;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.nio.ByteBuffer;

import no.nordicsemi.android.vega.adapter.ExtendedBluetoothDevice;
import no.nordicsemi.android.vega.adapter.LoraAdapter;
import no.nordicsemi.android.vega.utils.Utils;
import no.nordicsemi.android.vega.viewmodels.BlinkyViewModel;

//import android.support.v4.app.FragmentManager;

public class BlinkyActivity extends AppCompatActivity implements LoraAdapter.ClickLoraDialogListener, LoraParametersFragment.LoraDataListener, SealParametersFragment.SealDataListener {


	public static final String EXTRA_DEVICE = "no.nordicsemi.android.vega.EXTRA_DEVICE";

	SealParametersFragment sealParametersDialog = null;
    LoraParametersFragment loraParametersDialog = null;
    BlinkyViewModel viewModel;

	//ArrayList<LoraItem> loraItems = new ArrayList<LoraItem>();

	LoraAdapter loraAdapter;

	Button armControlBtn;
	ImageButton sealConfigBtn;
    Button wakeBtn;
	Button addLoraBtn;

	AlertDialog.Builder addLoraBuilder;
	EditText addLoraInput;

	AlertDialog.Builder armBuilder;

	ProgressBar armProgressBar;
	TextView armState;
	TextView mSerial;
	TextView mVersion;
	TextView mTempValue;

	ConstraintLayout armContainer;
	Handler mTimeoutHandler;

	MediaPlayer mp;

	boolean state = false;

	int armStateFlag = 0;

	boolean skipedFirstArm = true;

	boolean enableUpdateBattery = true;

//	int loraCount = 0;

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_blinky);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		setVolumeControlStream(AudioManager.STREAM_MUSIC);

		final Intent intent = getIntent();
		final ExtendedBluetoothDevice device = intent.getParcelableExtra(EXTRA_DEVICE);
		final String deviceName = device.getName();
		final String deviceAddress = device.getAddress();

		final Toolbar toolbar = findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		getSupportActionBar().setTitle(deviceName);
		getSupportActionBar().setSubtitle(deviceAddress);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		// Configure the view model
		viewModel = ViewModelProviders.of(this).get(BlinkyViewModel.class);
		viewModel.connect(device);

		loraAdapter = new LoraAdapter(this, /*loraItems*/ viewModel.getLoraItems().getValue());

		// настраиваем список
		ListView loraList = (ListView) findViewById(R.id.LoraListView);
		loraList.setClickable(true);
		loraList.setAdapter(loraAdapter);
		loraList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Log.e("Adapter", "Pos " + position);
			}
		});

		addLoraInput = new EditText(this);

		armBuilder = new AlertDialog.Builder(this);
		armBuilder.setTitle("Поставить на охрану?");
		// Set up the buttons
		armBuilder.setPositiveButton("Поставить", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				viewModel.armConfirm();
				dialog.cancel();
			}
		});
		armBuilder.setNegativeButton("Отмена", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				viewModel.armDiscard();
				dialog.cancel();
			}
		});

		addLoraBtn = findViewById(R.id.add_lora_item);
		addLoraBtn.setOnClickListener(addLoraClicked);

		armControlBtn = findViewById(R.id.arm_control_btn);
		armControlBtn.setOnClickListener(armControlClicked);

		sealConfigBtn = findViewById(R.id.sealConfigBtn);
		sealConfigBtn.setOnClickListener(btnSealConfigClicked);

        wakeBtn = findViewById(R.id.button_wake);
        wakeBtn.setOnClickListener(btnWakeClicked);
		armProgressBar = findViewById(R.id.arm_progress_bar);
		armProgressBar.setVisibility(View.GONE);

		// Set up views
		armState = findViewById(R.id.arm_control_state);

		armContainer = findViewById(R.id.arm_container);

		//final Switch led = findViewById(R.id.led_switch);
		final LinearLayout progressContainer = findViewById(R.id.progress_container);
		final TextView connectionState = findViewById(R.id.connection_state);
		final View content = findViewById(R.id.device_container);

		mSerial  = findViewById(R.id.info_device_serial_value);
		mVersion = findViewById(R.id.info_device_version_value);

		viewModel.getSerialNumber().observe(this, serial -> {
			mSerial.setText(Utils.byteArrayToHexString(serial));
		});

		viewModel.getVersionNumber().observe(this, version -> {
			mVersion.setText(version);
		});

		//mTempValue = findViewById(R.id.info_device_temp_value);

		viewModel.getTemperature().observe(this, temp -> {
			mTempValue.setText(temp.toString());
		});

		viewModel.isDeviceReady().observe(this, deviceReady -> {
			progressContainer.setVisibility(View.GONE);
			content.setVisibility(View.VISIBLE);
			viewModel.requestInfo();
		});
		viewModel.getConnectionState().observe(this, connectionState::setText);

		viewModel.isConnected().observe(this, connected -> {
			if(!connected) finish();
		});

		viewModel.getArmState().observe(this, value -> {
			Log.e("getArmState", "value = " + value);
			// ожидание троса
			if(value==0) {
				armContainer.setBackgroundColor(Color.CYAN);
				armState.setText(R.string.arm_state_prepare);
				armControlBtn.setText(R.string.arm_button_arm);
				armStateFlag = 1;
				armBuilder.show();
			}
			// охрана установлена
			else if(value==1) {
				mp=MediaPlayer.create(getApplicationContext(),R.raw.arm);
				mp.start();
				armContainer.setBackgroundColor(Color.GREEN);
				armState.setText(R.string.arm_state_true);
				armControlBtn.setText(R.string.arm_button_disarm);
				armStateFlag = 2;
				armBuilder.show().dismiss();
			}
			// охрана снята
			else if(value==3) {
				mp=MediaPlayer.create(getApplicationContext(),R.raw.disarm);
				mp.start();
				armContainer.setBackgroundColor(Color.WHITE);
				armState.setText(R.string.arm_state_false);
				armControlBtn.setText(R.string.arm_button_prepare);
				armStateFlag = 0;
			}
			// ошибка модуля
			else if(value==5) {
				armContainer.setBackgroundColor(Color.YELLOW);
				armState.setText(R.string.arm_gamma_error);
				armControlBtn.setText(R.string.arm_button_prepare);
				armStateFlag = 0;
			}
			// тревога
			else if(value==6) {
				mp=MediaPlayer.create(getApplicationContext(),R.raw.alarm);
				mp.start();
				armContainer.setBackgroundColor(Color.RED);
				armState.setText(R.string.arm_alarm);
				armControlBtn.setText(R.string.arm_button_disarm);
				armStateFlag = 2;
			}
			// предупреждение
			else if(value==7) {
				mp=MediaPlayer.create(getApplicationContext(),R.raw.alert);
				mp.start();
				armContainer.setBackgroundColor(0xffFFAE19);
				armState.setText(R.string.arm_alert);
				armControlBtn.setText(R.string.arm_button_disarm);
				armStateFlag = 2;
			}
			else {
				armContainer.setBackgroundColor(Color.WHITE);
				armState.setText(R.string.arm_state_unknown);
				armControlBtn.setText(R.string.arm_button_prepare);
				armStateFlag = 0;
				armBuilder.show().dismiss();
			}
			armControlBtn.setEnabled(true);
			armProgressBar.setVisibility(View.GONE);
		});

		viewModel.getGpsLat().observe(this, value -> {
			TextView text = findViewById(R.id.gps_latitute_value);
			text.setText(String.valueOf(value));
		});

		viewModel.getGpsLon().observe(this, value -> {
			TextView text = findViewById(R.id.gps_longitude_value);
			text.setText(String.valueOf(value));
		});

		viewModel.getGpsAlt().observe(this, value -> {
			TextView text = findViewById(R.id.gps_altitude_value);
			text.setText(String.valueOf(value));
		});

		viewModel.getGpsSpd().observe(this, value -> {
			TextView text = findViewById(R.id.gps_speed_value);
			text.setText(String.valueOf(value));
		});

		viewModel.getGpsSat().observe(this, value -> {
			TextView text = findViewById(R.id.gps_satelites_value);
			text.setText(String.valueOf(value));
		});

		viewModel.getGpsTime().observe(this, value -> {
			TextView text = findViewById(R.id.gps_fix_time_value);
			text.setText(String.valueOf(value));
		});

		viewModel.getGsmReady().observe(this, value -> {
			TextView text = findViewById(R.id.gsm_ready_value);
			text.setText((value>0)?"Готов":"Откл.");
		});

		viewModel.getGsmRegistered().observe(this, value -> {
			TextView text = findViewById(R.id.gsm_registered_value);
			     if(value==0) text.setText("Нет сети");
			else if(value==1) text.setText("ОК");
			else if(value==2) text.setText("Регистрация...");
			else if(value==3) text.setText("Отказано!");
			else text.setText("Ошибка");
		});

		viewModel.getLoraState().observe(this, value -> {
            Log.e("Observed lora state", String.valueOf(value[1]) );
			// CHAR_CMD_LORA events
			switch(value[1]) {
				// CHAR_LORA_DATA
				case 2 : {
					Log.e("LORA_data", String.valueOf(value[2]));
					if (loraParametersDialog != null) {
						if (mTimeoutHandler != null ) {
							mTimeoutHandler.removeCallbacksAndMessages(null);
							mTimeoutHandler = null;
						}
						loraParametersDialog.onReceiveData(value);
					}
				} break;
				default: break;
			}
		});
/*
		viewModel.getConfigSeal().observe(this, value -> {
			Log.e("Observed seal config", String.valueOf(value[1]) );
			// CHAR_CMD_LORA events
			if (sealParametersDialog != null) {
				if (mTimeoutHandler != null ) {
					mTimeoutHandler.removeCallbacksAndMessages(null);
					mTimeoutHandler = null;
				}
				sealParametersDialog.onReceiveData(value);
			}
		});
*/
		//-----------------------------------------------------------------------------------------
		viewModel.getConfigPhone().observe(this, value -> {
			setSealParameter(0, value);
		});
		viewModel.getConfigID().observe(this, value -> {
			setSealParameter(1, value);
		});
		viewModel.getConfigOID().observe(this, value -> {
			setSealParameter(2, value);
		});
		viewModel.getConfigSleepIdle().observe(this, value -> {
			setSealParameter(3, value);
		});
		viewModel.getConfigSleepArm().observe(this, value -> {
			setSealParameter(4, value);
		});
		viewModel.getConfigAccel().observe(this, value -> {
			setSealParameter(5, value);
		});
		viewModel.getConfigHall().observe(this, value -> {
			setSealParameter(6, value);
		});
		viewModel.getConfigWaitRope().observe(this, value -> {
			setSealParameter(7, value);
		});
		viewModel.getConfigTimeGSM().observe(this, value -> {
			setSealParameter(8, value);
		});
		viewModel.getConfigTimeSMS().observe(this, value -> {
			setSealParameter(9, value);
		});
		viewModel.getConfigTimeEGTS().observe(this, value -> {
			setSealParameter(10, value);
		});
		viewModel.getConfigSmsGps().observe(this, value -> {
			setSealParameter(11, value);
		});
		viewModel.getConfigSmsAlert().observe(this, value -> {
			setSealParameter(12, value);
		});
		viewModel.getConfigSmsWake().observe(this, value -> {
			setSealParameter(13, value);
		});
		viewModel.getConfigWialonUsage().observe(this, value -> {
			setSealParameter(14, value);
		});
		viewModel.getConfigWialonAddr().observe(this, value -> {
			setSealParameter(15, value);
		});
		viewModel.getConfigGlosavAddr().observe(this, value -> {
			setSealParameter(16, value);
		});
		viewModel.getConfigEgtsWake().observe(this, value -> {
			setSealParameter(17, value);
		});
		viewModel.getConfigLoraUsage().observe(this, value -> {
			setSealParameter(18, value);
		});
		viewModel.getConfigLoraP().observe(this, value -> {
			setSealParameter(19, value);
		});
		viewModel.getConfigLoraT().observe(this, value -> {
			setSealParameter(20, value);
		});
		viewModel.getConfigLoraD().observe(this, value -> {
			setSealParameter(21, value);
		});
		viewModel.getConfigAlertFT().observe(this, value -> {
			setSealParameter(22, value);
		});
		viewModel.getConfigAlertCL().observe(this, value -> {
			setSealParameter(23, value);
		});
		viewModel.getConfigAlertAL().observe(this, value -> {
			setSealParameter(24, value);
		});
		viewModel.getConfigGpsTFIX().observe(this, value -> {
			setSealParameter(25, value);
		});
		viewModel.getConfigGpsTPOS().observe(this, value -> {
			setSealParameter(26, value);
		});
		viewModel.getConfigGpsFNEAR().observe(this, value -> {
			setSealParameter(27, value);
		});
		viewModel.getConfigGpsFSTOP().observe(this, value -> {
			setSealParameter(28, value);
		});
		viewModel.getConfigGpsFSPD().observe(this, value -> {
			setSealParameter(29, value);
		});
		viewModel.getConfigGpsFSKIP().observe(this, value -> {
			setSealParameter(30, value);
		});
		viewModel.getConfigGpsFSAT().observe(this, value -> {
			setSealParameter(31, value);
		});
		viewModel.getConfigGpsOSI().observe(this, value -> {
			setSealParameter(32, value);
		});

		//-----------------------------------------------------------------------------------------

        viewModel.getGammaState().observe(this, value -> {
            TextView label = findViewById(R.id.gamma_state);
            switch(value) {
				case 0x01 : label.setText("State: OPEN");  break;
				case 0x20 : label.setText("State: CLOSE"); break;
				case 0x10 : label.setText("State: WAIT");  break;
				case 0x21 : label.setText("State: ARMED"); break;
				case 0x22 : label.setText("State: ALARM"); break;
				case 0x7F : label.setText("State: ERROR"); break;
				default   : label.setText("State: ---");   break;
			}
        });
        viewModel.getGammaFreq().observe(this, value -> {
            TextView label = findViewById(R.id.gamma_freq);
            label.setText("Freq: " + String.valueOf(value));
        });
        viewModel.getGammaRangeOpen().observe(this, value -> {

        });
        viewModel.getGammaRangeClose().observe(this, value -> {

        });
        viewModel.getGammaSavedOpen().observe(this, value -> {

        });
        viewModel.getGammaSavedClose().observe(this, value -> {

        });
        viewModel.getGammaVersionSW().observe(this, value -> {
            TextView label = findViewById(R.id.gamma_sw);
            label.setText("State: " + String.valueOf(value));
        });
        viewModel.getGammaVersionHW().observe(this, value -> {
            TextView label = findViewById(R.id.gamma_hw);
            label.setText("State: " + String.valueOf(value));
        });
        viewModel.getGammaMain().observe(this, value -> {
			byte [] data = ByteBuffer.allocate(4).putInt(value).array();
            TextView labelState = findViewById(R.id.gamma_state);
            TextView labelFreq = findViewById(R.id.gamma_freq);
            TextView labelSW = findViewById(R.id.gamma_sw);
            TextView labelHW = findViewById(R.id.gamma_hw);
            labelFreq.setText("Freq: " + String.valueOf(data[1]));
            labelSW.setText("SW: " + String.valueOf(data[2]));
            labelHW.setText("HW: " + String.valueOf(data[3]));
            switch(data[0]) {
                case 0x01 : labelState.setText("State: OPEN");  break;
                case 0x20 : labelState.setText("State: CLOSE"); break;
                case 0x10 : labelState.setText("State: WAIT");  break;
                case 0x21 : labelState.setText("State: ARMED"); break;
                case 0x22 : labelState.setText("State: ALARM"); break;
                case 0x7F : labelState.setText("State: ERROR"); break;
                default   : labelState.setText("State: ---");   break;
            }
        });
        viewModel.getGammaConfig().observe(this, value -> {
			byte [] data = ByteBuffer.allocate(4).putInt(value).array();
			TextView rangeOpen  = findViewById(R.id.gamma_range_open);
			TextView rangeClose = findViewById(R.id.gamma_range_close);
			TextView savedOpen  = findViewById(R.id.gamma_saved_open);
			TextView savedClose = findViewById(R.id.gamma_saved_close);
			rangeOpen.setText(String.valueOf(data[0]));
			rangeClose.setText(String.valueOf(data[1]));
			savedOpen.setText(String.valueOf(data[2]));
			savedClose.setText(String.valueOf(data[3]));
        });

        //-----------------------------------------------------------------------------------------

/*
        viewModel.getStatusState().observe(this, value -> {
            Log.e("getStatusState", String.valueOf(value[1]));
            byte data[] = new byte[]{value[2], value[3], value[4], value[5]};
            // CHAR_CMD_INFO events
            switch(value[1]) {
                // CHAR_INFO_OID
                case 0 : {
                    int oid = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN).getInt();
                    TextView info_oid = findViewById(R.id.info_device_oid_value);
                    info_oid.setText(String.valueOf(oid));
                    Log.e("CHAR_INFO_OID", String.valueOf(oid));
                } break;
                // CHAR_INFO_BAT
                case 1 : {
                    int bat = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN).getInt();
                    TextView info_bat = findViewById(R.id.info_device_bat_value);
                    info_bat.setText(String.valueOf(bat) + "%");
                    Log.e("CHAR_INFO_BAT", String.valueOf(bat));
                } break;
                // CHAR_INFO_TEMP
                case 2 : {
                    float temp = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN).getFloat();
                    String str = String.format("%.01f", temp) + "°C";
                    TextView info_temp = findViewById(R.id.info_device_temp_value);
                    info_temp.setText(str);
                    Log.e("CHAR_INFO_TEMP", str);
                } break;
                // CHAR_INFO_CURRENT
                case 3 : {
                    float current = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN).getFloat();
                    String str = String.format("%.02f", current) + "mAh";
                    TextView info_state = findViewById(R.id.info_device_state_value);
                    info_state.setText(str);
                    Log.e("CHAR_INFO_CURRENT", str);
                } break;
                default: break;
            };
        });
*/
		viewModel.getChargeState().observe(this, value -> {
			TextView charge_state = findViewById(R.id.charge_status_text);
			TextView battery = findViewById(R.id.battery);
			switch(value) {
				case 0 : {
					enableUpdateBattery = true;
					battery.setText("---");
					charge_state.setText("Отключена");
				}  break;
				case 1 : {
					enableUpdateBattery = false;
					battery.setText("CHG");
					charge_state.setText("Подключена");
				} break;
				case 2 : {
					enableUpdateBattery = false;
					battery.setText("CHG");
					charge_state.setText("Заряжается");
				} break;
				case 3 : {
					enableUpdateBattery = true;
					battery.setText("FULL");
					charge_state.setText("Завершена");
				}  break;
				default: {
					enableUpdateBattery = true;
					battery.setText("---");
					charge_state.setText("Неизвестно");
				} break;
			}
		});

		viewModel.getBootCauseState().observe(this, value -> {
			TextView boot_cause_state = findViewById(R.id.boot_cause_status_text);
            Log.e("getBootCauseState", String.valueOf(value));
			switch(value) {
				case 0   : boot_cause_state.setText("Нормальный старт");  break;
				case 1   : boot_cause_state.setText("Завис поток MAIN");  break;
				case 2   : boot_cause_state.setText("Завис поток GSM");   break;
                case 3   : boot_cause_state.setText("Завис поток YIELD"); break;
				case 4   : boot_cause_state.setText("Завис поток GPS");   break;
				case 8   : boot_cause_state.setText("Завис поток TWI");   break;
				case 16  : boot_cause_state.setText("Завис поток SPI");   break;
				case 32  : boot_cause_state.setText("Перезагрузка");      break;
				case 64  : boot_cause_state.setText("Hard Fault");        break;
				case 177 : boot_cause_state.setText("DFU Загрузчик");     break;
				default  : boot_cause_state.setText("Неизвестно");        break;
			}
		});

        viewModel.getWakeState().observe(this, value -> {
            TextView sleep_state = findViewById(R.id.info_device_sleep_value);
            if(value>0) {
                sleep_state.setText("Активна");
                wakeBtn.setVisibility(View.GONE);
            } else {
                sleep_state.setText("Во сне");
                wakeBtn.setVisibility(View.VISIBLE);
            }
        });

		viewModel.getStatusOID().observe(this, value -> {
			TextView info_oid = findViewById(R.id.info_device_oid_value);
			info_oid.setText(String.valueOf(value));
		});

		viewModel.getStatusBat().observe(this, value -> {
			if(enableUpdateBattery) {
				TextView info_bat = findViewById(R.id.battery);
				info_bat.setText(String.valueOf(value) + "%");
			}
		});

		viewModel.getStatusTemp().observe(this, value -> {
			String str = String.format("%.01f", value) + "°C";
			TextView info_temp = findViewById(R.id.info_device_temp_value);
			info_temp.setText(str);
		});

		viewModel.getStatusCurrent().observe(this, value -> {
			String str = String.format("%.02f", value) + "mAh";
			TextView info_state = findViewById(R.id.info_device_state_value);
			info_state.setText(str);
		});

		viewModel.getLoraItems().observe(this, value -> {
			loraAdapter.notifyDataSetChanged();
			Log.e("LORA_items ", Integer.toString(loraAdapter.getCount()));
		});

		Log.e("TEST", "Test");
	}


    View.OnClickListener btnWakeClicked = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            AlertDialog.Builder wakeBuilder = new AlertDialog.Builder(BlinkyActivity.this);
            wakeBuilder.setTitle("Пробудить пломбу?");
            wakeBuilder.setPositiveButton("Да", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    viewModel.wake();
                    dialog.cancel();
                }
            });
            wakeBuilder.setNegativeButton("Нет", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });
            AlertDialog alert = wakeBuilder.create();
            alert.show();
        }
    };

	View.OnClickListener armControlClicked = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			switch(armStateFlag) {
				// охрана снята
				case 0: {
                    armContainer.setBackgroundColor(Color.WHITE);
                    armProgressBar.setVisibility(View.VISIBLE);
                    armState.setText(R.string.arm_check);
                    armControlBtn.setEnabled(false);
					viewModel.prearm();
				} break;
				// ожидание троса
				case 1 : {
					//viewModel.armConfirm();
				} break;
				// охрана установлена
				case 2 : {
					AlertDialog.Builder disarmBuilder = new AlertDialog.Builder(BlinkyActivity.this);
					disarmBuilder.setTitle("Снять с охраны?");
					disarmBuilder.setPositiveButton("Снять", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
                            armContainer.setBackgroundColor(Color.WHITE);
                            armProgressBar.setVisibility(View.VISIBLE);
                            armState.setText(R.string.arm_check);
                            armControlBtn.setEnabled(false);
							viewModel.disarm();
							dialog.cancel();
						}
					});
					disarmBuilder.setNegativeButton("Отмена", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.cancel();
						}
					});
                    AlertDialog alert = disarmBuilder.create();
                    alert.show();
				} break;
				default: break;
			}
		}
	};

	View.OnClickListener addLoraClicked = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			AlertDialog.Builder builder = new AlertDialog.Builder(BlinkyActivity.this);
			if(addLoraInput.getParent()!=null)
				((ViewGroup)addLoraInput.getParent()).removeView(addLoraInput);
			builder.setView(addLoraInput);
			builder.setTitle("Add LoRa")
				.setCancelable(true)
				.setNegativeButton("Cancel",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								dialog.cancel();
							}
						})
				.setPositiveButton("OK",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								String str = addLoraInput.getText().toString();
								if(checkAddrBytes(str)) {
									viewModel.addLora(getAddrBytes(str));
									mTimeoutHandler = new Handler();
									mTimeoutHandler.postDelayed(new Runnable() {
										public void run() {

										}
									}, 5000);
								} else {
									Log.e("addLora", "Failed addr lenght");
									Toast.makeText(getApplicationContext(),"Неверная длина адреса", Toast.LENGTH_SHORT);
								}

								dialog.cancel();
							}
						});
			AlertDialog alert = builder.create();
			alert.show();
		}
	};

	private void setSealParameter(int index, Object value) {
		if (sealParametersDialog != null) {
			if (mTimeoutHandler != null ) {
				mTimeoutHandler.removeCallbacksAndMessages(null);
				mTimeoutHandler = null;
			}
			sealParametersDialog.onReceiveData(index, value);
		}

	}
	@Override
	public boolean onOptionsItemSelected(final MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				onBackPressed();
				return true;
		}
		return false;
	}

    @Override
    public void OnClickLoraItem(int index, String address) {
		viewModel.getLora(index);
        FragmentManager fm = getSupportFragmentManager();
        loraParametersDialog = LoraParametersFragment.newInstance(index, address);
        loraParametersDialog.show(fm, "Add Lora");
        mTimeoutHandler = new Handler();
        mTimeoutHandler.postDelayed(new Runnable() {
            public void run() {
                loraParametersDialog.dismiss();
                loraParametersDialog = null;
            }
        }, 5000);
    }

    @Override
    public void OnLoraDeleteClick(int index, String address) {
		if (checkAddrBytes(address)) {
			viewModel.deleteLora(getAddrBytes(address));
			mTimeoutHandler = new Handler();
			mTimeoutHandler.postDelayed(new Runnable() {
				public void run() {
				    if (loraParametersDialog != null) {
                        loraParametersDialog.dismiss();
                    }
					loraParametersDialog = null;
				}
			}, 5000);
		}
	}

	@Override
	public void OnLoraCloseClick() {
		loraParametersDialog.dismiss();
	}

	View.OnClickListener btnSealConfigClicked = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			viewModel.requestConfig();
			FragmentManager fm = getSupportFragmentManager();
			sealParametersDialog = SealParametersFragment.newInstance();
			sealParametersDialog.show(fm, "Seal Config");
			mTimeoutHandler = new Handler();
			mTimeoutHandler.postDelayed(new Runnable() {
				public void run() {
					sealParametersDialog.dismiss();
					sealParametersDialog = null;
				}
			}, 5000);
		}
	};

	@Override
	public void OnSealSaveClick() {

		mTimeoutHandler = new Handler();
		mTimeoutHandler.postDelayed(new Runnable() {
			public void run() {
				if (sealParametersDialog != null) {
					sealParametersDialog.dismiss();
				}
				sealParametersDialog = null;
			}
		}, 5000);

		for(int i=0; i<10; i++) {
			switch(i) {
				case 1 : {
					EditText view = sealParametersDialog.getView().findViewById(R.id.seal_config_value_id);
					int value = Integer.parseInt(view.getText().toString());
					viewModel.saveConfig(i, value);
				} break;
				case 2 : {
					EditText view = findViewById(R.id.seal_config_value_oid);
					int value = Integer.parseInt(view.getText().toString());
					viewModel.saveConfig(i, value);
				} break;
				case 3 : {
					EditText view = findViewById(R.id.seal_config_value_sleepIdle);
					int value = Integer.parseInt(view.getText().toString());
					viewModel.saveConfig(i, value);
				} break;
				case 4 : {
					EditText view = findViewById(R.id.seal_config_value_sleepArm);
					int value = Integer.parseInt(view.getText().toString());
					viewModel.saveConfig(i, value);
				} break;
				case 7 : {
					EditText view = findViewById(R.id.seal_config_value_wait_rope);
					int value = Integer.parseInt(view.getText().toString());
					viewModel.saveConfig(i, value);
				} break;
				case 8 : {
					EditText view = findViewById(R.id.seal_config_value_time_gsm);
					int value = Integer.parseInt(view.getText().toString());
					viewModel.saveConfig(i, value);
				} break;
				case 9 : {
					EditText view = findViewById(R.id.seal_config_value_time_sms);
					int value = Integer.parseInt(view.getText().toString());
					viewModel.saveConfig(i, value);
				} break;
				case 10 : {
					EditText view = findViewById(R.id.seal_config_value_time_egts);
					int value = Integer.parseInt(view.getText().toString());
					viewModel.saveConfig(i, value);
				} break;
				case 22 : {
					EditText view = findViewById(R.id.seal_config_value_alert_ft);
					int value = Integer.parseInt(view.getText().toString());
					viewModel.saveConfig(i, value);
				} break;
				case 23 : {
					EditText view = findViewById(R.id.seal_config_value_alert_cl);
					int value = Integer.parseInt(view.getText().toString());
					viewModel.saveConfig(i, value);
				} break;
				case 24 : {
					EditText view = findViewById(R.id.seal_config_value_alert_al);
					int value = Integer.parseInt(view.getText().toString());
					viewModel.saveConfig(i, value);
				} break;
				case 25 : {
					EditText view = findViewById(R.id.seal_config_value_gps_tfix);
					int value = Integer.parseInt(view.getText().toString());
					viewModel.saveConfig(i, value);
				} break;
				case 26 : {
					EditText view = findViewById(R.id.seal_config_value_gps_tpos);
					int value = Integer.parseInt(view.getText().toString());
					viewModel.saveConfig(i, value);
				} break;
				case 27 : {
					EditText view = findViewById(R.id.seal_config_value_gps_fnear);
					int value = Integer.parseInt(view.getText().toString());
					viewModel.saveConfig(i, value);
				} break;
				case 28 : {
					EditText view = findViewById(R.id.seal_config_value_gps_fstop);
					int value = Integer.parseInt(view.getText().toString());
					viewModel.saveConfig(i, value);
				} break;
				case 29 : {
					EditText view = findViewById(R.id.seal_config_value_gps_fspd);
					int value = Integer.parseInt(view.getText().toString());
					viewModel.saveConfig(i, value);
				} break;
				case 30 : {
					EditText view = findViewById(R.id.seal_config_value_gps_fskip);
					int value = Integer.parseInt(view.getText().toString());
					viewModel.saveConfig(i, value);
				} break;
				case 31 : {
					EditText view = findViewById(R.id.seal_config_value_gps_fsat);
					int value = Integer.parseInt(view.getText().toString());
					viewModel.saveConfig(i, value);
				} break;
				case 32 : {
					EditText view = findViewById(R.id.seal_config_value_gps_osi);
					int value = Integer.parseInt(view.getText().toString());
					viewModel.saveConfig(i, value);
				} break;
				default : break;
			}

		}

	}

    @Override
    public void OnSealCloseClick() {
        sealParametersDialog.dismiss();
    }

    @Override
    public void OnSealRebootClick() {
        sealParametersDialog.dismiss();
        AlertDialog.Builder rebootBuilder = new AlertDialog.Builder(BlinkyActivity.this);
        rebootBuilder.setTitle("Перезагрузить пломбу?");
        rebootBuilder.setPositiveButton("Да", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                viewModel.reboot();
                dialog.cancel();
            }
        });
        rebootBuilder.setNegativeButton("Нет", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        AlertDialog alert = rebootBuilder.create();
        alert.show();
    }

	boolean checkAddrBytes(String str) {
		if(str.length()==10) {
			char[] chars = str.toCharArray();
			for (int i = 0; i < chars.length; i++) {
				if((chars[i]>='0' && chars[i]<='9') || (chars[i]>='A' && chars[i]<='F') || (chars[i]>='a' && chars[i]<='f')) {
				} else return false;
			}
			return true;
		} else return false;
	};

	public byte[] getAddrBytes(String str) {
		char[] chars = str.toCharArray();
		byte[] addrByte = new byte[chars.length];
		for(int i= 0; i < chars.length; i++) {
			if(chars[i]>='0' && chars[i]<='9') addrByte[i] = (byte) (chars[i] - '0');
			else if(chars[i]>='a' && chars[i]<='f') addrByte[i] = (byte) (chars[i] - 'a' + 10);
			else addrByte[i] = (byte) (chars[i] - 'A' + 10);
		}
		byte addr[] = new byte[5];
		for(int i= 0; i < addr.length; i++) {
			addr[i] = (byte) (addrByte[i*2]*16);
			addr[i] += addrByte[i*2+1];
		}
		return addr;
	};
}
