package com.skwangles.servedcalculator_nzs3910;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class PublicHolidays {
    private LocalDate start;
    private String name;
    //private final String type;

    public PublicHolidays(Item item) {
        DateTimeFormatter parser = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.ENGLISH);
        this.name = "★ " + item.getSummary();
        //this.type = item.getDescription();
        this.start = LocalDate.parse(item.getStart().getDate(), parser);
        this.start = longWeekendCase();
    }

    public PublicHolidays(LocalDate otherEvent, int type) {
        if (type == 0) {
            if (otherEvent.getDayOfWeek().getValue() == 7) {
                this.name = "Sunday";
            } else if (otherEvent.getDayOfWeek().getValue() == 6) {
                this.name = "Saturday";
            }

        } else if (type == 1) {
            this.name = "⛨ Christmas Break";

        } else {
            this.name = "Unspecified";

        }
        this.start = otherEvent;
    }

    public LocalDate longWeekendCase() {
        if (start.getDayOfWeek().getValue() == 6) {
            name += " - Long Weekend";
            return this.start.plusDays(2);
        } else if (start.getDayOfWeek().getValue() == 7) {
            name += " - Long Weekend";
            return this.start.plusDays(1);
        }
        return this.start;

    }

    public LocalDate getStart() {
        return start;
    }

    public String getName() {
        return name;
    }

}
