package com.example.shubhangi.citytracker;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import java.util.List;
import java.util.Locale;

/**
 * Created by shubhangi on 14-06-2017.
 */

public class GetAddress {
    Context context;
    public void getAddressLocation(final double lat, final double lng, final Context context, final Handler handler) {
        Thread thread = new Thread() {
            public void run() {
                String result="";
                String city=" ";
                Geocoder geocoder = new Geocoder(context, Locale.getDefault());
                try {
                    List<Address> addressList = geocoder.getFromLocation(lat,lng, 1);
                    if (addressList != null && addressList.size() > 0) {
                        Address address = addressList.get(0);
                        city=address.getLocality();
                        StringBuilder sb = new StringBuilder();
                        for (int i = 0; i < address.getMaxAddressLineIndex(); i++) {
                            sb.append(address.getAddressLine(i)).append("\n");
                        }
                        result=sb.toString();
                    }
                }
                catch (Exception e){
                    e.printStackTrace();
                }
                finally {
                    Message message = Message.obtain();
                    message.setTarget(handler);
                    if (result != "") {
                        message.what = 1;
                        Bundle bundle = new Bundle();
                        bundle.putString("cityn",city);
                        bundle.putString("address", result);
                        message.setData(bundle);
                }

                    message.sendToTarget();
                    }
                }
        };
        thread.start();
    }
}
