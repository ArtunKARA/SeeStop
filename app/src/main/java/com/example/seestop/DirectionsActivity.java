package com.example.seestop;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.PolyUtil;
import com.google.maps.android.SphericalUtil;

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
    private static final double averageStepLength = 0.75; // Ortalama adım uzunluğu (metre cinsinden)

    private static final float PROXIMITY_THRESHOLD = 10.0f; // Talimat noktasına yaklaşma eşiği (metre cinsinden)
    private int RQ_SPEECH_REC = 102;
    private TextToSpeech textToSpeech;
    private GoogleMap mMap;
    private LatLng origin;
    private LatLng destination;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private Polyline routePolyline = null;
    private List<LatLng> path;
    private int currentStepIndex = 0;
    private Marker currentLocationMarker;

    private TextView directionTextView;
    private Button repeatButton;
    private String currentInstruction = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_directions);

        directionTextView = findViewById(R.id.direction_text);
        repeatButton = findViewById(R.id.repeat_button);

        repeatButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                speak(currentInstruction);
            }
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
                textToSpeech.setLanguage(new Locale("tr", "TR"));
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
                LocationRequest locationRequest = LocationRequest.create();
                locationRequest.setInterval(5000);
                locationRequest.setFastestInterval(2000);
                locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

                locationCallback = new LocationCallback() {
                    @Override
                    public void onLocationResult(LocationResult locationResult) {
                        if (locationResult == null) {
                            return;
                        }
                        for (Location location : locationResult.getLocations()) {
                            LatLng currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
                            if (origin == null) {
                                origin = currentLocation;
                                mMap.addMarker(new MarkerOptions().position(origin).title("Current Location"));
                                mMap.addMarker(new MarkerOptions().position(destination).title("Destination"));
                                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(origin, 17));
                                drawRoute(origin, destination);
                            }
                            handleLocationUpdate(currentLocation);
                        }
                    }
                };

                fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
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
                runOnUiThread(() -> {
                    parseDirectionsResponse(response);
                    startLocationUpdates(); // Yeni rota hesaplandıktan sonra konum güncellemelerini yeniden başlat
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void startLocationUpdates() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            LocationRequest locationRequest = LocationRequest.create();
            locationRequest.setInterval(5000);
            locationRequest.setFastestInterval(2000);
            locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

            locationCallback = new LocationCallback() {
                @Override
                public void onLocationResult(LocationResult locationResult) {
                    if (locationResult == null) {
                        return;
                    }
                    for (Location location : locationResult.getLocations()) {
                        LatLng currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
                        handleLocationUpdate(currentLocation);
                    }
                }
            };

            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
        }
    }


    private String getDirectionsUrl(LatLng origin, LatLng dest) {
        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;
        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;
        String mode = "mode=walking";
        String key = "key=AIzaSyCHvk7BoPzS4ba8AFivA_fYQa1RwmgDgc0";
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
                path = PolyUtil.decode(encodedString);
                PolylineOptions polylineOptions = new PolylineOptions()
                        .addAll(path)
                        .width(10)
                        .color(R.color.black)
                        .geodesic(true);
                if (routePolyline != null) {
                    routePolyline.remove();
                }
                routePolyline = mMap.addPolyline(polylineOptions);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleLocationUpdate(LatLng currentLocation) {
        if (path != null && currentStepIndex < path.size()) {
            // Haritadaki mevcut konum marker'ını güncelle
            if (currentLocationMarker == null) {
                currentLocationMarker = mMap.addMarker(new MarkerOptions().position(currentLocation).title("Mevcut Konum"));
            } else {
                currentLocationMarker.setPosition(currentLocation);
            }
            mMap.moveCamera(CameraUpdateFactory.newLatLng(currentLocation));

            LatLng targetPoint = path.get(currentStepIndex);
            float[] results = new float[1];
            Location.distanceBetween(currentLocation.latitude, currentLocation.longitude,
                    targetPoint.latitude, targetPoint.longitude, results);
            float distanceToTarget = results[0];

            if (distanceToTarget < PROXIMITY_THRESHOLD) {
                if (currentStepIndex < path.size() - 1) {
                    LatLng nextPoint = path.get(currentStepIndex + 1);
                    String direction = calculateDirection(targetPoint, nextPoint);
                    double distance = SphericalUtil.computeDistanceBetween(targetPoint, nextPoint);
                    int stepsToNext = (int) (distance / averageStepLength);
                    String detailedDirection = stepsToNext + " adım sonra " + direction + " yönüne gidin.";
                    currentInstruction = detailedDirection;
                    directionTextView.setText(detailedDirection);
                    Toast.makeText(DirectionsActivity.this, detailedDirection, Toast.LENGTH_LONG).show();
                    speak(detailedDirection);
                } else {
                    currentInstruction = "Navigasyon tamamlandı.";
                    directionTextView.setText(currentInstruction);
                    Toast.makeText(DirectionsActivity.this, currentInstruction, Toast.LENGTH_LONG).show();
                    speak(currentInstruction);
                    /*Intent intent =  new Intent(this, MainActivity.class);
                    startActivity(intent);*/
                }
                currentStepIndex++;
            }

            if (distanceToTarget > 50) { // Kullanıcı rotadan sapmışsa, yeniden rota hesapla
                stopLocationUpdates(); // Mevcut konum güncellemelerini durdur
                drawRoute(currentLocation, destination); // Yeni rotayı hesapla
                currentInstruction = "Rotadan sapıldı, rota yeniden hesaplandı.";
                directionTextView.setText(currentInstruction);
                speak(currentInstruction);
            }
        }
    }

    private void stopLocationUpdates() {
        if (fusedLocationClient != null && locationCallback != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
        }
    }


    private String calculateDirection(LatLng currentPoint, LatLng nextPoint) {
        double bearing = SphericalUtil.computeHeading(currentPoint, nextPoint);
        if (bearing >= -45 && bearing < 45) {
            return "doğru";
        } else if (bearing >= 45 && bearing < 135) {
            return "sağa";
        } else if (bearing >= -135 && bearing < -45) {
            return "sola";
        } else {
            return "geri";
        }
    }

    public void speak(String text) {
        textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
        if (fusedLocationClient != null && locationCallback != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
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
