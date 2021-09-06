package com.example.servedcalculator_nzs3910;


public class Item {

    private String kind;
    private String etag;
    private String summary;
    private String updated;
    private String description;
    private Creator creator;
    private Organizer organizer;
    private Start start;
    private End end;
    private String transparent;
    private String visibility;
    private String iCalUID;
    private int sequence;
    private String eventType;

   public Item(String kind, String etag, String summary, String updated, String description, Creator creator, Organizer organizer, Start start, End end, String transparent, String visibility, String iCalUID, int sequence, String eventType ){
       this.kind = kind;
       this.etag = etag;
       this.summary = summary;
       this.updated = updated;
       this.description = description;
       this.creator = creator;
       this.organizer = organizer;
       this.start = start;
       this.end = end;
       this.transparent = transparent;
       this.visibility = visibility;
       this.iCalUID = iCalUID;
       this.sequence = sequence;
       this.eventType = eventType;
   }
    @Override
    public boolean equals(Object anObject){
        if (anObject instanceof String) {
            String tempString = (String) anObject;
            int n = getDescription().toCharArray().length;
            if (n == tempString.toCharArray().length) {
                char v1[] = getDescription().toCharArray();
                char v2[] = tempString.toCharArray();
                int i = 0;
                while (n-- != 0) {
                    if (v1[i] != v2[i])
                        return false;
                    i++;
                }
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString(){
       return summary + " " + description + " " + start.toString();
    }

    public String getSummary() {
        return summary;
    }

    public String getDescription() {
        return description;
    }

    public Creator getCreator() {
        return creator;
    }

    public Start getStart() {
        return start;
    }
/*
   {
   "kind": "calendar#event",
   "etag": "\"3259935444936000\"",
   "id": "20200101_pms605jo0bbishheog4lp28i2s",
   "status": "confirmed",
   "htmlLink": "https://www.google.com/calendar/event?eid=MjAyMDAxMDFfcG1zNjA1am8wYmJpc2hoZW9nNGxwMjhpMnMgZW4ubmV3X3plYWxhbmQjaG9saWRheUB2",
   "created": "2021-08-26T08:48:42.000Z",
   "updated": "2021-08-26T08:48:42.468Z",
   "summary": "New Year's Day",
   "description": "Public holiday",
   "creator": {
    "email": "en.new_zealand#holiday@group.v.calendar.google.com",
    "displayName": "Holidays in New Zealand",
    "self": true
   },
   "organizer": {
    "email": "en.new_zealand#holiday@group.v.calendar.google.com",
    "displayName": "Holidays in New Zealand",
    "self": true
   },
   "start": {
    "date": "2020-01-01"
   },
   "end": {
    "date": "2020-01-02"
   },
   "transparency": "transparent",
   "visibility": "public",
   "iCalUID": "20200101_pms605jo0bbishheog4lp28i2s@google.com",
   "sequence": 0,
   "eventType": "default"
  }
    */
}

class Organizer {
    private String email;
    private String displayName;
    private String self;

    public Organizer(String email, String displayName, String self){
        this.email = email;
        this.displayName = displayName;
        this.self = self;
    }
}

class Start {
    private String Date;

    public String getDate() {
        return Date;
    }

    public Start(String Date){
        this.Date = Date;
    }

    @Override
    public String toString(){
        return Date;
    }
}

class End {
    private String Date;
    public End(String Date){
        this.Date = Date;
    }
}

class Creator {
    private String email;
    private String displayName;
    private String self;

    public Creator(String email, String displayName, String self){
        this.email = email;
        this.displayName = displayName;
        this.self = self;
    }
}