package com.example.hursat.smartpass2;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;


public class TicketFragment extends Fragment {

    private ListView eventListView;
    private DatabaseReference fbDatabaseReference;
    private TicketsListViewAdapter ticketListAdapter;

    private static final String TAG = "TicketFragment";
    private static final String spName = "SmartPassSharedPreferences";
    
    public TicketFragment() { }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_ticket, container, false);

        fbDatabaseReference = FirebaseDatabase.getInstance().getReference();

        final Context context = getActivity();
        final ArrayList<Ticket> ticketList = getTicketsFromDB();

        ticketListAdapter = new TicketsListViewAdapter(context, ticketList);

        eventListView = (ListView) view.findViewById(R.id.event_list);
        eventListView.setAdapter(ticketListAdapter);
        eventListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                final Ticket selectedTicket = ticketList.get(position);
                final String boughtTicketID = selectedTicket.eventID;

                final AlertDialog.Builder buyTicketAlertBuilder = new AlertDialog.Builder(context);

                buyTicketAlertBuilder.setMessage("Do you want to buy this ticket?").setTitle("Buy Ticket");

                buyTicketAlertBuilder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                        SharedPreferences sp = getActivity().getSharedPreferences(spName, Context.MODE_PRIVATE);
                        fbDatabaseReference.child("Tickets").child(boughtTicketID).child("participants").child(sp.getString("UID", "")).setValue("");
                        fbDatabaseReference.child("users").child(sp.getString("UID", "")).child("events").child(boughtTicketID).setValue("");
                        ticketList.clear();

                    }
                });
                buyTicketAlertBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });

                AlertDialog addFriendAlert = buyTicketAlertBuilder.create();
                addFriendAlert.show();

            }
        });

        return view;

    }

    public ArrayList<Ticket> getTicketsFromDB(){

        final ArrayList<Ticket> ticketList = new ArrayList<>();

        fbDatabaseReference.child("Tickets").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                for(DataSnapshot child: dataSnapshot.getChildren()){

                    Ticket tmp = new Ticket();
                    tmp.eventID = child.getKey();
                    tmp.eventName = (String) child.child("name").getValue();
                    tmp.eventPlace = (String) child.child("place").getValue();
                    tmp.eventDate = (String) child.child("date").getValue();

                    ticketList.add(tmp);
                    ticketListAdapter.notifyDataSetChanged();

                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d(TAG, "loadTickets:onCancelled", databaseError.toException());
            }
        });

        return ticketList;
    }

}