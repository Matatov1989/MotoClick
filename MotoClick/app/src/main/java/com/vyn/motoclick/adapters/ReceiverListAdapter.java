package com.vyn.motoclick.adapters;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.vyn.motoclick.R;

/**
 * Created by Yurka on 28.10.2017.
 */

public class ReceiverListAdapter extends ArrayAdapter<String> {
    private final Activity context;
    private final String[] itemName;
    private final int[] itemCnt;

    public ReceiverListAdapter(Activity context, String[] itemName, int[] itemCnt) {
        super(context, R.layout.element_dialog_receiver, itemName);
        // TODO Auto-generated constructor stub
        this.context = context;
        this.itemName = itemName;
        this.itemCnt = itemCnt;
    }

    public View getView(int position, View view, ViewGroup parent) {
        LayoutInflater inflater = context.getLayoutInflater();
        View rowView = inflater.inflate(R.layout.element_dialog_receiver, null, true);

        TextView textNameReceiver = (TextView) rowView.findViewById(R.id.textNameReceiver);
        TextView textCountSms = (TextView) rowView.findViewById(R.id.textCountSms);



        textNameReceiver.setText(itemName[position]);
        if (itemCnt[position] == 0)
            textCountSms.setText("");
        else
            textCountSms.setText(""+itemCnt[position]);
        return rowView;
    }
}