package com.example.servedcalculator_nzs3910;

import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Date;

public class MainActivity extends AppCompatActivity {
    public Date serve_date;
    private java.util.Calendar utilCalendar = java.util.Calendar.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Setup of Spinners-------
        Spinner day_view = findViewById(R.id.day_served_spinner);
        Spinner month_view = findViewById(R.id.month_served_spinner);
        Spinner year_view = findViewById(R.id.year_served_spinner);
        String[] day_items = new String[]{"1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23", "24", "25", "26", "27", "28", "29", "30", "31"};
        String[] month_items = new String[]{"1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12"};
        int year = utilCalendar.get(java.util.Calendar.YEAR);
        int day = utilCalendar.get(java.util.Calendar.DAY_OF_MONTH);
        int month = utilCalendar.get(java.util.Calendar.MONTH);
        String[] years_items = new String[3];
        for (int i = 0; i < 3; i++) {
            years_items[i] = String.valueOf(year - 1 + i);

            Log.d("Tag", years_items[i].toString());
        }

        // Sets the list of items in the spinners
        ArrayAdapter<String> day_adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, day_items);
        day_view.setAdapter(day_adapter);
        ArrayAdapter<String> month_adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, month_items);
        month_view.setAdapter(month_adapter);
        ArrayAdapter<String> year_adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, years_items);
        year_view.setAdapter(year_adapter);
        day_view.setSelection(day - 1);
        month_view.setSelection(month - 1);
        year_view.setSelection(1);
        // -- End of setup --------
    }



    protected void get_holidays()
    {
        // Here - Try process
        // en.new_zealand#holiday@group.v.calendar.google.com')

        
        
        

        String api_location = "https://www.googleapis.com/calendar/v3/calendars/en.new_zealand%23holiday%40group.v.calendar.google.com/events?key=AIzaSyD2Xy5SVR22tomUkKkxKEGMIboLbAO0ATE";
    }

}