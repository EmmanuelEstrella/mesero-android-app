package com.example.emmanuel.myapplication;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import Logic.MenuItem;

/**
 * Created by Emmanuel on 15/04/2018.
 */

public class MenuAdapter extends RecyclerView.Adapter<MenuAdapter.MenuItemHolder> implements MenuItemsListener{


    ArrayList<MenuItem> menuItems;

    public MenuAdapter() {
        this.menuItems = new ArrayList<>();
    }


    @Override
    public MenuItemHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View holder = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.menu_item, parent, false);
        return new MenuItemHolder(holder);
    }

    @Override
    public void onBindViewHolder(MenuItemHolder holder, int position) {
        holder.name.setText(menuItems.get(position).getName());
        holder.price.setText( String.valueOf(menuItems.get(position).getPrice()) );
    }


    @Override
    public int getItemCount() {
        return menuItems.size();
    }

    @Override
    public void OnDataReceived(List<MenuItem> menuItems) {
        this.menuItems = new ArrayList<>(menuItems);
        notifyDataSetChanged();

    }

    public class MenuItemHolder extends  RecyclerView.ViewHolder{

        public TextView name, price;
        public MenuItemHolder(View itemView) {
            super(itemView);
            name = itemView.findViewById( R.id.item_name);
            price = itemView.findViewById( R.id.item_price);
        }
    }
}


