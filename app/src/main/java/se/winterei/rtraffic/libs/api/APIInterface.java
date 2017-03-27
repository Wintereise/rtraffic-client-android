package se.winterei.rtraffic.libs.api;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;
import se.winterei.rtraffic.libs.generic.AuthRequest;
import se.winterei.rtraffic.libs.generic.ExcludedRegion;
import se.winterei.rtraffic.libs.generic.Point;
import se.winterei.rtraffic.libs.generic.PointOfInterest;
import se.winterei.rtraffic.libs.generic.Report;

/**
 * Created by reise on 2/24/2017.
 */

public interface APIInterface
{
    @GET("v1/points")
    Call<List<Point>> getPoints ();

    @GET("v1/point/{id}")
    Call<Point> getPoint (@Path("id") int id);

    @GET("v1/point/{lat}/{lng}/{distance}")
    Call<List<Point>> getPoints (@Path("lat") Double lat, @Path("lng") Double lng, @Path("distance") int distance);

    @GET("v1/reports")
    Call<List<Report>> getReports (@Query("history") String history);

    @GET("v1/reports/{id}")
    Call<Report> getReport (@Path("id") int id);

    @GET("v1/reports/{lat}/{lng}")
    Call<List<Report>> getReports (@Path("lat") Double lat, @Path("lng") Double lng);

    @POST("v1/reports")
    Call<Report> postReport (@Body Report report);

    @GET("v1/excluded-regions")
    Call<List<ExcludedRegion>> getExcludedRegions ();

    @POST("v1/excluded-regions")
    Call<GenericAPIResponse> postExcludedRegion (@Body ExcludedRegion excludedRegion);

    @DELETE("v1/excluded-regions/{id}")
    Call<GenericAPIResponse> deleteExcludedRegion (@Path("id") int id);

    @GET("v1/poi")
    Call<List<PointOfInterest>> getPointsOfInterest ();

    @POST("v1/poi")
    Call<GenericAPIResponse> postPointOfInterest (@Body PointOfInterest pointOfInterest);

    @DELETE("v1/poi/{id}")
    Call<GenericAPIResponse> deletePointOfInterest (@Path("id") int id);

    @POST("v1/oauth")
    Call<GenericAPIResponse> authRequest (@Body AuthRequest authRequest);

}
