package com.example.weatherapp;


import android.app.AlertDialog;
import android.app.Dialog;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.RemoteViews;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.DialogFragment;

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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public class WeatherDialogFragment extends DialogFragment {
    private ViewGroup dayGroup;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        // Créer une instance de la vue du fragment
        View fragmentView = LayoutInflater.from(getActivity()).inflate(R.layout.dialog, null);


        SharedPreferences preferences =
                PreferenceManager.getDefaultSharedPreferences(getContext());

        dayGroup = fragmentView.findViewById(R.id.days);
        TextView textView = fragmentView.findViewById(R.id.date);
        textView.setText(preferences.getString(MainActivity.WWPREF_DATE, ""));
        textView = fragmentView.findViewById(R.id.description);
        textView.setText(preferences.getString(MainActivity.WWPREF_DESC, ""));
        textView = fragmentView.findViewById(R.id.centigrade);
        textView.setText(preferences.getString(MainActivity.WWPREF_TEMP, "")+" °C");
        textView = fragmentView.findViewById(R.id.wind);
        textView.setText(preferences.getString(MainActivity.WWPREF_WIND, "")+" km/h");
        textView = fragmentView.findViewById(R.id.pressure);
        textView.setText("Pressure :"+preferences.getString(MainActivity.WWPREF_PRSS, "")+" Pa");
        textView = fragmentView.findViewById(R.id.humidity);
        textView.setText("humidity :"+preferences.getString(MainActivity.WWPREF_HUMD, "")+" %");

        ImageView image = (ImageView) fragmentView.findViewById(R.id.weather);;

        Picasso.get().load("http://openweathermap.org/img/wn/" + preferences.getString(MainActivity.WWPREF_ICON, "") + "@2x.png").into(image);


        if(isNetworkAvailable(getContext())&&preferences.contains(MainActivity.WWPREF_CITY)){
            FindForecast(preferences.getString(MainActivity.WWPREF_CITY, ""), getContext());
        }else{
            //verifier votre connexion
        }

        // Créer une nouvelle AlertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
       // builder.setTitle(preferences.getString(MainActivity.WWPREF_COUNTRY, "")+", "+preferences.getString(MainActivity.WWPREF_CITY, "")+" : Forecast of the following 5 days !!!");
        builder.setView(fragmentView);



       /* builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Code à exécuter lorsque l'utilisateur appuie sur le bouton OK
            }
        });
        builder.setNegativeButton("Annuler", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Code à exécuter lorsque l'utilisateur appuie sur le bouton Annuler
            }
        });*/


// Enlever les boutons
        builder.setPositiveButton(null, null);
        builder.setNegativeButton(null, null);

        // Enlever le titre
        builder.setTitle(null);

        // Créer et retourner l'instance de l'AlertDialog
        return builder.create();


    }
    private void FindForecast(String city, Context context) {



        String url = "http://api.openweathermap.org/data/2.5/forecast?q=" + city + "&appid=462f445106adc1d21494341838c10019&units=metric&lang=fr";
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        try {
                            JSONObject jsonObjectList = new JSONObject(response);

                            //find list
                            JSONArray jsonArray0 = jsonObjectList.getJSONArray("list");
                            for(int i=0;i<jsonArray0.length();i++){
                                JSONObject jsonObject = jsonArray0.getJSONObject(i);
                                //find dt
                                String dt = jsonObject.getString("dt_txt");


                                // Créer un objet SimpleDateFormat pour convertir la date
                                SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                                SimpleDateFormat outputFormat = new SimpleDateFormat("MMM dd, HH:mm");

                                try {
                                    // Convertir la date
                                    Date date = inputFormat.parse(dt);
                                     dt = outputFormat.format(date);

                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }

                            //find temperature

                            JSONObject object = jsonObject.getJSONObject("main");
                            String temp = object.getString("temp");
                            String temp_min = object.getString("temp_min");
                            String temp_max = object.getString("temp_max");


                            //find icon and desc
                            JSONArray jsonArray = jsonObject.getJSONArray("weather");
                            JSONObject obj = jsonArray.getJSONObject(0);
                            String icon = obj.getString("icon");
                            String desc = obj.getString("description");




                                    ViewGroup group = (ViewGroup) dayGroup.getChildAt(i);
                                    ViewGroup linearLayout = (ViewGroup) group.getChildAt(1);
                                    ViewGroup linearLayoutBis = (ViewGroup) linearLayout.getChildAt(0);
                                    ViewGroup frameLayout = (ViewGroup) linearLayout.getChildAt(1);
                                    ViewGroup linearLayoutFrame = (ViewGroup) frameLayout.getChildAt(0);

                                    TextView text = (TextView) linearLayoutBis.getChildAt(0);
                                    text.setText(dt);
                                    text = (TextView) linearLayoutBis.getChildAt(1);
                                    text.setText(desc);
                                    text = (TextView) linearLayoutFrame.getChildAt(0);
                                    text.setText(temp_max+" °C");
                                    text = (TextView) linearLayoutFrame.getChildAt(1);
                                    text.setText(temp_min+" °C");



                                    ImageView image = (ImageView) group.getChildAt(0);

                            Picasso.get().load("http://openweathermap.org/img/wn/" + icon + "@2x.png").into(image);







                        /*// Dans la classe MainActivity, après avoir mis à jour les données
                        // Créer un Intent pour envoyer les données mises à jour au widget
                        Intent intent = new Intent(MainActivity.this, WeatherWidget.class);
                        intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
                        // intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds);
                        sendBroadcast(intent);*/

                            }
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

    // Vérifie si le terminal est connecté à Internet
    private boolean isNetworkAvailable(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnected();
    }
}
