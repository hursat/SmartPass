package com.example.hursat.smartpass2;

/**
 * Created by hursat on 28.11.2016.
 */

public class Ticket {

    public String eventID;
    public String eventName;
    public String eventPlace;
    public String eventDate;

    public Ticket(){
        this.eventName = "deneme";
        this.eventPlace = "deneme2";
        this.eventDate = "deneme3";
    }

    public Ticket(String eventID, String date, String name, String place){
        this.eventID = eventID;
        this.eventName = name;
        this.eventPlace = place;
        this.eventDate = date;
    }

}
