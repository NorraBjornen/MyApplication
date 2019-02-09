package com.kostanay.alex.pegasdriver.Controller;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.kostanay.alex.pegasdriver.Controller.MapScreen.MapFragment;

public class ExitActivity extends AppCompatActivity {
    public static String balance;
    public static boolean active = false;
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.kostanay.alex.pegasdriver.R.layout.exit);
        TextView txt = findViewById(com.kostanay.alex.pegasdriver.R.id.debt);
        Button btn = findViewById(com.kostanay.alex.pegasdriver.R.id.ext);

        active = true;

        if(balance.equals("upd")){
            txt.setText("Обновите программу");
            btn.setText("Обновить");
            btn.setOnClickListener(new View.OnClickListener() {
                public void onClick(View view) {
                    String appPackageName = getPackageName();
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse("market://details?id=" + appPackageName));
                    startActivity(intent);
                }
            });
        } else {
            MapFragment.firstConnection = true;
            int debt = (1000 - Integer.parseInt(balance));
            if (debt < 0) {
                balance = "0";
            } else {
                double doubleDebt = debt;
                doubleDebt = doubleDebt / 100;
                if (doubleDebt != (int) doubleDebt) {
                    doubleDebt = ((int) doubleDebt) + 1;
                }
                doubleDebt = doubleDebt * 100;
                balance = String.valueOf((int) doubleDebt);
            }
            txt.append("\n" + balance);
            btn.setOnClickListener(new View.OnClickListener() {
                public void onClick(View view) {
                    onBackPressed();
                }
            });
        }
    }

    public void onBackPressed() {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            finishAndRemoveTask();
        } else{
            finish();
        }
        int pid = android.os.Process.myPid();
        android.os.Process.killProcess(pid);
    }

}
