package com.example.servedcalculator_nzs3910;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
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
    public static final String INDEXDATES = "selectedSpinnerIndex";
    final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd MMMM uuuu", Locale.ENGLISH); //Layout of Dates
    ArrayList<PublicHolidays> impactingPublicHolidays = new ArrayList<>();
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
    SwitchMaterial switchBetweenResultLayout;
    ListView popupListView;
    List<Item> publicHolidaysItemFromGoogle;
    int selectedDayIndex = 99;
    int selectedMonthIndex = 99;
    int selectedYearIndex = 99;
    int[] intArr = new int[]{99, 99, 99};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (loadRegionDate().equals("")) {
            startSetupActivity();
        }

        Intent intent = getIntent();
        if (intent.getExtras() != null) {
            intArr = intent.getIntArrayExtra(INDEXDATES);
            selectedDayIndex = intArr[0];
            selectedMonthIndex = intArr[1];
            selectedYearIndex = intArr[2];
        }


        setContentView(R.layout.activity_main);
        regionOfHolidays = loadRegionDate();
        Toast.makeText(this, "Region: " + regionOfHolidays, Toast.LENGTH_SHORT).show();


        //Defines each of the objects
        spinnerOfDay = findViewById(R.id.spinner_dayserved);
        spinnerOfMonth = findViewById(R.id.spinner_monthserved);
        spinnerOfYear = findViewById(R.id.spinner_yearserved);
        buttonProcessDates = findViewById(R.id.button_processDates);
        buttonChangeMyRegion = findViewById(R.id.button_changeregion);
        switchBetweenResultLayout = findViewById(R.id.switch_dateLayout);
        buttonShowNonWorkingDays = findViewById(R.id.button_showNonWorkingDays);

        // Setup of Spinners-------
        spinnerSetup();

        buttonSetup(); //Sets up event listeners and spinner adapters
        Toast.makeText(this, String.valueOf(selectedDayIndex), Toast.LENGTH_SHORT).show();
    }


    @Override
    public void onClick(View view) {
        if (view != null) {
            int id = view.getId();
            if (id == R.id.button_processDates) {
                fetchHolidayListFromInternet();//Will fetch and wait asynchronously
            }
            if (id == R.id.button_changeregion) {
                startSetupActivity();
            }
            if (id == R.id.button_showNonWorkingDays) {
                makeAndDisplayPopup();
            }
        }
    }

    private void buttonSetup() {
        buttonProcessDates.setOnClickListener(this);
        buttonChangeMyRegion.setOnClickListener(this);
        buttonShowNonWorkingDays.setOnClickListener(this);
        switchBetweenResultLayout.setOnCheckedChangeListener((compoundButton, b) -> {
            //Switches to display the layout
            saveSwitchStatus();
            if (switchBetweenResultLayout.isChecked()) {
                LinearLayout option1 = findViewById(R.id.linearlayout_three_two);
                option1.setVisibility(View.VISIBLE);
            } else {
                LinearLayout option1 = findViewById(R.id.linearlayout_three_two);
                option1.setVisibility(View.GONE);
            }
        });
    }

    private void spinnerSetup() {
        String[] day_items = new String[]{"1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23", "24", "25", "26", "27", "28", "29", "30", "31"};
        String[] month_items = new String[]{"1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12"};
        java.util.Calendar setupCalendar = java.util.Calendar.getInstance();
        int year = setupCalendar.get(Calendar.YEAR);
        int day = setupCalendar.get(Calendar.DAY_OF_MONTH);
        int month = setupCalendar.get(Calendar.MONTH);
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
            option1.setVisibility(View.GONE);//Hides the extra unneeded info
        }

        // Sets the list of items in the spinners
        ArrayAdapter<String> day_adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, day_items);
        spinnerOfDay.setAdapter(day_adapter);
        ArrayAdapter<String> month_adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, month_items);
        spinnerOfMonth.setAdapter(month_adapter);
        ArrayAdapter<String> year_adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, years_items);
        spinnerOfYear.setAdapter(year_adapter);
        Log.d("SuccessLog", day + " " + month);
        //The following checks changes in the spinner selection
        spinnerOfDay.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                selectedDayIndex = spinnerOfDay.getSelectedItemPosition();
                Log.d("Index check", String.valueOf(selectedDayIndex));
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                 }
        });
        spinnerOfMonth.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                selectedMonthIndex = spinnerOfMonth.getSelectedItemPosition();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        spinnerOfYear.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                selectedYearIndex = spinnerOfYear.getSelectedItemPosition();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        setSpinnerSelection(day, month);

        // -- End of setup --------
    }

    private void setSpinnerSelection(int day, int month) {
        Log.d("Index check 2", String.valueOf(spinnerOfDay.getCount()));
        if (selectedDayIndex < spinnerOfDay.getCount()){
            spinnerOfDay.setSelection(selectedDayIndex);}
        else {
            spinnerOfDay.setSelection(day - 1);
        }
        if (selectedMonthIndex < spinnerOfMonth.getCount()){
            spinnerOfMonth.setSelection(selectedMonthIndex);}
        else {
            spinnerOfMonth.setSelection(month);
        }
        if (selectedYearIndex < spinnerOfYear.getCount()) {
            spinnerOfYear.setSelection(selectedYearIndex);
        }
        else {
            spinnerOfYear.setSelection(1);
        }

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
        myIntent.putExtra(INDEXDATES, new int[]{spinnerOfDay.getSelectedItemPosition(), spinnerOfMonth.getSelectedItemPosition(), spinnerOfYear.getSelectedItemPosition()});
        MainActivity.this.startActivity(myIntent);
    }

    private String[] countDates(LocalDate workingDate) {
        List<PublicHolidays> publicHolidaysList = fromItemListGetPublicHolidays(publicHolidaysItemFromGoogle);
        impactingPublicHolidays = new ArrayList<>(); //Resets the numbers to be empty for the isPublicOrWeekend
        int workingDays = 17 + 7; //Fixed term length
        String[] WorkingDates = new String[4]; // 4 important dates
        while (workingDays > 0) {
            if (!isPublicOrWeekend(workingDate, publicHolidaysList, impactingPublicHolidays)) {
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

    private boolean isPublicOrWeekend(LocalDate date, List<PublicHolidays> publicHolidaysList, ArrayList<PublicHolidays> impactingPublicHolidays) {// Determines if date qualifies as a Working Date - as Per NZS3910 1.2 (Working Day)
        if (date.getMonth().getValue() == 12 && date.getDayOfMonth() >= 24) { //Checks for Christmas Dates
            impactingPublicHolidays.add(new PublicHolidays(date, 1));
            return true;
        } else if (date.getMonth().getValue() == 1 && date.getDayOfMonth() <= 5) {//Checks for January 5 Dates
            impactingPublicHolidays.add(new PublicHolidays(date, 1));
            return true;
        } else {
            if (date.getDayOfWeek().getValue() >= 6) {//Value is either Saturday or Sundar
                impactingPublicHolidays.add(new PublicHolidays(date, 0));
                return true;
            }
            for (PublicHolidays holiday : publicHolidaysList) {//Checks through all applicable Public holidays if it occurs on the same date.
                if (date.equals(holiday.getStart())) { //Is the date part of a holiday
                    impactingPublicHolidays.add(holiday);
                    return true;
                }
            }
            return false;
        }
    }

    protected List<PublicHolidays> fromItemListGetPublicHolidays(List<Item> publicHolidaysItemFromGoogle) {
        List<PublicHolidays> publicHolidays = new ArrayList<>();
        for (int i = 0; i < publicHolidaysItemFromGoogle.size(); i++) {
            Item currentItem = publicHolidaysItemFromGoogle.get(i);
            if (currentItem.getDescription().equals("Public holiday")) {
                publicHolidays.add(new PublicHolidays(currentItem));//gets Nation holidays
            } else if (!loadRegionDate().equals("") && currentItem.getDescription().contains("Public holiday") && publicHolidaysItemFromGoogle.get(i).getDescription().contains(regionOfHolidays)) { //gets Regional holidays
                publicHolidays.add(new PublicHolidays(currentItem));//adds
            }
        }
        return publicHolidays;
    }

    //Gets the list of public holidays from Google


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
        AppliedHolidaysAdapter appliedHolidaysAdapter = new AppliedHolidaysAdapter(this, impactingPublicHolidays);
        popupListView.setAdapter(appliedHolidaysAdapter);

        buttonExitPopup.setOnClickListener(view -> dialog.dismiss());
    }

    protected void fetchHolidayListFromInternet() {

        String BaseURL = "https://www.googleapis.com/calendar/v3/calendars/en.new_zealand%23holiday%40group.v.calendar.google.com/";
        // en.new_zealand#holiday@group.v.calendar.google.com')
        if (publicHolidaysItemFromGoogle == null) {
            Context context = this;
            final Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(BaseURL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
            JsonPlaceHolderApi jsonPlaceHolderApi = retrofit.create(JsonPlaceHolderApi.class);
            Call<Holidays> call = jsonPlaceHolderApi.getHolidays();
            Log.d("Oops-Succeed", "Running GET");
            call.enqueue(new Callback<Holidays>() {
                @Override
                public void onResponse(Call<Holidays> call, Response<Holidays> response) {
                    if (!response.isSuccessful()) {
                        Log.d("Oops-Succeed", String.valueOf(response.code()));
                        return;
                    }
                    Holidays holiday = response.body();
                    if (holiday == null) {
                        publicHolidaysItemFromGoogle = null;
                        Log.d("Oops-Succeed", "Response empty");
                        Toast.makeText(context, "Empty item - Must calculate w/o public holidays", Toast.LENGTH_SHORT).show();
                    } else {
                        Log.d("Oops-Succeed", "Success!!");
                        publicHolidaysItemFromGoogle = holiday.getItems();
                    }
                    runCalculations();

                }

                @Override
                public void onFailure(Call<Holidays> call, Throwable throwable) {
                    Log.d("Oops-Failure", throwable.getMessage());
                    publicHolidaysItemFromGoogle = null;
                    Toast.makeText(context, "Failed connection - Must calculate w/o public holidays", Toast.LENGTH_SHORT).show();
                    runCalculations();

                }
            });
        } else {
            runCalculations();
        }
        //String api_location = "https://www.googleapis.com/calendar/v3/calendars/en.new_zealand%23holiday%40group.v.calendar.google.com/events?key=AIzaSyD2Xy5SVR22tomUkKkxKEGMIboLbAO0ATE";
    }

    private void runCalculations() {
        try {
            LocalDate serveDate = LocalDate.of(Integer.parseInt(spinnerOfYear.getSelectedItem().toString()), Integer.parseInt(spinnerOfMonth.getSelectedItem().toString()), Integer.parseInt(spinnerOfDay.getSelectedItem().toString()));
            String[] deadlines = countDates(serveDate);
            displayDeadlines(deadlines);
        } catch (Exception ex) {
            Toast.makeText(this, "Invalid Serving Date", Toast.LENGTH_LONG).show();
        }
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

            TextView largeText = row.findViewById(R.id.larger_item);
            TextView smallText = row.findViewById(R.id.smaller_item);

            largeText.setText(appliedPublicHolidays.get(position).getName());
            smallText.setText("  " + dtf.format(appliedPublicHolidays.get(position).getStart()));


            return row;
        }
    }

}