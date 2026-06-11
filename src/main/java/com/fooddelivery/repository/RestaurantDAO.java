package com.fooddelivery.repository;

import com.fooddelivery.db.DatabaseConnection;
import com.fooddelivery.model.Category;
import com.fooddelivery.model.MenuItem;
import com.fooddelivery.model.Restaurant;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RestaurantDAO implements GenericDAO<Restaurant> {

    private static RestaurantDAO instance;

    private RestaurantDAO() throws SQLException {
        createTable();
    }

    public static RestaurantDAO getInstance() throws SQLException {
        if (instance == null) {
            instance = new RestaurantDAO();
        }
        return instance;
    }

    private void createTable() throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS restaurants (" +
                "id INTEGER PRIMARY KEY," +
                "name TEXT NOT NULL," +
                "address TEXT NOT NULL," +
                "category_id INTEGER NOT NULL," +
                "category_name TEXT NOT NULL)";
        try (Statement stmt = DatabaseConnection.getInstance().getConnection().createStatement()) {
            stmt.execute(sql);
        }
    }

    @Override
    public void create(Restaurant restaurant) throws SQLException {
        String sql = "INSERT OR REPLACE INTO restaurants (id, name, address, category_id, category_name) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = DatabaseConnection.getInstance().getConnection().prepareStatement(sql)) {
            ps.setInt(1, restaurant.getId());
            ps.setString(2, restaurant.getName());
            ps.setString(3, restaurant.getAddress());
            ps.setInt(4, restaurant.getCategory().getId());
            ps.setString(5, restaurant.getCategory().getName());
            ps.executeUpdate();
        }
        for (MenuItem item : restaurant.getMenu()) {
            MenuItemDAO.getInstance().create(item, restaurant.getId());
        }
    }

    @Override
    public Restaurant read(int id) throws SQLException {
        String sql = "SELECT * FROM restaurants WHERE id = ?";
        try (PreparedStatement ps = DatabaseConnection.getInstance().getConnection().prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return buildRestaurantFromResultSet(rs);
            }
        }
        return null;
    }

    @Override
    public List<Restaurant> readAll() throws SQLException {
        List<Restaurant> restaurants = new ArrayList<>();
        String sql = "SELECT * FROM restaurants";
        try (Statement stmt = DatabaseConnection.getInstance().getConnection().createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                restaurants.add(buildRestaurantFromResultSet(rs));
            }
        }
        return restaurants;
    }

    private Restaurant buildRestaurantFromResultSet(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        Category category = new Category(rs.getInt("category_id"), rs.getString("category_name"));
        Restaurant restaurant = new Restaurant(id, rs.getString("name"), rs.getString("address"), category);
        for (MenuItem item : MenuItemDAO.getInstance().readByRestaurant(id)) {
            restaurant.addMenuItem(item);
        }
        return restaurant;
    }

    @Override
    public void update(Restaurant restaurant) throws SQLException {
        String sql = "UPDATE restaurants SET name = ?, address = ?, category_id = ?, category_name = ? WHERE id = ?";
        try (PreparedStatement ps = DatabaseConnection.getInstance().getConnection().prepareStatement(sql)) {
            ps.setString(1, restaurant.getName());
            ps.setString(2, restaurant.getAddress());
            ps.setInt(3, restaurant.getCategory().getId());
            ps.setString(4, restaurant.getCategory().getName());
            ps.setInt(5, restaurant.getId());
            ps.executeUpdate();
        }
    }

    @Override
    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM restaurants WHERE id = ?";
        try (PreparedStatement ps = DatabaseConnection.getInstance().getConnection().prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }
}
