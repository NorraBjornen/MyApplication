package com.kostanay.alex.pegasdriver.Controller.ListScreen;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.kostanay.alex.pegasdriver.Controller.AuthorizationScreen.AuthorizationFragment;
import com.kostanay.alex.pegasdriver.Controller.DetailedScreen.DetailedActivity;
import com.kostanay.alex.pegasdriver.Controller.DetailedScreen.DetailedFragment;
import com.kostanay.alex.pegasdriver.Controller.Exit;;
import com.kostanay.alex.pegasdriver.Controller.Info;
import com.kostanay.alex.pegasdriver.Controller.SettingsScreen.SettingsActivity;
import com.kostanay.alex.pegasdriver.Controller.SwipeController;
import com.kostanay.alex.pegasdriver.Controller.SwipeControllerActions;
import com.kostanay.alex.pegasdriver.Model.NetWork;
import com.kostanay.alex.pegasdriver.Model.Order;
import com.kostanay.alex.pegasdriver.Model.OrderRepository;
import com.kostanay.alex.pegasdriver.Services.MyService;

import java.util.ArrayList;
import java.util.List;

public class ListFragment extends Fragment {
    private RecyclerView OrderRecyclerView;
    private OrderAdapter Adapter;
    public static Handler handler;
    private List<Order> Orders;
    public static boolean active;

    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(com.kostanay.alex.pegasdriver.R.menu.menu, menu);
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(com.kostanay.alex.pegasdriver.R.layout.order_list_fragment, container, false);
        OrderRecyclerView = v.findViewById(com.kostanay.alex.pegasdriver.R.id.order_recycler_view);
        OrderRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        Orders = new ArrayList<>();
        Orders.addAll(OrderRepository.get().getOrders());
        Adapter = new OrderAdapter();
        OrderRecyclerView.setAdapter(Adapter);

        SwipeController swipeController = new SwipeController(new SwipeControllerActions() {
            @Override
            public void onRightClicked(int position) {
                OrderRepository.get().getOrder(Orders.get(position).getOrderNumber()).hide();
                handler.sendEmptyMessage(7);
            }
        });
        ItemTouchHelper itemTouchhelper = new ItemTouchHelper(swipeController);
        itemTouchhelper.attachToRecyclerView(OrderRecyclerView);

        handler = new Handler() {
            public void handleMessage(Message msg) {
                if(msg.obj != null){
                    String message = (String) msg.obj;
                    try {
                        FragmentManager manager = getFragmentManager();
                        Info dialog = Info.newInstance(message);
                        dialog.show(manager, "DialogDate");
                    } catch (Exception e){
                        msg = Message.obtain();
                        msg.obj = message;
                        msg.setTarget(DetailedFragment.handler);
                        msg.sendToTarget();
                    }
                } else {
                    switch (msg.what) {
                        case 9:
                            Toast.makeText(getActivity(), "Теперь Ваш статус:\nСвободен", Toast.LENGTH_SHORT).show();
                            break;
                        case 10:
                            Toast.makeText(getActivity(), "Теперь Ваш статус:\nЗанят", Toast.LENGTH_SHORT).show();
                            break;
                        case 4:
                            try {
                                getActivity().finish();
                            } catch (Exception e) {
                            }
                            break;
                        case 5:
                            try {
                                Toast.makeText(getActivity(), "Ваш баланс:\n" + MyService.Balance, Toast.LENGTH_SHORT).show();
                            } catch (Exception e) {
                            }
                            break;
                        case 6:
                            Toast.makeText(getActivity(), "Вышел", Toast.LENGTH_SHORT).show();
                            break;
                        case 7:
                            Orders = new ArrayList<>();

                            for (Order order : OrderRepository.get().getOrders()) {
                                if (!order.isHide()) {
                                    Orders.add(order);
                                }
                            }

                            Adapter.notifyDataSetChanged();
                            break;
                        case 98:
                            try {
                                ((ListActivity) getActivity()).setActionBarTitle("НЕТ СОЕДИНЕНИЯ");
                            } catch (Exception e) {
                            }
                            break;
                        case 99:
                            try {
                                ((ListActivity) getActivity()).setActionBarTitle("Пегас");
                            } catch (Exception e) {
                            }
                            break;
                    }
                }
            }
        };
        return v;
    }

    private class OrderHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        private TextView From, To, Price, OfferedPrice;
        private Order Order;
        public OrderHolder(View itemView){
            super(itemView);
            itemView.setOnClickListener(this);
            From = itemView.findViewById(com.kostanay.alex.pegasdriver.R.id.from);
            To = itemView.findViewById(com.kostanay.alex.pegasdriver.R.id.to);
            Price = itemView.findViewById(com.kostanay.alex.pegasdriver.R.id.price);
            OfferedPrice = itemView.findViewById(com.kostanay.alex.pegasdriver.R.id.offered_price);
        }
        public void bindOrder(Order order){
            Order = order;
            From.setText(order.getFromW());
            To.setText(order.getToW());
            Price.setText(order.getPrice());
            if(Order.isPriceOffered()){
                OfferedPrice.setText(Order.getOfferedPrice());
            } else {
                OfferedPrice.setText("");
            }
        }
        public void onClick(View v) {
            Intent intent = DetailedActivity.newIntent(getActivity(), Order.getOrderNumber());
            startActivity(intent);
        }
    }

    private class OrderAdapter extends RecyclerView.Adapter<OrderHolder>{

        public OrderHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
            View view = layoutInflater.inflate(com.kostanay.alex.pegasdriver.R.layout.list_item_order, parent, false);
            return new OrderHolder(view);
        }

        public void onBindViewHolder(OrderHolder holder, int position) {
            holder.bindOrder(Orders.get(position));
        }

        public int getItemCount() {
            return Orders.size();
        }
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

    public void onResume(){
        super.onResume();
        handler.sendEmptyMessage(7);
        active = true;

    }

    public void onPause(){
        super.onPause();
        active = false;
    }
}
