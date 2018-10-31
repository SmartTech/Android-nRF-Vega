package no.nordicsemi.android.vega.adapter;
import no.nordicsemi.android.vega.LoraItem;
import no.nordicsemi.android.vega.R;

import java.util.ArrayList;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;


/**
 * Created by dev12 on 10.10.2018.
 */

public class LoraAdapter extends ArrayAdapter<LoraItem> implements View.OnClickListener {

    Context ctx;
    LayoutInflater lInflater;
    final ArrayList<LoraItem> loraObjects;
    ClickLoraDialogListener mListener;

    public interface ClickLoraDialogListener {
        void OnClickLoraItem(int index, String address);
    }
    private static class ViewHolder {
        TextView txtName;
        TextView txtType;
        TextView txtVersion;
    }

    @Override
    public void onClick(View v) {

    }

    public LoraAdapter(Context context, ArrayList<LoraItem> objects) {
        super(context,-1, objects);
        ctx = context;
        loraObjects = objects;
        lInflater = (LayoutInflater) ctx
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (context instanceof ClickLoraDialogListener) {
            mListener = (ClickLoraDialogListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }



    // кол-во элементов
    @Override
    public int getCount() {
        return loraObjects.size();
    }

    // элемент по позиции
    @Override
    public LoraItem getItem(int position) {
        return loraObjects.get(position);
    }

    // id по позиции
    @Override
    public long getItemId(int position) {
        return position;
    }

    // пункт списка
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // используем созданные, но не используемые view
        View view = convertView;
        if (view == null) {
            view = lInflater.inflate(R.layout.lora_item, parent, false);
        }

        LoraItem p = getLoraItem(position);
        view.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                //new AlertDialog.Builder(ctx).setTitle("touched").show();
                if (mListener != null ) {
                    mListener.OnClickLoraItem(position, p.getAddress() );
                }
            }

        });

        // заполняем View
        ((TextView) view.findViewById(R.id.addr_lora_item)).setText(position + ". " + p.getAddress());

        //ImageButton deleteBtn = (ImageButton) view.findViewById(R.id.delete_lora_item);
        // присваиваем обработчик
        //deleteBtn.setOnClickListener(loraDeleteClicked);
        // пишем позицию
        //deleteBtn.setTag(position);
        return view;
    }

    // товар по позиции
    LoraItem getLoraItem(int position) {
        return ((LoraItem) getItem(position));
    }

    public View.OnClickListener getLoraDeleteClicked() {
        return loraDeleteClicked;
    }

    View.OnClickListener loraDeleteClicked = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            // TODO Auto-generated method stub
            //deleteLora((Integer) v.getTag());
        }
    };
}
