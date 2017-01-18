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

public class TicketsListViewAdapter extends BaseAdapter {

    private Context taContext;
    private ArrayList<Ticket> ticketList;
    private LayoutInflater taInflater;
    private StorageReference fbStorageRef;


    public TicketsListViewAdapter(Context context, ArrayList<Ticket> tickets){
        this.taContext = context;
        this.ticketList = tickets;
        this.taInflater = (LayoutInflater) taContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        fbStorageRef = FirebaseStorage.getInstance().getReference();
    }

    private static class ticketViewRowHolder {
        public TextView txtTitleHolder;
        public TextView txtSubtitleHolder;
        public TextView txtDetailHolder;
        public ImageView imgThumbnailHolder;
    }

    @Override
    public int getCount() {
        return ticketList.size();
    }

    @Override
    public Object getItem(int position) {
        return ticketList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ticketViewRowHolder rowHolder;

        if(convertView == null){

            convertView = taInflater.inflate(R.layout.list_item, parent, false);

            rowHolder = new ticketViewRowHolder();
            rowHolder.txtTitleHolder = (TextView) convertView.findViewById(R.id.list_title);
            rowHolder.txtSubtitleHolder = (TextView) convertView.findViewById(R.id.list_subtitle);
            rowHolder.txtDetailHolder = (TextView) convertView.findViewById(R.id.list_detail);
            rowHolder.imgThumbnailHolder = (ImageView) convertView.findViewById(R.id.list_thumbnail);

            convertView.setTag(rowHolder);

        }
        else{
            rowHolder = (ticketViewRowHolder) convertView.getTag();
        }

        TextView txtTitleRow = rowHolder.txtTitleHolder;
        TextView txtSubtitleRow = rowHolder.txtSubtitleHolder;
        TextView txtDetailRow = rowHolder.txtDetailHolder;
        ImageView imgImageRow = rowHolder.imgThumbnailHolder;

        Ticket tmp = (Ticket) getItem(position);

        txtTitleRow.setText(tmp.eventName);
        txtSubtitleRow.setText(tmp.eventPlace);
        txtDetailRow.setText(tmp.eventDate);

        Glide.with(parent.getContext()).using(new FirebaseImageLoader()).load(fbStorageRef.child("events").child(tmp.eventID + ".jpg")).into(imgImageRow);


        return convertView;
    }
}
