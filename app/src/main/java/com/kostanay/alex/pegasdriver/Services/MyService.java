package com.kostanay.alex.pegasdriver.Services;

import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.util.Log;

import com.kostanay.alex.pegasdriver.Controller.AuthorizationScreen.AuthorizationActivity;
import com.kostanay.alex.pegasdriver.Controller.AuthorizationScreen.AuthorizationFragment;
import com.kostanay.alex.pegasdriver.Controller.DetailedScreen.DetailedFragment;
import com.kostanay.alex.pegasdriver.Controller.DialogScreen.DialogActivity;
import com.kostanay.alex.pegasdriver.Controller.DialogScreen.DialogFragment;
import com.kostanay.alex.pegasdriver.Controller.Exit;
import com.kostanay.alex.pegasdriver.Controller.ExitActivity;
import com.kostanay.alex.pegasdriver.Controller.ListScreen.ListActivity;
import com.kostanay.alex.pegasdriver.Controller.ListScreen.ListFragment;
import com.kostanay.alex.pegasdriver.Controller.MapScreen.MapActivity;
import com.kostanay.alex.pegasdriver.Controller.MapScreen.MapFragment;
import com.kostanay.alex.pegasdriver.Controller.ReconnectionScreen.ReconnectionActivity;
import com.kostanay.alex.pegasdriver.Controller.ReconnectionScreen.ReconnectionFragment;
import com.kostanay.alex.pegasdriver.Controller.Refuse;
import com.kostanay.alex.pegasdriver.Model.BeatBox;
import com.kostanay.alex.pegasdriver.Model.NetWork;
import com.kostanay.alex.pegasdriver.Model.Order;
import com.kostanay.alex.pegasdriver.Model.OrderRepository;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;

public class MyService extends Service {
    public static String Balance = "0";
    public static boolean status;
    public static boolean alreadyGo = false;

    public static com.kostanay.alex.pegasdriver.Model.BeatBox BeatBox;
    public static com.kostanay.alex.pegasdriver.Model.Sound Sound1, Sound2, Sound3, Sound4;

    public static final String COMMAND_DELIMITER = "\\~";
    public static final String ELEMENT_DELIMITER = "\\$";
    public static final String TOKEN_DELIMITER = "\\|";

    public static final String COMMAND_GET = "get";
    public static final String COMMAND_NEW = "new";
    public static final String COMMAND_UPDATE = "upd";
    public static final String COMMAND_DELETE = "del";
    public static final String COMMAND_GO = "go";
    public static final String COMMAND_GET_OFFERED_PRICES = "getoff";
    public static final String COMMAND_AUTH = "auth";
    public static final String COMMAND_REGISTRATION = "reg";
    public static final String COMMAND_EXIT = "exit";
    public static final String COMMAND_ACCEPT = "acc";

    public static int SOUND_NEW;
    public static int SOUND_DISCONNECT;
    public static int SOUND_GO;
    public static int SOUND_RECONNECTED;

    private boolean pingReceived;

