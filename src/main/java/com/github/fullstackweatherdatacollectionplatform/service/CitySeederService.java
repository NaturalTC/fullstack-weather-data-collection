package com.github.fullstackweatherdatacollectionplatform.service;

import com.github.fullstackweatherdatacollectionplatform.model.City;
import com.github.fullstackweatherdatacollectionplatform.repository.CityRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CitySeederService {

    private final CityRepository cityRepository;

    public CitySeederService(CityRepository cityRepository) {
        this.cityRepository = cityRepository;
    }

    // Predefined US cities: name, state, lat, lon
    private static final List<Object[]> US_CITIES = List.of(
        // Original New England cities
        new Object[]{"Boston",          "MA", 42.3601,  -71.0589},
        new Object[]{"Worcester",       "MA", 42.2626,  -71.8023},
        new Object[]{"Springfield",     "MA", 42.1015,  -72.5898},
        new Object[]{"Providence",      "RI", 41.8240,  -71.4128},
        new Object[]{"Hartford",        "CT", 41.7658,  -72.6851},
        new Object[]{"Burlington",      "VT", 44.4759,  -73.2121},
        new Object[]{"Concord",         "NH", 43.2081,  -71.5376},
        new Object[]{"Bangor",          "ME", 44.8016,  -68.7712},
        new Object[]{"Portland",        "ME", 43.6615,  -70.2553},
        // Major US cities
        new Object[]{"New York",        "NY", 40.7128,  -74.0060},
        new Object[]{"Los Angeles",     "CA", 34.0522, -118.2437},
        new Object[]{"Chicago",         "IL", 41.8781,  -87.6298},
        new Object[]{"Houston",         "TX", 29.7604,  -95.3698},
        new Object[]{"Phoenix",         "AZ", 33.4484, -112.0740},
        new Object[]{"Philadelphia",    "PA", 39.9526,  -75.1652},
        new Object[]{"San Antonio",     "TX", 29.4241,  -98.4936},
        new Object[]{"San Diego",       "CA", 32.7157, -117.1611},
        new Object[]{"Dallas",          "TX", 32.7767,  -96.7970},
        new Object[]{"San Jose",        "CA", 37.3382, -121.8863},
        new Object[]{"Austin",          "TX", 30.2672,  -97.7431},
        new Object[]{"Jacksonville",    "FL", 30.3322,  -81.6557},
        new Object[]{"Columbus",        "OH", 39.9612,  -82.9988},
        new Object[]{"Charlotte",       "NC", 35.2271,  -80.8431},
        new Object[]{"Indianapolis",    "IN", 39.7684,  -86.1581},
        new Object[]{"San Francisco",   "CA", 37.7749, -122.4194},
        new Object[]{"Seattle",         "WA", 47.6062, -122.3321},
        new Object[]{"Denver",          "CO", 39.7392, -104.9903},
        new Object[]{"Nashville",       "TN", 36.1627,  -86.7816},
        new Object[]{"Oklahoma City",   "OK", 35.4676,  -97.5164},
        new Object[]{"Las Vegas",       "NV", 36.1699, -115.1398},
        new Object[]{"Memphis",         "TN", 35.1495,  -90.0490},
        new Object[]{"Eugene",           "OR", 44.0521, -123.0868},
        new Object[]{"Baltimore",       "MD", 39.2904,  -76.6122},
        new Object[]{"Milwaukee",       "WI", 43.0389,  -87.9065},
        new Object[]{"Albuquerque",     "NM", 35.0844, -106.6504},
        new Object[]{"Tucson",          "AZ", 32.2226, -110.9747},
        new Object[]{"Fresno",          "CA", 36.7378, -119.7871},
        new Object[]{"Sacramento",      "CA", 38.5816, -121.4944},
        new Object[]{"Kansas City",     "MO", 39.0997,  -94.5786},
        new Object[]{"Atlanta",         "GA", 33.7490,  -84.3880},
        new Object[]{"Omaha",           "NE", 41.2565,  -95.9345},
        new Object[]{"Colorado Springs","CO", 38.8339, -104.8214},
        new Object[]{"Raleigh",         "NC", 35.7796,  -78.6382},
        new Object[]{"Minneapolis",     "MN", 44.9778,  -93.2650},
        new Object[]{"Tampa",           "FL", 27.9506,  -82.4572},
        new Object[]{"New Orleans",     "LA", 29.9511,  -90.0715},
        new Object[]{"Miami",           "FL", 25.7617,  -80.1918},
        new Object[]{"Cleveland",       "OH", 41.4993,  -81.6944},
        new Object[]{"Pittsburgh",      "PA", 40.4406,  -79.9959},
        new Object[]{"Cincinnati",      "OH", 39.1031,  -84.5120},
        new Object[]{"St. Louis",       "MO", 38.6270,  -90.1994},
        new Object[]{"Louisville",      "KY", 38.2527,  -85.7585},
        new Object[]{"Richmond",        "VA", 37.5407,  -77.4360},
        new Object[]{"Salt Lake City",  "UT", 40.7608, -111.8910},
        new Object[]{"Boise",           "ID", 43.6150, -116.2023},
        new Object[]{"Detroit",         "MI", 42.3314,  -83.0458},
        new Object[]{"Buffalo",         "NY", 42.8864,  -78.8784},
        new Object[]{"Albany",          "NY", 42.6526,  -73.7562},
        new Object[]{"Honolulu",        "HI", 21.3069, -157.8583},
        new Object[]{"Anchorage",       "AK", 61.2181, -149.9003},
        // Filling regional gaps
        new Object[]{"El Paso",         "TX", 31.7619, -106.4850},
        new Object[]{"Tulsa",           "OK", 36.1540,  -95.9928},
        new Object[]{"Des Moines",      "IA", 41.5868,  -93.6250},
        new Object[]{"Spokane",         "WA", 47.6587, -117.4260},
        new Object[]{"Birmingham",      "AL", 33.5207,  -86.8025},
        new Object[]{"Virginia Beach",  "VA", 36.8529,  -75.9780},
        new Object[]{"Fargo",           "ND", 46.8772,  -96.7898},
        new Object[]{"Little Rock",     "AR", 34.7465,  -92.2896},
        new Object[]{"Billings",        "MT", 45.7833, -108.5007},
        new Object[]{"Fairbanks",       "AK", 64.8378, -147.7164},
        new Object[]{"Cheyenne",        "WY", 41.1400, -104.8202},
        new Object[]{"Sioux Falls",     "SD", 43.5446,  -96.7311}
    );

    @PostConstruct
    public void seedCities() {
        int added = 0;
        for (Object[] row : US_CITIES) {
            String name = (String) row[0];
            if (cityRepository.findByName(name).isEmpty()) {
                cityRepository.save(new City(name, (String) row[1], "US",
                        (double) row[2], (double) row[3]));
                added++;
            }
        }
        if (added > 0) {
            System.out.printf("[CitySeeder] Added %d new US cities to monitoring.%n", added);
        }
    }

    public SeedResult bulkSeed() {
        int added = 0, skipped = 0;
        for (Object[] row : US_CITIES) {
            String name = (String) row[0];
            if (cityRepository.findByName(name).isEmpty()) {
                cityRepository.save(new City(name, (String) row[1], "US",
                        (double) row[2], (double) row[3]));
                added++;
            } else {
                skipped++;
            }
        }
        return new SeedResult(added, skipped);
    }

    public record SeedResult(int added, int skipped) {}
}
