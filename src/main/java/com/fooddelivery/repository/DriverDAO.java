package com.fooddelivery.repository;

import com.fooddelivery.db.DatabaseConnection;
import com.fooddelivery.model.Driver;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DriverDAO implements GenericDAO<Driver> {

    private static DriverDAO instance;

    private DriverDAO() throws SQLException {
        createTable();
    }

    public static DriverDAO getInstance() throws SQLException {
        if (instance == null) {
            instance = new DriverDAO();
        }
        return instance;
    }

    private void createTable() throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS drivers (" +
                "id INTEGER PRIMARY KEY," +
                "name TEXT NOT NULL," +
                "email TEXT NOT NULL," +
                "phone TEXT NOT NULL," +
                "vehicle_plate TEXT NOT NULL," +
                "available INTEGER NOT NULL)";
        try (Statement stmt = DatabaseConnection.getInstance().getConnection().createStatement()) {
            stmt.execute(sql);
        }
    }

    @Override
    public void create(Driver driver) throws SQLException {
        String sql = "INSERT OR REPLACE INTO drivers (id, name, email, phone, vehicle_plate, available) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = DatabaseConnection.getInstance().getConnection().prepareStatement(sql)) {
            ps.setInt(1, driver.getId());
            ps.setString(2, driver.getName());
            ps.setString(3, driver.getEmail());
            ps.setString(4, driver.getPhone());
            ps.setString(5, driver.getVehiclePlate());
            ps.setInt(6, driver.isAvailable() ? 1 : 0);
            ps.executeUpdate();
        }
    }

    @Override
    public Driver read(int id) throws SQLException {
        String sql = "SELECT * FROM drivers WHERE id = ?";
        try (PreparedStatement ps = DatabaseConnection.getInstance().getConnection().prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Driver d = new Driver(rs.getInt("id"), rs.getString("name"),
                        rs.getString("email"), rs.getString("phone"), rs.getString("vehicle_plate"));
                d.setAvailable(rs.getInt("available") == 1);
                return d;
            }
        }
        return null;
    }

    @Override
    public List<Driver> readAll() throws SQLException {
        List<Driver> drivers = new ArrayList<>();
        String sql = "SELECT * FROM drivers";
        try (Statement stmt = DatabaseConnection.getInstance().getConnection().createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Driver d = new Driver(rs.getInt("id"), rs.getString("name"),
                        rs.getString("email"), rs.getString("phone"), rs.getString("vehicle_plate"));
                d.setAvailable(rs.getInt("available") == 1);
                drivers.add(d);
            }
        }
        return drivers;
    }

    @Override
    public void update(Driver driver) throws SQLException {
        String sql = "UPDATE drivers SET name = ?, email = ?, phone = ?, vehicle_plate = ?, available = ? WHERE id = ?";
        try (PreparedStatement ps = DatabaseConnection.getInstance().getConnection().prepareStatement(sql)) {
            ps.setString(1, driver.getName());
            ps.setString(2, driver.getEmail());
            ps.setString(3, driver.getPhone());
            ps.setString(4, driver.getVehiclePlate());
            ps.setInt(5, driver.isAvailable() ? 1 : 0);
            ps.setInt(6, driver.getId());
            ps.executeUpdate();
        }
    }

    @Override
    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM drivers WHERE id = ?";
        try (PreparedStatement ps = DatabaseConnection.getInstance().getConnection().prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }
}
