package com.vyn.motoclick.adapters;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.vyn.motoclick.R;

/**
 * Created by Yurka on 22.10.2017.
 */

public class FriendsListAdapter extends ArrayAdapter<String> {
    private final Activity context;
    private final String[] itemName;

    public FriendsListAdapter(Activity context, String[] itemName) {
        super(context, R.layout.element_dialog_friends, itemName);
        // TODO Auto-generated constructor stub
        this.context = context;
        this.itemName = itemName;
    }

    public View getView(int position, View view, ViewGroup parent) {
        LayoutInflater inflater = context.getLayoutInflater();
        View rowView = inflater.inflate(R.layout.element_dialog_friends, null, true);

        TextView textNameFriend = (TextView) rowView.findViewById(R.id.textNameFriend);

        textNameFriend.setText(itemName[position]);
        return rowView;
    }
}