package Logic;

import java.io.Serializable;

/**
 * Created by Emmanuel on 15/04/2018.
 */

public class MenuItem implements Serializable {

    private String name;
    private float price;

    public MenuItem() {
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
}
