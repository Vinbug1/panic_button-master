package com.example.ubfac.panicbutton.views;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ubfac.panicbutton.R;
import com.example.ubfac.panicbutton.model.PollingUnit;


/**
 * Created by vincent on 11/1/18.
 */
public class CustomInfoWindowGoogleMap implements GoogleMap.InfoWindowAdapter {

    private Context context;

    public CustomInfoWindowGoogleMap(Context ctx) {
        context = ctx;
    }

    @Override
    public View getInfoWindow(Marker marker) {
        return null;
    }

    @Override
    public View getInfoContents(Marker marker) {
        View view = ((Activity) context).getLayoutInflater()
                .inflate(R.layout.maps_info_window, null);

        /*TextView infoTv = view.findViewById(R.id.textView_info);
        ImageView directions = view.findViewById(R.id.imageView_directions);
        ImageView report = view.findViewById(R.id.imageView_report);*/

        TextView name_tv = view.findViewById(R.id.name);
        TextView directions = view.findViewById(R.id.name);
        TextView report = view.findViewById(R.id.name);

        final PollingUnit infoWindowData = (PollingUnit) marker.getTag();
        name_tv.setText(Jsoup.parse(infoWindowData.getDescription()).text());

        directions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Create a Uri from an intent string. Use the result to create an Intent.
                Uri gmmIntentUri = Uri.parse("google.navigation:q=" + infoWindowData.getLatitude() + "," + infoWindowData.getLongitude());

                // Create an Intent from gmmIntentUri. Set the action to ACTION_VIEW
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                // Make the Intent explicit by setting the Google Maps package
                mapIntent.setPackage("com.google.android.apps.maps");

                // Attempt to start an activity that can handle the Intent
                context.startActivity(mapIntent);
            }
        });

        directions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(context, "hello", Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }
}