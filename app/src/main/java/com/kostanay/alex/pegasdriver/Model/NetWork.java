package com.kostanay.alex.pegasdriver.Model;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.util.Log;
import android.widget.Toast;

import com.kostanay.alex.pegasdriver.Controller.AuthorizationScreen.AuthorizationFragment;
import com.kostanay.alex.pegasdriver.Controller.DetailedScreen.DetailedFragment;
import com.kostanay.alex.pegasdriver.Controller.DialogScreen.DialogFragment;
import com.kostanay.alex.pegasdriver.Controller.ListScreen.ListFragment;
import com.kostanay.alex.pegasdriver.Controller.MapScreen.MapFragment;
import com.kostanay.alex.pegasdriver.Services.MyService;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static android.content.Context.MODE_PRIVATE;

public class NetWork {
    private static NetWork NetWork;
    private static final String IP_HOST = "83.166.242.161";
    private static final String IP_LOCAL = "192.168.1.151";
    private static final String IPL = "127.0.0.1";
    private static final String IP = IP_LOCAL;
    private Socket Socket;
    private Context Context;
    private boolean reconnecting = false;
    public static String version;
    private Thread checker;

    private List<String> messages = new ArrayList<>();

    public static NetWork get(){
        if(NetWork == null){
            NetWork = new NetWork();
        }
        return NetWork;
    }

    public void clear(){
        messages.clear();
    }

    public void setContext(Context context){
        Context = context;
        try {
            PackageInfo pInfo = Context.getPackageManager().getPackageInfo(Context.getPackageName(), 0);
            version = String.valueOf(pInfo.versionCode);
        } catch (Exception e){}
    }

    public android.content.Context getContext() {
        return Context;
    }

    public Socket getSocket() {
        return Socket;
    }

    public boolean isReach(){
        try {
            //if(InetAddress.getByName(IP).isReachable(10000)){
                Socket socket = new Socket();
                socket.connect(new InetSocketAddress(IP, 1488), 10000);
                socket.close();
                return true;
            //} else {
            //    return false;
            //}
        } catch (IOException e) {
            return false;
        }
    }

    public boolean isConnected(){
        if(Socket != null){
            if(Socket.isConnected()){
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    public void send(final String message){
        messages.add(message);
        new Thread(new Runnable() {
            public void run() {
                if(isReach()) {
                    try {
                        OutputStream output = Socket.getOutputStream();
                        output.write(message.getBytes(StandardCharsets.UTF_8));
                        output.flush();
                    } catch (Exception e) {
                        Log.w("myApp", e);
                        reconnect();
                    }
                } else {
                    reconnect();
                }
            }
        }).start();

    }

    public void sendWithoutSave(final String message){
        new Thread(new Runnable() {
            public void run() {
                if(isReach()) {
                    try {
                        OutputStream output = Socket.getOutputStream();
                        output.write(message.getBytes(StandardCharsets.UTF_8));
                        output.flush();
                    } catch (Exception e) {
                        Log.w("myApp", e);
                        reconnect();
                    }
                } else {
                    reconnect();
                }
            }
        }).start();

    }

    public void reconnect(){
        new Thread(new Runnable() {
            public void run() {
                if (!reconnecting) {
                    reconnecting = true;
                    try {
                        DetailedFragment.handler.sendEmptyMessage(98);
                    } catch (Exception ea) {
                    }
                    try {
                        ListFragment.handler.sendEmptyMessage(98);
                    } catch (Exception ea) {
                    }
                    try {
                        DialogFragment.handler.sendEmptyMessage(98);
                    } catch (Exception ea) {
                    }
                    int count = 0;
                    while (!connect()) {
                        SharedPreferences pref = getContext().getSharedPreferences("MyPref", MODE_PRIVATE);
                        boolean disconnectDisabled = pref.getBoolean("disconnectDisabled", false);

                        if(count >= 7 && !disconnectDisabled){
                            MyService.BeatBox.play(MyService.Sound3);
                        }
                        try {
                            Thread.sleep(3000);
                        } catch (Exception ea) {}
                        count++;
                        if(checker.isInterrupted()){
                            break;
                        }
                    }
                    if(!checker.isInterrupted()) {
                        Intent serviceIntent = new Intent(Context, MyService.class);
                        Context.startService(serviceIntent);
                        reconnecting = false;
                    }
                    try {
                        DetailedFragment.handler.sendEmptyMessage(99);
                    } catch (Exception ea) {
                    }
                    try {
                        ListFragment.handler.sendEmptyMessage(99);
                    } catch (Exception ea) {
                    }
                    try {
                        DialogFragment.handler.sendEmptyMessage(99);
                    } catch (Exception ea) {
                    }
                }
            }
        }).start();
    }

    public boolean connect(){
        try {
            Socket = new Socket(IP, 1490);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public void sendMessages(){
        String msg = "";
        for(String message : messages){
            msg = msg + message;
        }
        if(!msg.equals(""))
        sendWithoutSave(msg);
    }

    public void checkConnection(){
        checker = new Thread(new Runnable() {
            public void run() {
                while (true){
                    if(!isReach()){
                        reconnect();
                    }
                    try{Thread.sleep(10000);} catch (Exception e){
                        break;
                    }
                }
            }
        });
        checker.start();
    }

    public void close(){
        try {
            Socket.close();
        } catch (Exception e){}
    }

    public void exit(){
        checker.interrupt();
        close();
    }
}
