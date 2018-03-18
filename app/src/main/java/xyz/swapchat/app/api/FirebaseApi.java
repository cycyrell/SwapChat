package xyz.teamcatalyst.breedr.api;

import android.location.Location;

import com.firebase.geofire.GeoFire;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;

/**
 * @author A-Ar Andrew Concepcion
 * @createdOn 25/07/2017
 */
public class FirebaseApi {
    private static final FirebaseApi ourInstance = new FirebaseApi();
    private final FirebaseDatabase firebaseDatabase;
    private final GeoFire geofire;
    private Location location;

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public GeoFire getGeofire() {
        return geofire;
    }

    public static FirebaseApi getInstance() {
        return ourInstance;
    }

    public static FirebaseUser getUser() {
        return FirebaseAuth.getInstance().getCurrentUser();
    }

    public FirebaseDatabase getDatabase() {
        return firebaseDatabase;
    }

    private FirebaseApi() {
        firebaseDatabase = FirebaseDatabase.getInstance();
        geofire = new GeoFire(firebaseDatabase.getReference("geofire"));
    }
}
