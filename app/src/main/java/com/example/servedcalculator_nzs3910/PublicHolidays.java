package com.example.servedcalculator_nzs3910;

import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Locale;

public class PublicHolidays {
    private LocalDate start;
    private String name;
    private String type;

    public PublicHolidays(Item item){
        DateTimeFormatter parser = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.ENGLISH);
        this.name = item.getSummary();
        this.type = item.getDescription();
        this.start = LocalDate.parse(item.getStart().getDate(), parser);
    }

    public PublicHolidays(LocalDate otherEvent, int type){
        DateTimeFormatter parser = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.ENGLISH);
        if (type == 0) {
            if (otherEvent.getDayOfWeek().getValue() == 7) {
                this.name = "⍟ Sunday";
            } else if (otherEvent.getDayOfWeek().getValue() == 6) {
                this.name = "★ Saturday";
            }
            this.type = "Weekend";

        }
        else if (type == 1) {
            this.name = "Christmas Break";
            this.type = "Christmas Break";

        }
        else{
            this.name = "Unspecified";
            this.type = "Unspecified";
        }
        this.start = otherEvent;
    }

    public LocalDate getStart() {
        return start;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }
}
