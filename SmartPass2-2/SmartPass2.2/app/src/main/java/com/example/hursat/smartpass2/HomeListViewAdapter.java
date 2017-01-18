package com.example.hursat.smartpass2;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

/**
 * Created by hursat on 28.11.2016.
 */

public class HomeListViewAdapter extends BaseAdapter {

    private Context homeContext;
    private ArrayList<Ticket> userEventList;
    private LayoutInflater hlInflater;
    private StorageReference fbStorageRef;

    public HomeListViewAdapter(Context context, ArrayList<Ticket> eventList){

        this.homeContext = context;
        this.userEventList = eventList;
        this.hlInflater = (LayoutInflater) homeContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        fbStorageRef = FirebaseStorage.getInstance().getReference();
    }

    private static class eventViewRowHolder {
        public TextView txtEventNameHolder;
        public TextView txtDateHolder;
        public ImageView imgBackgroundHolder;
    }

    @Override
    public int getCount() {
        return userEventList.size();
    }

    @Override
    public Object getItem(int position) {
        return userEventList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        eventViewRowHolder rowHolder;

        if(convertView == null){

            convertView = hlInflater.inflate(R.layout.list_item2, parent, false);

            rowHolder = new eventViewRowHolder();
            rowHolder.txtEventNameHolder = (TextView) convertView.findViewById(R.id.list_item_text_event);
            rowHolder.txtDateHolder = (TextView) convertView.findViewById(R.id.list_item_text_date);
            rowHolder.imgBackgroundHolder = (ImageView) convertView.findViewById(R.id.list_item_background_image);

            convertView.setTag(rowHolder);

        }
        else{
            rowHolder = (eventViewRowHolder) convertView.getTag();
        }

        TextView txtEventName = rowHolder.txtEventNameHolder;
        TextView txtDateRow = rowHolder.txtDateHolder;
        ImageView imgImageRow = rowHolder.imgBackgroundHolder;

        Ticket tmp = (Ticket) getItem(position);

        txtEventName.setText(tmp.eventName + " at " + tmp.eventPlace);
        txtDateRow.setText(tmp.eventDate);

        Glide.with(parent.getContext()).using(new FirebaseImageLoader()).load(fbStorageRef.child("events").child(tmp.eventID + ".jpg")).into(imgImageRow);

        return convertView;
    }
}
