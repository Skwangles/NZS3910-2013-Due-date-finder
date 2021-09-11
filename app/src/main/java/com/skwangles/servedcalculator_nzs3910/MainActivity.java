package com.skwangles.servedcalculator_nzs3910;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
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
import android.widget.ProgressBar;
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

    //Shared Pref keys
    public static final String SHARED_PREFS = "NZS3910-Region";
    public static final String USER_REGION = "WhereAreYou";
    public static final String SWITCH_STATE = "Layout";
    public static final String SPINNER_INDEX = "selectedSpinnerIndex";

    final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd MMMM uuuu", Locale.ENGLISH); //Layout of Dates

    //Popup Makers
    AlertDialog.Builder excludedDatesDialogBuilder;
    AlertDialog.Builder infoDialogBuilder;
    AlertDialog excludedDatesDialog;
    AlertDialog informationDialog;

    //Important Info
    ArrayList<PublicHolidays> excludedDatesArrayList = new ArrayList<>();
    ListView popupListView;
    List<Item> publicHolidaysItemFromGoogle;

    //Activity Views
    Button buttonExitExcludedDatesPopup;
    Button buttonExitInfoPopup;
    Button buttonShowInformation;
    Spinner spinnerOfDay;
    Spinner spinnerOfMonth;
    Spinner spinnerOfYear;
    Button buttonProcessDates;
    Button buttonChangeMyRegion;
    Button buttonShowNonWorkingDays;
    String regionOfHolidays;
    SwitchMaterial switchBetweenResultLayout;
    ProgressBar loadingCircle;


    // Spinner Selections
    int[] selectedSpinnerIndexes = new int[]{99, 99, 99};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (loadRegionDateFromSharedPref().equals("")) {
            startRegionSelectActivity();
        }

        setContentView(R.layout.activity_main);

        //Defines each of the objects
        spinnerOfDay = findViewById(R.id.spinner_dayserved);
        spinnerOfMonth = findViewById(R.id.spinner_monthserved);
        spinnerOfYear = findViewById(R.id.spinner_yearserved);
        buttonProcessDates = findViewById(R.id.button_processDates);
        buttonChangeMyRegion = findViewById(R.id.button_changeregion);
        switchBetweenResultLayout = findViewById(R.id.switch_dateLayout);
        buttonShowNonWorkingDays = findViewById(R.id.button_showNonWorkingDays);
        buttonShowInformation = findViewById(R.id.button_definitions);
        loadingCircle = (ProgressBar) findViewById(R.id.progressBar1);

        //Setup loading circle
        loadingCircle.setVisibility(View.GONE);
        //Get additional Information
        Intent intent = getIntent();
        if (intent.getExtras() != null) {
            selectedSpinnerIndexes = intent.getIntArrayExtra(SPINNER_INDEX);
        }

        //Gets and notifies of globally stored Region of User.
        regionOfHolidays = loadRegionDateFromSharedPref();
        Toast.makeText(this, "Region: " + regionOfHolidays, Toast.LENGTH_SHORT).show();

        // Setup of Spinners-------
        spinnerAdapterAndStateSetup();


        buttonAndSwitchEventsSetup(); //Sets up event listeners and spinner adapters

        //setup finished
    }

    @Override
    public void onClick(View view) {
        if (view != null) {
            int id = view.getId();
            if (id == R.id.button_processDates) {
                loadingCircle.setVisibility(View.VISIBLE);
                fetchHolidayListFromGoogle();//Will fetch and wait asynchronously
            }
            if (id == R.id.button_changeregion) {
                startRegionSelectActivity();
            }
            if (id == R.id.button_showNonWorkingDays) {
                makeAndDisplayExcludedDatesPopup();
            }
            if (id == R.id.button_definitions) {
                makeAndDisplayInformationPopup();
            }
        }
    }

    private void buttonAndSwitchEventsSetup() {
        buttonProcessDates.setOnClickListener(this);
        buttonChangeMyRegion.setOnClickListener(this);
        buttonShowNonWorkingDays.setOnClickListener(this);
        buttonShowInformation.setOnClickListener(this);
        switchBetweenResultLayout.setOnCheckedChangeListener((compoundButton, b) -> {
            //Switches to display the layout
            saveSwitchStatusToSharedPref();
            if (switchBetweenResultLayout.isChecked()) {
                LinearLayout option1 = findViewById(R.id.linearlayout_three_two);
                option1.setVisibility(View.VISIBLE);
            } else {
                LinearLayout option1 = findViewById(R.id.linearlayout_three_two);
                option1.setVisibility(View.GONE);
            }
        });
    }

    private void spinnerAdapterAndStateSetup() {
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
        switchBetweenResultLayout.setChecked(loadSwitchStatusFromSharedPref());
        saveSwitchStatusToSharedPref();
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
        //The following checks changes in the spinner selection
        spinnerOfDay.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                selectedSpinnerIndexes[0] = spinnerOfDay.getSelectedItemPosition();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });
        spinnerOfMonth.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                selectedSpinnerIndexes[1] = spinnerOfMonth.getSelectedItemPosition();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        spinnerOfYear.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                selectedSpinnerIndexes[2] = spinnerOfYear.getSelectedItemPosition();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        setSpinnerSelections(day, month);

        // -- End of setup --------
    }

    private void setSpinnerSelections(int day, int month) {
        if (selectedSpinnerIndexes[0] < spinnerOfDay.getCount()) {
            spinnerOfDay.setSelection(selectedSpinnerIndexes[0]);
        } else {
            spinnerOfDay.setSelection(day - 1);
        }
        if (selectedSpinnerIndexes[1] < spinnerOfMonth.getCount()) {
            spinnerOfMonth.setSelection(selectedSpinnerIndexes[1]);
        } else {
            spinnerOfMonth.setSelection(month);
        }
        if (selectedSpinnerIndexes[2] < spinnerOfYear.getCount()) {
            spinnerOfYear.setSelection(selectedSpinnerIndexes[2]);
        } else {
            spinnerOfYear.setSelection(1);
        }

    }

    private void setDeadlineTextViewValues(String[] deadlines) {
        TextView seven = findViewById(R.id.result_seven_days_1);
        TextView three = findViewById(R.id.result_seven_three_days);
        TextView two = findViewById(R.id.result_seven_three_two_days);
        TextView finalResult = findViewById(R.id.result_seventeen);
        seven.setText(deadlines[0]);
        three.setText(deadlines[1]);
        two.setText(deadlines[2]);
        finalResult.setText(deadlines[3]);
    }

    private void startRegionSelectActivity() {
        saveSpinnerIndexToVariable();
        Intent myIntent = new Intent(MainActivity.this, RegionSelect.class);
        myIntent.putExtra(SPINNER_INDEX, selectedSpinnerIndexes);
        MainActivity.this.startActivity(myIntent);
        finish();
    }

    private String[] calculateDeadlinesInWorkingDays(LocalDate workingDate) {
        List<PublicHolidays> publicHolidaysList = fromItemListGetPublicHolidays(publicHolidaysItemFromGoogle);
        excludedDatesArrayList = new ArrayList<>(); //Resets the numbers to be empty for the isPublicOrWeekend
        int workingDays = 17; //Fixed term length
        String[] WorkingDates = new String[4]; // 4 important dates
        while (workingDays >= 0) {
            if (!isPublicOrWeekend(workingDate, publicHolidaysList, excludedDatesArrayList)) {
                workingDays--;//only decrements when considering actual working days
                Log.d("WorkingDays", workingDate.toString() + " " + workingDays);
                if (workingDays == 10) { //Numbers apply themselves, 1 cycle behind.
                    WorkingDates[0] = dateTimeFormatter.format(workingDate); // 7th day
                } else if (workingDays == 7) {
                    WorkingDates[1] = dateTimeFormatter.format(workingDate); // 7 + 3rd day
                } else if (workingDays == 5) {
                    WorkingDates[2] = dateTimeFormatter.format(workingDate); // 7 + 3 + 2nd day
                } else if (workingDays == 0) {
                    WorkingDates[3] = dateTimeFormatter.format(workingDate); // Final Day (7 + 3 + 2 + 5th day)
                }

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
            } else if (!loadRegionDateFromSharedPref().equals("") && currentItem.getDescription().contains("Public holiday") && publicHolidaysItemFromGoogle.get(i).getDescription().contains(regionOfHolidays)) { //gets Regional holidays
                publicHolidays.add(new PublicHolidays(currentItem));//adds
            }
        }
        return publicHolidays;
    }

    private String loadRegionDateFromSharedPref() {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        return sharedPreferences.getString(USER_REGION, "");
    }

    private boolean loadSwitchStatusFromSharedPref() {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        return sharedPreferences.getBoolean(SWITCH_STATE, false);
    }

    private void saveSwitchStatusToSharedPref() {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(SWITCH_STATE, switchBetweenResultLayout.isChecked());
        editor.apply();
    }

    private void saveSpinnerIndexToVariable() {
        if (spinnerOfDay == null || spinnerOfMonth == null || spinnerOfYear == null)
            return;
        selectedSpinnerIndexes = new int[]{spinnerOfDay.getSelectedItemPosition(), spinnerOfMonth.getSelectedItemPosition(), spinnerOfYear.getSelectedItemPosition()};
    }

    private void makeAndDisplayExcludedDatesPopup() {//creates the NonWorking Days Listview popup
        excludedDatesDialogBuilder = new AlertDialog.Builder(this);

        //Formats the dialog
        final View popupView = getLayoutInflater().inflate(R.layout.popup, null);
        buttonExitExcludedDatesPopup = popupView.findViewById(R.id.popup_exit);
        popupListView = popupView.findViewById(R.id.popup_listview);
        //Displays the Dialog
        excludedDatesDialogBuilder.setView(popupView);
        excludedDatesDialog = excludedDatesDialogBuilder.create();
        excludedDatesDialog.show();
        ExcludedDatesAdapter excludedDatesAdapter = new ExcludedDatesAdapter(this, excludedDatesArrayList);
        popupListView.setAdapter(excludedDatesAdapter);

        buttonExitExcludedDatesPopup.setOnClickListener(view -> excludedDatesDialog.dismiss());//closes window upon button press
    }

    private void makeAndDisplayInformationPopup() { //Makes the important information popup
        infoDialogBuilder = new AlertDialog.Builder(this);

        //Formats the dialog
        final View popupView = getLayoutInflater().inflate(R.layout.information_popup, null);
        buttonExitInfoPopup = popupView.findViewById(R.id.information_exitbutton);
        //Displays the Dialog
        infoDialogBuilder.setView(popupView);
        informationDialog = infoDialogBuilder.create();
        informationDialog.show();//Shows the popup
        buttonExitInfoPopup.setOnClickListener(view -> informationDialog.dismiss());//closes the dialog on click
    }

    protected void fetchHolidayListFromGoogle() {

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
            call.enqueue(new Callback<Holidays>() {
                @Override
                public void onResponse(Call<Holidays> call, Response<Holidays> response) {
                    if (!response.isSuccessful()) {
                        return;
                    }
                    Holidays holiday = response.body();
                    if (holiday == null) {
                        publicHolidaysItemFromGoogle = null;
                        Toast.makeText(context, "Failed to get List - Must calculate w/o public holidays", Toast.LENGTH_SHORT).show();
                    } else {
                        publicHolidaysItemFromGoogle = holiday.getItems();
                    }
                    runDateCalculationProcesses();

                }

                @Override
                public void onFailure(Call<Holidays> call, Throwable throwable) {
                    publicHolidaysItemFromGoogle = null;
                    Toast.makeText(context, "Failed connection - Must calculate w/o public holidays", Toast.LENGTH_SHORT).show();
                    runDateCalculationProcesses();
                }
            });
        } else {
            runDateCalculationProcesses();
        }
        //String api_location = "https://www.googleapis.com/calendar/v3/calendars/en.new_zealand%23holiday%40group.v.calendar.google.com/events?key=AIzaSyD2Xy5SVR22tomUkKkxKEGMIboLbAO0ATE";
    }

    private void runDateCalculationProcesses() {
        try {
            //Gets Current Selection from Spinners
            LocalDate startOfCalculationsDate = LocalDate.of(Integer.parseInt(spinnerOfYear.getSelectedItem().toString()), Integer.parseInt(spinnerOfMonth.getSelectedItem().toString()), Integer.parseInt(spinnerOfDay.getSelectedItem().toString()));
            String[] deadlinesInWorkingDays = calculateDeadlinesInWorkingDays(startOfCalculationsDate);
            setDeadlineTextViewValues(deadlinesInWorkingDays);
        } catch (Exception ex) {
            Toast.makeText(this, "Invalid Serving Date", Toast.LENGTH_LONG).show();
        }
        finally {
            loadingCircle.setVisibility(View.GONE);//hide loading circle
        }
    }

    private class ExcludedDatesAdapter extends BaseAdapter {
        private final Context context;
        private final ArrayList<PublicHolidays> appliedPublicHolidays;

        public ExcludedDatesAdapter(Context context, ArrayList<PublicHolidays> appliedPublicHolidays) {
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
            smallText.setText(dateTimeFormatter.format(appliedPublicHolidays.get(position).getStart()));


            return row;
        }
    }

}