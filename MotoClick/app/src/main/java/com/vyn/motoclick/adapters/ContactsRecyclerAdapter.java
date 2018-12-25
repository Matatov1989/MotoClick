package com.vyn.motoclick.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.vyn.motoclick.R;
import com.vyn.motoclick.database.ContactData;
import com.vyn.motoclick.database.UserData;

import java.util.ArrayList;

public class ContactsRecyclerAdapter extends RecyclerView.Adapter<ContactsRecyclerAdapter.ContactsHolder> {

    private Context context;
    private ArrayList<UserData> arrayListContact;

    public ContactsRecyclerAdapter(Context context, ArrayList<UserData> arrayListContact) {
        this.context = context;
        this.arrayListContact = arrayListContact;
    }

    @Override
    public ContactsRecyclerAdapter.ContactsHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater myInflator = LayoutInflater.from(context);
        View view = myInflator.inflate(R.layout.element_list_contact, parent, false);
        return new ContactsHolder(view);
    }

    @Override
    public void onBindViewHolder(final ContactsRecyclerAdapter.ContactsHolder holder, int position) {
        holder.textNameContact.setText(arrayListContact.get(position).getUserName());

        RequestOptions requestOptions = new RequestOptions()
                .placeholder(R.mipmap.ic_launcher)
                .error(R.mipmap.ic_launcher);

        Glide.with(context)
                .setDefaultRequestOptions(requestOptions)
                .load(arrayListContact.get(position).getUserUriPhoto())
                .into(holder.imageContact);

        holder.imageHaveMsg.setVisibility(View.VISIBLE);

        //     Log.d(LOG_TAG, "arrayListUser "+arrayListUser.get(position).getUserName() +" "+arrayListUser.get(position).getCntMsg());
/*      if (arrayListContact.get(position).getUid().equals(arrayListContact.get(position).getUid())){
          if (arrayListContact.get(position).getCntMsg() != 0)
              holder.imageHaveMsg.setVisibility(View.VISIBLE);
          else
              holder.imageHaveMsg.setVisibility(View.INVISIBLE);
      }*/

    }

    @Override
    public int getItemCount() {
        return arrayListContact.size();
    }

    public class ContactsHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {

        ImageView imageContact, imageHaveMsg;
        TextView textNameContact;

        public ContactsHolder(View view) {
            super(view);
            imageContact = (ImageView) view.findViewById(R.id.imageContact);
            imageHaveMsg = (ImageView) view.findViewById(R.id.imageHaveMsg);
            textNameContact = (TextView) view.findViewById(R.id.textNameContact);

            view.setOnClickListener(this);
            view.setOnLongClickListener(this);
        }

        @Override
        public void onClick(View v) {

        }

        @Override
        public boolean onLongClick(View v) {
            // Handle long click
            // Return true to indicate the click was handled
            //    dialogDeleteContact(arrayListUser.get(getAdapterPosition()).getUid(), arrayListUser.get(getAdapterPosition()).getUserName());

            return true;
        }
    }
}