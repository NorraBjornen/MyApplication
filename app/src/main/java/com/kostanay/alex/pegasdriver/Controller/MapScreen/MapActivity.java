package com.kostanay.alex.pegasdriver.Controller.MapScreen;

import android.content.Intent;
import android.support.v4.app.Fragment;

import com.kostanay.alex.pegasdriver.Abstract.SingleFragmentActivity;
import com.kostanay.alex.pegasdriver.Controller.AuthorizationScreen.AuthorizationActivity;
import com.kostanay.alex.pegasdriver.Controller.ListScreen.ListActivity;

public class MapActivity extends SingleFragmentActivity {
    protected Fragment createFragment() {
        Intent intent = new Intent(this, AuthorizationActivity.class);
        startActivity(intent);
        return new MapFragment();
    }

    public void onBackPressed(){
        if(!MapFragment.firstConnection) {
            Intent Intent = new Intent(MapActivity.this, ListActivity.class);
            startActivity(Intent);
        } else {
            super.onBackPressed();
        }
    }
}
