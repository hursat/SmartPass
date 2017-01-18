package com.example.hursat.smartpass2;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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


public class HomeFragment extends Fragment {

    private DatabaseReference fbDatabaseRef;
    private StorageReference fbStorageRef;

    private SharedPreferences sp;
    private TextView userName;
    private TextView userEmail;
    private ImageView userPhoto;
    private ListView userEventsListView;
    private HomeListViewAdapter homeListViewAdapter;

    private static final String spName = "SmartPassSharedPreferences";
    private static final String TAG = "HomeFragment";

    public HomeFragment() { }

     @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_home, container, false);

        fbDatabaseRef = FirebaseDatabase.getInstance().getReference();
        fbStorageRef = FirebaseStorage.getInstance().getReference();

        sp = getActivity().getSharedPreferences(spName, Context.MODE_PRIVATE);

        userName = (TextView) view.findViewById(R.id.user_profile_name);
        userEmail = (TextView) view.findViewById(R.id.user_profile_email);
        userPhoto = (ImageView) view.findViewById(R.id.user_profile_photo);
        userEventsListView = (ListView) view.findViewById(R.id.user_event_list);

        getUserInfoFromDB();

        final Context context = getActivity();

        ArrayList<Ticket> userEventsList = getUserEventsFromDB();

        homeListViewAdapter = new HomeListViewAdapter(context, userEventsList);

        userEventsListView.setAdapter(homeListViewAdapter);

        return view;
    }

    public void getUserInfoFromDB(){

        String uid = sp.getString("UID", "");

        fbDatabaseRef.child("users").child(uid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                String email = (String) dataSnapshot.child("email").getValue();
                String name = (String) dataSnapshot.child("name").getValue();
                String surname = (String) dataSnapshot.child("surname").getValue();

                userName.setText(name + " " + surname);
                userEmail.setText(email);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, "getUserInfoFromDB::OnCancelled : " + databaseError.getDetails());
            }
        });

        Glide.with(this).using(new FirebaseImageLoader()).load(fbStorageRef.child("users").child(uid + ".jpg")).into(userPhoto);

    }

    public ArrayList getUserEventsFromDB(){

        final ArrayList<Ticket> userEvents = new ArrayList<>();

        String uid = sp.getString("UID", "");

        fbDatabaseRef.child("users").child(uid).child("events").addValueEventListener(new ValueEventListener() {
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

                            userEvents.add(tmp);
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

        return userEvents;

    }

}
