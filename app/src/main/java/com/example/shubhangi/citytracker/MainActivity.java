package com.example.shubhangi.citytracker;

import android.Manifest;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.analytics.FirebaseAnalytics;

public class MainActivity extends AppCompatActivity implements LocationListener {
    private LocationManager locationManager;
    Location location;
    private static final int MY_PERMISSION_REQUEST_CODE = 7171;
    private double lat, lng;
    String cityname;
    TextView addr;
    private static int flag=0;
    private FirebaseAnalytics mFirebaseAnalytics;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        View loadingIndicator = findViewById(R.id.loading_indicator);
        loadingIndicator.setVisibility(View.GONE);
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        addr = (TextView) findViewById(R.id.addrtxt);
        mFirebaseAnalytics=FirebaseAnalytics.getInstance(this);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSION_REQUEST_CODE);
        } else {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 200, 10, this);
            location=locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if(location!=null){
                lat=location.getLatitude();
                lng=location.getLongitude();
                Log.i("lon",lng+"value");
            }

        }
    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getlocation();
                    break;
                }
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        lat = location.getLatitude();
        lng = location.getLongitude();
        GetAddress ga=new GetAddress();
        Addhandler addhandler=new Addhandler();
        ga.getAddressLocation(lat,lng,getApplicationContext(),addhandler);
        if(cityname.equals("New Delhi")){
            if(flag==0) {
                Bundle event = new Bundle();
                event.putString("traveling_Delhi", cityname);
                mFirebaseAnalytics.logEvent("Dwell", event);
                flag=1;
            }
            else {
                if(flag==2){
                    Bundle evnt2=new Bundle();
                    evnt2.putString("enter_delhi",cityname);
                    mFirebaseAnalytics.logEvent("Enter",evnt2);
                    flag=0;
                    enternotify();
                }
            }
        }
        else {
            if(flag==1||flag==0) {
                Bundle event1 = new Bundle();
                event1.putString("not_delhi", cityname);
                mFirebaseAnalytics.logEvent("Exit", event1);
                flag=2;
            }
        }
    }
    @Override
    public void onStatusChanged(String provider, int status, Bundle
 extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {
        Toast.makeText(getBaseContext(), "Gps is turned on!! ",
                Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onProviderDisabled(String provider) {
        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        startActivity(intent);
        Toast.makeText(getBaseContext(), "Gps is turned off!! ",
                Toast.LENGTH_SHORT).show();

    }

    public void getlocation() {
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        lat=location.getLatitude();
        lng=location.getLongitude();
    }
    private void enternotify(){
        int mId=0;
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this).setSmallIcon(R.drawable.delhi_icon).setContentTitle("Entered Delhi city")
                .setContentText("You are in New Delhi now");
        Intent resultIntent = new Intent(this, MainActivity.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(MainActivity.class);
// Adds the Intent that starts the Activity to the top of the stack
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        mBuilder.setContentIntent(resultPendingIntent);
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(mId,mBuilder.build());
    }
    private class Addhandler extends Handler{
        public void handleMessage(Message message){
            String locationAddress;
            switch (message.what){
                case 1:
                    Bundle bundle=message.getData();
                    locationAddress=bundle.getString("address");
                    cityname=bundle.getString("cityn");
                    if(!cityname.equals("New Delhi")){
                        flag=2;
                    }
                    break;
                default:
                    locationAddress=null;
            }

            addr.setText(locationAddress);
        }
    }

}
