package com.skwangles.servedcalculator_nzs3910;

import android.view.View;
import android.widget.TextView;

public class AppliedHolidaysViewHolder {
    TextView larger_item;
    TextView smaller_item;
    public AppliedHolidaysViewHolder(View v){
        larger_item = v.findViewById(R.id.larger_item);
        smaller_item = v.findViewById(R.id.smaller_item);
    }
}
