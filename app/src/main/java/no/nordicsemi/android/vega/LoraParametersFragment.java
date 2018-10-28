package no.nordicsemi.android.vega;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

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

    public void onReceiveData(byte[] addr) {
//        if (mListener != null) {
 //           mListener.OnClick();
 //       }


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
