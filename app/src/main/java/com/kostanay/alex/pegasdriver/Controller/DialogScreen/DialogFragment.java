package com.kostanay.alex.pegasdriver.Controller.DialogScreen;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.kostanay.alex.pegasdriver.Controller.AuthorizationScreen.AuthorizationFragment;
import com.kostanay.alex.pegasdriver.Controller.Call;
import com.kostanay.alex.pegasdriver.Controller.Complete;
import com.kostanay.alex.pegasdriver.Controller.DetailedScreen.DetailedFragment;
import com.kostanay.alex.pegasdriver.Controller.ListScreen.ListFragment;
import com.kostanay.alex.pegasdriver.Controller.Refuse;
import com.kostanay.alex.pegasdriver.Model.NetWork;
import com.kostanay.alex.pegasdriver.Model.Order;
import com.kostanay.alex.pegasdriver.Model.OrderRepository;
import com.kostanay.alex.pegasdriver.Model.Sound;
import com.kostanay.alex.pegasdriver.Services.MyService;

public class DialogFragment extends Fragment {
    private static final String ARG_ORDER_NUMBER = "order_number";
    private Button button_accept, button_refuse, button_hurry, button_completed;
    private TextView dialog_info;
    private Order Order;
    private Sound Sound, Sound5;
    public static boolean alarm = true;
    public static Handler handler;
    public static boolean active = false;
    private Thread thread;

