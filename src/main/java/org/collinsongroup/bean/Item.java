package org.collinsongroup.bean;

public enum Item {
    COKE("Coke", 25), PEPSI("Pepsi", 35), SODA("Soda", 45);
    private final String name;
    private final int price;

    Item(String name, int price) {
        this.name = name;
        this.price = price;
    }

    public String getName() {
        return name;
    }

    public long getPrice() {
        return price;
    }
}

