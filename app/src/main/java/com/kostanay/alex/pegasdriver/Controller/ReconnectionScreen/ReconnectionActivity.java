package com.kostanay.alex.pegasdriver.Controller.ReconnectionScreen;

import android.support.v4.app.Fragment;

import com.kostanay.alex.pegasdriver.Abstract.SingleFragmentActivity;

public class ReconnectionActivity extends SingleFragmentActivity {
    protected Fragment createFragment() {
        return new ReconnectionFragment();
    }
}
