package com.kostanay.alex.pegasdriver.Controller;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;

import com.kostanay.alex.pegasdriver.Controller.AuthorizationScreen.AuthorizationFragment;
import com.kostanay.alex.pegasdriver.Model.NetWork;
import com.kostanay.alex.pegasdriver.R;

import static android.content.Context.MODE_PRIVATE;

public class Info extends DialogFragment {
    private static final String ARG = "info";

    public static Info newInstance(String message){
        Bundle args = new Bundle();
        args.putSerializable(ARG, message);

        Info fragment = new Info();
        fragment.setArguments(args);
        return fragment;
    }

    public Dialog onCreateDialog(Bundle savedInstanceState) {
        String info = (String) getArguments().getSerializable(ARG);
        View v = LayoutInflater.from(getActivity()).inflate(R.layout.info, null);
        final TextView textView = v.findViewById(R.id.info_container);
        final CheckBox checkBox = v.findViewById(R.id.dontShow);
        textView.setText(info);
        return new AlertDialog.Builder(getActivity()).setView(v).setTitle("Информация").setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                NetWork.get().sendWithoutSave("[i~" + AuthorizationFragment.DriverId + "~" + AuthorizationFragment.DriverId + "]");
                SharedPreferences pref = getActivity().getApplicationContext().getSharedPreferences("MyPref", MODE_PRIVATE);
                SharedPreferences.Editor editor = pref.edit();
                editor.putBoolean("showMessage", !checkBox.isChecked());
                editor.commit();
            }
        }).create();
    }
}
