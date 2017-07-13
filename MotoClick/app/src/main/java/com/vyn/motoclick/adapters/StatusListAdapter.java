package com.vyn.motoclick.adapters;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.vyn.motoclick.R;

/**
 * Created by Yurka on 25.06.2017.
 */

public class StatusListAdapter extends ArrayAdapter<String> {

    private final Activity context;
    private final String[] itemname;
    private final Integer[] imgid;

    public StatusListAdapter(Activity context, String[] itemname, Integer[] imgid) {
        super(context, R.layout.element_dialog_status, itemname);
        // TODO Auto-generated constructor stub

        this.context = context;
        this.itemname = itemname;
        this.imgid = imgid;
    }

    public View getView(int position, View view, ViewGroup parent) {
        LayoutInflater inflater = context.getLayoutInflater();
        View rowView = inflater.inflate(R.layout.element_dialog_status, null, true);

        ImageView imageStatus = (ImageView) rowView.findViewById(R.id.icStatus);
        TextView textStatus = (TextView) rowView.findViewById(R.id.textStatus);

        imageStatus.setImageResource(imgid[position]);
        textStatus.setText(itemname[position]);
        return rowView;
    }
}