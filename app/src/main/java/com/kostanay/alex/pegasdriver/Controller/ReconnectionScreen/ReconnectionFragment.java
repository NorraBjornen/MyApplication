package com.kostanay.alex.pegasdriver.Controller.ReconnectionScreen;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.kostanay.alex.pegasdriver.Controller.AuthorizationScreen.AuthorizationFragment;
import com.kostanay.alex.pegasdriver.Controller.DialogScreen.DialogFragment;
import com.kostanay.alex.pegasdriver.Controller.ExitActivity;
import com.kostanay.alex.pegasdriver.Model.NetWork;
import com.kostanay.alex.pegasdriver.Services.MyService;

import static com.kostanay.alex.pegasdriver.Model.Constants.CONNECT;

public class ReconnectionFragment extends Fragment {
    private TextView info;
    private EditText edit;
    private Button button, button_free, button_busy;
    public static Handler handler;
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(com.kostanay.alex.pegasdriver.R.layout.authorization_fragment, container, false);
        edit = v.findViewById(com.kostanay.alex.pegasdriver.R.id.edit);
        button = v.findViewById(com.kostanay.alex.pegasdriver.R.id.button);
        button_free = v.findViewById(com.kostanay.alex.pegasdriver.R.id.button_free);
        button_busy = v.findViewById(com.kostanay.alex.pegasdriver.R.id.button_busy);
        edit.setVisibility(View.INVISIBLE);
        button.setVisibility(View.INVISIBLE);
        button_free.setVisibility(View.INVISIBLE);
        button_busy.setVisibility(View.INVISIBLE);

        info = v.findViewById(com.kostanay.alex.pegasdriver.R.id.info);

        DialogFragment.alarm = false;

        handler = new Handler() {
            public void handleMessage(Message msg) {
                if(msg.obj != null){
                    try {
                        String information = (String) msg.obj;
                        info.setText(information);
                        if(information.equals("ОБНОВИТЕ ПРОГРАММУ")){
                            NetWork.get().exit();
                            ExitActivity.balance = "upd";
                            Intent Intent = new Intent(getActivity(), ExitActivity.class);
                            Intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            Intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                            Intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            Intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(Intent);
                        }
                    } catch (Exception e){}
                } else {

                }
            };
        };

        return v;
    }

    private void thread(final int parameter){
        final Thread thread = new Thread(new Runnable() {
            public void run() {
                switch (parameter){
                    case CONNECT:

                        NetWork.get().send("kek");

                        if(!NetWork.get().connect()){
                            try {Thread.sleep(1000);} catch (Exception e){}
                            thread(CONNECT);
                        } else {
                            try {
                                Intent serviceIntent = new Intent(getActivity(), MyService.class);
                                getActivity().startService(serviceIntent);
                                getActivity().finish();
                            } catch (Exception e){
                                Log.w("myApp", e);
                            }
                        }
                        break;
                }
            }
        });
        thread.start();
    }

    public void onResume(){
        super.onResume();
        thread(CONNECT);
    }

}
