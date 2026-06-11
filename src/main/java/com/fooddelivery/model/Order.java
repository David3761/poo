package com.fooddelivery.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Order {
    private int id;
    private User user;
    private Restaurant restaurant;
    private Driver driver;
    private List<OrderItem> items;
    private OrderStatus status;
    private String deliveryAddress;
    private LocalDateTime createdAt;

    public Order(int id, User user, Restaurant restaurant, String deliveryAddress) {
        this(id, user, restaurant, deliveryAddress, LocalDateTime.now());
    }

    // Folosit la reconstruirea comenzii din baza de date (pastreaza createdAt original)
    public Order(int id, User user, Restaurant restaurant, String deliveryAddress, LocalDateTime createdAt) {
        this.id = id;
        this.user = user;
        this.restaurant = restaurant;
        this.deliveryAddress = deliveryAddress;
        this.items = new ArrayList<>();
        this.status = OrderStatus.PENDING;
        this.createdAt = createdAt;
    }

    public int getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public Restaurant getRestaurant() {
        return restaurant;
    }

    public Driver getDriver() {
        return driver;
    }

    public void setDriver(Driver driver) {
        this.driver = driver;
    }

    public List<OrderItem> getItems() {
        return items;
    }

    public void addItem(OrderItem item) {
        items.add(item);
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public String getDeliveryAddress() {
        return deliveryAddress;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public double getTotal() {
        return items.stream().mapToDouble(OrderItem::getSubtotal).sum();
    }

    @Override
    public String toString() {
        return "Order{id=" + id + ", user=" + user.getName() + ", restaurant=" + restaurant.getName()
                + ", status=" + status + ", total=" + getTotal() + " RON}";
    }
}
