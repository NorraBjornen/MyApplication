package com.kostanay.alex.pegasdriver.Controller.DistrictScreen;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import static android.app.Activity.RESULT_OK;

public class DistrictFragment extends Fragment {
    private RecyclerView RecyclerView;
    private Adapter Adapter;
    private List<District> data = new ArrayList<>();

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        data.add(new District("1 МКР", "53.174877, 63.605472"));
        data.add(new District("2 МКР", "53.178589, 63.606116"));
        data.add(new District("3 МКР", "53.176733, 63.612253"));
        data.add(new District("4 МКР", "53.181760, 63.608531"));
        data.add(new District("5 МКР", "53.172968, 63.593296"));
        data.add(new District("6 МКР", "53.177067, 63.590421"));
        data.add(new District("7 МКР", "53.181399, 63.589734"));
        data.add(new District("8 МКР", "53.185343, 63.590936"));
        data.add(new District("9 МКР", "53.184028, 63.601021"));
        data.add(new District("ФМЛ", "53.178127, 63.598422"));
        data.add(new District("КЖБИ", "53.169719, 63.602714"));
        data.add(new District("СХИ", "53.185464, 63.610930"));
        data.add(new District("НАУРЫЗ", "53.185696, 63.619151"));
        data.add(new District("ЕМШАН (ГАИ)", "53.191033, 63.612947"));
        data.add(new District("САДОВАЯ", "53.189498, 63.602630"));
        data.add(new District("ВОЕННЫЙ ГОРОДОК", "53.190483, 63.593212"));
        data.add(new District("НАРИМАНОВКА", "53.200706, 63.626840"));
        data.add(new District("ЖИЛОЙ РАЙОН НАРИМАНОВСКИЙ", "53.199066, 63.597911"));
        data.add(new District("ЗАПАДНЫЙ", "53.205921, 63.594392"));
        data.add(new District("МЕЛЬЗАВОД", "53.210005, 63.615048"));
        data.add(new District("ФРОЛОВА-БОРОДИНА", "53.213459, 63.608055"));
        data.add(new District("УРАЛЬСКИЙ", "53.216419, 63.597550"));
        data.add(new District("ЖД", "53.225875, 63.609349"));
        data.add(new District("ЦЕНТР", "53.215662, 63.629369"));
        data.add(new District("КОЛЁСНЫЕ РЯДЫ", "53.202834, 63.643761"));
        data.add(new District("ЦУМ", "53.208127, 63.646888"));
        data.add(new District("ТРУДОВЫЕ РЕЗЕРВЫ", "53.228411, 63.633294"));
        data.add(new District("НЕФТЕБАЗА", "53.232926, 63.620933"));
        data.add(new District("2 КОСТАНАЙ", "53.237952, 63.577363"));
        data.add(new District("ДК СТРОИТЕЛЬ", "53.234741, 63.638815"));
        data.add(new District("СКЛАДСКАЯ", "53.239154, 63.641938"));
        data.add(new District("КИЕВСКИЙ", "53.246115, 63.662000"));
        data.add(new District("КСК", "53.246641, 63.676837"));
        data.add(new District("ДОМ СТУДЕНТОВ", "53.241009, 63.688145"));
        data.add(new District("СЕВЕР", "53.257239, 63.695860"));




        View v = inflater.inflate(com.kostanay.alex.pegasdriver.R.layout.order_list_fragment, container, false);
        RecyclerView = v.findViewById(com.kostanay.alex.pegasdriver.R.id.order_recycler_view);
        RecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        Adapter = new Adapter();
        RecyclerView.setAdapter(Adapter);
        return v;
    }

    private class Holder extends RecyclerView.ViewHolder implements View.OnClickListener{
        private District District;
        private TextView district;
        public Holder(View itemView){
            super(itemView);
            itemView.setOnClickListener(this);
            district = itemView.findViewById(com.kostanay.alex.pegasdriver.R.id.district);
        }
        public void bind(District district){
            District = district;
            this.district.setText(district.getName());
        }

        public void onClick(View v) {
            String geo = District.getGeo();
            Intent intent = new Intent();
            intent.putExtra("geo", geo);
            getActivity().setResult(RESULT_OK, intent);
            getActivity().finish();
        }
    }

    private class Adapter extends RecyclerView.Adapter<Holder>{

        public Holder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
            View view = layoutInflater.inflate(com.kostanay.alex.pegasdriver.R.layout.list_item_district, parent, false);
            return new Holder(view);
        }

        public void onBindViewHolder(Holder holder, int position) {
            holder.bind(data.get(position));
        }

        public int getItemCount() {
            return data.size();
        }
    }

    private class District{
        private String Name;
        private String Geo;
        public District(String name, String geo){
            Name = name;
            Geo = geo;
        }

        public String getName() {
            return Name;
        }

        public String getGeo() {
            return Geo;
        }
    }
}
