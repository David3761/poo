package com.fooddelivery.repository;

import com.fooddelivery.db.DatabaseConnection;
import com.fooddelivery.model.MenuItem;
import com.fooddelivery.model.Order;
import com.fooddelivery.model.OrderItem;
import com.fooddelivery.model.OrderStatus;
import com.fooddelivery.model.Restaurant;
import com.fooddelivery.model.User;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class OrderDAO implements GenericDAO<Order> {

    private static OrderDAO instance;

    private OrderDAO() throws SQLException {
        createTable();
    }

    public static OrderDAO getInstance() throws SQLException {
        if (instance == null) {
            instance = new OrderDAO();
        }
        return instance;
    }

    private void createTable() throws SQLException {
        Connection con = DatabaseConnection.getInstance().getConnection();
        try (Statement stmt = con.createStatement()) {
            stmt.execute("CREATE TABLE IF NOT EXISTS orders (" +
                    "id INTEGER PRIMARY KEY," +
                    "user_id INTEGER NOT NULL," +
                    "restaurant_id INTEGER NOT NULL," +
                    "driver_id INTEGER," +
                    "status TEXT NOT NULL," +
                    "delivery_address TEXT NOT NULL," +
                    "created_at TEXT NOT NULL)");
            stmt.execute("CREATE TABLE IF NOT EXISTS order_items (" +
                    "order_id INTEGER NOT NULL," +
                    "menu_item_id INTEGER NOT NULL," +
                    "quantity INTEGER NOT NULL," +
                    "PRIMARY KEY (order_id, menu_item_id))");
        }
    }

    @Override
    public void create(Order order) throws SQLException {
        Connection con = DatabaseConnection.getInstance().getConnection();
        String sql = "INSERT OR REPLACE INTO orders (id, user_id, restaurant_id, driver_id, status, delivery_address, created_at) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, order.getId());
            ps.setInt(2, order.getUser().getId());
            ps.setInt(3, order.getRestaurant().getId());
            if (order.getDriver() != null) {
                ps.setInt(4, order.getDriver().getId());
            } else {
                ps.setNull(4, Types.INTEGER);
            }
            ps.setString(5, order.getStatus().name());
            ps.setString(6, order.getDeliveryAddress());
            ps.setString(7, order.getCreatedAt().toString());
            ps.executeUpdate();
        }
        String itemSql = "INSERT OR REPLACE INTO order_items (order_id, menu_item_id, quantity) VALUES (?, ?, ?)";
        try (PreparedStatement ps = con.prepareStatement(itemSql)) {
            for (OrderItem item : order.getItems()) {
                ps.setInt(1, order.getId());
                ps.setInt(2, item.getMenuItem().getId());
                ps.setInt(3, item.getQuantity());
                ps.executeUpdate();
            }
        }
    }

    @Override
    public Order read(int id) throws SQLException {
        String sql = "SELECT * FROM orders WHERE id = ?";
        try (PreparedStatement ps = DatabaseConnection.getInstance().getConnection().prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return buildOrderFromResultSet(rs);
            }
        }
        return null;
    }

    @Override
    public List<Order> readAll() throws SQLException {
        List<Order> orders = new ArrayList<>();
        String sql = "SELECT * FROM orders";
        try (Statement stmt = DatabaseConnection.getInstance().getConnection().createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                orders.add(buildOrderFromResultSet(rs));
            }
        }
        return orders;
    }

    @Override
    public void update(Order order) throws SQLException {
        String sql = "UPDATE orders SET user_id = ?, restaurant_id = ?, driver_id = ?, status = ?, delivery_address = ? WHERE id = ?";
        try (PreparedStatement ps = DatabaseConnection.getInstance().getConnection().prepareStatement(sql)) {
            ps.setInt(1, order.getUser().getId());
            ps.setInt(2, order.getRestaurant().getId());
            if (order.getDriver() != null) {
                ps.setInt(3, order.getDriver().getId());
            } else {
                ps.setNull(3, Types.INTEGER);
            }
            ps.setString(4, order.getStatus().name());
            ps.setString(5, order.getDeliveryAddress());
            ps.setInt(6, order.getId());
            ps.executeUpdate();
        }
    }

    @Override
    public void delete(int id) throws SQLException {
        Connection con = DatabaseConnection.getInstance().getConnection();
        String deleteItems = "DELETE FROM order_items WHERE order_id = ?";
        try (PreparedStatement ps = con.prepareStatement(deleteItems)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
        String deleteOrder = "DELETE FROM orders WHERE id = ?";
        try (PreparedStatement ps = con.prepareStatement(deleteOrder)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    private Order buildOrderFromResultSet(ResultSet rs) throws SQLException {
        int orderId = rs.getInt("id");
        User user = UserDAO.getInstance().read(rs.getInt("user_id"));
        Restaurant restaurant = RestaurantDAO.getInstance().read(rs.getInt("restaurant_id"));
        String deliveryAddress = rs.getString("delivery_address");
        OrderStatus status = OrderStatus.valueOf(rs.getString("status"));
        LocalDateTime createdAt = LocalDateTime.parse(rs.getString("created_at"));

        Order order = new Order(orderId, user, restaurant, deliveryAddress, createdAt);
        order.setStatus(status);

        int driverId = rs.getInt("driver_id");
        if (driverId != 0) {
            order.setDriver(DriverDAO.getInstance().read(driverId));
        }

        // Incarca itemele comenzii
        String itemSql = "SELECT * FROM order_items WHERE order_id = ?";
        try (PreparedStatement ps = DatabaseConnection.getInstance().getConnection().prepareStatement(itemSql)) {
            ps.setInt(1, orderId);
            ResultSet itemRs = ps.executeQuery();
            while (itemRs.next()) {
                MenuItem menuItem = MenuItemDAO.getInstance().read(itemRs.getInt("menu_item_id"));
                order.addItem(new OrderItem(menuItem, itemRs.getInt("quantity")));
            }
        }

        return order;
    }
}
