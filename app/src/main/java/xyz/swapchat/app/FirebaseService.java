package xyz.teamcatalyst.breedr;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import xyz.teamcatalyst.breedr.data.Item;

/**
 * @author A-Ar Andrew Concepcion
 * @createdOn 29/06/2017
 */
public interface FirebaseService {

    @POST("user_items/{userId}.json")
    Call<Void> addDog(@Path("userId") String userId, @Body Item item);

    @GET("user_items/{userId}.json")
    Call<List<Item>> getDogs(@Path("userId") String userId);

}
