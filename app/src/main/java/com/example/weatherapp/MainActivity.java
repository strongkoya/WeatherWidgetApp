package com.example.weatherapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.Manifest;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RemoteViews;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    public static final String WWPREF_DATE = "wwpref_date";
    public static final String WWPREF_DESC = "wwpref_desc";
    public static final String WWPREF_WIND = "wwpref_wind";
    public static final String WWPREF_HUMD = "wwpref_humd";
    public static final String WWPREF_TEMP = "wwpref_temp";
    public static final int ADDRESSES = 10;
    public static final String WWPREF_COUNTRY = "WWPREF_COUNTRY";
    public static final String WWPREF_CITY = "WWPREF_CITY";
    public static final String WWPREF_ICON = "WWPREF_ICON";
    public static final String WWPREF_LAT = "WWPREF_LAT";
    public static final String WWPREF_LNG = "WWPREF_LNG";
    public static final String WWPREF_PRSS = "WWPREF_PRSS";
    public static final String WWPREF_MINT = "WWPREF_MINT";
    public static final String WWPREF_MAXT = "WWPREF_MAXT";
    public static final String WWPREF_FEEL = "WWPREF_FEEL";
    public static final String WWPREF_SUNS = "WWPREF_SUNS";
    public static final String WWPREF_SUNR = "WWPREF_SUNR";
    String city = null;
    public LocationListener listener;

    public static final int REQUEST_PERMS = 1;


    EditText editText;
    Button button, locate, forecast;
    ImageView imageView;
    TextView temptv, time, longitude, latitude, humidity, sunrise, sunset, pressure, wind, country, city_nam, max_temp, min_temp, feels;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editText = findViewById(R.id.editTextTextPersonName);
        button = findViewById(R.id.button);
        locate = findViewById(R.id.buttonLocation);
        forecast = findViewById(R.id.buttonForecast);
        imageView = findViewById(R.id.imageView);
        temptv = findViewById(R.id.textView3);
        time = findViewById(R.id.textView2);

        longitude = findViewById(R.id.longitude);
        latitude = findViewById(R.id.latitude);
        humidity = findViewById(R.id.humidity);
        sunrise = findViewById(R.id.sunrise);
        sunset = findViewById(R.id.sunset);
        pressure = findViewById(R.id.pressure);
        wind = findViewById(R.id.wind);
        country = findViewById(R.id.country);
        city_nam = findViewById(R.id.city_nam);
        max_temp = findViewById(R.id.temp_max);
        min_temp = findViewById(R.id.min_temp);
        feels = findViewById(R.id.feels);


        refresh();
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Toast.makeText(getApplicationContext(), city,Toast.LENGTH_LONG).show();
                if (TextUtils.isEmpty(editText.getText())) {

                    editText.setError("City Name is required to search manually");

                } else {
                    city = editText.getText().toString();
                    FindWeather(city);
                }
            }
        });

        locate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_PERMS);
                    return;
                }


                LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                String provider = locationManager.getBestProvider(new Criteria(), true);
                listener = new LocationListener() {
                    @Override
                    public void onLocationChanged(@NonNull Location location) {
                    }

                    @Override
                    public void onStatusChanged(String provider, int status,
                                                Bundle extras) {
                    }

                    @Override
                    public void onProviderEnabled(String provider) {
                    }

                    @Override
                    public void onProviderDisabled(String provider) {
                    }
                };
                if (provider != null) {
                    Location location = locationManager.getLastKnownLocation(provider);

                    locationManager.requestLocationUpdates(provider, 300000, 0, listener);

                    if (location == null) {
                        locationManager.requestSingleUpdate(provider, listener, null);

                        return;
                    }

                    //located(location) affecte à city le nom de la cité via getAdminArea()
                    located(location);
                    FindWeather(city);
                    // Toast.makeText(getApplicationContext(),location.toString(),Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(getApplicationContext(), "La localisation est désactivée, veuillez l'activer ou bien chercher une autre ville!!!", Toast.LENGTH_LONG).show();

                }
            }
        });

        forecast.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                WeatherDialogFragment dialogFragment = new WeatherDialogFragment();
                dialogFragment.show(getSupportFragmentManager(), "weather_fragment_tag");

            }
        });

        SwipeRefreshLayout swipeRefreshLayout = findViewById(R.id.swipe_refresh_layout);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // Code à exécuter lorsqu'un swipe est détecté
                // Par exemple, vous pouvez appeler une fonction pour rafraîchir les données
                refreshDataAfterSwipe();
            }
        });

    }



    @Override
    protected void onStart() {
        super.onStart();
        getPref();
    }

    @Override
    protected void onResume() {
        super.onResume();
        getPref();
    }

    public void FindWeather(String city) {


        String url = "http://api.openweathermap.org/data/2.5/weather?q=" + city + "&appid=462f445106adc1d21494341838c10019&units=metric";
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        try {
                            //find temperature
                            JSONObject jsonObject = new JSONObject(response);
                            JSONObject object = jsonObject.getJSONObject("main");
                            double temp = object.getDouble("temp");
                            temptv.setText("Temperature\n" + temp + "°C");

                            //find country
                            JSONObject object8 = jsonObject.getJSONObject("sys");
                            String count = object8.getString("country");
                            country.setText(count + "  :");

                            //find city
                            String city = jsonObject.getString("name");
                            city_nam.setText(city);

                            //find icon
                            JSONArray jsonArray = jsonObject.getJSONArray("weather");
                            JSONObject obj = jsonArray.getJSONObject(0);
                            String icon = obj.getString("icon");
                            Picasso.get().load("http://openweathermap.org/img/wn/" + icon + "@2x.png").into(imageView);

                            //find date & time

                            SimpleDateFormat formatter = new SimpleDateFormat("HH:mm  \nE, MMM dd yyyy");
                            Date dateCurrent = new Date();
                            String date = formatter.format(dateCurrent);
                            time.setText(date);

                            //find latitude
                            JSONObject object2 = jsonObject.getJSONObject("coord");
                            double lat_find = object2.getDouble("lat");
                            latitude.setText(lat_find + "°  N");

                            //find longitude
                            JSONObject object3 = jsonObject.getJSONObject("coord");
                            double long_find = object3.getDouble("lon");
                            longitude.setText(long_find + "°  E");

                            //find humidity
                            JSONObject object4 = jsonObject.getJSONObject("main");
                            int humidity_find = object4.getInt("humidity");
                            humidity.setText(humidity_find + "  %");

                            //find sunrise
                            JSONObject object5 = jsonObject.getJSONObject("sys");

                            String sunrise_find = object5.getString("sunrise");
                            String formattedSunrise = null;
                            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                                //instant = Instant.ofEpochSecond(Long.parseLong(sunrise_find));

                                LocalDateTime dateTime = LocalDateTime.ofEpochSecond(Long.parseLong(sunrise_find), 0, ZoneOffset.UTC);
                                    /*Log.d("Locale.forgeTag(count)",Locale.forLanguageTag(count).toString());
                                    Log.d("Locale.forgeTag(count)",count.toString());
                                    Log.d("Locale.forgeTag(count)", String.valueOf(Locale.US));*/
                                DateTimeFormatter formatte = DateTimeFormatter.ofPattern("HH:mm");
                                formattedSunrise = dateTime.format(formatte);
                                sunrise.setText(formattedSunrise);
                            }
                            // Date sunris = new Date(Long.parseLong(sunrise_find));


                            //find sunrise
                            JSONObject object6 = jsonObject.getJSONObject("sys");
                            String sunset_find = object6.getString("sunset");
                            String formattedSunset = null;
                            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                                LocalDateTime dateTime = LocalDateTime.ofEpochSecond(Long.parseLong(sunset_find), 0, ZoneOffset.UTC);
                                DateTimeFormatter formatte = DateTimeFormatter.ofPattern("HH:mm");
                                formattedSunset = dateTime.format(formatte);
                                sunset.setText(formattedSunset);
                            }


                            //find pressure
                            JSONObject object7 = jsonObject.getJSONObject("main");
                            String pressure_find = object7.getString("pressure");
                            pressure.setText(pressure_find + "  hPa");

                            //find wind speed
                            JSONObject object9 = jsonObject.getJSONObject("wind");
                            String wind_find = object9.getString("speed");
                            wind.setText(wind_find + "  km/h");

                            //find min temperature
                            JSONObject object10 = jsonObject.getJSONObject("main");
                            double mintemp = object10.getDouble("temp_min");
                            min_temp.setText("Min Temp\n" + mintemp + " °C");

                            //find max temperature
                            JSONObject object12 = jsonObject.getJSONObject("main");
                            double maxtemp = object12.getDouble("temp_max");
                            max_temp.setText("Max Temp\n" + maxtemp + " °C");

                            //find feels
                            JSONObject object13 = jsonObject.getJSONObject("main");
                            double feels_find = object13.getDouble("feels_like");
                            feels.setText(feels_find + " °C");

                            //find description
                            JSONArray jsonArray1 = jsonObject.getJSONArray("weather");
                            JSONObject obj1 = jsonArray1.getJSONObject(0);
                            String desc = obj1.getString("description");

                            savePref(String.valueOf(temp), count, city, icon, date, String.valueOf(lat_find), String.valueOf(long_find), wind_find, humidity_find,
                                    pressure_find, String.valueOf(mintemp), String.valueOf(maxtemp), String.valueOf(feels_find), formattedSunrise, formattedSunset, desc);


                            //mettre à jour le widget
                            updateWidget(count, city,date , desc, temp, wind_find, pressure_find, humidity_find, icon);




                        } catch (JSONException e) {

                            e.printStackTrace();
                        }

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (!isNetworkAvailable(getApplicationContext())) {
                    Toast.makeText(getApplicationContext(), "Il n'y a pas de connexion, veuillez l'activer!!!", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(getApplicationContext(), "Essayez une autre ville !!!", Toast.LENGTH_LONG).show();
                }
                // Toast.makeText(MainActivity.this,error.getLocalizedMessage(),Toast.LENGTH_SHORT).show();
            }
        });

        RequestQueue requestQueue = Volley.newRequestQueue(MainActivity.this);
        requestQueue.add(stringRequest);

    }


    private void located(Location location) {
        double lat = location.getLatitude();
        double lng = location.getLongitude();

        if (!Geocoder.isPresent()) {
            Toast.makeText(getApplicationContext(), "Les coordonnées sont indisponibles", Toast.LENGTH_LONG).show();
            return;
        }

        Geocoder geocoder = new Geocoder(this);

        try {
            List<Address> addressList = geocoder.getFromLocation(lat, lng, ADDRESSES);
            //  String cityName = addressList.get(0).getAddressLine(0);
            //   String stateName = addressList.get(0).getAddressLine(1);
            // String countryName = addressList.get(0).getAddressLine(2);
            // Toast.makeText(getApplicationContext(), String.valueOf(addressList.get(0).getAdminArea()),Toast.LENGTH_LONG).show();
            //  Toast.makeText(getApplicationContext(), addressList.get(0).getAdminArea(),Toast.LENGTH_LONG).show();

            if (addressList == null) {
                Toast.makeText(getApplicationContext(), "Les coordonnées sont indisponibles", Toast.LENGTH_LONG).show();
                return;
            }


            city = addressList.get(0).getAdminArea();
            // Toast.makeText(getApplicationContext(), city,Toast.LENGTH_LONG).show();


        } catch (Exception e) {

            e.printStackTrace();
        }
    }


    public void savePref(String temp, String country, String cityName, String icon, String date,
                         String lat_find, String long_find, String wind_find, int humidity_find,
                         String pressure_find, String mintemp, String maxtemp, String feels_find,
                         String sunrise, String sunset, String desc) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
        SharedPreferences.Editor editor = preferences.edit();

        editor.putString(WWPREF_TEMP, temp);
        editor.putString(WWPREF_COUNTRY, country);
        editor.putString(WWPREF_CITY, cityName);
        editor.putString(WWPREF_ICON, icon);
        editor.putString(WWPREF_DATE, date);
        editor.putString(WWPREF_LAT, lat_find);
        //editor.putString(WWPREF_LAT, "koussay");
        Log.d("lat_find          :", "             " + lat_find);
        editor.putString(WWPREF_LNG, long_find);
        editor.putString(WWPREF_HUMD, String.valueOf(humidity_find));
        editor.putString(WWPREF_PRSS, pressure_find);
        editor.putString(WWPREF_WIND, wind_find);
        editor.putString(WWPREF_MINT, mintemp);
        editor.putString(WWPREF_MAXT, maxtemp);
        editor.putString(WWPREF_FEEL, feels_find);
        editor.putString(WWPREF_SUNR, sunrise);
        editor.putString(WWPREF_SUNS, sunset);

        editor.putString(WWPREF_DESC, desc);


        editor.apply();
    }

    public void getPref() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
        if (preferences.contains(MainActivity.WWPREF_TEMP)) {

            temptv.setText("Temperature\n" + preferences.getString(MainActivity.WWPREF_TEMP, "") + "°C");
            country.setText(preferences.getString(MainActivity.WWPREF_COUNTRY, "") + "  :");
            city_nam.setText(preferences.getString(MainActivity.WWPREF_CITY, ""));
            Picasso.get().load("http://openweathermap.org/img/wn/" + preferences.getString(MainActivity.WWPREF_ICON, "") + "@2x.png").into(imageView);
            time.setText(preferences.getString(MainActivity.WWPREF_DATE, ""));
            latitude.setText(preferences.getString(MainActivity.WWPREF_LAT, "") + "°  N");
            Log.d("MainAWWPREF_LAT:", "             " + preferences.getString(WWPREF_LAT, ""));
            longitude.setText(preferences.getString(MainActivity.WWPREF_LNG, "") + "°  E");
            humidity.setText(preferences.getString(MainActivity.WWPREF_HUMD, "") + "  %");
            pressure.setText(preferences.getString(MainActivity.WWPREF_PRSS, "") + "  hPa");
            wind.setText(preferences.getString(MainActivity.WWPREF_WIND, "") + "  km/h");
            min_temp.setText("Min Temp\n" + preferences.getString(MainActivity.WWPREF_MINT, "") + " °C");
            max_temp.setText("Max Temp\n" + preferences.getString(MainActivity.WWPREF_MAXT, "") + " °C");
            feels.setText(preferences.getString(MainActivity.WWPREF_FEEL, "") + " °C");
            sunrise.setText(preferences.getString(MainActivity.WWPREF_SUNR, ""));
            sunset.setText(preferences.getString(MainActivity.WWPREF_SUNS, ""));

        }
    }

    // Vérifie si le terminal est connecté à Internet
    private boolean isNetworkAvailable(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnected();
    }

    public void updateWidget(String count, String city, String date, String desc, Double temp, String wind_find, String pressure_find, int humidity_find, String icon) {
        // Créer une instance de AppWidgetManager
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(getApplicationContext());
        // Récupérer l'ID de votre widget
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(getApplicationContext(), WeatherWidget.class));
        // Créer une instance de RemoteViews avec les mises à jour que vous souhaitez appliquer au widget
        RemoteViews views = new RemoteViews(getPackageName(), R.layout.widget_layout);
        // views.setTextViewText(R.id.location, "Nouveau");
        views.setTextViewText(R.id.location, count + ", " + city);
        views.setTextViewText(R.id.date, date);
        views.setTextViewText(R.id.description, desc);
        views.setTextViewText(R.id.centigrade, temp + " °C");
        views.setTextViewText(R.id.wind, wind_find + " km/h");
        views.setTextViewText(R.id.pressure, "Pressure :" + pressure_find + " Pa");
        views.setTextViewText(R.id.humidity, "Humidity :" + humidity_find + " %");
        RemoteViewsTarget target = new RemoteViewsTarget(getApplicationContext(), views, R.id.weather, appWidgetIds);
        Picasso.get().load("http://openweathermap.org/img/wn/" + icon + "@2x.png").into(target);

        // Mettre à jour le widget en appelant la méthode updateAppWidget() avec l'ID du widget et l'instance de RemoteViews
        appWidgetManager.updateAppWidget(appWidgetIds, views);
    }

    private void refresh() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
        if (isNetworkAvailable(getApplicationContext())&&preferences.contains(MainActivity.WWPREF_CITY))
        {
            FindWeather(preferences.getString(MainActivity.WWPREF_CITY,""));
        }
    }
    private void refreshDataAfterSwipe() {
        refresh();
        // Indiquez que le SwipeRefreshLayout est terminé de se rafraîchir
        SwipeRefreshLayout swipeRefreshLayout = findViewById(R.id.swipe_refresh_layout);
        swipeRefreshLayout.setRefreshing(false);
    }


}