package com.example.emmanuel.myapplication.Logic;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by moise on 12/5/2017.
 */

public class Order implements Serializable{
    private int tableId;
    private ArrayList<String> items;
    private int positionA;
    private int positionB;
    private int positionC;

    public Order(){}

    public Order(int tableId, ArrayList<String> items,int positionA, int positionB, int positionC) {
        this.tableId = tableId;
        this.items = items;
        this.positionA = positionA;
        this.positionB = positionB;
        this.positionC = positionC;
    }

    public int getTableId() {
        return tableId;
    }

    public void setTableId(int tableId) {
        this.tableId = tableId;
    }

    public ArrayList<String> getItems() {
        return items;
    }

    public void setItems(ArrayList<String> items) {
        this.items = items;
    }

    public int getPositionA() {return positionA;}

    public void setPositionA(int positionA) {
        this.positionA = positionA;
    }

    public int getPositionB() {return positionB;}

    public void setPositionB(int positionB) {
        this.positionB = positionB;
    }

    public int getPositionC() {return positionC;}

    public void setPositionC(int positionC) {
        this.positionC = positionC;
    }


}
