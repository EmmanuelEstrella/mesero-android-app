package com.example.emmanuel.myapplication;

import java.util.List;

import com.example.emmanuel.myapplication.Logic.MenuItem;

/**
 * Created by Emmanuel on 15/04/2018.
 */

public interface MenuItemsListener {

    void OnDataReceived(List<MenuItem> menuItems);
}
