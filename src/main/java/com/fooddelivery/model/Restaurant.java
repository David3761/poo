package com.fooddelivery.model;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class Restaurant implements Comparable<Restaurant> {
    private int id;
    private String name;
    private String address;
    private Category category;
    private List<MenuItem> menu;
    private List<Review> reviews;

    public static final Comparator<Restaurant> BY_RATING = Comparator.comparingDouble(Restaurant::getAverageRating)
            .reversed();

    public Restaurant(int id, String name, String address, Category category) {
        this.id = id;
        this.name = name;
        this.address = address;
        this.category = category;
        this.menu = new ArrayList<>();
        this.reviews = new ArrayList<>();
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public List<MenuItem> getMenu() {
        return menu;
    }

    public void addMenuItem(MenuItem item) {
        menu.add(item);
    }

    public List<Review> getReviews() {
        return reviews;
    }

    public void addReview(Review review) {
        reviews.add(review);
    }

    public double getAverageRating() {
        if (reviews.isEmpty())
            return 0.0;
        return reviews.stream().mapToInt(Review::getRating).average().orElse(0.0);
    }

    @Override
    public int compareTo(Restaurant other) {
        return this.name.compareTo(other.name);
    }

    @Override
    public String toString() {
        return "Restaurant{id=" + id + ", name=" + name + ", category=" + category.getName()
                + ", rating=" + String.format("%.1f", getAverageRating()) + "}";
    }
}
