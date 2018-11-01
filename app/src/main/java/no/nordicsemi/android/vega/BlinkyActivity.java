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
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import no.nordicsemi.android.vega.adapter.ExtendedBluetoothDevice;
import no.nordicsemi.android.vega.adapter.LoraAdapter;
import no.nordicsemi.android.vega.utils.Utils;
import no.nordicsemi.android.vega.viewmodels.BlinkyViewModel;

//import android.support.v4.app.FragmentManager;

public class BlinkyActivity extends AppCompatActivity implements LoraAdapter.ClickLoraDialogListener, LoraParametersFragment.LoraDataListener {


	public static final String EXTRA_DEVICE = "no.nordicsemi.android.vega.EXTRA_DEVICE";

    LoraParametersFragment loraParametersDialog = null;
    BlinkyViewModel viewModel;

	//ArrayList<LoraItem> loraItems = new ArrayList<LoraItem>();

	LoraAdapter loraAdapter;

	Button armControlBtn;
	Button addLoraBtn;

	AlertDialog.Builder addLoraBuilder;
	EditText addLoraInput;

	AlertDialog.Builder armBuilder;

	ProgressBar armProgressBar;
	TextView armState;
	TextView mSerial;
	TextView mTempValue;

	ConstraintLayout armContainer;
	Handler mTimeoutHandler;

	MediaPlayer mp;

	boolean state = false;

	int armStateFlag = 0;

//	int loraCount = 0;

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_blinky);
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

		armProgressBar = findViewById(R.id.arm_progress_bar);
		armProgressBar.setVisibility(View.GONE);

		// Set up views
		armState = findViewById(R.id.arm_control_state);

		armContainer = findViewById(R.id.arm_container);

		//final Switch led = findViewById(R.id.led_switch);
		final LinearLayout progressContainer = findViewById(R.id.progress_container);
		final TextView connectionState = findViewById(R.id.connection_state);
		final View content = findViewById(R.id.device_container);

		mSerial = findViewById(R.id.info_device_serial_value);

		viewModel.getSerialNumber().observe(this, serial -> {
			mSerial.setText(Utils.byteArrayToHexString(serial));
		});

		mTempValue = findViewById(R.id.info_device_temp_value);

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

		viewModel.getLoraState().observe(this, value -> {
			// CHAR_CMD_LORA events
            Log.e("Observed lora state", String.valueOf(value[1]) );
			switch(value[1]) {
				// CHAR_LORA_COUNT

//				case 0 : {
//					if(loraCount>0) {
//						loraItems.clear();
//						loraAdapter.notifyDataSetChanged();
//					}
//					loraCount = value[2];
//					Log.e("LORA_count", "Unknown");
//				} break;

				// CHAR_LORA_ADDR
				case 2 : {
					Log.e("LORA_addr", String.valueOf(value[2]));
                    if (loraParametersDialog != null) {
                        if (mTimeoutHandler != null ) {
                            mTimeoutHandler.removeCallbacksAndMessages(null);
                            mTimeoutHandler = null;
                        }
                        loraParametersDialog.onReceiveData(value);
                    }
//					if(value[2]<loraCount) {
//						StringBuffer addr = new StringBuffer();
//						for(int i=0; i<5; i++) {
//							int intVal = value[3+i] & 0xff;
//							if (intVal < 0x10) addr.append("0");
//							addr.append(Integer.toHexString(intVal));
//						}
//						loraItems.add(new LoraItem(addr.toString()));
//					loraAdapter.notifyDataSetChanged();

//					}
				} break;
				// CHAR_LORA_DATA
//				case 2 : {
//
//				} break;
				// CHAR_LORA_GET
//				case 3 : {
//
//				} break;
				// CHAR_LORA_ADD
//				case 4 : {
//
//				} break;
				// CHAR_LORA_DEL
//				case 5 : {
//
//				} break;
				default: break;
			};
		});

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

		viewModel.getLoraItems().observe(this, value -> {
			loraAdapter.notifyDataSetChanged();
			Log.e("LORA_items ", Integer.toString(loraAdapter.getCount()));
		});


		//loraParametersDialog = new LoraParametersFragment();
		//viewModel.getButtonState().observe(this, pressed -> buttonState.setText(pressed ? R.string.button_pressed : R.string.button_released));
		Log.e("TEST", "Test");
	}

	View.OnClickListener armControlClicked = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			armContainer.setBackgroundColor(Color.WHITE);
			armProgressBar.setVisibility(View.VISIBLE);
			armState.setText(R.string.arm_check);
			armControlBtn.setEnabled(false);
			switch(armStateFlag) {
				case 0 : viewModel.prearm();     break; // охрана снята
				//case 1 : viewModel.armConfirm(); break; // ожидание троса
				case 2 : viewModel.disarm();     break; // охрана установлена
				default: break;
			}
		}
	};

	View.OnClickListener addLoraClicked = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			AlertDialog.Builder builder = new AlertDialog.Builder(BlinkyActivity.this);
			if(addLoraInput.getParent()!=null)
				((ViewGroup)addLoraInput.getParent()).removeView(addLoraInput); // <- fix
			builder.setView(addLoraInput);
			builder.setTitle("Add LoRa")
					//.setIcon(R.drawable.ic_android_cat)
					.setCancelable(true)
					//.setView(addLoraInput)
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
    public void OnDeleteClick(int index, String address) {
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
