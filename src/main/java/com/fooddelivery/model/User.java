package com.fooddelivery.model;

public class User extends Person {
    private int id;
    private String address;

    public User(int id, String name, String email, String phone, String address) {
        super(name, email, phone);
        this.id = id;
        this.address = address;
    }

    public int getId() {
        return id;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    @Override
    public String toString() {
        return "User{id=" + id + ", name=" + getName() + ", address=" + address + "}";
    }
}
