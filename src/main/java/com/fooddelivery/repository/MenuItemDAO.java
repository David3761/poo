package com.fooddelivery.repository;

import com.fooddelivery.db.DatabaseConnection;
import com.fooddelivery.model.MenuItem;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MenuItemDAO implements GenericDAO<MenuItem> {

    private static MenuItemDAO instance;

    private MenuItemDAO() throws SQLException {
        createTable();
    }

    public static MenuItemDAO getInstance() throws SQLException {
        if (instance == null) {
            instance = new MenuItemDAO();
        }
        return instance;
    }

    private void createTable() throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS menu_items (" +
                "id INTEGER PRIMARY KEY," +
                "restaurant_id INTEGER NOT NULL," +
                "name TEXT NOT NULL," +
                "price REAL NOT NULL," +
                "description TEXT NOT NULL)";
        try (Statement stmt = DatabaseConnection.getInstance().getConnection().createStatement()) {
            stmt.execute(sql);
        }
    }

    @Override
    public void create(MenuItem item) throws SQLException {
        throw new UnsupportedOperationException("Foloseste create(item, restaurantId)");
    }

    public void create(MenuItem item, int restaurantId) throws SQLException {
        String sql = "INSERT OR REPLACE INTO menu_items (id, restaurant_id, name, price, description) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = DatabaseConnection.getInstance().getConnection().prepareStatement(sql)) {
            ps.setInt(1, item.getId());
            ps.setInt(2, restaurantId);
            ps.setString(3, item.getName());
            ps.setDouble(4, item.getPrice());
            ps.setString(5, item.getDescription());
            ps.executeUpdate();
        }
    }

    @Override
    public MenuItem read(int id) throws SQLException {
        String sql = "SELECT * FROM menu_items WHERE id = ?";
        try (PreparedStatement ps = DatabaseConnection.getInstance().getConnection().prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return new MenuItem(rs.getInt("id"), rs.getString("name"),
                        rs.getDouble("price"), rs.getString("description"));
            }
        }
        return null;
    }

    public List<MenuItem> readByRestaurant(int restaurantId) throws SQLException {
        List<MenuItem> items = new ArrayList<>();
        String sql = "SELECT * FROM menu_items WHERE restaurant_id = ?";
        try (PreparedStatement ps = DatabaseConnection.getInstance().getConnection().prepareStatement(sql)) {
            ps.setInt(1, restaurantId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                items.add(new MenuItem(rs.getInt("id"), rs.getString("name"),
                        rs.getDouble("price"), rs.getString("description")));
            }
        }
        return items;
    }

    @Override
    public List<MenuItem> readAll() throws SQLException {
        List<MenuItem> items = new ArrayList<>();
        String sql = "SELECT * FROM menu_items";
        try (Statement stmt = DatabaseConnection.getInstance().getConnection().createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                items.add(new MenuItem(rs.getInt("id"), rs.getString("name"),
                        rs.getDouble("price"), rs.getString("description")));
            }
        }
        return items;
    }

    @Override
    public void update(MenuItem item) throws SQLException {
        String sql = "UPDATE menu_items SET name = ?, price = ?, description = ? WHERE id = ?";
        try (PreparedStatement ps = DatabaseConnection.getInstance().getConnection().prepareStatement(sql)) {
            ps.setString(1, item.getName());
            ps.setDouble(2, item.getPrice());
            ps.setString(3, item.getDescription());
            ps.setInt(4, item.getId());
            ps.executeUpdate();
        }
    }

    @Override
    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM menu_items WHERE id = ?";
        try (PreparedStatement ps = DatabaseConnection.getInstance().getConnection().prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }
}
