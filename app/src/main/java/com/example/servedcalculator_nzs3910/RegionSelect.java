package com.example.servedcalculator_nzs3910;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import java.util.ArrayList;
import java.util.List;

import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

public class RegionSelect extends AppCompatActivity implements View.OnClickListener{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_region_select);
        Intent intent = getIntent();
        String region_preselected = intent.getStringExtra("Region");
        Button proceed = findViewById(R.id.Complete_setup);
        Spinner region_select_spinner = findViewById(R.id.region_selection_spinner);
        String[] region_item_list = new String[]{"","Waikato", "Auckland", "Nelson"};

        // Sets the list of items in the spinners
        ArrayAdapter<String> region_adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, region_item_list);
        region_select_spinner.setAdapter(region_adapter);

        region_select_spinner.setSelection(0);

        proceed.setOnClickListener(this);

        TextView preselected = findViewById(R.id.previous_region);
        if (region_preselected == null){
            preselected.setText("");
        }else {
            preselected.setText("Previous Region " + region_preselected);
        }
    }

    private boolean hasRegion(String region, String[] itemList){
        for (String r: itemList) {
            if (r.equals(region)){
                return true;
            }
        }
        return false;
    }

    private void switchActivities(String region_item){
        Intent myIntent = new Intent(RegionSelect.this, MainActivity.class);
        myIntent.putExtra("Region", region_item); //Optional parameters
        RegionSelect.this.startActivity(myIntent);
    }

    @Override
    public void onClick(View view){
        Spinner spinner = findViewById(R.id.region_selection_spinner);
        if(view != null){
            int id = view.getId();
            if(id == R.id.Complete_setup){
                if (!spinner.getSelectedItem().toString().equals("")){
                    switchActivities(spinner.getSelectedItem().toString());
                }
            }


        }
    }
}
