package com.example.servedcalculator_nzs3910;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.switchmaterial.SwitchMaterial;

import retrofit2.*;

import java.util.ArrayList;

import java.util.List;


import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    String region = null;
    List<Item> public_holidays_items;
    private java.util.Calendar utilCalendar = java.util.Calendar.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Startup setup check - Bit dodgy
        Intent intent = getIntent();
        String region = intent.getStringExtra("Region"); //if it's a string you stored.
        setContentView(R.layout.activity_main);
        if (region == null){
            startSetupActivity();
        }
        // Setup of Spinners-------
        Spinner day_view = findViewById(R.id.day_served_spinner);
        Spinner month_view = findViewById(R.id.month_served_spinner);
        Spinner year_view = findViewById(R.id.year_served_spinner);
        Button process_button = findViewById(R.id.process_button);
        Button change_region = findViewById(R.id.change_region_button);
        //region Spinner List setups
        // SwitchMaterial alternative_path_switch = findViewById(R.id.switch_alternative_path);
        String[] day_items = new String[]{"1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23", "24", "25", "26", "27", "28", "29", "30", "31"};
        String[] month_items = new String[]{"1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12"};
        int year = utilCalendar.get(java.util.Calendar.YEAR);
        int day = utilCalendar.get(java.util.Calendar.DAY_OF_MONTH);
        int month = utilCalendar.get(java.util.Calendar.MONTH);
        String[] years_items = new String[3];
        for (int i = 0; i < 3; i++) {
            years_items[i] = String.valueOf(year - 1 + i);
        }//Gets the three surrounding years

        // Sets the list of items in the spinners
        ArrayAdapter<String> day_adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, day_items);
        day_view.setAdapter(day_adapter);
        ArrayAdapter<String> month_adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, month_items);
        month_view.setAdapter(month_adapter);
        ArrayAdapter<String> year_adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, years_items);
        year_view.setAdapter(year_adapter);
        day_view.setSelection(day - 1);
        month_view.setSelection(month - 1);
        year_view.setSelection(1);
        // -- End of setup --------
        //endregion
        getHolidaysFromGCalendar(); //Gets the list of public holidays in NZ
        process_button.setOnClickListener(this);
        change_region.setOnClickListener(this);

    }


    @Override
    public void onClick(View view){
        if(view != null){
            int id = view.getId();
            if(id == R.id.process_button){
                Log.d("SuccessLog", "Processing");
            }
            if(id == R.id.change_region_button){
                Log.d("SuccessLog", "Changing Region");
                startSetupActivity();
            }

        }
    }

    private void startSetupActivity(){
        Intent myIntent = new Intent(MainActivity.this, RegionSelect.class);
        myIntent.putExtra("Region", region); //Optional parameters
        MainActivity.this.startActivity(myIntent);
    }


    protected List<PublicHolidays> getListOfPublicHolidays(){
        List<PublicHolidays> publicHolidays = new ArrayList<PublicHolidays>();
        for (int i = 0; i < public_holidays_items.size(); i++){
            if (public_holidays_items.get(i).getDescription().equals("Public Holiday")) {
                publicHolidays.add(new PublicHolidays(public_holidays_items.get(i)));
            }
            else if (public_holidays_items.get(i).getDescription().contains("Public Holiday") && public_holidays_items.get(i).getDescription().contains(region)){
                publicHolidays.add(new PublicHolidays(public_holidays_items.get(i)));
            }
        }
        return publicHolidays;
    }
    
    protected void getHolidaysFromGCalendar()
    {
        // Here - Try process
        // Base URL https://www.googleapis.com/calendar/v3/calendars/en.new_zealand%23holiday%40group.v.calendar.google.com/
        // en.new_zealand#holiday@group.v.calendar.google.com')

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://www.googleapis.com/calendar/v3/calendars/en.new_zealand%23holiday%40group.v.calendar.google.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        JsonPlaceHolderApi jsonPlaceHolderApi = retrofit.create(JsonPlaceHolderApi.class);
        Call<Holidays> call = jsonPlaceHolderApi.getHolidays();
        call.enqueue(new Callback<Holidays>() {
            @Override
            public void onResponse(Call<Holidays> call, Response<Holidays> response) {
                if(!response.isSuccessful()){
                    Log.d("Oops-Succeed", String.valueOf(response.code()));
                    return;
                }
                Holidays holiday = response.body();
                Log.d("HolidayPrintout", holiday.getItems().toString());
                public_holidays_items = holiday.getItems();
            }

            @Override
            public void onFailure(Call<Holidays> call, Throwable throwable) {
                    Log.d("Oops-Failure", throwable.getMessage());
                    public_holidays_items = null;
            }
        });
        return;
        //String api_location = "https://www.googleapis.com/calendar/v3/calendars/en.new_zealand%23holiday%40group.v.calendar.google.com/events?key=AIzaSyD2Xy5SVR22tomUkKkxKEGMIboLbAO0ATE";
    }

}