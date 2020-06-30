package com.example.ubfac.panicbutton;

import android.arch.lifecycle.ReportFragment;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.ubfac.panicbutton.api.ApiService;
import com.example.ubfac.panicbutton.api.RetrofitInstance;
import com.example.ubfac.panicbutton.model.LGAs;
import com.example.ubfac.panicbutton.model.PollingUnit;
import com.example.ubfac.panicbutton.views.CustomInfoWindowGoogleMap;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.GeoDataClient;
import com.google.android.gms.location.places.PlaceDetectionClient;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MapsActivity extends FragmentActivity implements GoogleMap.OnMarkerClickListener, OnMapReadyCallback, GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks {

    private static final String TAG = MapsActivity.class.getSimpleName();
    private static final int DEFAULT_ZOOM = 13;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    // Keys for storing activity state.
    private static final String KEY_CAMERA_POSITION = "camera_position";
    private static final String KEY_LOCATION = "location";

    private final LatLng mDefaultLocation = new LatLng(-6.5244, 3.3792);
    public String[] states;
    public String[] statesId;
    public ArrayList<LGAs> lgas = new ArrayList<>();


    String[] stateDrp, local_govt;
    GoogleApiClient googleApiClient;
    private GoogleMap mMap;
    private CameraPosition mCameraPosition;
    // The entry points to the Places API.
    private GeoDataClient mGeoDataClient;
    private PlaceDetectionClient mPlaceDetectionClient;
    // The entry point to the Fused Location Provider.
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private boolean mLocationPermissionGranted;
    // The geographical location where the device is currently located. That is, the last-known
    // location retrieved by the Fused Location Provider.
    private Location mLastKnownLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Retrieve location and camera position from saved instance state.
        if (savedInstanceState != null) {
            mLastKnownLocation = savedInstanceState.getParcelable(KEY_LOCATION);
            mCameraPosition = savedInstanceState.getParcelable(KEY_CAMERA_POSITION);
        }

        states = this.getResources().getStringArray(R.array.states);
        statesId = this.getResources().getStringArray(R.array.state_ids);


        // Construct a GeoDataClient
        mGeoDataClient = Places.getGeoDataClient(this);

        // Construct a PlaceDetectionClient.
        mPlaceDetectionClient = Places.getPlaceDetectionClient(this);

        // Construct a FusedLocationProviderClient.
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


        new GetLGAsTask().execute();
        showProgressbar();

    }

    /**
     * Saves the state of the map when the activity is paused.
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if (mMap != null) {
            outState.putParcelable(KEY_CAMERA_POSITION, mMap.getCameraPosition());
            outState.putParcelable(KEY_LOCATION, mLastKnownLocation);
            super.onSaveInstanceState(outState);
        }
    }
    Spinner statesSpinner;
    Spinner lgaSpinner;
    ArrayList<String> currentLGAs;
    String currentLGA = "";
    private void setUpSpinner() {
        CardView cardViewStates = findViewById(R.id.cardView);
        cardViewStates.setVisibility(View.VISIBLE);
        statesSpinner = findViewById(R.id.state);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.states, android.R.layout.simple_spinner_item);
// Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
// Apply the adapter to the spinner
        statesSpinner.setAdapter(adapter);

        lgaSpinner = findViewById(R.id.local_govt);
        CardView cardViewLga = findViewById(R.id.cardView2);
        cardViewLga.setVisibility(View.VISIBLE);

        if(mLastKnownLocation != null) {
            getStateFromLatAndLng(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude());
        }

        statesSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                //get the current state id for position
                final String stateId = statesId[position];

                final ArrayList<LGAs> currentStateLGAs = new ArrayList<>();
                currentLGAs = new ArrayList<>();

                //filter list with the state id
                for (LGAs lga : lgas) {
                    if (lga.getStateId().equals(stateId)) {
                        currentStateLGAs.add(lga);
                        currentLGAs.add(lga.getName());
                    }
                }
                ArrayAdapter adapter = new ArrayAdapter(MapsActivity.this,
                        android.R.layout.simple_spinner_item, currentLGAs);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                lgaSpinner.setAdapter(adapter);


                lgaSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        showProgressbar();
                        clearMap();
                        getCoordinatesForPollingUnits(getJsonString(stateId, String.valueOf(currentStateLGAs.get(position).getValue())));
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

                    }
                });

                for(int i=0; i<currentLGAs.size();i++){
                    if(currentLGA == null) {
                        return;
                    }
                    if(currentLGAs.get(i).toLowerCase().equals(currentLGA.toLowerCase())){
                        lgaSpinner.setSelection(i);
                    }
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

    }


    public void onStart() {
        super.onStart();
//         googleApiClient.connect();
    }

    public void onStop() {
        super.onStop();
//         googleApiClient.disconnect();
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
        // checkLocationandAddToMap();

        // Prompt the user for permission.
        getLocationPermission();

        // Turn on the My Location layer and the related control on the map.
        updateLocationUI();

        // Get the current location of the device and set the position of the map.
        getDeviceLocation();

        mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                showReportDialog();
            }
        });
    }

    private void getDeviceLocation() {
        /*
         * Get the best and most recent location of the device, which may be null in rare
         * cases when a location is not available.
         */
        try {
            if (mLocationPermissionGranted) {
                Task<Location> locationResult = mFusedLocationProviderClient.getLastLocation();
                locationResult.addOnCompleteListener(this, new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        if (task.isSuccessful()) {
                            // Set the map's camera position to the current location of the device.
                            mLastKnownLocation = task.getResult();
                            Log.e("TAG", "mLastKnownLocation " + mLastKnownLocation);
                            mMap.setMyLocationEnabled(true);
                            if(mLastKnownLocation != null){
                                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                        new LatLng(mLastKnownLocation.getLatitude(),
                                            mLastKnownLocation.getLongitude()), DEFAULT_ZOOM));
                            }else {
                                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mDefaultLocation
                                        /*new LatLng(5.677*//*mLastKnownLocation.getLatitude()*//*,
                                            7.899*//*mLastKnownLocation.getLongitude()*//*)*/, DEFAULT_ZOOM));
                            }

                        } else {
                            Log.e(TAG, "Current location is null. Using defaults.");
                            Log.e(TAG, "Exception: %s", task.getException());
                            mMap.moveCamera(CameraUpdateFactory
                                    .newLatLngZoom(mDefaultLocation, DEFAULT_ZOOM));
                            mMap.getUiSettings().setMyLocationButtonEnabled(false);
                        }
                    }
                });
            }
        } catch (SecurityException e) {
            Log.e("Exception: %s", e.getMessage());
        }
    }

    /**
     * Prompts the user for permission to use the device location.
     */
    private void getLocationPermission() {
        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }


    @Override
    public boolean onMarkerClick(Marker marker) {

        Integer clickCount = (Integer) marker.getTag();

        // Check if a click count was set, then display the click count.
        if (clickCount != null) {
            clickCount = clickCount + 1;
            marker.setTag(clickCount);
            Toast.makeText(this,
                    marker.getTitle() +
                            " has been clicked " + clickCount + " times.",
                    Toast.LENGTH_SHORT).show();
            //.(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
        }

        return false;
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        /*SupportMapFragment mapFragment = (SupportMapFragment)
                getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);*/
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    /**
     * Handles the result of the request for location permissions.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        mLocationPermissionGranted = false;
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mLocationPermissionGranted = true;
                }
            }
        }
        updateLocationUI();
    }

    /**
     * Updates the map's UI settings based on whether the user has granted location permission.
     */
    private void updateLocationUI() {
        if (mMap == null) {
            return;
        }
        try {
            if (mLocationPermissionGranted) {
                mMap.setMyLocationEnabled(true);
                mMap.getUiSettings().setMyLocationButtonEnabled(true);
            } else {
                mMap.setMyLocationEnabled(false);
                mMap.getUiSettings().setMyLocationButtonEnabled(false);
                mLastKnownLocation = null;
                getLocationPermission();
            }
        } catch (SecurityException e) {
            Log.e("Exception: %s", e.getMessage());
        }
    }

    private String getJsonString(String state, String lgaValue) {

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("state", state);
            jsonObject.put("lga", lgaValue);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return jsonObject.toString();
    }

    private void getCoordinatesForPollingUnits(String data) {
        /*Create handle for the RetrofitInstance interface*/
        ApiService service = RetrofitInstance.getRetrofitInstance().create(ApiService.class);

        Call<JsonObject> call = service.getAllPhotos(data);

        Log.e("TAG", "url " + service.getAllPhotos(data).request().url().toString());
        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                Log.e(TAG, "response gotten");
                hideProgressbar();
                JsonArray jsonArray = response.body().get("photos").getAsJsonArray();
                ArrayList<PollingUnit> pollingUnits = new ArrayList<>();
                for (int i = 0; i < jsonArray.size(); i++) {
                    PollingUnit pollingUnit = new Gson().fromJson(jsonArray.get(i), PollingUnit.class);
                    pollingUnits.add(pollingUnit);

                }
                Log.e("TAG", "pollingUnits " + pollingUnits.size());
                displayMarkersOnMap(pollingUnits);


            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Log.e("TAG", "error " + t.getLocalizedMessage());
                hideProgressbar();
            }
        });

    }

    private void displayMarkersOnMap(ArrayList<PollingUnit> pollingUnits) {

        pollingUnits = calculateDistanceDifference(pollingUnits);

        CustomInfoWindowGoogleMap customInfoWindow = new CustomInfoWindowGoogleMap(this);
        mMap.setInfoWindowAdapter(customInfoWindow);

        for (int i = 0; i < pollingUnits.size(); i++) {
            mMap.addMarker(new MarkerOptions().position(new LatLng(Double.parseDouble(pollingUnits.get(i).getLatitude()),
                    Double.parseDouble(pollingUnits.get(i).getLongitude())))
                    .title(pollingUnits.get(i).getDescription())
                    .icon(bitmapDescriptorFromVector(this, R.drawable.ic_location_pin)))
                    .setTag(pollingUnits.get(i));

            if (i == pollingUnits.size() - 1) {
                //animate the marker there
                mMap.moveCamera(CameraUpdateFactory
                        .newLatLngZoom(new LatLng(Double.parseDouble(pollingUnits.get(i).getLatitude()),
                                Double.parseDouble(pollingUnits.get(i).getLongitude())), 15f));

            }

        }
    }

    private BitmapDescriptor bitmapDescriptorFromVector(Context context, @DrawableRes int vectorDrawableResourceId) {
        Drawable vectorDrawable = ContextCompat.getDrawable(context, vectorDrawableResourceId);
        vectorDrawable.setBounds(0, 0, vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight());
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    private void clearMap() {
        if (mMap != null) {
            mMap.clear();
        }
    }

    private void showProgressbar() {
        ProgressBar progressBar = findViewById(R.id.progressBar);
        progressBar.setVisibility(View.VISIBLE);
    }

    private void hideProgressbar() {
        ProgressBar progressBar = findViewById(R.id.progressBar);
        progressBar.setVisibility(View.GONE);
    }

    public ArrayList<PollingUnit> calculateDistanceDifference(ArrayList<PollingUnit> pollingUnits) {
        if(mLastKnownLocation == null){
            return pollingUnits;
        }

        for(int i=0; i<pollingUnits.size();i++){
            Location locationA = new Location("point A");

            locationA.setLatitude(mLastKnownLocation.getLatitude());
            locationA.setLongitude(mLastKnownLocation.getLongitude());

            Location locationB = new Location("point B");

            locationB.setLatitude(Double.parseDouble(pollingUnits.get(i).getLatitude()));
            locationB.setLongitude(Double.parseDouble(pollingUnits.get(i).getLongitude()));

            double distance = locationA.distanceTo(locationB);
            Log.e(TAG, "distance " + distance);
            pollingUnits.get(i).setDistance(distance);
        }

        Collections.sort(pollingUnits, new Comparator<PollingUnit>() {
            @Override
            public int compare(PollingUnit lhs, PollingUnit rhs) {
                double distance1 = lhs.getDistance();
                double distance2 = rhs.getDistance();

                /*For descending order*/
                Double difference = distance2-distance1;
                return difference.intValue();
            }
        });

        Log.e(TAG, "polling units reordered " + pollingUnits.toString());

        return pollingUnits;
    }

    protected void getStateFromLatAndLng(double lat, double lng) {
        Log.e(TAG, "geocoder absent");
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        String errorMessage = "";

        List<Address> addresses = null;
        try {
            addresses = geocoder.getFromLocation(
                    lat,
                    lng,
                    // In this sample, get just a single address.
                    1);
            Log.e(TAG, "success geocoder is available lat: " + lat + " lng: " + lng);
        } catch (IOException ioException) {
            errorMessage = "Geo-coder Service is not available";

            Log.e(TAG, "error " + errorMessage);

            reverseGeocodeLatLngFromWeb(lat, lng);

        } catch (IllegalArgumentException illegalArgumentException) {
            // Catch invalid latitude or longitude values.
            errorMessage = "Invalid lat or long used";
            Log.e(TAG, errorMessage + ". " +
                    "Latitude = " + lat +
                    ", Longitude = " +
                    lng, illegalArgumentException);
        }

        // Handle case where no address was found.
        if (addresses == null || addresses.size() == 0) {
            if (errorMessage.isEmpty()) {
                errorMessage = "No Address was found";
            }
        } else {
            Address address = addresses.get(0);
            Log.e(TAG, "origin: city " + address.getSubAdminArea() + " state " + address.getAdminArea());
            for(int i=0;i<states.length; i++){
                if(states[i].toLowerCase().equals(address.getAdminArea().toLowerCase())){
                    statesSpinner.setSelection(i);
                }
            }
            currentLGA = address.getSubAdminArea();
        }

    }

    String errorMessage = "";
    private void reverseGeocodeLatLngFromWeb(double lat, double lng) {
        ApiService service = RetrofitInstance.getGeoRetrofitInstance().create(ApiService.class);

        service.getGeoCoderAddress("AIzaSyAU0jXmPg1IFecKdOuZKso72p9pQnZhLbE", lat + "," +lng).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {

                if(!response.body().get("status").getAsString().equals("OK")){
                    errorMessage = "Geocoder failed https://developers.google.com/maps/documentation/geocoding/intro#StatusCodes";
                    return;
                }

                String address = null;
                parseCityAndStateFromGeocoder(response.body());


            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                errorMessage = "No Internet for geocoder";
            }
        });
    }

    void parseCityAndStateFromGeocoder(JsonObject jsonObject){
        try {
            //try catch for any necessity
            JsonArray resultsArray = jsonObject.get("results").getAsJsonArray();
            for(int i=0; i<resultsArray.size();i++){
                JsonArray addressComponentsArray = resultsArray.get(i).getAsJsonObject().get("address_components").getAsJsonArray();
                for (int j=0; j<addressComponentsArray.size(); j++){
                    JsonArray typesArray = addressComponentsArray.get(j).getAsJsonObject().get("types").getAsJsonArray();
                    for(int k=0; k<typesArray.size(); k++){
                        if(typesArray.get(k).getAsString().equals("administrative_area_level_1")){
                            String state = addressComponentsArray.get(j).getAsJsonObject().get("long_name").getAsString();
                            Log.e(TAG, "geocoder web state " + state );
                            for(int m=0;i<states.length; m++){
                                if(states[m].toLowerCase().equals(state.toLowerCase())){
                                    statesSpinner.setSelection(m);
                                }
                            }

                        }
                        if(typesArray.get(k).getAsString().equals("administrative_area_level_2")){
                            String city = addressComponentsArray.get(j).getAsJsonObject().get("long_name").getAsString();
                            Log.e(TAG, "geocoder web city " + city );
                            currentLGA = city;
                        }
                    }

                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void showReportDialog() {
        FragmentManager fm = getSupportFragmentManager();
        ReportDialog reportDialog = ReportDialog.newInstance();
        reportDialog.show(fm, "fragment_edit_name");
    }

    /*public void onClick(View view) {
        showReportDialog();
    }*/



    class GetLGAsTask extends AsyncTask<Void, Void, ArrayList<LGAs>> {
        @Override
        protected ArrayList<LGAs> doInBackground(Void... voids) {
            String url = "http://www.inecnigeria.org/?page_id=937";
            try {
                // Connect to the web site
                Document mBlogDocument = Jsoup.connect(url).get();
                for (int i = 0; i < statesId.length; i++) {

                    Elements mElementDataSize = mBlogDocument.select("option[class=" + statesId[i] + "]");
                    int mElementSize = mElementDataSize.size();
                    // loop through each state to get the lgas
                    for (int j = 0; j < mElementSize; j++) {
                        LGAs currentLGA = new LGAs();
                        Elements lga = mBlogDocument.select("option[class=" + statesId[i] + "]").eq(j);
                        currentLGA.setName(lga.text());
                        currentLGA.setState(states[i]);
                        currentLGA.setStateId(statesId[i]);
                        currentLGA.setValue(Integer.parseInt(lga.attr("value")));

                        lgas.add(currentLGA);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(ArrayList<LGAs> lgAses) {
            super.onPostExecute(lgAses);
            hideProgressbar();
            setUpSpinner();

        }
    }


}
