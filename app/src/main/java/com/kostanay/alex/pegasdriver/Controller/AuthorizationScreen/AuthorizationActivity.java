package com.kostanay.alex.pegasdriver.Controller.AuthorizationScreen;

import android.support.v4.app.Fragment;

import com.kostanay.alex.pegasdriver.Abstract.SingleFragmentActivity;

public class AuthorizationActivity extends SingleFragmentActivity {
    @Override
    protected Fragment createFragment() {
        return new AuthorizationFragment();
    }
}
