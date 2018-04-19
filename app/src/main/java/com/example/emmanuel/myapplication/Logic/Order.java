package com.example.emmanuel.myapplication.Logic;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by moise on 12/5/2017.
 */

public class Order implements Serializable{
    private int tableId;
    private ArrayList<MenuItem> items;
    private String token;


    public Order(){}

    public Order(int tableId, ArrayList<MenuItem> items,String token) {
        this.tableId = tableId;
        this.items = items;
        this.token = token;

    }

    public int getTableId() {
        return tableId;
    }

    public void setTableId(int tableId) {
        this.tableId = tableId;
    }

    public ArrayList<MenuItem> getItems() {
        return items;
    }

    public void setItems(ArrayList<MenuItem> items) {
        this.items = items;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
