package com.kostanay.alex.pegasdriver.Controller;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.os.Message;
import android.support.v4.app.DialogFragment;

import com.kostanay.alex.pegasdriver.Controller.AuthorizationScreen.AuthorizationFragment;
import com.kostanay.alex.pegasdriver.Controller.MapScreen.MapFragment;
import com.kostanay.alex.pegasdriver.Model.NetWork;
import com.kostanay.alex.pegasdriver.Model.OrderRepository;
import com.kostanay.alex.pegasdriver.Services.MyService;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Locale;

public class Complete extends DialogFragment {
    private final String USER_AGENT = "Mozilla/5.0";
    private static final String ARG_DATE = "date";
    private com.kostanay.alex.pegasdriver.Model.Order Order;

    public static Complete newInstance(String orderNumber){
        Bundle args = new Bundle();
        args.putSerializable(ARG_DATE, orderNumber);

        Complete fragment = new Complete();
        fragment.setArguments(args);
        return fragment;
    }

    public Dialog onCreateDialog(Bundle savedInstanceState) {
        String orderNumber = (String) getArguments().getSerializable(ARG_DATE);
        Order = OrderRepository.get().getOrder(orderNumber);
        return new AlertDialog.Builder(getActivity()).setTitle("Завершить").setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                new Thread(new Runnable() {
                    public void run() {
                        NetWork.get().send("[comp~" + Order.getOrderNumber() + "~" + AuthorizationFragment.DriverId + "]");
                        try {
                            String locationAddress = Order.getToW();
                            if (!locationAddress.contains(",")) {
                                locationAddress = "КОСТАНАЙ," + locationAddress;
                            }

                            try {
                                String url = "https://maps.googleapis.com/maps/api/geocode/json?address=" + locationAddress.replace(" ", "+") + "&key=AIzaSyA9QA7CRvUQc0Il8dsqlDYH_W23gosAXok&language=ru";
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

                        } catch (Exception e) {
                        }
                    }
                }).start();
            }
        }).create();
    }
}
