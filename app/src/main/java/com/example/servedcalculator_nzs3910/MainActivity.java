package com.example.servedcalculator_nzs3910;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.switchmaterial.SwitchMaterial;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    public static final String SHARED_PREFS = "NZS3910-Region";
    public static final String USERREGION = "WhereAreYou";
    public static final String SWITCHSTATE = "Layout";
    final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd MMMM uuuu", Locale.ENGLISH);
    AlertDialog.Builder dialogBuilder;
    AlertDialog dialog;
    Button buttonExitPopup;
    Spinner spinnerOfDay;
    Spinner spinnerOfMonth;
    Spinner spinnerOfYear;
    Button buttonProcessDates;
    Button buttonChangeMyRegion;
    Button buttonShowNonWorkingDays;
    String regionOfHolidays;
    List<Item> publicHolidaysItemFromGoogle;
    SwitchMaterial switchBetweenResultLayout;
    ListView popupListView;
    private ArrayList<PublicHolidays> appliedPublicHolidays = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (loadRegionDate().equals("")) {
            startSetupActivity();
        }
        setContentView(R.layout.activity_main);
        regionOfHolidays = loadRegionDate();
        Toast.makeText(this, "Region: " + regionOfHolidays, Toast.LENGTH_SHORT).show();

        spinnerOfDay = findViewById(R.id.spinner_dayserved);
        spinnerOfMonth = findViewById(R.id.spinner_monthserved);
        spinnerOfYear = findViewById(R.id.spinner_yearserved);
        buttonProcessDates = findViewById(R.id.button_processDates);
        buttonChangeMyRegion = findViewById(R.id.button_changeregion);
        switchBetweenResultLayout = findViewById(R.id.switch_dateLayout);
        buttonShowNonWorkingDays = findViewById(R.id.button_showNonWorkingDays);

        // Setup of Spinners-------
        spinnerSetup();

        fetchHolidayListFromInternet(); //Gets the list of public holidays in NZ

        buttonSetup(); //Sets up event listeners and spinner adapters

    }


    @Override
    public void onClick(View view) {
        if (view != null) {
            int id = view.getId();
            if (id == R.id.button_processDates) {
                Log.d("SuccessLog", "Processing Button Clicked");
                fetchHolidayListFromInternet();
                LocalDate serveDate = LocalDate.of(Integer.parseInt(spinnerOfYear.getSelectedItem().toString()), Integer.parseInt(spinnerOfMonth.getSelectedItem().toString()), Integer.parseInt(spinnerOfDay.getSelectedItem().toString()));
                String[] deadlines = countDates(serveDate);
                displayDeadlines(deadlines);
            }
            if (id == R.id.button_changeregion) {
                startSetupActivity();
            }
            if (id == R.id.button_showNonWorkingDays) {
                makeAndDisplayPopup();
            }
        }
    }

    private void buttonSetup(){
        buttonProcessDates.setOnClickListener(this);
        buttonChangeMyRegion.setOnClickListener(this);
        buttonShowNonWorkingDays.setOnClickListener(this);
        switchBetweenResultLayout.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                //Switches to display the layout
                saveSwitchStatus();
                if (switchBetweenResultLayout.isChecked()) {
                    LinearLayout option1 = findViewById(R.id.linearlayout_three_two);
                    option1.setVisibility(View.VISIBLE);
                } else {
                    LinearLayout option1 = findViewById(R.id.linearlayout_three_two);
                    option1.setVisibility(View.GONE);
                }
            }
        });
    }
    private void spinnerSetup() {
        String[] day_items = new String[]{"1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23", "24", "25", "26", "27", "28", "29", "30", "31"};
        String[] month_items = new String[]{"1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12"};
        java.util.Calendar setupCalendar = java.util.Calendar.getInstance();
        java.util.Calendar today = java.util.Calendar.getInstance();
        int year = setupCalendar.get(today.YEAR);
        int day = setupCalendar.get(today.DAY_OF_MONTH);
        int month = setupCalendar.get(today.MONTH);
        String[] years_items = new String[3];
        for (int i = 0; i < 3; i++) {
            years_items[i] = String.valueOf(year - 1 + i);
        }//Gets the three surrounding years

        //Sets up the layout of the result
        switchBetweenResultLayout.setChecked(loadSwitchStatus());
        saveSwitchStatus();
        if (switchBetweenResultLayout.isChecked()) {
            LinearLayout option1 = findViewById(R.id.linearlayout_three_two);
            option1.setVisibility(View.VISIBLE);

        } else {
            LinearLayout option1 = findViewById(R.id.linearlayout_three_two);
            option1.setVisibility(View.GONE);
        }
        // Sets the list of items in the spinners
        ArrayAdapter<String> day_adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, day_items);
        spinnerOfDay.setAdapter(day_adapter);
        ArrayAdapter<String> month_adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, month_items);
        spinnerOfMonth.setAdapter(month_adapter);
        ArrayAdapter<String> year_adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, years_items);
        spinnerOfYear.setAdapter(year_adapter);
        Log.d("SuccessLog", day + " " + month);
        spinnerOfDay.setSelection(day-1);
        spinnerOfMonth.setSelection(month);
        spinnerOfYear.setSelection(1);
        // -- End of setup --------
    }

    private void displayDeadlines(String[] deadlines) {
        TextView seven = findViewById(R.id.result_seven_days_1);
        TextView three = findViewById(R.id.result_seven_three_days);
        TextView two = findViewById(R.id.result_seven_three_two_days);
        TextView finalResult = findViewById(R.id.result_seventeen);
        seven.setText(deadlines[0]);
        three.setText(deadlines[1]);
        two.setText(deadlines[2]);
        finalResult.setText(deadlines[3]);
    }

    private void startSetupActivity() {
        Intent myIntent = new Intent(MainActivity.this, RegionSelect.class);
        MainActivity.this.startActivity(myIntent);
    }

    private String[] countDates(LocalDate workingDate) {
        appliedPublicHolidays = new ArrayList<>();
        int workingDays = 17 + 7;
        String[] WorkingDates = new String[4];
        while (workingDays > 0) {
            if (!isPublicOrWeekend(workingDate)) {
                if (workingDays == 17) {
                    WorkingDates[0] = dtf.format(workingDate); // 7th day
                } else if (workingDays == 14) {
                    WorkingDates[1] = dtf.format(workingDate); // 7 + 3rd day
                } else if (workingDays == 12) {
                    WorkingDates[2] = dtf.format(workingDate); // 7 + 3 + 2nd day
                } else if (workingDays == 1) {
                    WorkingDates[3] = dtf.format(workingDate); // Final Day (7 + 17th day)
                }
                workingDays--; //Only decrements when Workdays are considered
            }
            workingDate = workingDate.plusDays(1); //Moves forward a day
        }
        return WorkingDates;  //Returns list of each important date
    }

    private boolean isPublicOrWeekend(LocalDate date) {// Determines if date qualifies as a Working Date - as Per NZS3910 1.2 (Working Day)
        if (date.getDayOfWeek().getValue() == 6 || date.getDayOfWeek().getValue() == 7) {
            appliedPublicHolidays.add(new PublicHolidays(date, 0));
            return true;
        }
        List<PublicHolidays> publicHolidaysList = getListOfPublicHolidays();
        if (date.getMonth().getValue() == 12 && date.getDayOfMonth() >= 24){ //Checks for Christmas Dates
            appliedPublicHolidays.add(new PublicHolidays(date, 1));
            return true;
        }
        else if (date.getMonth().getValue() == 1 && date.getDayOfMonth() <= 5){//Checks for January 5 Dates
            appliedPublicHolidays.add(new PublicHolidays(date, 1));
            return true;
        }
        for (PublicHolidays holiday : publicHolidaysList) {//Checks through all applicable Public holidays if it occurs on the same date.
            if (date == holiday.getStart()) { //Is the date part of a holiday
                appliedPublicHolidays.add(holiday);
                return true;
            }
        }
        return false;
    }

    protected List<PublicHolidays> getListOfPublicHolidays() {
        List<PublicHolidays> publicHolidays = new ArrayList<PublicHolidays>();
        for (int i = 0; i < publicHolidaysItemFromGoogle.size(); i++) {
            if (publicHolidaysItemFromGoogle.get(i).getDescription().equals("Public holiday")) {
                Log.d("MyErrors", publicHolidaysItemFromGoogle.get(i).toString());
                publicHolidays.add(new PublicHolidays(publicHolidaysItemFromGoogle.get(i)));//gets Nation holidays
            }
            if (!loadRegionDate().equals("") && publicHolidaysItemFromGoogle.get(i).getDescription().contains("Public holiday") && publicHolidaysItemFromGoogle.get(i).getDescription().contains(regionOfHolidays)) { //gets Regional holidays
                publicHolidays.add(new PublicHolidays(publicHolidaysItemFromGoogle.get(i)));
            }
        }
        return publicHolidays;
    }

    //Gets the list of public holidays from Google
    protected void fetchHolidayListFromInternet() {

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
                if (!response.isSuccessful()) {
                    Log.d("Oops-Succeed", String.valueOf(response.code()));
                    return;
                }
                Holidays holiday = response.body();
                Log.d("HolidayPrintout", holiday.getItems().toString());
                publicHolidaysItemFromGoogle = holiday.getItems();
            }

            @Override
            public void onFailure(Call<Holidays> call, Throwable throwable) {
                Log.d("Oops-Failure", throwable.getMessage());
                publicHolidaysItemFromGoogle = null;
            }
        });
        //String api_location = "https://www.googleapis.com/calendar/v3/calendars/en.new_zealand%23holiday%40group.v.calendar.google.com/events?key=AIzaSyD2Xy5SVR22tomUkKkxKEGMIboLbAO0ATE";
    }

    private String loadRegionDate() {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        return sharedPreferences.getString(USERREGION, "");
    }

    private boolean loadSwitchStatus() {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        return sharedPreferences.getBoolean(SWITCHSTATE, false);
    }

    private void saveSwitchStatus() {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(SWITCHSTATE, switchBetweenResultLayout.isChecked());
        editor.apply();
    }

    private void makeAndDisplayPopup() {
        dialogBuilder = new AlertDialog.Builder(this);

        //Formats the dialog
        final View popupView = getLayoutInflater().inflate(R.layout.popup, null);
        buttonExitPopup = popupView.findViewById(R.id.popup_exit);
        popupListView = popupView.findViewById(R.id.popup_listview);
        //Displays the Dialog
        dialogBuilder.setView(popupView);
        dialog = dialogBuilder.create();
        dialog.show();
        AppliedHolidaysAdapter appliedHolidaysAdapter = new AppliedHolidaysAdapter(this, appliedPublicHolidays);
        popupListView.setAdapter(appliedHolidaysAdapter);

        buttonExitPopup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
    }

    private class AppliedHolidaysAdapter extends BaseAdapter {
        private final Context context;
        private final ArrayList<PublicHolidays> appliedPublicHolidays;

        public AppliedHolidaysAdapter(Context context, ArrayList<PublicHolidays> appliedPublicHolidays) {
            this.context = context;
            this.appliedPublicHolidays = appliedPublicHolidays;
        }

        @Override
        public int getCount() {
            return appliedPublicHolidays.size();
        }

        @Override
        public Object getItem(int i) {
            return appliedPublicHolidays.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int position, View view, ViewGroup viewGroup) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View row = inflater.inflate(R.layout.main_row, viewGroup, false);

            TextView largeText = (TextView) row.findViewById(R.id.larger_item);
            TextView smallText = (TextView) row.findViewById(R.id.smaller_item);

            largeText.setText(appliedPublicHolidays.get(position).getName());
            smallText.setText("  " + dtf.format(appliedPublicHolidays.get(position).getStart()));


            return row;
        }
    }

}