package com.kostanay.alex.pegasdriver.Controller;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Message;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;


import com.kostanay.alex.pegasdriver.Controller.MapScreen.MapFragment;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class Find extends DialogFragment {
    private final String USER_AGENT = "Mozilla/5.0";
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View v = LayoutInflater.from(getActivity()).inflate(com.kostanay.alex.pegasdriver.R.layout.dialog_find, null);
        final EditText find = v.findViewById(com.kostanay.alex.pegasdriver.R.id.find);
        return new AlertDialog.Builder(getActivity()).setView(v).setTitle("Найти место по адресу").setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                new Thread(new Runnable() {
                    public void run() {
                        try {
                            String url = "https://maps.googleapis.com/maps/api/geocode/json?address=" + find.getText().toString().replace(" ", "+") + "&key=AIzaSyA9QA7CRvUQc0Il8dsqlDYH_W23gosAXok&language=ru";
                            URL obj = new URL(url);
                            HttpURLConnection con = (HttpURLConnection) obj.openConnection();
                            con.setRequestMethod("GET");
                            con.setRequestProperty("User-Agent", USER_AGENT);
                            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
                            String inputLine;
                            StringBuffer response = new StringBuffer();
                            while ((inputLine = in.readLine()) != null) {
                                response.append(inputLine);
                            }
                            in.close();
                            JSONObject obj1 = new JSONObject(response.toString());
                            obj1 = (JSONObject) obj1.getJSONArray("results").get(0);
                            String lat = obj1.getJSONObject("geometry").getJSONObject("location").get("lat").toString();
                            String lng = obj1.getJSONObject("geometry").getJSONObject("location").get("lng").toString();
                            Message message = Message.obtain();
                            message.obj = lat + "," + lng;
                            message.setTarget(MapFragment.handler);
                            message.sendToTarget();
                        } catch (Exception e){
                            Message message = Message.obtain();
                            message.obj = "Не удалось определить координаты";
                            message.setTarget(MapFragment.handler);
                            message.sendToTarget();
                        }
                    }
                }).start();
            }
        }).create();
    }
}
