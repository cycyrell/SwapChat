package xyz.teamcatalyst.breedr.shops;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import xyz.teamcatalyst.breedr.R;
import xyz.teamcatalyst.breedr.api.MapsApi;
import xyz.teamcatalyst.breedr.data.MapQueryResult;
import xyz.teamcatalyst.breedr.data.POI;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private List<POI> results = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        ButterKnife.bind(this);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        LatLng jru = new LatLng(14.592642, 121.0259619);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(jru, 11));
        mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
            @Override
            public View getInfoWindow(Marker marker) {
                POI result = (POI) marker.getTag();
                View viewMarker = ((LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE))
                        .inflate(R.layout.marker_veterenary, null);
                ButterKnife.bind(this, viewMarker);
                TextView address = (TextView) viewMarker.findViewById(R.id.address);
                TextView name = (TextView) viewMarker.findViewById(R.id.name);
                RatingBar rating = (RatingBar) viewMarker.findViewById(R.id.rating);
                CircleImageView image = (CircleImageView) viewMarker.findViewById(R.id.image);

                address.setText(result.getFormattedAddress());
                name.setText(result.getName());
                rating.setRating(result.getRating());
//                if (result.getPhotos() != null && result.getPhotos().size() > 0) {
//                    String imageUrl = getString(R.string.photo_request_format,
//                            400,
//                            result.getPhotos().get(0).getPhotoReference(),
//                            getString(R.string.google_api_key));
//                    GlideApp.with(MapsActivity.this).load(imageUrl).fitCenter().centerCrop().into(image);
//                }
                return viewMarker;
            }

            @Override
            public View getInfoContents(Marker marker) {
                return null;
            }
        });
        getVets(null);
    }

    public void getVets(String nextPageToken) {
        MapsApi.getInstance().getMapsService().queryPlacesOfInterest("AIzaSyCXmhvvo7_TjPozDlJn7YENS5OTMG5ivMk", "metro manila veterinary care", nextPageToken)
                .enqueue(new Callback<MapQueryResult>() {
                    @Override
                    public void onResponse(Call<MapQueryResult> call, Response<MapQueryResult> response) {
                        MapQueryResult mapQueryResult = response.body();
                        if (mapQueryResult == null) return;
                        results.addAll(mapQueryResult.getResults());
                        if (!TextUtils.isEmpty(mapQueryResult.getNextPageToken())) {
                            getVets(mapQueryResult.getNextPageToken());
                        } else {
                            markOnMap();
                        }
                    }

                    @Override
                    public void onFailure(Call<MapQueryResult> call, Throwable t) {

                    }
                });
    }

    private void markOnMap() {
        for (POI result : results) {
            Marker marker = mMap.addMarker(new MarkerOptions()
                    .position(new LatLng(result.getGeometry().getLocation().getLat(), result.getGeometry().getLocation().getLng()))
                    .title(result.getName())
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_hospital))
            );
            marker.setTag(result);
        }

    }


    public static Bitmap createDrawableFromView(Context context, View view) {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((Activity) context).getWindowManager().getDefaultDisplay()
                .getMetrics(displayMetrics);
        view.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        view.measure(displayMetrics.widthPixels, displayMetrics.heightPixels);
        view.layout(0, 0, displayMetrics.widthPixels,
                displayMetrics.heightPixels);
        view.buildDrawingCache();
        Bitmap bitmap = Bitmap.createBitmap(view.getMeasuredWidth(),
                view.getMeasuredHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        view.draw(canvas);
        return bitmap;
    }
}
