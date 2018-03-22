package xyz.teamcatalyst.breedr;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Looper;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.firebase.geofire.GeoLocation;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.auth.FirebaseAuth;
import com.pixplicity.easyprefs.library.Prefs;

import java.util.Date;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import xyz.teamcatalyst.breedr.api.FirebaseApi;
import xyz.teamcatalyst.breedr.lovematch.ItemListActivity;
import xyz.teamcatalyst.breedr.notifications.NotificationsActivity;
import xyz.teamcatalyst.breedr.profile.ProfileActivity;
import xyz.teamcatalyst.breedr.shops.MapsActivity;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static com.google.android.gms.location.LocationServices.FusedLocationApi;

public class MyDashboard extends AppCompatActivity {

    @BindView(R.id.last_login)
    TextView mLastLogin;
    private GoogleApiClient googleApiClient;
    private LocationListener mLocationListener;
    private boolean isRequestingLocationUpdates;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_dashboard);
        ButterKnife.bind(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Swap Chat");
        setSupportActionBar(toolbar);
        mLastLogin.setText(getString(R.string.last_login) + new Date(Prefs.getLong("login_attempt", System.currentTimeMillis())));
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setVisibility(View.GONE);
        fab.setOnClickListener(view -> Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show());

        mLocationListener = location -> {
            Log.d("LOCATION", location.toString());
            FirebaseApi.getInstance().setLocation(location);
            FirebaseApi.getInstance().getGeofire().setLocation(FirebaseApi.getUser().getUid(), new GeoLocation(location.getLatitude(), location.getLongitude()));
        };
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!isRequestingLocationUpdates) {
            requestLocationUpdates();
        }
    }

    private void requestLocationUpdates() {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    Looper.prepare();
                } catch (Exception e) {
                    // do nothing
                }

                if (googleApiClient != null && googleApiClient.isConnected() && !googleApiClient.isConnecting()) {
                    // don't do anything
                } else {
                    googleApiClient = new GoogleApiClient.Builder(MyDashboard.this).addApi(LocationServices.API).build();
                    googleApiClient.blockingConnect();
                }

                LocationRequest mLocationRequest = LocationRequest.create();
                mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
                mLocationRequest.setInterval(15_000);
                mLocationRequest.setFastestInterval(1_000);
                mLocationRequest.setSmallestDisplacement(0);
                mLocationRequest.setNumUpdates(Integer.MAX_VALUE);
                if (ActivityCompat.checkSelfPermission(MyDashboard.this, ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(MyDashboard.this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    ActivityCompat.requestPermissions(MyDashboard.this,
                            new String[]{ACCESS_FINE_LOCATION},
                            1);
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                isRequestingLocationUpdates = true;
                FusedLocationApi.requestLocationUpdates(googleApiClient, mLocationRequest,
                        mLocationListener);
                Location location = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
                if (location != null) {
                    FirebaseApi.getInstance().setLocation(location);
                    FirebaseApi.getInstance().getGeofire().setLocation(FirebaseApi.getUser().getUid(), new GeoLocation(location.getLatitude(), location.getLongitude()));
                }
            }
        });
    }

    @OnClick(R.id.profile)
    public void onMProfileClicked() {
        ProfileActivity.start(this);
    }

    @OnClick(R.id.btn_items)
    public void onMBtnItemsClicked() {
        startActivity(new Intent(this, ItemListActivity.class));
    }

    @OnClick(R.id.notifications)
    public void onNotificationsClicked() {
        startActivity(new Intent(this, NotificationsActivity.class));
    }

    @OnClick(R.id.maps)
    public void onMapsClicked() {
        startActivity(new Intent(this, MapsActivity.class));
    }

    @OnClick(R.id.logout)
    public void onLogoutClicked() {
        new AlertDialog.Builder(this)
                .setMessage(R.string.dashboard_log_out)
                .setPositiveButton(R.string.agree, (dialogInterface, i) -> {
                    Prefs.putBoolean("isSameFace", false);
                    for (Map.Entry<String, ?> stringEntry : Prefs.getAll().entrySet()) {
                        if (stringEntry.getKey().startsWith("dislike")) {
                            Prefs.remove(stringEntry.getKey());
                        }
                    }
                    dialogInterface.dismiss();
                    FirebaseAuth.getInstance().signOut();
                    finish();
                })
                .setNegativeButton(R.string.disagree, ((dialogInterface, i) -> {
                    dialogInterface.dismiss();
                }))
                .show();

    }
}
