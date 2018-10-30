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
//import android.support.v4.app.FragmentManager;
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

import java.util.ArrayList;

import no.nordicsemi.android.vega.adapter.ExtendedBluetoothDevice;
import no.nordicsemi.android.vega.adapter.LoraAdapter;
import no.nordicsemi.android.vega.utils.Utils;
import no.nordicsemi.android.vega.viewmodels.BlinkyViewModel;

public class BlinkyActivity extends AppCompatActivity implements LoraAdapter.ClickLoraDialogListener, LoraParametersFragment.LoraDataListener {


	public static final String EXTRA_DEVICE = "no.nordicsemi.android.vega.EXTRA_DEVICE";

    LoraParametersFragment loraParametersDialog = null;
    BlinkyViewModel viewModel;

	ArrayList<LoraItem> loraItems = new ArrayList<LoraItem>();

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

	int loraCount = 0;

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

		loraAdapter = new LoraAdapter(this, loraItems);

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
			if (connected) {
		//		viewModel.requestInfo();
			} else {
				finish();
			}
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
				mp=MediaPlayer.create(getApplicationContext(),R.raw.arm);// the song is a filename which i have pasted inside a folder **raw** created under the **res** folder.//
				mp.start();
				armContainer.setBackgroundColor(Color.GREEN);
				armState.setText(R.string.arm_state_true);
				armControlBtn.setText(R.string.arm_button_disarm);
				armStateFlag = 2;
				armBuilder.show().dismiss();
			}
			// охрана снята
			else if(value==3) {
				mp=MediaPlayer.create(getApplicationContext(),R.raw.disarm);// the song is a filename which i have pasted inside a folder **raw** created under the **res** folder.//
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
				mp=MediaPlayer.create(getApplicationContext(),R.raw.alarm);// the song is a filename which i have pasted inside a folder **raw** created under the **res** folder.//
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
			switch(value[1]) {
				// CHAR_LORA_COUNT
				case 0 : {
					if(loraCount>0) {
						loraItems.clear();
						loraAdapter.notifyDataSetChanged();
					}
					loraCount = value[2];
					Log.e("LORA_count", String.valueOf(loraCount));
				} break;
				// CHAR_LORA_ADDR
				case 1 : {
					Log.e("LORA_addr", String.valueOf(value[2]));
                    if (loraParametersDialog != null) {
                        if (mTimeoutHandler != null ) {
                            mTimeoutHandler.removeCallbacksAndMessages(null);
                            mTimeoutHandler = null;
                        }
                        loraParametersDialog.onReceiveData(value);
                    }
					if(value[2]<loraCount) {
						StringBuffer addr = new StringBuffer();
						for(int i=0; i<5; i++) {
							int intVal = value[3+i] & 0xff;
							if (intVal < 0x10) addr.append("0");
							addr.append(Integer.toHexString(intVal));
						}
						loraItems.add(new LoraItem(addr.toString()));
						loraAdapter.notifyDataSetChanged();

					}
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
		});

		viewModel.getStatusState().observe(this, value -> {

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
            // TODO Auto-generated method stub

			AlertDialog.Builder builder = new AlertDialog.Builder(BlinkyActivity.this);
			if(addLoraInput.getParent()!=null)
				((ViewGroup)addLoraInput.getParent()).removeView(addLoraInput); // <- fix
			builder.setView(addLoraInput);
			builder.setTitle("Add LoRa")
					//.setMessage("Покормите кота!")
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
							loraItems.add(new LoraItem( addLoraInput.getText().toString()));
							addLoraInput.setText("");
							loraAdapter.notifyDataSetChanged();
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
        viewModel.requestInfo();
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
        mTimeoutHandler = new Handler();
        mTimeoutHandler.postDelayed(new Runnable() {

            public void run() {
                loraParametersDialog.dismiss();
                loraParametersDialog = null;
            }
        }, 5000);
    }


}
