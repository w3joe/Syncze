package com.t4g.location_tracking_application;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface OneMapAPICall {
    @GET("search")
    Call<JSONResponse> getData(@Query("searchVal") String searchval,
                               @Query("returnGeom") String returnGeom,
                               @Query("getAddrDetails")String getAddrDetails);
}
