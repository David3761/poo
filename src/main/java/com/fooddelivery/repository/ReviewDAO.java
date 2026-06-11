package com.fooddelivery.repository;

import com.fooddelivery.db.DatabaseConnection;
import com.fooddelivery.model.Restaurant;
import com.fooddelivery.model.Review;
import com.fooddelivery.model.User;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ReviewDAO implements GenericDAO<Review> {

    private static ReviewDAO instance;

    private ReviewDAO() throws SQLException {
        createTable();
    }

    public static ReviewDAO getInstance() throws SQLException {
        if (instance == null) {
            instance = new ReviewDAO();
        }
        return instance;
    }

    private void createTable() throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS reviews (" +
                "id INTEGER PRIMARY KEY," +
                "user_id INTEGER NOT NULL," +
                "restaurant_id INTEGER NOT NULL," +
                "rating INTEGER NOT NULL," +
                "comment TEXT NOT NULL," +
                "created_at TEXT NOT NULL)";
        try (Statement stmt = DatabaseConnection.getInstance().getConnection().createStatement()) {
            stmt.execute(sql);
        }
    }

    @Override
    public void create(Review review) throws SQLException {
        String sql = "INSERT OR REPLACE INTO reviews (id, user_id, restaurant_id, rating, comment, created_at) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = DatabaseConnection.getInstance().getConnection().prepareStatement(sql)) {
            ps.setInt(1, review.getId());
            ps.setInt(2, review.getUser().getId());
            ps.setInt(3, review.getRestaurant().getId());
            ps.setInt(4, review.getRating());
            ps.setString(5, review.getComment());
            ps.setString(6, review.getCreatedAt().toString());
            ps.executeUpdate();
        }
    }

    @Override
    public Review read(int id) throws SQLException {
        String sql = "SELECT * FROM reviews WHERE id = ?";
        try (PreparedStatement ps = DatabaseConnection.getInstance().getConnection().prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return buildReviewFromResultSet(rs);
            }
        }
        return null;
    }

    @Override
    public List<Review> readAll() throws SQLException {
        List<Review> reviews = new ArrayList<>();
        String sql = "SELECT * FROM reviews";
        try (Statement stmt = DatabaseConnection.getInstance().getConnection().createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                reviews.add(buildReviewFromResultSet(rs));
            }
        }
        return reviews;
    }

    private Review buildReviewFromResultSet(ResultSet rs) throws SQLException {
        User user = UserDAO.getInstance().read(rs.getInt("user_id"));
        Restaurant restaurant = RestaurantDAO.getInstance().read(rs.getInt("restaurant_id"));
        LocalDateTime createdAt = LocalDateTime.parse(rs.getString("created_at"));
        return new Review(rs.getInt("id"), user, restaurant,
                rs.getInt("rating"), rs.getString("comment"), createdAt);
    }

    @Override
    public void update(Review review) throws SQLException {
        String sql = "UPDATE reviews SET user_id = ?, restaurant_id = ?, rating = ?, comment = ? WHERE id = ?";
        try (PreparedStatement ps = DatabaseConnection.getInstance().getConnection().prepareStatement(sql)) {
            ps.setInt(1, review.getUser().getId());
            ps.setInt(2, review.getRestaurant().getId());
            ps.setInt(3, review.getRating());
            ps.setString(4, review.getComment());
            ps.setInt(5, review.getId());
            ps.executeUpdate();
        }
    }

    @Override
    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM reviews WHERE id = ?";
        try (PreparedStatement ps = DatabaseConnection.getInstance().getConnection().prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }
}
