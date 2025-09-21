package com.example.fyp;

import retrofit2.Call;
import retrofit2.http.GET;

public interface SpinnerApi {
    @GET("api/spinner-data")
    <SpinnerDataResponse>
    Call<SpinnerDataResponse> getSpinnerData();
}

