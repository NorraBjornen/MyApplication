package com.kostanay.alex.pegasdriver.Model;

import java.util.ArrayList;
import java.util.List;

public class OrderRepository {
    private static OrderRepository OrderRepository;
    private List<Order> Orders;
    public boolean newOrder = false;

    public static OrderRepository get(){
        if(OrderRepository == null){
            OrderRepository = new OrderRepository();
        }
        return OrderRepository;
    }

    private OrderRepository(){
        Orders = new ArrayList<>();
    }

    public List<Order> getOrders() {
        return Orders;
    }

    public Order getOrder(String orderNumber){
        for(Order order: Orders){
            if(order.getOrderNumber().equals(orderNumber)){
                return order;
            }
        }
        return null;
    }

    public void addOrder(Order order){
        Orders.add(order);
    }

    public void removeOrder(String orderNumber){
        Order or = new Order();
        for(Order order : Orders){
            if(order.getOrderNumber().equals(orderNumber)){
                or = order;
            }
        }
        Orders.remove(or);
    }

    public void removeOtherPrices(String orderNumber){
        for(Order order: Orders){
            if(!order.getOrderNumber().equals(orderNumber)){
               order.setPriceOffered(false);
               order.setOfferedPrice("");
            }
        }
    }

    public void clear(){
        Orders.clear();
    }

}
