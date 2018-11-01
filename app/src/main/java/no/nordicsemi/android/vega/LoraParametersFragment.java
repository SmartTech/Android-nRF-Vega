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
import android.widget.TextView;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import static android.view.View.GONE;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link LoraParametersFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link LoraParametersFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class LoraParametersFragment extends DialogFragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_INDEX = "index";
    private static final String ARG_ADDRESS = "param2";

    // TODO: Rename and change types of parameters
    private int mIndex;
    private String mAddress;
    private View mProgressBar;
    private View mParametersView;

    private LoraDataListener mListener;
    private byte[] dialogResult;

    public LoraParametersFragment() {
        // Required empty public constructor
    }
    public interface LoraDataListener {
        void OnDeleteClick(int index, String address);

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
    public static LoraParametersFragment newInstance(int param1, String param2) {
        LoraParametersFragment fragment = new LoraParametersFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_INDEX, param1);
        args.putString(ARG_ADDRESS, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mIndex = getArguments().getInt(ARG_INDEX);
            mAddress = getArguments().getString(ARG_ADDRESS);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_add_lora, container, false);
        TextView index = view.findViewById(R.id.lora_index);
        index.setText("Index: " + mIndex);
        TextView address = view.findViewById(R.id.lora_address);
        address.setText("Adderss: " + mAddress);
        mProgressBar = view.findViewById(R.id.lora_progress);
        mParametersView = view.findViewById(R.id.lora_parameters);

        Button but = (Button) view.findViewById(R.id.lora_del_but);
        but.setOnClickListener( new View.OnClickListener() {

                                    @Override
                                    public void onClick(View v) {
                                        onButtonPressed(mIndex, mAddress);
                                    }
                                }
        );
        return view;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(int index, String address) {
        if (mListener != null) {
            mListener.OnDeleteClick(index, address );
            mProgressBar.setVisibility(View.VISIBLE);
            mParametersView.setVisibility(View.GONE);
        }
    }

    int bitRead(byte b, int bitPos)
    {
        int x = b & (1 << bitPos);
        return x == 0 ? 0 : 1;
    }

    public void onReceiveData(byte[] data) {
//        if (mListener != null) {
        //           mListener.OnClick();
        //       }
        //data[2]

        TextView lora_battery = mParametersView.findViewById(R.id.lora_battery);
        TextView lora_rssi    = mParametersView.findViewById(R.id.lora_rssi);
        TextView lora_hall1   = mParametersView.findViewById(R.id.lora_hall1);
        TextView lora_hall2   = mParametersView.findViewById(R.id.lora_hall2);
        TextView lora_jumper  = mParametersView.findViewById(R.id.lora_jumper);
        TextView lora_tamper  = mParametersView.findViewById(R.id.lora_tamper);
        TextView lora_accel   = mParametersView.findViewById(R.id.lora_accel);
        TextView lora_axis    = mParametersView.findViewById(R.id.lora_axis);
        TextView lora_temp    = mParametersView.findViewById(R.id.lora_temp);

        lora_battery.setText("Battery: " + String.valueOf(data[4]));
        lora_rssi.setText("RSSI: " + String.valueOf(data[5]));

        lora_jumper.setText("Jumper: " + String.valueOf(bitRead(data[5], 0)));
        lora_hall1.setText("Hall 1: " + String.valueOf(bitRead(data[5], 1)));
        lora_hall2.setText("Hall 2: " + String.valueOf(bitRead(data[5], 2)));
        lora_tamper.setText("Tamper: " + String.valueOf(bitRead(data[5], 3)));
        lora_accel.setText("Accel: " + String.valueOf(bitRead(data[5], 4)));

        byte dataTemp[] = new byte[]{data[6], data[7], data[8], data[9]};
        float temp = ByteBuffer.wrap(dataTemp).order(ByteOrder.LITTLE_ENDIAN).getFloat();
        lora_temp.setText("Temp: " + String.format("%.01f", temp) + "°C");

        Log.e("LoRa", "getData index = " + data[2]);
        Log.e("Events" , String.valueOf(data[3]));
        Log.e("RSSI" , String.valueOf(data[5]));
        Log.e("Temp" , String.format("%.01f", temp) + "°C");
        Log.e("Bat" , String.valueOf(data[4]));

        mProgressBar.setVisibility(GONE);
        mParametersView.setVisibility(View.VISIBLE);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
       if (context instanceof LoraDataListener) {
            mListener = (LoraDataListener) context;
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
