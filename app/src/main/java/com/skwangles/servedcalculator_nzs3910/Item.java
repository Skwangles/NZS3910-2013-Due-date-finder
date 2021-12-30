package com.skwangles.servedcalculator_nzs3910;


import androidx.annotation.NonNull;

public class Item {


    private String summary;

    private String description;

    private Start start;


   public Item( String summary, String description, Start start){
       this.summary = summary;
       this.description = description;
       this.start = start;
   }
    @Override
    public boolean equals(Object anObject){
        if (anObject instanceof String) {
            String tempString = (String) anObject;
            int n = getDescription().toCharArray().length;
            if (n == tempString.toCharArray().length) {
                char[] v1 = getDescription().toCharArray();
                char[] v2 = tempString.toCharArray();
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

    @NonNull
    @Override
    public String toString(){
       return description + " " + start.toString();
    }

    public String getSummary() {
        return summary;
    }

    public String getDescription() {
        return description;
    }

    public Start getStart() {
        return start;
    }

}

class Start {
    private String date;

    public String getDate() {
        return date;
    }

    public Start(String date){
        this.date = date;
    }

    @NonNull
    @Override
    public String toString(){
        return date;
    }
}

