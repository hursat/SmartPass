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

public class FriendsListViewAdapter extends BaseAdapter {

    private Context faContext;
    private ArrayList<User> friendList;
    private LayoutInflater faInflater;
    private StorageReference fbStorageRef;

    public FriendsListViewAdapter(Context context, ArrayList<User> friends){
        this.faContext = context;
        this.friendList = friends;
        this.faInflater = (LayoutInflater) faContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        fbStorageRef = FirebaseStorage.getInstance().getReference();
    }

    private static class friendViewRowHolder {
        public TextView txtTitleHolder;
        public TextView txtSubtitleHolder;
        public ImageView imgThumbnailHolder;
    }

    @Override
    public int getCount() {
        return friendList.size();
    }

    @Override
    public Object getItem(int position) {
        return friendList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        friendViewRowHolder rowHolder;

        if(convertView == null){

            convertView = faInflater.inflate(R.layout.list_item, parent, false);

            rowHolder = new friendViewRowHolder();
            rowHolder.txtTitleHolder = (TextView) convertView.findViewById(R.id.list_title);
            rowHolder.txtSubtitleHolder = (TextView) convertView.findViewById(R.id.list_subtitle);
            rowHolder.imgThumbnailHolder = (ImageView) convertView.findViewById(R.id.list_thumbnail);

            convertView.setTag(rowHolder);


        }
        else{
            rowHolder = (friendViewRowHolder) convertView.getTag();
        }

        TextView txtTitleRow = rowHolder.txtTitleHolder;
        TextView txtSubtitleRow = rowHolder.txtSubtitleHolder;
        ImageView imgImageRow = rowHolder.imgThumbnailHolder;

        User tmp = (User) getItem(position);

        txtTitleRow.setText(tmp.name + " " + tmp.surname);
        txtSubtitleRow.setText(tmp.email);

        Glide.with(parent.getContext()).using(new FirebaseImageLoader()).load(fbStorageRef.child("users").child(tmp.uid + ".jpg")).into(imgImageRow);

        return convertView;
    }
}
