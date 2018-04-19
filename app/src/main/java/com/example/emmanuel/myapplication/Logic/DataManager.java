package com.example.emmanuel.myapplication.Logic;

import android.content.Context;
import android.util.Log;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.androidnetworking.interfaces.ParsedRequestListener;
import com.example.emmanuel.myapplication.MenuItemsListener;
import com.example.emmanuel.myapplication.OrderSentListener;
import com.example.emmanuel.myapplication.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Emmanuel on 15/04/2018.
 */

public class DataManager {

    private String baseUrl = "";
    static DataManager instance;
    private ArrayList<MenuItemsListener> menuItemsListeners;
    private ArrayList<OrderSentListener> orderSentListeners;
    private List<MenuItem> menuItems;
    private DataManager(){
        orderSentListeners = new ArrayList<>();
        menuItemsListeners = new ArrayList<>();
    }

    public static DataManager getInstance() {
        if( instance == null){
            instance = new DataManager();
        }
        return instance;
    }

    public void setMenuItemsListeners(MenuItemsListener menuItemsListener){
        menuItemsListeners.add(menuItemsListener);
    }
    public void setOrderSentListener(OrderSentListener orderSentListener){
        orderSentListeners.add(orderSentListener);
    }
    public void initClient(Context appContext){

        baseUrl = appContext.getString(R.string.server_address) +"/api/";
        AndroidNetworking.initialize(appContext);
    }

    public void getMenuItems(){
        AndroidNetworking.get(baseUrl + "items")
                .setPriority(Priority.LOW)
                .build()
                .getAsObjectList(MenuItem.class, new ParsedRequestListener<List<MenuItem>>() {
                    @Override
                    public void onResponse(List<MenuItem> menuItemList) {
                        // do anything with response
                        menuItems = menuItemList;

                        for (MenuItemsListener m : menuItemsListeners) {
                            m.OnDataReceived(menuItems);
                        }
                    }
                    @Override
                    public void onError(ANError anError) {
                        // handle error
                    }
                });

    }

    public void sendOrder(String order) throws JSONException{
        AndroidNetworking.post(baseUrl + "orders")
                .addJSONObjectBody(new JSONObject(order))
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d("RECEIVED", response.toString());
                        for (OrderSentListener listener : orderSentListeners) {
                            listener.onOrderSent(true);
                        }
                    }

                    @Override
                    public void onError(ANError anError) {

                        for (OrderSentListener listener : orderSentListeners) {
                            listener.onOrderSent(false);
                        }

                        Log.d("RECEIVED", "ERROR:" + anError.getErrorCode());
                        Log.d("RECEIVED", anError.getErrorBody());
                    }
                });

    }

    public boolean isOrderEmpty(){
        for (MenuItem menuItem : menuItems){
            if(menuItem.getQuantity() > 0)
                return false;
        }
        return true;
    }

    public ArrayList<MenuItem> getOderedItems(){
        ArrayList<MenuItem> items = new ArrayList<>();
        for (MenuItem menuItem : menuItems){
            if(menuItem.getQuantity() > 0)
                items.add(menuItem);
        }

        return items;
    }





}
