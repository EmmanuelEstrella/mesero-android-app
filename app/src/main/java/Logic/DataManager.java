package Logic;

import android.content.Context;
import android.util.Log;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONArrayRequestListener;
import com.androidnetworking.interfaces.ParsedRequestListener;
import com.example.emmanuel.myapplication.MenuItemsListener;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Emmanuel on 15/04/2018.
 */

public class DataManager {

    private String baseUrl = "http://192.168.137.1:8000/api/";
    static DataManager instance;
    private ArrayList<MenuItemsListener> menuItemsListeners;
    private DataManager(){
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
    public void initClient(Context appContext){
        AndroidNetworking.initialize(appContext);
    }

    public void getMenuItems(){
        AndroidNetworking.get(baseUrl + "items")
                .setPriority(Priority.LOW)
                .build()
                .getAsObjectList(MenuItem.class, new ParsedRequestListener<List<MenuItem>>() {
                    @Override
                    public void onResponse(List<MenuItem> menuItems) {
                        // do anything with response


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





}
