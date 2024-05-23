package com.example.seestop;

import java.util.HashMap;
import java.util.Map;

public class StopsData {

    // Şehirlerin koordinatlarını tutan HashMap'ler
    private static final Map<String, Double> cityLatitudes = new HashMap<>();
    private static final Map<String, Double> cityLongitudes = new HashMap<>();

    static {
        // Şehir ve koordinat bilgilerini statik blokta initialize edelim
        cityLatitudes.put("b kapsısı", 40.82367);
        cityLongitudes.put("b kapsısı", 29.92495);

        cityLatitudes.put("a kapısı", 40.824136);
        cityLongitudes.put("a kapısı", 29.920707);

        cityLatitudes.put("hop stop", 40.823451);
        cityLongitudes.put("hop stop", 29.926805);

    }

    // Şehir ismine göre enlem bilgisini dönen metot
    public static double getLatitude(String cityName) {
        Double latitude = cityLatitudes.get(cityName.toLowerCase());
        if (latitude == null) {
            return 0.0;
        }
        return latitude;
    }

    // Şehir ismine göre boylam bilgisini dönen metot
    public static double getLongitude(String cityName) {
        Double longitude = cityLongitudes.get(cityName.toLowerCase());
        if (longitude == null) {
            return 0.0;
        }
        return longitude;
    }
}