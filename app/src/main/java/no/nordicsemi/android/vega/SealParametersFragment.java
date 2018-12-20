package no.nordicsemi.android.vega;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link SealParametersFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link SealParametersFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SealParametersFragment extends DialogFragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
//    private static final String ARG_INDEX = "index";
//    private static final String ARG_ADDRESS = "param2";

    // TODO: Rename and change types of parameters
//    private int mIndex;
//    private String mAddress;
    private View mProgressBar;
    private View mParametersView;

    private SealDataListener mListener;
    private byte[] dialogResult;

    public SealParametersFragment() {
        // Required empty public constructor
    }
    public interface SealDataListener {
        void OnSealSaveClick();
        void OnSealCloseClick();
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment LoraParametersFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static SealParametersFragment newInstance() {
        SealParametersFragment fragment = new SealParametersFragment();
//        Bundle args = new Bundle();
        //args.putInt(ARG_INDEX, param1);
        //args.putString(ARG_ADDRESS, param2);
//        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        if (getArguments() != null) {
//            mIndex = getArguments().getInt(ARG_INDEX);
//            mAddress = getArguments().getString(ARG_ADDRESS);
//        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_config, container, false);

        mProgressBar = view.findViewById(R.id.seal_config_progress);
        mParametersView = view.findViewById(R.id.seal_parameters);

        Button btnSave = view.findViewById(R.id.btn_seal_config_save);
        btnSave.setOnClickListener( new View.OnClickListener() {

                                    @Override
                                    public void onClick(View v) {
                                        onSavePressed();
                                    }
                                }
        );

        Button btnClose = view.findViewById(R.id.btn_seal_config_cancel);
        btnClose.setOnClickListener( new View.OnClickListener() {

                                    @Override
                                    public void onClick(View v) {
                                        onClosePressed();
                                    }
                                }
        );

        return view;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onSavePressed() {
        if (mListener != null) {
            mListener.OnSealSaveClick();
        }
        mProgressBar.setVisibility(View.VISIBLE);
        mParametersView.setVisibility(View.GONE);
    }

    public void onClosePressed() {
        if (mListener != null) {
            mListener.OnSealCloseClick();
        }
        mProgressBar.setVisibility(View.GONE);
        mParametersView.setVisibility(View.GONE);
    }

    int bitRead(byte b, int bitPos)
    {
        int x = b & (1 << bitPos);
        return x == 0 ? 0 : 1;
    }

    public void onReceiveData(int index, Object data) {


        switch(index) {
        	// CHAR_CONFIG_PHONE
        	case 0 : {

        	} break;
        	// CHAR_CONFIG_ID
        	case 1 : {
                EditText seal_param = mParametersView.findViewById(R.id.seal_config_value_id);
                String value = (String) data;
                seal_param.setText(value);
        	} break;
        	// CHAR_CONFIG_OID
        	case 2 : {
                EditText seal_param = mParametersView.findViewById(R.id.seal_config_value_oid);
                Integer value = (Integer) data;
                seal_param.setText(String.valueOf(value));
        	} break;
        	// CHAR_CONFIG_SLEEP_IDLE
        	case 3 : {
                EditText seal_param = mParametersView.findViewById(R.id.seal_config_value_sleepIdle);
                Integer value = (Integer) data;
                seal_param.setText(String.valueOf(value));
        	} break;
        	// CHAR_CONFIG_SLEEP_ARM
        	case 4 : {
                EditText seal_param = mParametersView.findViewById(R.id.seal_config_value_sleepArm);
                Integer value = (Integer) data;
                seal_param.setText(String.valueOf(value));
        	} break;
        	// CHAR_CONFIG_ACCEL
        	case 5 : {
                Switch seal_switch = mParametersView.findViewById(R.id.seal_config_switch_accel);
                seal_switch.setChecked((Integer) data > 0);
        	} break;
        	// CHAR_CONFIG_HALL
        	case 6 : {
                Switch seal_switch = mParametersView.findViewById(R.id.seal_config_switch_hall);
                seal_switch.setChecked((Integer) data > 0);
        	} break;
        	// CHAR_CONFIG_WAIT
        	case 7 : {
                EditText seal_param = mParametersView.findViewById(R.id.seal_config_value_wait_rope);
                Integer value = (Integer) data;
                seal_param.setText(String.valueOf(value));
        	} break;
        	// CHAR_CONFIG_TIME_GSM
        	case 8 : {
                EditText seal_param = mParametersView.findViewById(R.id.seal_config_value_time_gsm);
                Integer value = (Integer) data;
                seal_param.setText(String.valueOf(value));
        	} break;
        	// CHAR_CONFIG_TIME_SMS
        	case 9 : {
                EditText seal_param = mParametersView.findViewById(R.id.seal_config_value_time_sms);
                Integer value = (Integer) data;
                seal_param.setText(String.valueOf(value));
        	} break;
        	// CHAR_CONFIG_TIME_EGTS
        	case 10 : {
                EditText seal_param = mParametersView.findViewById(R.id.seal_config_value_time_egts);
                Integer value = (Integer) data;
                seal_param.setText(String.valueOf(value));
        	} break;
        	// CHAR_CONFIG_SMS_GPS
        	case 11 : {
                Switch seal_switch = mParametersView.findViewById(R.id.seal_config_switch_smsGps);
                seal_switch.setChecked((Integer) data > 0);
        	} break;
        	// CHAR_CONFIG_SMS_ALERT
        	case 12 : {
                Switch seal_switch = mParametersView.findViewById(R.id.seal_config_switch_smsAlert);
                seal_switch.setChecked((Integer) data > 0);
        	} break;
        	// CHAR_CONFIG_SMS_WAKE
        	case 13 : {

        	} break;
        	// CHAR_CONFIG_WIALON_USAGE
        	case 14 : {
                Switch seal_switch = mParametersView.findViewById(R.id.seal_config_switch_wialonUsage);
                seal_switch.setChecked((Integer) data > 0);
        	} break;
        	// CHAR_CONFIG_WIALON_ADDR
        	case 15 : {

        	} break;
        	// CHAR_CONFIG_GLOSAV_ADDR
        	case 16 : {

        	} break;
        	// CHAR_CONFIG_EGTS_WAKE
        	case 17 : {

        	} break;
        	// CHAR_CONFIG_LORA_USAGE
        	case 18 : {
                Switch seal_switch = mParametersView.findViewById(R.id.seal_config_switch_loraUsage);
                seal_switch.setChecked((Integer) data > 0);
        	} break;
        	// CHAR_CONFIG_LORA_PERIOD
        	case 19 : {

        	} break;
        	// CHAR_CONFIG_LORA_TIME
        	case 20 : {

        	} break;
        	// CHAR_CONFIG_LORA_POWER
        	case 21 : {

        	} break;
        	// CHAR_CONFIG_ALERT_FT
        	case 22 : {
                EditText seal_param = mParametersView.findViewById(R.id.seal_config_value_alert_ft);
                Integer value = (Integer) data;
                seal_param.setText(String.valueOf(value));
        	} break;
        	// CHAR_CONFIG_ALERT_CL
        	case 23 : {
                EditText seal_param = mParametersView.findViewById(R.id.seal_config_value_alert_cl);
                Integer value = (Integer) data;
                seal_param.setText(String.valueOf(value));
        	} break;
        	// CHAR_CONFIG_ALERT_AL
        	case 24 : {
                EditText seal_param = mParametersView.findViewById(R.id.seal_config_value_alert_al);
                Integer value = (Integer) data;
                seal_param.setText(String.valueOf(value));
        	} break;
        	// CHAR_CONFIG_GPS_TFIX
        	case 25 : {
                EditText seal_param = mParametersView.findViewById(R.id.seal_config_value_gps_tfix);
                Integer value = (Integer) data;
                seal_param.setText(String.valueOf(value));
        	} break;
        	// CHAR_CONFIG_GPS_TPOS
        	case 26 : {
                EditText seal_param = mParametersView.findViewById(R.id.seal_config_value_gps_tpos);
                Integer value = (Integer) data;
                seal_param.setText(String.valueOf(value));
        	} break;
        	// CHAR_CONFIG_GPS_FNEAR
        	case 27 : {
                EditText seal_param = mParametersView.findViewById(R.id.seal_config_value_gps_fnear);
                Integer value = (Integer) data;
                seal_param.setText(String.valueOf(value));
        	} break;
        	// CHAR_CONFIG_GPS_FSTOP
        	case 28 : {
                EditText seal_param = mParametersView.findViewById(R.id.seal_config_value_gps_fstop);
                Integer value = (Integer) data;
                seal_param.setText(String.valueOf(value));
        	} break;
        	// CHAR_CONFIG_GPS_FSPD
        	case 29 : {
                EditText seal_param = mParametersView.findViewById(R.id.seal_config_value_gps_fspd);
                Integer value = (Integer) data;
                seal_param.setText(String.valueOf(value));
        	} break;
        	// CHAR_CONFIG_GPS_FSKIP
        	case 30 : {
                EditText seal_param = mParametersView.findViewById(R.id.seal_config_value_gps_fskip);
                Integer value = (Integer) data;
                seal_param.setText(String.valueOf(value));
        	} break;
        	// CHAR_CONFIG_GPS_FSAT
        	case 31 : {
                EditText seal_param = mParametersView.findViewById(R.id.seal_config_value_gps_fsat);
                Integer value = (Integer) data;
                seal_param.setText(String.valueOf(value));
        	} break;
        	// CHAR_CONFIG_GPS_OSI
        	case 32 : {
                EditText seal_param = mParametersView.findViewById(R.id.seal_config_value_gps_osi);
                Integer value = (Integer) data;
                seal_param.setText(String.valueOf(value));
        	} break;
        	default: {
        		Log.e("onCmdConfig", "Unknown subCmd");
        		break;
        	}
        }

        mProgressBar.setVisibility(View.GONE);
        mParametersView.setVisibility(View.VISIBLE);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
       if (context instanceof SealDataListener) {
            mListener = (SealDataListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

}
