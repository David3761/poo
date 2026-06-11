package com.fooddelivery.model;

public class Driver extends Person {
    private int id;
    private String vehiclePlate;
    private boolean available;

    public Driver(int id, String name, String email, String phone, String vehiclePlate) {
        super(name, email, phone);
        this.id = id;
        this.vehiclePlate = vehiclePlate;
        this.available = true;
    }

    public int getId() {
        return id;
    }

    public String getVehiclePlate() {
        return vehiclePlate;
    }

    public void setVehiclePlate(String vehiclePlate) {
        this.vehiclePlate = vehiclePlate;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    @Override
    public String toString() {
        return "Driver{id=" + id + ", name=" + getName() + ", plate=" + vehiclePlate + ", available=" + available + "}";
    }
}
