package Logic;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by moise on 12/5/2017.
 */

public class Order implements Serializable{
    private int tableId;
    private ArrayList<String> items;

    public Order(){}

    public Order(int tableId, ArrayList<String> items) {
        this.tableId = tableId;
        this.items = items;
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
}
