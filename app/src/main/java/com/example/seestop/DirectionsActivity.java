package com.example.seestop;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.PolyUtil;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Locale;

public class DirectionsActivity extends AppCompatActivity implements OnMapReadyCallback {
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private int RQ_SPEECH_REC = 102;
    private TextToSpeech textToSpeech;
    private GoogleMap mMap;
    private LatLng origin;
    private LatLng destination;
    private FusedLocationProviderClient fusedLocationClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_directions);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Gelen verilerden destination konumunu al
        String latStr = getIntent().getStringExtra("lat");
        String lngStr = getIntent().getStringExtra("lng");
        double lat = Double.parseDouble(latStr);
        double lng = Double.parseDouble(lngStr);
        destination = new LatLng(lat, lng);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        // Initialize TextToSpeech
        textToSpeech = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                textToSpeech.setLanguage(Locale.getDefault());
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            getDeviceLocation();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    private void getDeviceLocation() {
        try {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                fusedLocationClient.getLastLocation()
                        .addOnSuccessListener(this, location -> {
                            if (location != null) {
                                origin = new LatLng(location.getLatitude(), location.getLongitude());
                                mMap.addMarker(new MarkerOptions().position(origin).title("Current Location"));
                                mMap.addMarker(new MarkerOptions().position(destination).title("Destination"));
                                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(origin, 10));
                                drawRoute(origin, destination);
                            }
                        });
            }
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    private void drawRoute(LatLng origin, LatLng dest) {
        String url = getDirectionsUrl(origin, dest);
        new Thread(() -> {
            try {
                HttpURLConnection urlConnection = (HttpURLConnection) new URL(url).openConnection();
                urlConnection.connect();
                BufferedReader br = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    sb.append(line);
                }
                br.close();
                String response = sb.toString();
                runOnUiThread(() -> parseDirectionsResponse(response));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private String getDirectionsUrl(LatLng origin, LatLng dest) {
        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;
        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;
        String mode = "mode=walking";
        String key = "key=AIzaSyAej1Jp0p05Sjx8laIdIHUmKDnHWFMeZyE";
        String parameters = str_origin + "&" + str_dest + "&" + mode + "&" + key;
        return "https://maps.googleapis.com/maps/api/directions/json?" + parameters;
    }

    private void parseDirectionsResponse(String jsonData) {
        try {
            JSONObject jsonObject = new JSONObject(jsonData);
            JSONArray routes = jsonObject.getJSONArray("routes");
            if (routes.length() > 0) {
                JSONObject route = routes.getJSONObject(0);
                JSONObject overviewPolyline = route.getJSONObject("overview_polyline");
                String encodedString = overviewPolyline.getString("points");
                List<LatLng> points = PolyUtil.decode(encodedString);
                PolylineOptions polylineOptions = new PolylineOptions()
                        .addAll(points)
                        .width(10)
                        .color(R.color.black)
                        .geodesic(true);
                mMap.addPolyline(polylineOptions);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void speak(String text) {
        textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, "UtteranceId");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getDeviceLocation();
            }
        }
    }
}
