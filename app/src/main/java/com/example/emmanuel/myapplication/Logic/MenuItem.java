package com.example.emmanuel.myapplication.Logic;

import java.io.Serializable;

/**
 * Created by Emmanuel on 15/04/2018.
 */

public class MenuItem implements Serializable {

    private String name;
    private float price;
    private int quantity = 0;

    public MenuItem() {
        quantity = 0;
    }

    public MenuItem(String name, float price) {
        this.name = name;
        this.price = price;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public float getPrice() {
        return price;
    }

    public void setPrice(float price) {
        this.price = price;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public int changeQuantity(int valueToAdd){
        quantity = quantity + valueToAdd;
        if(quantity < 0)
            quantity = 0;
        return quantity;
    }
}
