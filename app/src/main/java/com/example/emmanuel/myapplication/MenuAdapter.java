package com.example.emmanuel.myapplication;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import com.example.emmanuel.myapplication.Logic.MenuItem;

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
        holder.quantity.setText(String.valueOf(menuItems.get(position).getQuantity()));
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

    public class MenuItemHolder extends  RecyclerView.ViewHolder implements View.OnClickListener{

        public TextView name, price, quantity;
        public ImageView addBtn, removeBtn;
        public MenuItemHolder(View itemView) {
            super(itemView);
            name = itemView.findViewById( R.id.item_name);
            price = itemView.findViewById( R.id.item_price);
            quantity = itemView.findViewById(R.id.item_quantity);
            removeBtn = itemView.findViewById(R.id.item_remove);
            removeBtn.setOnClickListener(this);
            addBtn = itemView.findViewById(R.id.item_add);
            addBtn.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (view.getId() == R.id.item_add){
                //getAdapterPosition me retorna la posicion de mi holder.
                //Esta posicion es la misma que tiene en el elemento del arreglo items utilizado por el holder
                int newQuantity = menuItems.get(getAdapterPosition()).changeQuantity(1);
                updateQuantityText(newQuantity);
            }
            if (view.getId() == R.id.item_remove){
                int newQuantity = menuItems.get(getAdapterPosition()).changeQuantity(-1);
                updateQuantityText(newQuantity);
            }
        }

        public void  updateQuantityText(int value){
            quantity.setText(String.valueOf(value));
        }

    }
}


