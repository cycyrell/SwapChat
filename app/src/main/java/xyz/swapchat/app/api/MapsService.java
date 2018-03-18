package xyz.teamcatalyst.breedr.api;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;
import xyz.teamcatalyst.breedr.data.MapQueryResult;

/**
 * @author A-Ar Andrew Concepcion
 * @createdOn 24/07/2017
 */
public interface MapsService {

    @GET("place/textsearch/json")
    Call<MapQueryResult> queryPlacesOfInterest(@Query("key") String key, @Query("query") String query, @Query("pagetoken") String pageToken);

}
