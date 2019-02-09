package com.kostanay.alex.pegasdriver.Controller.DistrictScreen;

import android.support.v4.app.Fragment;
import com.kostanay.alex.pegasdriver.Abstract.SingleFragmentActivity;

public class DistrictActivity extends SingleFragmentActivity {
    protected Fragment createFragment() {
        return new DistrictFragment();
    }

    public void onBackPressed(){
        finish();
    }
}
