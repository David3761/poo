package com.fooddelivery.model;

import java.time.LocalDateTime;

public class Review {
    private int id;
    private User user;
    private Restaurant restaurant;
    private int rating;
    private String comment;
    private LocalDateTime createdAt;

    public Review(int id, User user, Restaurant restaurant, int rating, String comment) {
        this(id, user, restaurant, rating, comment, LocalDateTime.now());
    }

    // Folosit la reconstruirea review-ului din baza de date (pastreaza createdAt original)
    public Review(int id, User user, Restaurant restaurant, int rating, String comment, LocalDateTime createdAt) {
        this.id = id;
        this.user = user;
        this.restaurant = restaurant;
        setRating(rating);
        this.comment = comment;
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

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        if (rating < 1 || rating > 5) {
            throw new IllegalArgumentException("Rating must be between 1 and 5, got: " + rating);
        }
        this.rating = rating;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    @Override
    public String toString() {
        return "Review{user=" + user.getName() + ", restaurant=" + restaurant.getName()
                + ", rating=" + rating + "/5, comment=\"" + comment + "\"}";
    }
}
