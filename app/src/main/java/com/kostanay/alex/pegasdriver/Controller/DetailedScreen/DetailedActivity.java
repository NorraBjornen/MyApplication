package com.kostanay.alex.pegasdriver.Controller.DetailedScreen;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.Fragment;

import com.kostanay.alex.pegasdriver.Abstract.SingleFragmentActivity;

public class DetailedActivity extends SingleFragmentActivity {
    private static final String EXTRA_ORDER_NUMBER = "order_number";

    public static Intent newIntent(Context packageContext, String order_number){
        Intent intent = new Intent(packageContext, DetailedActivity.class);
        intent.putExtra(EXTRA_ORDER_NUMBER, order_number);
        return intent;
    }

    protected Fragment createFragment() {
        String orderNumber = (String) getIntent().getSerializableExtra(EXTRA_ORDER_NUMBER);
        return DetailedFragment.newInstance(orderNumber);
    }

    public void onBackPressed() {
        finish();
    }

    public void setActionBarTitle(String title) {
        getSupportActionBar().setTitle(title);
    }
}
