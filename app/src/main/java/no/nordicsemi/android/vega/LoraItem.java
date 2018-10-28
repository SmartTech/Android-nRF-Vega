package no.nordicsemi.android.vega;

import android.content.Context;
import android.view.View;

/**
 * Created by dev12 on 10.10.2018.
 */

public class LoraItem  {

    private String address;
    private Context ctx;
    LoraItem( String _addrress) {
        address = _addrress;
    }

    public String getAddress() {
        return address;
    }
}
