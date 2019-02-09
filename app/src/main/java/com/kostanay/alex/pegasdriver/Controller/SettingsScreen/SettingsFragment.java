package com.kostanay.alex.pegasdriver.Controller.SettingsScreen;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.Toast;

import com.kostanay.alex.pegasdriver.Controller.AuthorizationScreen.AuthorizationFragment;
import com.kostanay.alex.pegasdriver.Model.BeatBox;
import com.kostanay.alex.pegasdriver.Model.NetWork;
import com.kostanay.alex.pegasdriver.R;
import com.kostanay.alex.pegasdriver.Services.MyService;

import java.util.ArrayList;
import java.util.List;

import static android.content.Context.MODE_PRIVATE;
import static com.kostanay.alex.pegasdriver.Services.MyService.SOUND_DISCONNECT;
import static com.kostanay.alex.pegasdriver.Services.MyService.SOUND_GO;
import static com.kostanay.alex.pegasdriver.Services.MyService.SOUND_NEW;
import static com.kostanay.alex.pegasdriver.Services.MyService.SOUND_RECONNECTED;

public class SettingsFragment extends Fragment {
    private Spinner New, Go;
    private Switch Status, DisableSound, DisableDisconnect;
    private EditText Radius;
    private Button Save;
    private com.kostanay.alex.pegasdriver.Model.BeatBox BeatBox;

    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(com.kostanay.alex.pegasdriver.R.menu.menu_settings, menu);
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(com.kostanay.alex.pegasdriver.R.layout.settings, container, false);
        List<String> soundsList = new ArrayList<>();
        soundsList.add("Фьть, Ха!");
        soundsList.add("Стнд. разр.");
        soundsList.add("Стнд. переподкл.");
        soundsList.add("Пятачок");
        New = v.findViewById(com.kostanay.alex.pegasdriver.R.id.new_spinner);
        //Disconnect = v.findViewById(com.kostanay.alex.pegasdriver.R.id.disconnect_spinner);
        //Reconnect = v.findViewById(com.kostanay.alex.pegasdriver.R.id.reconnected_spinner);
        Go = v.findViewById(com.kostanay.alex.pegasdriver.R.id.go_spinner);
        Status = v.findViewById(com.kostanay.alex.pegasdriver.R.id.switch_status);
        DisableSound = v.findViewById(R.id.disable_sound);
        DisableDisconnect = v.findViewById(R.id.disable_disconnect);
        Radius = v.findViewById(com.kostanay.alex.pegasdriver.R.id.radius);
        Save = v.findViewById(com.kostanay.alex.pegasdriver.R.id.button_save);

        ArrayAdapter<String> adapter1 = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, soundsList);
        adapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        ArrayAdapter<String> adapter2 = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, soundsList);
        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        ArrayAdapter<String> adapter3 = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, soundsList);
        adapter3.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        ArrayAdapter<String> adapter4 = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, soundsList);
        adapter4.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        New.setAdapter(adapter1);
        New.setSelection(SOUND_NEW);
        //Disconnect.setAdapter(adapter2);
        //Disconnect.setSelection(SOUND_DISCONNECT);
        //Reconnect.setAdapter(adapter3);
        //Reconnect.setSelection(SOUND_RECONNECTED);
        Go.setAdapter(adapter4);
        Go.setSelection(SOUND_GO);

        if(MyService.status){
            Status.setText("Статус: Свободен");
        } else {
            Status.setText("Статус: Занят");
        }
        Status.setChecked(MyService.status);
        Status.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(Status.isChecked()){
                    Status.setText("Статус: Свободен");
                } else {
                    Status.setText("Статус: Занят");
                }
            }
        });

        SharedPreferences pref = getActivity().getSharedPreferences("MyPref", MODE_PRIVATE);
        boolean soundDisabled = pref.getBoolean("soundDisabled", false);
        boolean disconnectDisabled = pref.getBoolean("disconnectDisabled", false);

        if(soundDisabled){
            DisableSound.setChecked(true);
        } else {
            DisableSound.setChecked(false);
        }

        if(disconnectDisabled){
            DisableDisconnect.setChecked(true);
        } else {
            DisableDisconnect.setChecked(false);
        }

        Save.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                SharedPreferences pref = getActivity().getApplicationContext().getSharedPreferences("MyPref", MODE_PRIVATE);
                SharedPreferences.Editor editor = pref.edit();
                editor.putString("sound_new", String.valueOf(New.getSelectedItemPosition()));
                //editor.putString("sound_disconnect", String.valueOf(Disconnect.getSelectedItemPosition()));
                //editor.putString("sound_reconnected", String.valueOf(Reconnect.getSelectedItemPosition()));
                editor.putString("sound_go", String.valueOf(Go.getSelectedItemPosition()));
                editor.putString("radius", Radius.getText().toString());
                editor.putBoolean("soundDisabled", DisableSound.isChecked());
                editor.putBoolean("disconnectDisabled", DisableDisconnect.isChecked());
                editor.commit();
                SOUND_NEW = Integer.parseInt(pref.getString("sound_new", "0"));
                SOUND_DISCONNECT = Integer.parseInt(pref.getString("sound_disconnect", "1"));
                SOUND_RECONNECTED = Integer.parseInt(pref.getString("sound_reconnected", "2"));
                SOUND_GO = Integer.parseInt(pref.getString("sound_go", "3"));

                BeatBox = new BeatBox(getActivity());

                MyService.Sound1 = BeatBox.getSounds().get(SOUND_NEW);
                MyService.Sound2 = BeatBox.getSounds().get(SOUND_DISCONNECT);
                MyService.Sound3 = BeatBox.getSounds().get(SOUND_RECONNECTED);
                MyService.Sound4 = BeatBox.getSounds().get(SOUND_GO);

                MyService.status = Status.isChecked();
                final String sts;
                if(Status.isChecked()){
                    sts = "1";
                } else {
                    sts = "2";
                }
                new Thread(new Runnable() {
                    public void run() {
                        if(Radius.getText().toString().length() != 0) {
                            NetWork.get().send("[sts~" + sts + "~" + AuthorizationFragment.DriverId + "][rds~" + Radius.getText().toString() + "~" + AuthorizationFragment.DriverId + "]");
                        } else {
                            NetWork.get().send("[sts~" + sts + "~" + AuthorizationFragment.DriverId + "]");
                        }
                        getActivity().finish();
                    }
                }).start();
            }
        });

        return v;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case com.kostanay.alex.pegasdriver.R.id.menu_item_info:
                SharedPreferences pref = getActivity().getApplicationContext().getSharedPreferences("MyPref", MODE_PRIVATE);
                String driverId = pref.getString("DriverId", null);
                String callsign = pref.getString("callsign", null);
                Toast.makeText(getActivity(), "Ваш позывной: " + callsign + "\nDriverId: " + driverId, Toast.LENGTH_LONG).show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

}
