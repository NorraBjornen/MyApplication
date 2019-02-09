package com.kostanay.alex.pegasdriver.Controller.SettingsScreen;

import android.support.v4.app.Fragment;

import com.kostanay.alex.pegasdriver.Abstract.SingleFragmentActivity;

public class SettingsActivity extends SingleFragmentActivity {
    protected Fragment createFragment() {
        return new SettingsFragment();
    }

    public void onBackPressed() {
        finish();
    }

}
