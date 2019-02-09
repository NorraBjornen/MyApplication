package com.kostanay.alex.pegasdriver.Controller;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

import com.kostanay.alex.pegasdriver.Model.OrderRepository;

public class Call extends DialogFragment {
    private static final String ARG_DATE = "date";
    private com.kostanay.alex.pegasdriver.Model.Order Order;

    public static Call newInstance(String orderNumber){
        Bundle args = new Bundle();
        args.putSerializable(ARG_DATE, orderNumber);

        Call fragment = new Call();
        fragment.setArguments(args);
        return fragment;
    }

    public Dialog onCreateDialog(Bundle savedInstanceState) {
        String orderNumber = (String) getArguments().getSerializable(ARG_DATE);
        Order = OrderRepository.get().getOrder(orderNumber);
        return new AlertDialog.Builder(getActivity()).setTitle("Позвонить клиенту").setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                Intent callIntent;
                if(Order.getPhone().length() != 11){
                    callIntent = new Intent(Intent.ACTION_DIAL);
                } else {
                    callIntent = new Intent(Intent.ACTION_CALL);
                }
                callIntent.setData(Uri.parse("tel:"+Order.getPhone()));
                startActivity(callIntent);
            }
        }).create();
    }
}