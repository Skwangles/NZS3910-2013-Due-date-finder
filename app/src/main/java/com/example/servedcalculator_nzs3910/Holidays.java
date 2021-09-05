package com.example.servedcalculator_nzs3910;

import java.util.List;

public class Holidays {

    private String kind;
    private String etag;
    private String summary;
    private String updated;
    private String timeZone;
    private String accessRole;
    private List<String> defaultReminders;
    private String nextSyncToken;
    private List<Item> items;

    public Holidays(String kind, String etag, String summary, String updated, String timeZone, String accessRole, List<String> defaultReminders, String nextSyncToken, List<Item> items){
        this.kind = kind;
        this.etag = etag;
        this.summary = summary;
        this.updated = updated;
        this.timeZone = timeZone;
        this.accessRole = accessRole;
        this.defaultReminders = defaultReminders;
        this.nextSyncToken = nextSyncToken;
        this.items = items;
    }

    public String getSummary() {
        return summary;
    }


    public String getTimeZone() {
        return timeZone;
    }

    public List<Item> getItems() {
        return items;
    }

    /*
   "kind": "calendar#events",
           "etag": "\"p33mbv5kuujif40g\"",
           "summary": "Holidays in New Zealand",
           "updated": "2021-09-03T01:14:06.000Z",
           "timeZone": "UTC",
           "accessRole": "reader",
           "defaultReminders": [],
           "nextSyncToken": "CIDnk6DQ4fICEAAYASCQw-u6AQ==",
           "items": [

     */

}
