package com.example.servedcalculator_nzs3910;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;

public interface JsonPlaceHolderApi {
    @GET("events?key=AIzaSyD2Xy5SVR22tomUkKkxKEGMIboLbAO0ATE")
    Call<Holidays> getHolidays();
}
