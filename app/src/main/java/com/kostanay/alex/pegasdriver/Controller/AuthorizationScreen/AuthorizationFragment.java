package com.kostanay.alex.pegasdriver.Controller.AuthorizationScreen;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;

import static android.content.Context.MODE_PRIVATE;
import static com.kostanay.alex.pegasdriver.Model.Constants.CONNECT;
import static com.kostanay.alex.pegasdriver.Model.Constants.GO_ONLINE_BUSY;
import static com.kostanay.alex.pegasdriver.Model.Constants.GO_ONLINE_FREE;

import com.kostanay.alex.pegasdriver.Controller.DialogScreen.DialogFragment;
import com.kostanay.alex.pegasdriver.Controller.Exit;
import com.kostanay.alex.pegasdriver.Controller.Info;
import com.kostanay.alex.pegasdriver.Controller.ListScreen.ListActivity;
import com.kostanay.alex.pegasdriver.Controller.ListScreen.ListFragment;
import com.kostanay.alex.pegasdriver.Controller.MapScreen.MapFragment;
import com.kostanay.alex.pegasdriver.Controller.Refuse;
import com.kostanay.alex.pegasdriver.Model.NetWork;
import com.kostanay.alex.pegasdriver.Services.MyService;

public class AuthorizationFragment extends Fragment {
    public static String DriverId;
    private TextView info;
    private EditText edit;
    private Button button, button_free, button_busy;
    public static Handler handler;
    private String Password;
    public static boolean active;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DriverId = getDriverId();
    }

    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(com.kostanay.alex.pegasdriver.R.layout.authorization_fragment, container, false);
        info = v.findViewById(com.kostanay.alex.pegasdriver.R.id.info);
        edit = v.findViewById(com.kostanay.alex.pegasdriver.R.id.edit);
        button = v.findViewById(com.kostanay.alex.pegasdriver.R.id.button);
        button_free = v.findViewById(com.kostanay.alex.pegasdriver.R.id.button_free);
        button_busy = v.findViewById(com.kostanay.alex.pegasdriver.R.id.button_busy);

        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Password = edit.getText().toString();
                if (Password.matches("[0-9]+") && Password.length() == 12) {
                    SharedPreferences pref = getActivity().getApplicationContext().getSharedPreferences("MyPref", MODE_PRIVATE);
                    SharedPreferences.Editor editor = pref.edit();
                    editor.putString("DriverId", Password);
                    editor.commit();
                    info.setText("Перезепустите \n программу \n");
                    button.setVisibility(View.INVISIBLE);
                    edit.setVisibility(View.INVISIBLE);
                }
            }
        });

        button_free.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                button_busy.setVisibility(View.INVISIBLE);
                button_free.setVisibility(View.INVISIBLE);
                thread(GO_ONLINE_FREE);
            }
        });

        button_busy.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                button_busy.setVisibility(View.INVISIBLE);
                button_free.setVisibility(View.INVISIBLE);
                thread(GO_ONLINE_BUSY);
            }
        });

        edit.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                if(edit.getText().length() != 12){
                    button.setEnabled(false);
                } else {
                    button.setEnabled(true);
                }
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }


            public void afterTextChanged(Editable s) {
                if(edit.getText().length() != 12){
                    button.setEnabled(false);
                } else {
                    button.setEnabled(true);
                }
            }
        });

        edit.setVisibility(View.INVISIBLE);
        button.setVisibility(View.INVISIBLE);
        button_free.setVisibility(View.INVISIBLE);
        button_busy.setVisibility(View.INVISIBLE);
        button.setEnabled(false);
        handler = new Handler() {
            public void handleMessage(Message msg) {
                if(msg.obj != null){
                    String message = (String) msg.obj;
                    try {
                        FragmentManager manager = getFragmentManager();
                        Info dialog = Info.newInstance(message);
                        dialog.show(manager, "DialogDate");
                    } catch (Exception e){
                        try {
                            msg = Message.obtain();
                            msg.obj = message;
                            msg.setTarget(ListFragment.handler);
                            msg.sendToTarget();
                        } catch (Exception ea){}
                    }
                } else {
                    switch (msg.what){
                        case 2:
                            info.setText("Баланс недостаточен \n для выхода \n на линию\n");
                            break;
                        case 3:
                            info.setText("Введите \n код доступа");
                            edit.setVisibility(View.VISIBLE);
                            edit.setFocusable(true);
                            button.setVisibility(View.VISIBLE);
                            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                            imm.showSoftInput(edit, InputMethodManager.SHOW_IMPLICIT);
                            break;
                        case 4:
                            info.setText("Выйти на линию");
                            button_free.setVisibility(View.VISIBLE);
                            button_busy.setVisibility(View.VISIBLE);
                            break;
                        case 5:
                            info.setText("Ваши данные внесены \n \n \n");
                            break;
                        case 7:
                            try{getActivity().finish();} catch (Exception e){}
                            break;
                    }
                }
            };
        };
        if(NetWork.get().isConnected()){
            NetWork.get().sendWithoutSave("[i~" + AuthorizationFragment.DriverId + "~" + AuthorizationFragment.DriverId + "]");
        } else {
            thread(CONNECT);
        }
        return v;
    }

    private void thread(final int parameter){
        final Thread thread = new Thread(new Runnable() {
            public void run() {
                switch (parameter){
                    case CONNECT:
                        DriverId = getDriverId();
                        NetWork.get().setContext(getActivity());
                        if(!NetWork.get().connect()){
                            try {Thread.sleep(1000);} catch (Exception e){}
                            thread(CONNECT);
                        } else {
                            Intent serviceIntent = new Intent(getActivity(), MyService.class);
                            getActivity().startService(serviceIntent);
                            NetWork.get().checkConnection();
                        }
                        break;
                    case GO_ONLINE_FREE:
                        MapFragment.firstConnection = true;
                        NetWork.get().send("[online~1$0,0~" + DriverId + "]");
                        break;
                    case GO_ONLINE_BUSY:
                        MapFragment.firstConnection = true;
                        NetWork.get().send("[online~2$0,0~" + DriverId + "]");
                        break;
                    default:

                        break;
                }
            }
        });
        thread.start();
    }

    private String getDriverId(){
        SharedPreferences pref = getActivity().getApplicationContext().getSharedPreferences("MyPref", MODE_PRIVATE);
        String a = pref.getString("DriverId", null);
        if(a == null){
            SimpleDateFormat sdfDate = new SimpleDateFormat("yyMMddHHmmss");
            Date now = new Date();
            String strDate = sdfDate.format(now);
            SharedPreferences.Editor editor = pref.edit();
            editor.putString("DriverId", strDate);
            editor.commit();
            a = pref.getString("DriverId", null);
        }
        return a;
    }

    public void onResume(){
        super.onResume();
        active = true;
    }

    public void onPause(){
        super.onPause();
        active = false;
    }

    public void onDestroy(){
        super.onDestroy();
    }
}