    public void onCreate() {
        super.onCreate();
        SharedPreferences pref = getApplicationContext().getSharedPreferences("MyPref", MODE_PRIVATE);
        SOUND_NEW = Integer.parseInt(pref.getString("sound_new", "0"));
        SOUND_DISCONNECT = Integer.parseInt(pref.getString("sound_disconnect", "1"));
        SOUND_RECONNECTED = Integer.parseInt(pref.getString("sound_reconnected", "2"));
        SOUND_GO = Integer.parseInt(pref.getString("sound_go", "3"));

        BeatBox = new BeatBox(this);
        Sound1 = BeatBox.getSounds().get(SOUND_NEW);
        Sound2 = BeatBox.getSounds().get(SOUND_DISCONNECT);
        Sound3 = BeatBox.getSounds().get(SOUND_RECONNECTED);
        Sound4 = BeatBox.getSounds().get(SOUND_GO);
        LocationManager locationManager = (LocationManager) getApplication().getSystemService(Context.LOCATION_SERVICE);
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            if (ActivityCompat.checkSelfPermission(getApplication(), Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission
                    (getApplication(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            } else {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 500, 100, new LocationListener() {
                    public void onLocationChanged(Location location) {
                        String latitude,longitude;
                        latitude = String.valueOf(location.getLatitude());
                        longitude = String.valueOf(location.getLongitude());
                        NetWork.get().send("[geo~"+ AuthorizationFragment.DriverId+"|"+latitude+", "+longitude+"~" + AuthorizationFragment.DriverId + "]");
                    }
                    public void onStatusChanged(String s, int i, Bundle bundle) {}
                    public void onProviderEnabled(String s) {}
                    public void onProviderDisabled(String s) {}
                });
            }
        }
    }

    public int onStartCommand(final Intent intent, int flags, int startId) {

        new Thread(new Runnable() {
            public void run() {
                try(InputStream in = NetWork.get().getSocket().getInputStream()){
                    byte[] data = new byte[32 * 1024];
                    int readBytes;
                    String message;
                    while (!NetWork.get().getSocket().isClosed()){
                        readBytes = in.read(data);
                        message = new String(data, 0, readBytes);
                        adapt(message);
                    }
                } catch (Exception e){
                    NetWork.get().close();
                    NetWork.get().reconnect();
                }
            }
        }).start();

        new Thread(new Runnable() {
            public void run() {
                pingReceived = true;
                while (pingReceived){
                    pingReceived = false;
                    try{Thread.sleep(20000);} catch (Exception e){}
                }
                NetWork.get().close();
            }
        }).start();

        return START_NOT_STICKY;
    }

    private void adapt(String message){
        if(!ExitActivity.active) {
            String[] messages = message.split("\\]");
            for (String str : messages) {
                str = str.substring(1);
                identify(str);
            }
            if(!message.contains("version")) {
                NetWork.get().clear();
            }
            pingReceived = true;
        }
    }

    private void identify(String message) {
        try {
            String command = message.split(COMMAND_DELIMITER)[0];
            String command_text = message.split(COMMAND_DELIMITER)[1];
            switch (command) {
                case COMMAND_AUTH:
                    switch (command_text.split(TOKEN_DELIMITER)[0]) {
                        case "ALREADY":
                            if(AuthorizationFragment.active) {
                                ListFragment.active = true;
                                Intent Intent = new Intent(MyService.this, ListActivity.class);
                                Intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(Intent);
                            }
                            try{AuthorizationFragment.handler.sendEmptyMessage(7);} catch (Exception e){}
                            NetWork.get().sendMessages();
                            break;
                        case "OK":
                            if(AuthorizationFragment.active) {
                                AuthorizationFragment.handler.sendEmptyMessage(4);
                            } else {
                                Intent Intent = new Intent(MyService.this, MapActivity.class);
                                Intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                Intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                                Intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                Intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(Intent);
                            }
                            break;
                        case "ADDED":
                            MapFragment.active = true;
                            try{AuthorizationFragment.handler.sendEmptyMessage(7);} catch (Exception e){}
                            break;
                        case "NO_BALANCE":
                            if(AuthorizationFragment.active) {
                                AuthorizationFragment.handler.sendEmptyMessage(2);
                            } else {
                                Intent Intent = new Intent(MyService.this, MapActivity.class);
                                Intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                Intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                                Intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                Intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(Intent);
                            }
                            NetWork.get().exit();
                            break;
                        case "NO_REG":
                            AuthorizationFragment.handler.sendEmptyMessage(3);
                            NetWork.get().exit();
                            break;
                    }
                    break;
                case COMMAND_GET_OFFERED_PRICES:
                    if (!command_text.equals("NULL")) {
                        String[] orderPrices = command_text.split(TOKEN_DELIMITER);
                        for (String string : orderPrices) {
                            String elements[] = string.split(ELEMENT_DELIMITER);
                            Order order = OrderRepository.get().getOrder(elements[1]);
                            order.setOfferedPrice(elements[2]);
                            order.setTime(elements[3]);
                            order.setPriceOffered(true);
                        }
                    }
                    break;
////////////////////////////////////////////////////////////////////////////////////////////////////
                case COMMAND_NEW:
                    String orderNumber = command_text.split(ELEMENT_DELIMITER)[0];
                    if (OrderRepository.get().getOrder(orderNumber) == null) {
                        Order order = new Order();
                        order.setOrderNumber(command_text.split(ELEMENT_DELIMITER)[0]);
                        order.setFromW(command_text.split(ELEMENT_DELIMITER)[1]);
                        if (order.getFromW().contains("КОСТАНАЙ")) {
                            order.setFromW(order.getFromW().split(",")[1]);
                        }
                        order.setToW(command_text.split(ELEMENT_DELIMITER)[2]);
                        order.setPrice(command_text.split(ELEMENT_DELIMITER)[3]);
                        order.setPhone(command_text.split(ELEMENT_DELIMITER)[4]);
                        order.setDescription(command_text.split(ELEMENT_DELIMITER)[5]);
                        order.setHide();
                        OrderRepository.get().addOrder(order);

                        if (ListFragment.active) {
                            ListFragment.handler.sendEmptyMessage(7);
                            BeatBox.play(Sound1);
                        } else if (DialogFragment.active) {

                        } else if (DetailedFragment.active) {
                            BeatBox.play(Sound1);
                        } else if (MapFragment.active) {
                            BeatBox.play(Sound1);
                        } else {
                            try {
                                DetailedFragment.handler.sendEmptyMessage(4);
                            } catch (Exception e) {
                            }
                            try {
                                ListFragment.handler.sendEmptyMessage(4);
                            } catch (Exception e) {
                            }
                            Intent Intent = new Intent(MyService.this, ListActivity.class);
                            Intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(Intent);
                            BeatBox.play(Sound1);
                        }
                    }
                    break;
                case COMMAND_UPDATE:
                    orderNumber = command_text.split(TOKEN_DELIMITER)[0];
                    String orderInfo = command_text.split(TOKEN_DELIMITER)[1];
                    String orderElement[] = orderInfo.split(ELEMENT_DELIMITER);
                    Order order = OrderRepository.get().getOrder(orderNumber);
                    order.setFromW(command_text.split(ELEMENT_DELIMITER)[0]);
                    if (order.getFromW().contains("КОСТАНАЙ")) {
                        order.setFromW(order.getFromW().split(",")[1]);
                    }
                    order.setToW(orderElement[1]);
                    order.setPrice(orderElement[2]);
                    order.setOfferedPrice(orderElement[2]);
                    order.setPhone(orderElement[3]);
                    order.setDescription(orderElement[4]);
                    if (ListFragment.active) {
                        ListFragment.handler.sendEmptyMessage(7);
                    } else if (DetailedFragment.active) {
                        DetailedFragment.handler.sendEmptyMessage(3);
                    }
                    try {
                        DialogFragment.handler.sendEmptyMessage(5);
                    } catch (Exception e) {
                    }
                    break;
                case COMMAND_DELETE:
                    OrderRepository.get().removeOrder(command_text);
                    if (ListFragment.active) {
                        ListFragment.handler.sendEmptyMessage(7);
                    }
                    if (DialogFragment.active) {
                        try {
                            Message msg = Message.obtain();
                            msg.obj = command_text;
                            msg.setTarget(DialogFragment.handler);
                            msg.sendToTarget();
                        } catch (Exception e) {
                        }
                    }
                    break;
                case COMMAND_GO:
                    if (OrderRepository.get().getOrder(command_text) != null) {
                        if (!DialogFragment.active) {
                            if (!alreadyGo) {
                                Intent Intent = DialogActivity.newIntent(MyService.this, command_text);
                                Intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(Intent);
                                alreadyGo = true;
                            }
                        }
                    }
                    break;
////////////////////////////////////////////////////////////////////////////////////////////////////
                case COMMAND_ACCEPT:
                    DialogFragment.handler.sendEmptyMessage(4);
                    break;
                case "bal":
                    Balance = command_text;
                    break;
                case "takeoff":
                    try{DialogFragment.handler.sendEmptyMessage(9);} catch (Exception e){}
                    break;
                case "getbal":
                    Balance = command_text;
                    try {
                        DetailedFragment.handler.sendEmptyMessage(5);
                    } catch (Exception e) {
                    }
                    try {
                        ListFragment.handler.sendEmptyMessage(5);
                    } catch (Exception e) {
                    }
                    break;
////////////////////////////////////////////////////////////////////////////////////////////////////
                case COMMAND_EXIT:
                    ExitActivity.balance = command_text;
                    NetWork.get().exit();
                    Intent Intent = new Intent(MyService.this, ExitActivity.class);
                    Intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    Intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    Intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    Intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(Intent);
                    break;
                case "trp":
                    DialogFragment.handler.sendEmptyMessage(3);
                    break;
                case "comp":
                    OrderRepository.get().removeOrder(command_text);
                    try {
                        DetailedFragment.handler.sendEmptyMessage(4);
                    } catch (Exception e) {
                    }
                    try {
                        ListFragment.handler.sendEmptyMessage(4);
                    } catch (Exception e) {
                    }
                    try {
                        DialogFragment.handler.sendEmptyMessage(7);
                    } catch (Exception e) {
                    }
                    try {
                        AuthorizationFragment.handler.sendEmptyMessage(7);
                    } catch (Exception e) {
                    }
                    DialogFragment.active = false;
                    MyService.alreadyGo = false;
                    break;
                case "ref":
                    Order Order = OrderRepository.get().getOrder(command_text);
                    Order.setPriceOffered(false);
                    Order.setOfferedPrice(null);
                    try {
                        DetailedFragment.handler.sendEmptyMessage(4);
                    } catch (Exception e) {
                    }
                    try {
                        ListFragment.handler.sendEmptyMessage(4);
                    } catch (Exception e) {
                    }
                    try {
                        DialogFragment.handler.sendEmptyMessage(7);
                    } catch (Exception e) {
                    }
                    try {
                        AuthorizationFragment.handler.sendEmptyMessage(7);
                    } catch (Exception e) {
                    }
                    DialogFragment.active = false;
                    MyService.alreadyGo = false;
                    break;
                case "sts":
                    switch (command_text) {
                        case "1":
                            status = true;
                            break;
                        case "2":
                            status = false;
                            break;
                    }
                    break;
                case "sw":
                    int c;
                    if (command_text.equals("1")) {
                        c = 9;
                        status = true;
                    } else {
                        c = 10;
                        status = false;
                    }
                    try {
                        DetailedFragment.handler.sendEmptyMessage(c);
                        ListFragment.handler.sendEmptyMessage(c);
                    } catch (Exception e) {
                        ListFragment.handler.sendEmptyMessage(c);
                    }
                    break;
                case "getcall":
                    SharedPreferences pref = getApplicationContext().getSharedPreferences("MyPref", MODE_PRIVATE);
                    SharedPreferences.Editor editor = pref.edit();
                    editor.putString("callsign", command_text);
                    editor.commit();
                    break;
                case "ping":

                        String orderList = "";
                        for (Order order1 : OrderRepository.get().getOrders()) {
                            orderList = orderList + order1.getOrderNumber() + "$";
                        }
                        if (orderList.equals("")) {
                            orderList = "NULL";
                        } else {
                            orderList = orderList.substring(0, orderList.length() - 1);
                        }
                        orderList = "or>" + orderList;

                        String go;
                        if (DialogFragment.active) {
                            go = "yes";
                        } else {
                            go = "no";
                        }
                        go = "go>" + go;

                        message = "[sync~" + orderList + "|" + go + "~" + AuthorizationFragment.DriverId + "]";
                        NetWork.get().send(message);

                    break;
                case "info":


                    break;
                case "version":
                    if(command_text.equals(NetWork.version)){
                        pref = getApplicationContext().getSharedPreferences("MyPref", MODE_PRIVATE);
                        boolean showMessage = pref.getBoolean("showMessage", true);
                        String messageText = pref.getString("messageText", "empty");

                        String infoFromServer = message.split(COMMAND_DELIMITER)[2];

                        if(!messageText.equals(infoFromServer)){
                            showMessage = true;
                            editor = pref.edit();
                            editor.putBoolean("showMessage", true);
                            editor.commit();
                        }

                        if(showMessage) {
                            editor = pref.edit();
                            editor.putString("messageText", infoFromServer);
                            editor.commit();

                            Message msg = Message.obtain();
                            msg.obj = infoFromServer;
                            msg.setTarget(AuthorizationFragment.handler);
                            msg.sendToTarget();
                        } else {
                            NetWork.get().sendWithoutSave("[i~" + AuthorizationFragment.DriverId + "~" + AuthorizationFragment.DriverId + "]");
                        }

                    } else {
                        NetWork.get().exit();
                        ExitActivity.balance = "upd";
                        Intent = new Intent(getApplicationContext(), ExitActivity.class);
                        Intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        Intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                        Intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        Intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(Intent);
                    }
                    break;
                default:

                    break;
            }
        } catch (Exception e){
            Log.w("ERROR ALARM!!!!!!!!!!! ", e);
        }
    }



    public IBinder onBind(Intent intent) {
        return null;
    }

    public void onDestroy(){
        super.onDestroy();
        BeatBox.release();
    }
}
