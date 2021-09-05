package com.example.servedcalculator_nzs3910;

public class PublicHolidays {
    private String start;
    private String name;
    private String type;

    public PublicHolidays(Item item){
        this.start = item.getStart().getDate();
        this.name = item.getSummary();
        this.type = item.getDescription();
    }

}
