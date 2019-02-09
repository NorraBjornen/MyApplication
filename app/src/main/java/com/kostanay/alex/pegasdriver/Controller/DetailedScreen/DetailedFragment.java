package com.kostanay.alex.pegasdriver.Controller.DetailedScreen;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.kostanay.alex.pegasdriver.Controller.AuthorizationScreen.AuthorizationFragment;
import com.kostanay.alex.pegasdriver.Controller.Exit;
import com.kostanay.alex.pegasdriver.Controller.Info;
import com.kostanay.alex.pegasdriver.Controller.ListScreen.ListFragment;
import com.kostanay.alex.pegasdriver.Controller.SettingsScreen.SettingsActivity;
import com.kostanay.alex.pegasdriver.Model.NetWork;
import com.kostanay.alex.pegasdriver.Model.Order;
import com.kostanay.alex.pegasdriver.Model.OrderRepository;
import com.kostanay.alex.pegasdriver.Services.MyService;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class DetailedFragment extends Fragment {
    private static final String ARG_ORDER_NUMBER = "order_number";
    private TextView From, To, Price, Description;
    private EditText OfferPrice;
    private Button Accept;
    private Order Order;
    private boolean a = false;
    public static Handler handler;
    public static boolean active = false;
    private final String USER_AGENT = "Mozilla/5.0";

    public static DetailedFragment newInstance(String orderNumber){
        Bundle args = new Bundle();
        args.putSerializable(ARG_ORDER_NUMBER, orderNumber);
        DetailedFragment fragment = new DetailedFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(com.kostanay.alex.pegasdriver.R.menu.menu, menu);
    }


    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Order = OrderRepository.get().getOrder((String) getArguments().getSerializable(ARG_ORDER_NUMBER));
        setHasOptionsMenu(true);
    }


    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(com.kostanay.alex.pegasdriver.R.layout.detailed_fragment, container, false);
        From = v.findViewById(com.kostanay.alex.pegasdriver.R.id.detailed_screen_from);
        To = v.findViewById(com.kostanay.alex.pegasdriver.R.id.detailed_screen_to);
        Price = v.findViewById(com.kostanay.alex.pegasdriver.R.id.detailed_screen_price);
        Description = v.findViewById(com.kostanay.alex.pegasdriver.R.id.detailed_screen_description);
        OfferPrice = v.findViewById(com.kostanay.alex.pegasdriver.R.id.detailed_screen_offer_price);

        Accept = v.findViewById(com.kostanay.alex.pegasdriver.R.id.detailed_screen_accept);

        From.setText(Order.getFromW());
        To.setText(Order.getToW());
        Price.setText(Order.getPrice());
        Description.setText(Order.getDescription());

        OfferPrice.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                if((!Order.getPrice().equals("-") && OfferPrice.getText().length() > 2)){
                    Accept.setEnabled(true);
                } else {
                    Accept.setEnabled(false);
                }
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            public void afterTextChanged(Editable s) {
                if((!Order.getPrice().equals("-") && OfferPrice.getText().length() > 2)){
                    Accept.setEnabled(true);
                } else {
                    Accept.setEnabled(false);
                }
            }
        });

        Accept.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (a) {
                    Accept.setText("Принять");
                    OfferPrice.setEnabled(true);
                    Accept.setEnabled(false);
                    OfferPrice.setText("");
                    a = false;
                    OfferPrice.setFocusable(true);
                    OfferPrice.requestFocus();
                    InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.showSoftInput(OfferPrice, InputMethodManager.SHOW_IMPLICIT);
                } else {
                    if(Order.isPriceOffered()){
                        String rdyPrice;
                        if (OfferPrice.getText().length() == 0) {
                            rdyPrice = Order.getPrice();
                        } else {
                            rdyPrice = OfferPrice.getText().toString();
                        }
                        Order.setPriceOffered(true);
                        Order.setOfferedPrice(rdyPrice);
                        Order.setTime("7");
                        NetWork.get().send("[updoff~"+Order.getOrderNumber()+"$"+rdyPrice+"$7~" + AuthorizationFragment.DriverId + "]");
                        Accept.setText("Изменить цену");
                        OfferPrice.setEnabled(false);
                        OfferPrice.setFocusable(true);
                        a = true;
                    } else {
                        String rdyPrice;
                        if (OfferPrice.getText().length() == 0) {
                            rdyPrice = Order.getPrice();
                        } else {
                            rdyPrice = OfferPrice.getText().toString();
                        }
                        NetWork.get().send("[offer~" + Order.getOrderNumber() + "$" + rdyPrice + "$7~" + AuthorizationFragment.DriverId + "]");
                        Order.setPriceOffered(true);
                        Order.setOfferedPrice(rdyPrice);
                        Order.setTime("7");
                        Accept.setText("Изменить цену");
                        OfferPrice.setEnabled(false);
                        a = true;
                    }
                }
            }
        });

        handler = new Handler() {
            public void handleMessage(Message msg) {
                if(msg.obj != null){
                    String message = (String) msg.obj;
                    try {
                        FragmentManager manager = getFragmentManager();
                        Info dialog = Info.newInstance(message);
                        dialog.show(manager, "DialogDate");
                    } catch (Exception e){}
                } else {
                    switch (msg.what){
                        case 1:
                            OfferPrice.setText(Order.getOfferedPrice());
                            OfferPrice.setEnabled(false);
                            OfferPrice.setFocusable(true);
                            Accept.setText("Изменить цену");
                            Accept.setEnabled(true);
                            a = true;
                            break;
                        case 2:
                            OfferPrice.setFocusableInTouchMode(true);
                            break;
                        case 3:
                            From.setText(Order.getFromW());
                            To.setText(Order.getToW());
                            Price.setText(Order.getPrice());
                            Description.setText(Order.getDescription());
                            break;
                        case 4:
                            try{getActivity().finish();} catch (Exception e){}
                            break;
                        case 5:
                            try {Toast.makeText(getActivity(),"Ваш баланс:\n" + MyService.Balance, Toast.LENGTH_SHORT).show();}catch (Exception e){}
                            break;
                        case 6:
                            Toast.makeText(getActivity(), "Вышел", Toast.LENGTH_SHORT).show();
                            break;
                        case 8:
                            Toast.makeText(getActivity(), "Произошел разрыв соединения\nНевозможно выйти с линии", Toast.LENGTH_SHORT).show();
                            break;
                        case 9:
                            try {
                                Toast.makeText(getActivity(), "Теперь Ваш статус:\nСвободен", Toast.LENGTH_SHORT).show();
                            } catch (Exception e){}
                            break;
                        case 10:
                            try {
                                Toast.makeText(getActivity(), "Теперь Ваш статус:\nЗанят", Toast.LENGTH_SHORT).show();
                            } catch (Exception e){}
                            break;
                        case 98:
                            try {
                                ((DetailedActivity) getActivity()).setActionBarTitle("НЕТ СОЕДИНЕНИЯ");
                            } catch (Exception e){}
                            break;
                        case 99:
                            try {
                                ((DetailedActivity) getActivity()).setActionBarTitle("Пегас");
                            } catch (Exception e){}
                            break;
                    }
                }
            };
        };
        a = false;
        OfferPrice.setFocusable(false);
        Accept.setEnabled(false);
        thread();

        return v;
    }

    private void thread(){
        new Thread(new Runnable() {
            public void run() {
                try{Thread.sleep(100);}catch (Exception e){}
                handler.sendEmptyMessage(2);
                if(Order.isPriceOffered()) {
                    try {
                        Thread.sleep(100);
                    } catch (Exception e) {
                    }
                    handler.sendEmptyMessage(1);
                }
            }
        }).start();
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case com.kostanay.alex.pegasdriver.R.id.menu_item_exit_line:
                new Thread(new Runnable() {
                    public void run() {
                        FragmentManager manager = getFragmentManager();
                        Exit dialog = new Exit();
                        dialog.show(manager, "DialogDate");
                    }
                }).start();
                return true;
            case com.kostanay.alex.pegasdriver.R.id.menu_item_balance:
                new Thread(new Runnable() {
                    public void run() {
                        NetWork.get().send("[getbal~ok~" + AuthorizationFragment.DriverId + "]");
                    }
                }).start();
                return true;
            case com.kostanay.alex.pegasdriver.R.id.menu_item_switch:
                new Thread(new Runnable() {
                    public void run() {
                        NetWork.get().send("[sw~ok~" + AuthorizationFragment.DriverId + "]");
                    }
                }).start();
                return true;
            case com.kostanay.alex.pegasdriver.R.id.menu_item_map:
                try{ListFragment.handler.sendEmptyMessage(4);} catch (Exception e){}
                getActivity().finish();
                return true;
            case com.kostanay.alex.pegasdriver.R.id.menu_item_settings:
                Intent intent = new Intent(getActivity(), SettingsActivity.class);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private String getGeo(){
        LocationManager locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);

        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) && !locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER) && !locationManager.isProviderEnabled(LocationManager.PASSIVE_PROVIDER)) {
            return null;
        } else if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission
                    (getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return null;
            } else {

                Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

                Location location1 = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

                Location location2 = locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);

                if (location != null) {
                    String lattitude = String.valueOf(location.getLatitude());
                    String longitude = String.valueOf(location.getLongitude());
                    return lattitude + "," + longitude;
                }else if (location1 != null) {
                    String lattitude = String.valueOf(location1.getLatitude());
                    String longitude = String.valueOf(location1.getLongitude());
                    return lattitude + "," + longitude;
                }else if (location2 != null) {
                    String lattitude = String.valueOf(location2.getLatitude());
                    String longitude = String.valueOf(location2.getLongitude());
                    return lattitude + "," + longitude;
                }else{
                    return null;
                }
            }
        } else {
            return null;
        }
    }

    public void onResume(){
        super.onResume();
        active = true;
    }

    public void onDestroyView(){
        super.onDestroyView();
        active = false;
    }
}
