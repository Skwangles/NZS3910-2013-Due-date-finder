package com.example.servedcalculator_nzs3910;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import java.util.ArrayList;
import java.util.List;

import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class RegionSelect extends AppCompatActivity implements View.OnClickListener {
    public static final String SHARED_PREFS = "NZS3910-Region";
    public static final String REGION = "WhereAreYou";
    public static final String INDEXDATES = "selectedSpinnerIndex";
    Button proceed;
    Spinner region_select_spinner;
    Button goBack;
    int[] currentDateSelected;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_region_select);
        proceed = findViewById(R.id.Complete_setup);
        region_select_spinner = findViewById(R.id.region_selection_spinner);
        goBack = findViewById(R.id.Go_back);
        TextView preselected = findViewById(R.id.previous_region);
        currentDateSelected = getIntent().getIntArrayExtra(INDEXDATES);
        spinnerSetup();

        if (loadData().equals("")) {//Sets the text based on the region, as well as the GO BACK button
            preselected.setText("");
            goBack.setVisibility(View.GONE);
        } else {
            preselected.setText("Current Region: " + loadData());
            goBack.setVisibility(View.VISIBLE);
        }
        // set event listeners to avoid bugs
        proceed.setOnClickListener(this);
        goBack.setOnClickListener(this);
    }

    private int regionIndex(String region, String[] itemList) {
        for (int index = 0; index < itemList.length; index++) {
           if (region.equals(itemList[index])){
               return index;
           }
        }
        return 0;
    }



    @Override
    public void onClick(View view) {
        Spinner spinner = findViewById(R.id.region_selection_spinner);
        if (view != null) {
            int id = view.getId();
            if (id == R.id.Complete_setup) {
                if (!spinner.getSelectedItem().toString().equals("")) {
                    saveData();
                    switchActivities();
                } else {
                    Toast.makeText(this, "Please select a region", Toast.LENGTH_SHORT).show();
                }
            }
            if (id == R.id.Go_back) {
                if (loadData() == "") return;
                switchActivities();
            }
        }
    }

    //Base Level Functions
    //region base level
    private void spinnerSetup(){
        //Spinner Info Setup
        String[] region_item_list = new String[]{"Auckland", "Bay of Plenty", "Canterbury", "Gisborne", "Hawke's Bay", "Manawatu-Whanganui" ,"Marlborough", "Nelson", "Northland", "Otago", "Southland", "Taranaki", "Tasman", "Waikato", "Wellington", "West Coast"};
        // Sets the list of items in the spinners
        ArrayAdapter<String> region_adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, region_item_list);
        region_select_spinner.setAdapter(region_adapter);

        region_select_spinner.setSelection(regionIndex(loadData(), region_item_list));
    }

    private void switchActivities() {
        Intent myIntent = new Intent(RegionSelect.this, MainActivity.class);
        myIntent.putExtra(INDEXDATES, currentDateSelected);
        RegionSelect.this.startActivity(myIntent);
    }

    private void saveData() {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(REGION, region_select_spinner.getSelectedItem().toString());
        editor.apply();
    }

    private String loadData() {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        return sharedPreferences.getString(REGION, "");
    }
    //endregion
}
