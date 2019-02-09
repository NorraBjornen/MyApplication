package com.kostanay.alex.pegasdriver.Model;

public class Order {
    private String OrderNumber, FromW, ToW, Price, OfferedPrice, Time, Description, Phone;
    private boolean isPriceOffered;
    private boolean Hide = false;

    public String getOrderNumber() {
        return OrderNumber;
    }

    public void setOrderNumber(String orderNumber) {
        OrderNumber = orderNumber;
    }

    public String getFromW() {
        return FromW;
    }

    public void setFromW(String fromW) {
        FromW = fromW;
    }

    public String getToW() {
        return ToW;
    }

    public void setToW(String toW) {
        ToW = toW;
    }

    public String getPrice() {
        return Price;
    }

    public void setPrice(String price) {
        Price = price;
    }

    public String getOfferedPrice() {
        return OfferedPrice;
    }

    public void setOfferedPrice(String offeredPrice) {
        OfferedPrice = offeredPrice;
    }

    public boolean isPriceOffered() {
        return isPriceOffered;
    }

    public void setPriceOffered(boolean priceOffered) {
        isPriceOffered = priceOffered;
    }

    public String getTime() {
        return Time;
    }

    public void setTime(String time) {
        Time = time;
    }

    public String getDescription() {
        return Description;
    }

    public void setDescription(String description) {
        Description = description;
    }

    public String getPhone() {
        return Phone;
    }

    public void setPhone(String phone) {
        Phone = phone;
    }

    public void setHide(){
        Hide = false;
    }

    public boolean isHide() {
        return Hide;
    }

    public void hide(){
        Hide = true;
    }
}