    public static DialogFragment newInstance(String orderNumber){
        Bundle args = new Bundle();
        args.putSerializable(ARG_ORDER_NUMBER, orderNumber);
        DialogFragment fragment = new DialogFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Order = OrderRepository.get().getOrder((String) getArguments().getSerializable(ARG_ORDER_NUMBER));
        Sound = MyService.BeatBox.getSounds().get(MyService.SOUND_GO);
        Sound5 = MyService.BeatBox.getSounds().get(4);
        active = true;
    }

    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(com.kostanay.alex.pegasdriver.R.layout.dialog_fragment, container, false);
        button_accept = v.findViewById(com.kostanay.alex.pegasdriver.R.id.dialog_button_accept);
        button_refuse = v.findViewById(com.kostanay.alex.pegasdriver.R.id.dialog_button_refuse);
        button_hurry = v.findViewById(com.kostanay.alex.pegasdriver.R.id.dialog_button_hurry);
        button_completed = v.findViewById(com.kostanay.alex.pegasdriver.R.id.dialog_button_completed);
        dialog_info = v.findViewById(com.kostanay.alex.pegasdriver.R.id.dialog_info);
        if(!Order.isPriceOffered()){
            Order.setOfferedPrice(Order.getPrice());
            Order.setTime("7");
        }
        dialog_info.setText("Ваша заявка:\n" + Order.getFromW() + "\n" + Order.getToW() + "\n" + Order.getTime() + " минут\n" + "за " + Order.getOfferedPrice());
        button_accept.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                DialogFragment.handler.sendEmptyMessage(4);
                OrderRepository.get().removeOtherPrices(Order.getOrderNumber());
                alarm = false;
                thread.interrupt();
                Thread thread = new Thread(new Runnable() {
                    public void run() {
                        NetWork.get().send("[acc~" + Order.getOrderNumber() + "~" + AuthorizationFragment.DriverId + "]");
                    }
                });
                thread.start();
                button_accept.setVisibility(View.INVISIBLE);
                button_hurry.setVisibility(View.VISIBLE);
                button_completed.setVisibility(View.VISIBLE);
                dialog_info.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        if (ActivityCompat.checkSelfPermission(getContext(),
                                Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.CALL_PHONE}, 0);
                        }
                        FragmentManager manager = getFragmentManager();
                        Call dialog = Call.newInstance(Order.getOrderNumber());
                        dialog.show(manager, "DialogDate");
                    }
                });
            }
        });
        button_refuse.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                alarm = false;
                thread.interrupt();
                Thread thread = new Thread(new Runnable() {
                    public void run() {
                        FragmentManager manager = getFragmentManager();
                        Refuse dialog = Refuse.newInstance(Order.getOrderNumber());
                        dialog.show(manager, "DialogDate");
                    }
                });
                thread.start();
            }
        });
        button_hurry.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Thread thread = new Thread(new Runnable() {
                    public void run() {
                        NetWork.get().send("[hur~" + Order.getOrderNumber() + "~" + AuthorizationFragment.DriverId + "]");
                        handler.sendEmptyMessage(2);
                    }
                });
                thread.start();
            }
        });
        button_completed.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                FragmentManager manager = getFragmentManager();
                Complete dialog = Complete.newInstance(Order.getOrderNumber());
                dialog.show(manager, "DialogDate");
            }
        });
        button_hurry.setVisibility(View.INVISIBLE);
        button_completed.setVisibility(View.INVISIBLE);

        handler = new Handler() {
            public void handleMessage(Message msg) {
                if (msg.obj != null) {
                    String message = (String) msg.obj;
                    if(message.equals(Order.getOrderNumber())){
                        handler.sendEmptyMessage(6);
                    }
                } else {
                    switch (msg.what) {
                        case 1:

                            break;
                        case 2:
                            button_hurry.setEnabled(false);
                            break;
                        case 3:
                            button_hurry.setEnabled(true);
                            MyService.BeatBox.play(Sound5);
                            dialog_info.setText("Номер клиента: " + Order.getPhone() + "\n" + Order.getFromW() + "\n(" + Order.getDescription() + ")\n" +Order.getToW() + "\n" + Order.getTime() + " минут\n" + "за " + Order.getOfferedPrice() + "\nКлиент уведомлен");
                            break;
                        case 4:
                            dialog_info.setText("Номер клиента: " + Order.getPhone() + "\n" + Order.getFromW() + "\n(" + Order.getDescription() + ")\n" +Order.getToW() + "\n" + Order.getTime() + " минут\n" + "за " + Order.getOfferedPrice() + "\nВы приняли заявку");
                            break;
                        case 5:
                            dialog_info.setText("Номер клиента: " + Order.getPhone() + "\n" + Order.getFromW() + "\n(" + Order.getDescription() + ")\n" +Order.getToW() + " минут\n" + "за " + Order.getPrice() + "\nВы приняли заявку");
                            break;
                        case 6:
                            if(active) {
                                dialog_info.setText("Клиент отменил заказ");
                                button_accept.setVisibility(View.INVISIBLE);
                                button_completed.setVisibility(View.INVISIBLE);
                                button_hurry.setVisibility(View.INVISIBLE);
                                alarm = true;
                                updating();
                                button_refuse.setOnClickListener(new View.OnClickListener() {
                                    public void onClick(View v) {
                                        alarm = false;
                                        thread.interrupt();
                                        try {DetailedFragment.handler.sendEmptyMessage(4);} catch (Exception e) {}
                                        try {ListFragment.handler.sendEmptyMessage(4);} catch (Exception e) {}
                                        try {com.kostanay.alex.pegasdriver.Controller.DialogScreen.DialogFragment.handler.sendEmptyMessage(7);} catch (Exception e) {}
                                        try{AuthorizationFragment.handler.sendEmptyMessage(7);} catch (Exception e){}
                                        com.kostanay.alex.pegasdriver.Controller.DialogScreen.DialogFragment.active = false;
                                        MyService.alreadyGo = false;
                                    }
                                });
                            }
                            break;
                        case 7:
                            try{getActivity().finish();} catch (Exception e){}
                            active = false;
                            break;
                        case 8:
                            try {
                                button_accept.setEnabled(false);
                                button_completed.setEnabled(false);
                                button_hurry.setEnabled(false);
                                button_refuse.setEnabled(false);
                            } catch (Exception e){}
                            break;
                        case 9:
                            if(active) {
                                dialog_info.setText("Вы сняты с заявки");
                                button_accept.setVisibility(View.INVISIBLE);
                                button_completed.setVisibility(View.INVISIBLE);
                                button_hurry.setVisibility(View.INVISIBLE);
                                alarm = true;
                                updating();
                                button_refuse.setText("Ок");
                                button_refuse.setOnClickListener(new View.OnClickListener() {
                                    public void onClick(View v) {
                                        Order.setPriceOffered(false);
                                        Order.setOfferedPrice("");
                                        alarm = false;
                                        thread.interrupt();
                                        try {DetailedFragment.handler.sendEmptyMessage(4);} catch (Exception e) {}
                                        try {ListFragment.handler.sendEmptyMessage(4);} catch (Exception e) {}
                                        try {com.kostanay.alex.pegasdriver.Controller.DialogScreen.DialogFragment.handler.sendEmptyMessage(7);} catch (Exception e) {}
                                        try{AuthorizationFragment.handler.sendEmptyMessage(7);} catch (Exception e){}
                                        com.kostanay.alex.pegasdriver.Controller.DialogScreen.DialogFragment.active = false;
                                        MyService.alreadyGo = false;
                                    }
                                });
                            }
                            break;
                        case 98:
                            try {
                                ((DialogActivity) getActivity()).setActionBarTitle("НЕТ СОЕДИНЕНИЯ");
                            } catch (Exception e){}
                            break;
                        case 99:
                            try {
                                ((DialogActivity) getActivity()).setActionBarTitle("Пегас");
                            } catch (Exception e){}
                            break;
                    }
                }
            }
        };
        alarm = true;
        updating();
        return v;
    }

    private void updating(){
        thread = new Thread(new Runnable() {
            public void run() {
                while(alarm){
                    MyService.alreadyGo = true;
                    MyService.BeatBox.play(Sound);
                    try{Thread.sleep(9000);} catch (InterruptedException e){break;}
                }
            }
        });
        thread.start();
    }

    public void onResume(){
        super.onResume();
        active = true;
    }
}
