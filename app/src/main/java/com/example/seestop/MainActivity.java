package com.example.seestop;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;
import java.util.Locale;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Locale;

import com.example.seestop.StopsData;

public class MainActivity extends AppCompatActivity {

    private int RQ_SPEECH_REC= 102;
    private TextToSpeech textToSpeech;

    private PlaceApiService placeApiService;
    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // ses artırma
        AudioManager audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        int maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, maxVolume, 0);

        // sesli bilgilendirme
        textToSpeech = new TextToSpeech(this, this::onInit);

        // buton id find
        Button btn = findViewById(R.id.talkBtn);
        btn.setOnClickListener(v -> {
            askspeechinput();
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


        if (requestCode == RQ_SPEECH_REC && resultCode == Activity.RESULT_OK && data != null) {
            ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if (result != null && !result.isEmpty()) {
                String veri = result.get(0);
                Toast.makeText(getApplicationContext(), veri, Toast.LENGTH_SHORT).show();
                Intent intentveri = new Intent(this, MapActivity.class);
                Intent  intentDirection = new Intent(this, DirectionsActivity.class);

                placeApiService = new PlaceApiService(this);

                String query = veri; // Aramak istediğiniz mekanın adı
                if(StopsData.getLatitude(query) != 0.0 && StopsData.getLongitude(query) != 0.0){

                    String lngStr = String.valueOf(StopsData.getLongitude(query));
                    String latStr = String.valueOf(StopsData.getLatitude(query));

                    //Google turn by tunr navigation
                                /*Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("google.navigation:q=" + latStr + "," + lngStr+"&mode=w"));
                                intent.setPackage("com.google.android.apps.maps");
                                startActivity(intent);*/

                    //mapview
                                /*
                                intentveri.putExtra("lat", latStr);
                                intentveri.putExtra("lng", lngStr);
                                startActivity(intentveri);*/

                    //google diraction rotate

                    intentDirection.putExtra("lat", latStr);
                    intentDirection.putExtra("lng", lngStr);
                    startActivity(intentDirection);
                }else{
                    //speak("Aranan yer hazır duraklarda bulunamadı ilgili lokasyon aranıyor.");
                    placeApiService.getPlaceId(query, new PlaceApiService.PlaceIdCallback() {
                        @Override
                        public void onSuccess(String placeId) {
                            placeApiService.getPlaceDetails(placeId, new PlaceApiService.PlaceDetailsCallback() {
                                @Override
                                public void onSuccess(double lat, double lng) {
                                    Log.d(TAG, "Latitude: " + lat + ", Longitude: " + lng);
                                    Toast.makeText(MainActivity.this, "Latitude: " + lat + ", Longitude: " + lng, Toast.LENGTH_LONG).show();
                                    String latStr = String.valueOf(lat);
                                    String lngStr = String.valueOf(lng);

                                    //Google turn by tunr navigation
                                /*Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("google.navigation:q=" + latStr + "," + lngStr+"&mode=w"));
                                intent.setPackage("com.google.android.apps.maps");
                                startActivity(intent);*/

                                    //mapview
                                /*
                                intentveri.putExtra("lat", latStr);
                                intentveri.putExtra("lng", lngStr);
                                startActivity(intentveri);*/

                                    //google diraction rotate

                                    intentDirection.putExtra("lat", latStr);
                                    intentDirection.putExtra("lng", lngStr);
                                    startActivity(intentDirection);

                                }

                                @Override
                                public void onError(String error) {
                                    Log.e(TAG, "Hata: " + error);
                                    Toast.makeText(MainActivity.this, "Error: " + error, Toast.LENGTH_LONG).show();
                                }
                            });
                        }

                        @Override
                        public void onError(String error) {
                            speak("İstenilen yer bulunamadı");
                            Log.e(TAG, "Error: " + error);
                            Toast.makeText(MainActivity.this, "Error: " + error, Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }
        }
    }

    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            int result = textToSpeech.setLanguage(new Locale("tr", "TR"));

            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Toast.makeText(this, "Language not supported", Toast.LENGTH_SHORT).show();
            }else {
                speak("SeeStop uygulaması açıldı ses en yüksek konuma getirildi. Gitmek istediğiniz yeri söylemek için lütfen ekranın ortasına dokununuz.");
            }
        } else {
            Toast.makeText(this, "Initialization failed", Toast.LENGTH_SHORT).show();
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

    private void askspeechinput() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE,"tr-TR");
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Durak Konumu İçin Konuşunuz");
        speak("Durak Konumu İçin Konuşunuz");
        try {
            startActivityForResult(intent, RQ_SPEECH_REC);
        } catch (ActivityNotFoundException a) {
            Toast.makeText(getApplicationContext(), "Uygulamanın ses iele ilgili kımını desteklememektedir.", Toast.LENGTH_SHORT).show();
            speak("Uygulamanın ses iele ilgili kımını desteklememektedir.");
        }
    }
}


class PlaceApiService{
    private static final String API_KEY = "AIzaSyCHvk7BoPzS4ba8AFivA_fYQa1RwmgDgc0";
    private static final String PLACE_SEARCH_URL = "https://maps.googleapis.com/maps/api/place/findplacefromtext/json";
    private static final String PLACE_DETAILS_URL = "https://maps.googleapis.com/maps/api/place/details/json";
    private RequestQueue requestQueue;

    public PlaceApiService(Context context) {
        this.requestQueue = Volley.newRequestQueue(context);
    }

    public void getPlaceId(String query, final PlaceIdCallback callback) {
        String url = PLACE_SEARCH_URL + "?input=" + query + "&inputtype=textquery&key=" + API_KEY;

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONArray candidates = response.getJSONArray("candidates");
                            if (candidates.length() > 0) {
                                String placeId = candidates.getJSONObject(0).getString("place_id");
                                callback.onSuccess(placeId);
                            } else {
                                callback.onError("No place found");
                            }
                        } catch (JSONException e) {
                            callback.onError(e.getMessage());
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        callback.onError(error.getMessage());
                    }
                });

        requestQueue.add(request);
    }

    public void getPlaceDetails(String placeId, final PlaceDetailsCallback callback) {
        String url = PLACE_DETAILS_URL + "?placeid=" + placeId + "&key=" + API_KEY;

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONObject result = response.getJSONObject("result");
                            JSONObject geometry = result.getJSONObject("geometry");
                            JSONObject location = geometry.getJSONObject("location");
                            double lat = location.getDouble("lat");
                            double lng = location.getDouble("lng");
                            callback.onSuccess(lat, lng);
                        } catch (JSONException e) {
                            callback.onError(e.getMessage());
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        callback.onError(error.getMessage());
                    }
                });

        requestQueue.add(request);
    }

    public interface PlaceIdCallback {
        void onSuccess(String placeId);
        void onError(String error);
    }

    public interface PlaceDetailsCallback {
        void onSuccess(double lat, double lng);
        void onError(String error);
    }
}