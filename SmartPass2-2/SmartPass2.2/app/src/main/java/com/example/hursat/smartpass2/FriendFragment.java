package com.example.hursat.smartpass2;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;


public class FriendFragment extends Fragment {

    private ListView friendListView;
    private FriendsListViewAdapter friendListAdapter;
    private Button btnAddFriend;
    private DatabaseReference friendsReference;
    private DatabaseReference friendsInfoReference;
    private SharedPreferences sp;
    private String newFriendUID;
    private ArrayList<User> friendList;
    private Handler handler;

    private static final String TAG = "FriendsAct";
    private static final String spName = "SmartPassSharedPreferences";

    public FriendFragment() {
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        getActivity().invalidateOptionsMenu();
        View view = inflater.inflate(R.layout.fragment_friend, container, false);

        sp = getActivity().getSharedPreferences(spName, Context.MODE_PRIVATE);

        handler = new Handler();

        friendsReference = FirebaseDatabase.getInstance().getReference().child("friendRelations");
        friendsInfoReference = FirebaseDatabase.getInstance().getReference().child("users");

        final Context context = getActivity();
        friendList = getFriendsFromDB();

        friendListAdapter = new FriendsListViewAdapter(context, friendList);

        friendListView = (ListView) view.findViewById(R.id.friend_list);
        friendListView.setAdapter(friendListAdapter);

        friendListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Intent toFriendProfileAct = new Intent(getContext(), FriendProfileActivity.class);
                String friendID = friendList.get(position).uid;
                toFriendProfileAct.putExtra("Friend_ID", friendID);
                startActivity(toFriendProfileAct);

            }
        });

        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        getActivity().getMenuInflater().inflate(R.menu.friend, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if(id == R.id.action_add_friend){

            final AlertDialog.Builder addFriendAlertBuilder = new AlertDialog.Builder(getContext());

            addFriendAlertBuilder.setMessage("Enter the email address of the user:").setTitle("Add Friend");

            LayoutInflater dialogInflater = (LayoutInflater) getActivity().getSystemService(getContext().LAYOUT_INFLATER_SERVICE);
            final View alertDialogView = dialogInflater.inflate(R.layout.alert_dialog_add_friend, null);
            addFriendAlertBuilder.setView(alertDialogView);

            addFriendAlertBuilder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    final EditText txtNewFriendEmail = (EditText) alertDialogView.findViewById(R.id.add_friend_email);
                    String newFriendEmail = txtNewFriendEmail.getText().toString();
                    addFriend(newFriendEmail);
                    friendList.clear();

                }
            });
            addFriendAlertBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    dialog.dismiss();
                }
            });

            AlertDialog addFriendAlert = addFriendAlertBuilder.create();
            addFriendAlert.show();

        }

        return super.onOptionsItemSelected(item);

    }

    public ArrayList<User> getFriendsFromDB() {

        final ArrayList<User> friends = new ArrayList<>();

        String uid = sp.getString("UID", "");
        friendsReference.child(uid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                for(DataSnapshot child: dataSnapshot.getChildren()){
                    final String friendUID = child.getKey();

                    friendsInfoReference.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {

                            User tmp = new User();
                            tmp.uid = friendUID;
                            tmp.email = (String) dataSnapshot.child(friendUID).child("email").getValue();
                            tmp.name = (String) dataSnapshot.child(friendUID).child("name").getValue();
                            tmp.surname = (String) dataSnapshot.child(friendUID).child("surname").getValue();

                            friends.add(tmp);
                            friendListAdapter.notifyDataSetChanged();

                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                            Log.w(TAG, "loadFriendInfo::onCancelled", databaseError.toException());

                        }
                    });
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

                Log.w(TAG, "loadFriendUID::onCancelled", databaseError.toException());

            }
        });

        return friends;

    }

    public void addFriend(String email){

        friendsInfoReference.orderByChild("email").equalTo(email).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                for(DataSnapshot child: dataSnapshot.getChildren()){
                    newFriendUID = child.getKey();
                }

                Log.i(TAG, "Friend UID: " + newFriendUID);

                String uid = sp.getString("UID", "");
                friendsReference.child(uid).child(newFriendUID).setValue("");
                friendsReference.child(newFriendUID).child(uid).setValue("");

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "findFriendUID::onCancelled", databaseError.toException());
            }
        });

    }

}