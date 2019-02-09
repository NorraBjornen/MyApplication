package com.kostanay.alex.pegasdriver.Controller;

import android.app.AlertDialog;
import android.app.Dialog;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

import com.kostanay.alex.pegasdriver.Controller.AuthorizationScreen.AuthorizationFragment;
import com.kostanay.alex.pegasdriver.Model.NetWork;
import com.kostanay.alex.pegasdriver.Services.MyService;

public class Exit extends DialogFragment {
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return new AlertDialog.Builder(getActivity()).setTitle("Выход").setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                NetWork.get().send("[exit~ok~" + AuthorizationFragment.DriverId + "]");
            }
        }).create();
    }
}
