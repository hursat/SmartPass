package com.example.hursat.smartpass2;


import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

public class FriendProfileActivity extends AppCompatActivity {

    private DatabaseReference fbDatabaseRef;
    private StorageReference fbStorageRef;

    private TextView friendName;
    private TextView friendEmail;
    private ImageView friendPhoto;
    private ListView friendEventsListView;
    private HomeListViewAdapter homeListViewAdapter;

    private static final String spName = "SmartPassSharedPreferences";
    private static final String TAG = "FriendProfileAct";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_profile);

        String friendID = getIntent().getStringExtra("Friend_ID");

        fbDatabaseRef = FirebaseDatabase.getInstance().getReference();
        fbStorageRef = FirebaseStorage.getInstance().getReference();

        friendName = (TextView) findViewById(R.id.friend_profile_name);
        friendEmail = (TextView) findViewById(R.id.friend_profile_email);
        friendPhoto = (ImageView) findViewById(R.id.friend_profile_photo);
        friendEventsListView = (ListView) findViewById(R.id.friend_event_list);

        getUserInfoFromDB(friendID);

        final Context context = this;

        ArrayList<Ticket> friendEventsList = getUserEventsFromDB(friendID);

        homeListViewAdapter = new HomeListViewAdapter(context, friendEventsList);

        friendEventsListView.setAdapter(homeListViewAdapter);

    }

    public void getUserInfoFromDB(String id){

        fbDatabaseRef.child("users").child(id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                String email = (String) dataSnapshot.child("email").getValue();
                String name = (String) dataSnapshot.child("name").getValue();
                String surname = (String) dataSnapshot.child("surname").getValue();

                friendName.setText(name + " " + surname);
                friendEmail.setText(email);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, "getUserInfoFromDB::OnCancelled : " + databaseError.getDetails());
            }
        });

        Glide.with(this).using(new FirebaseImageLoader()).load(fbStorageRef.child("users").child(id + ".jpg")).into(friendPhoto);

    }

    public ArrayList getUserEventsFromDB(String id){

        final ArrayList<Ticket> friendEvents = new ArrayList<>();

        fbDatabaseRef.child("users").child(id).child("events").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                for(DataSnapshot child: dataSnapshot.getChildren()){

                    fbDatabaseRef.child("Tickets").child(child.getKey()).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {

                            Ticket tmp = new Ticket();
                            tmp.eventID = dataSnapshot.getKey();
                            tmp.eventName = (String) dataSnapshot.child("name").getValue();
                            tmp.eventPlace = (String) dataSnapshot.child("place").getValue();
                            tmp.eventDate = (String) dataSnapshot.child("date").getValue();

                            friendEvents.add(tmp);
                            homeListViewAdapter.notifyDataSetChanged();

                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            Log.e(TAG, "getUserEventsFromDB::OnCancelled 1: " + databaseError.getDetails());
                        }
                    });

                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, "getUserEventsFromDB::OnCancelled 2: " + databaseError.getDetails());
            }
        });

        return friendEvents;

    }


}
