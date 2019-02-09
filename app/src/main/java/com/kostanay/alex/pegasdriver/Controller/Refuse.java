package com.kostanay.alex.pegasdriver.Controller;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;

import com.kostanay.alex.pegasdriver.Controller.AuthorizationScreen.AuthorizationFragment;
import com.kostanay.alex.pegasdriver.Controller.DetailedScreen.DetailedFragment;
import com.kostanay.alex.pegasdriver.Controller.ListScreen.ListFragment;
import com.kostanay.alex.pegasdriver.Model.NetWork;
import com.kostanay.alex.pegasdriver.Model.Order;
import com.kostanay.alex.pegasdriver.Model.OrderRepository;
import com.kostanay.alex.pegasdriver.Services.MyService;

public class Refuse extends DialogFragment {
    private static final String ARG_DATE = "date";
    private Order Order;

    public static Refuse newInstance(String orderNumber){
        Bundle args = new Bundle();
        args.putSerializable(ARG_DATE, orderNumber);

        Refuse fragment = new Refuse();
        fragment.setArguments(args);
        return fragment;
    }

    public Dialog onCreateDialog(Bundle savedInstanceState) {
        String orderNumber = (String) getArguments().getSerializable(ARG_DATE);
        Order = OrderRepository.get().getOrder(orderNumber);
        return new AlertDialog.Builder(getActivity()).setTitle("Отказ").setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                if(Order != null) {
                    NetWork.get().send("[ref~" + Order.getOrderNumber() + "~" + AuthorizationFragment.DriverId + "]");
                } else {
                    try {DetailedFragment.handler.sendEmptyMessage(4);} catch (Exception e) {}
                    try {ListFragment.handler.sendEmptyMessage(4);} catch (Exception e) {}
                    try {com.kostanay.alex.pegasdriver.Controller.DialogScreen.DialogFragment.handler.sendEmptyMessage(7);} catch (Exception e) {}
                    try{AuthorizationFragment.handler.sendEmptyMessage(7);} catch (Exception e){}
                    com.kostanay.alex.pegasdriver.Controller.DialogScreen.DialogFragment.active = false;
                    MyService.alreadyGo = false;
                }
            }
        }).create();
    }
}
