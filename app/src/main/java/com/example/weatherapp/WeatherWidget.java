package com.example.weatherapp;


import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;
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

public class WeatherWidget extends AppWidgetProvider {




   // onAppWidgetOptionsChanged
   @Override
   public void onAppWidgetOptionsChanged(Context context, AppWidgetManager appWidgetManager, int appWidgetId, Bundle newOptions)
   {
       int appWidgetIds[] = {appWidgetId};
       onUpdate(context, appWidgetManager, appWidgetIds);
   }
    @Override
    @SuppressLint("InlinedApi")
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // Create an Intent to launch Weather

        Intent intent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        // Create an Intent to update Weather widget
        Intent updateIntent = new Intent(context, WeatherWidget.class);
        updateIntent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        updateIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS,appWidgetIds);
        PendingIntent pendingUpdate = PendingIntent.getBroadcast(context, 0, updateIntent,PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);


        // Get the layout for the widget and attach an on-click listener to the view.
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_layout);
        views.setOnClickPendingIntent(R.id.widget, pendingIntent);
        views.setOnClickPendingIntent(R.id.update, pendingUpdate);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);

        if(isNetworkAvailable(context)&&preferences.contains(MainActivity.WWPREF_CITY)){
            FindWeather(preferences.getString(MainActivity.WWPREF_CITY, ""), views, context, appWidgetIds);
        }

       else if (preferences.contains(MainActivity.WWPREF_TEMP))
        {
            views.setTextViewText(R.id.location,preferences.getString(MainActivity.WWPREF_COUNTRY, "")+", "+preferences.getString(MainActivity.WWPREF_CITY, ""));
            views.setTextViewText(R.id.date, preferences.getString(MainActivity.WWPREF_DATE, ""));
            views.setTextViewText(R.id.description,preferences.getString(MainActivity.WWPREF_DESC, ""));
            views.setTextViewText(R.id.centigrade,preferences.getString(MainActivity.WWPREF_TEMP, "")+" °C");
            views.setTextViewText(R.id.wind,preferences.getString(MainActivity.WWPREF_WIND, "")+" km/h");
            views.setTextViewText(R.id.pressure,"Pressure :"+preferences.getString(MainActivity.WWPREF_PRSS, "")+" Pa");
            views.setTextViewText(R.id.humidity, "Humidity :"+preferences.getString(MainActivity.WWPREF_HUMD, "")+" %");


            RemoteViewsTarget target = new RemoteViewsTarget(context, views, R.id.weather, appWidgetIds);
            Picasso.get().load("http://openweathermap.org/img/wn/" + preferences.getString(MainActivity.WWPREF_ICON, "") + "@2x.png").into(target);



        }



        /*views.setViewVisibility(R.id.progress, View.VISIBLE);
        views.setViewVisibility(R.id.update, View.INVISIBLE);*/
        views.setViewVisibility(R.id.progress, View.INVISIBLE);
        views.setViewVisibility(R.id.update, View.VISIBLE);

        appWidgetManager.updateAppWidget(appWidgetIds, views);


    }

    private void FindWeather(String city, RemoteViews views, Context context, int[] appWidgetIds) {



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


                            //find country
                            JSONObject object8 = jsonObject.getJSONObject("sys");
                            String count = object8.getString("country");


                            //find city
                            String city = jsonObject.getString("name");


                            //find icon
                            JSONArray jsonArray = jsonObject.getJSONArray("weather");
                            JSONObject obj = jsonArray.getJSONObject(0);
                            String icon = obj.getString("icon");

                            //find date & time

                            SimpleDateFormat formatter = new SimpleDateFormat("HH:mm  \nE, MMM dd yyyy");
                            Date dateCurrent = new Date();
                            String date = formatter.format(dateCurrent);


                            //find latitude
                            JSONObject object2 = jsonObject.getJSONObject("coord");
                            double lat_find = object2.getDouble("lat");


                            //find longitude
                            JSONObject object3 = jsonObject.getJSONObject("coord");
                            double long_find = object3.getDouble("lon");


                            //find humidity
                            JSONObject object4 = jsonObject.getJSONObject("main");
                            int humidity_find = object4.getInt("humidity");


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

                            }
                            // Date sunris = new Date(Long.parseLong(sunrise_find));


                            //find sunrise
                            JSONObject object6 = jsonObject.getJSONObject("sys");
                            String sunset_find = object6.getString("sunset");
                            String formattedSunset = null;
                            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                                LocalDateTime dateTime = LocalDateTime.ofEpochSecond(Long.parseLong(sunset_find), 0, ZoneOffset.UTC);
                                DateTimeFormatter formatte = DateTimeFormatter.ofPattern("HH:mm");
                                formattedSunset= dateTime.format(formatte);

                            }


                            //find pressure
                            JSONObject object7 = jsonObject.getJSONObject("main");
                            String pressure_find = object7.getString("pressure");


                            //find wind speed
                            JSONObject object9 = jsonObject.getJSONObject("wind");
                            String wind_find = object9.getString("speed");


                            //find min temperature
                            JSONObject object10 = jsonObject.getJSONObject("main");
                            double mintemp = object10.getDouble("temp_min");


                            //find max temperature
                            JSONObject object12 = jsonObject.getJSONObject("main");
                            double maxtemp = object12.getDouble("temp_max");


                            //find feels
                            JSONObject object13 = jsonObject.getJSONObject("main");
                            double feels_find = object13.getDouble("feels_like");


                            //find description
                            JSONArray jsonArray1 = jsonObject.getJSONArray("weather");
                            JSONObject obj1 = jsonArray1.getJSONObject(0);
                            String desc = obj1.getString("description");

                            views.setTextViewText(R.id.location,count+", "+city);
                            views.setTextViewText(R.id.date, date);
                            views.setTextViewText(R.id.description,desc);
                            views.setTextViewText(R.id.centigrade,String.valueOf(temp)+" °C");
                            views.setTextViewText(R.id.wind,wind_find+" km/h");
                            views.setTextViewText(R.id.pressure,"Pressure :"+pressure_find+" Pa");
                            views.setTextViewText(R.id.humidity, "Humidity :"+humidity_find+" %");

                            //
                           // views.setImageViewUri(R.id.weather, Uri.parse("http://openweathermap.org/img/wn/01d@2x.png"));
                            RemoteViewsTarget target = new RemoteViewsTarget(context, views, R.id.weather, appWidgetIds);
                            Picasso.get().load("http://openweathermap.org/img/wn/" + icon + "@2x.png").into(target);


                            savePref(String.valueOf(temp), count, city, icon, date, String.valueOf(lat_find), String.valueOf(long_find), wind_find,humidity_find,
                                    pressure_find, String.valueOf(mintemp), String.valueOf(maxtemp), String.valueOf(feels_find),formattedSunrise,formattedSunset,desc,context);



                        } catch (JSONException e) {

                            e.printStackTrace();
                        }

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(context.getApplicationContext(), "Essayez une autre ville !!!",Toast.LENGTH_LONG).show();

                // Toast.makeText(MainActivity.this,error.getLocalizedMessage(),Toast.LENGTH_SHORT).show();
            }
        });

        RequestQueue requestQueue = Volley.newRequestQueue(context.getApplicationContext());
        requestQueue.add(stringRequest);

    }

    public void savePref(String temp, String country, String cityName, String icon, String date,
                         String lat_find, String long_find, String wind_find, int humidity_find,
                         String pressure_find, String mintemp, String maxtemp, String feels_find,
                         String sunrise, String sunset, String desc,Context context){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();

        editor.putString(MainActivity.WWPREF_TEMP, temp);
        editor.putString(MainActivity.WWPREF_COUNTRY, country);
        editor.putString(MainActivity.WWPREF_CITY, cityName);
        editor.putString(MainActivity.WWPREF_ICON, icon);
        editor.putString(MainActivity.WWPREF_DATE, date);
        editor.putString(MainActivity.WWPREF_LAT, lat_find);
        //editor.putString(WWPREF_LAT, "koussay");
        Log.d("lat_find          :","             "+lat_find);
        editor.putString(MainActivity.WWPREF_LNG, long_find);
        editor.putString(MainActivity.WWPREF_HUMD, String.valueOf(humidity_find));
        editor.putString(MainActivity.WWPREF_PRSS, pressure_find);
        editor.putString(MainActivity.WWPREF_WIND, wind_find);
        editor.putString(MainActivity.WWPREF_MINT, mintemp);
        editor.putString(MainActivity.WWPREF_MAXT, maxtemp);
        editor.putString(MainActivity.WWPREF_FEEL, feels_find);
        editor.putString(MainActivity.WWPREF_SUNR, sunrise);
        editor.putString(MainActivity.WWPREF_SUNS, sunset);

        editor.putString(MainActivity.WWPREF_DESC, desc);



        editor.apply();
    }







    // Vérifie si le terminal est connecté à Internet
    private boolean isNetworkAvailable(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnected();
    }



}
