package com.fooddelivery.service;

import com.fooddelivery.db.DatabaseConnection;
import com.fooddelivery.model.*;
import com.fooddelivery.repository.*;

import java.sql.SQLException;
import java.util.*;

public class FoodDeliveryService {

    // Lista comenzilor (nesortata, acces rapid prin index)
    private List<Order> orders = new ArrayList<>();

    // Set sortat de restaurante (alfabetic dupa nume, foloseste compareto din
    // restaurant)
    private TreeSet<Restaurant> restaurants = new TreeSet<>();

    private List<Driver> drivers = new ArrayList<>();
    private List<User> users = new ArrayList<>();
    private List<Review> reviews = new ArrayList<>();
    private List<Person> persons = new ArrayList<>();

    // Servicii de persistenta (singleton generice peste JDBC)
    private final UserDAO userDAO;
    private final DriverDAO driverDAO;
    private final RestaurantDAO restaurantDAO;
    private final OrderDAO orderDAO;
    private final ReviewDAO reviewDAO;

    public FoodDeliveryService() {
        try {
            userDAO = UserDAO.getInstance();
            driverDAO = DriverDAO.getInstance();
            restaurantDAO = RestaurantDAO.getInstance();
            orderDAO = OrderDAO.getInstance();
            reviewDAO = ReviewDAO.getInstance();
            loadFromDatabase();
        } catch (SQLException e) {
            throw new IllegalStateException("Eroare la initializarea bazei de date: " + e.getMessage(), e);
        }
    }

    // Incarca starea persistata in colectiile in-memory la pornirea aplicatiei
    private void loadFromDatabase() throws SQLException {
        for (User u : userDAO.readAll()) {
            users.add(u);
            persons.add(u);
        }
        for (Driver d : driverDAO.readAll()) {
            drivers.add(d);
            persons.add(d);
        }
        for (Restaurant r : restaurantDAO.readAll()) {
            restaurants.add(r);
        }
        for (Order o : orderDAO.readAll()) {
            orders.add(o);
        }
        for (Review rev : reviewDAO.readAll()) {
            reviews.add(rev);
            // Ataseaza review-ul la restaurantul deja incarcat (pentru media corecta)
            Restaurant canonical = getRestaurantById(rev.getRestaurant().getId());
            if (canonical != null) {
                canonical.addReview(rev);
            }
        }
    }

    // 1. Cauta restaurante dupa categorie
    public List<Restaurant> getRestaurantsByCategory(Category category) {
        AuditService.getInstance().log("getRestaurantsByCategory");
        List<Restaurant> result = new ArrayList<>();
        for (Restaurant r : restaurants) {
            if (r.getCategory().getId() == category.getId()) {
                result.add(r);
            }
        }
        return result;
    }

    // 2. Listeaza meniul unui restaurant
    public List<MenuItem> getMenu(Restaurant restaurant) {
        AuditService.getInstance().log("getMenu");
        return restaurant.getMenu();
    }

    // 3. Plaseaza o comanda
    public Order placeOrder(int id, User user, Restaurant restaurant, String deliveryAddress, List<OrderItem> items) {
        AuditService.getInstance().log("placeOrder");
        if (user == null || restaurant == null) {
            throw new IllegalArgumentException("Comanda necesita un utilizator si un restaurant valide");
        }
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("Comanda trebuie sa contina cel putin un produs");
        }
        Order order = new Order(id, user, restaurant, deliveryAddress);
        for (OrderItem item : items) {
            order.addItem(item);
        }
        orders.add(order);
        persist(() -> orderDAO.create(order));
        return order;
    }

    // 4. Calculeaza totalul unei comenzi
    public double calculateTotal(Order order) {
        AuditService.getInstance().log("calculateTotal");
        return order.getTotal();
    }

    // 5. Listeaza soferii disponibili
    public List<Driver> getAvailableDrivers() {
        AuditService.getInstance().log("getAvailableDrivers");
        List<Driver> result = new ArrayList<>();
        for (Driver d : drivers) {
            if (d.isAvailable()) {
                result.add(d);
            }
        }
        return result;
    }

    // 6. Atribuie un sofer unei comenzi
    public boolean assignDriver(Order order, Driver driver) {
        AuditService.getInstance().log("assignDriver");
        if (order == null || driver == null || !driver.isAvailable()) {
            return false;
        }
        order.setDriver(driver);
        order.setStatus(OrderStatus.ASSIGNED);
        driver.setAvailable(false);
        persist(() -> {
            orderDAO.update(order);
            driverDAO.update(driver);
        });
        return true;
    }

    // 7. Cauta comenzi dupa status
    public List<Order> getOrdersByStatus(OrderStatus status) {
        AuditService.getInstance().log("getOrdersByStatus");
        List<Order> result = new ArrayList<>();
        for (Order o : orders) {
            if (o.getStatus() == status) {
                result.add(o);
            }
        }
        return result;
    }

    // 8. Marcheaza comanda ca livrata
    public void markDelivered(Order order) {
        AuditService.getInstance().log("markDelivered");
        order.setStatus(OrderStatus.DELIVERED);
        Driver driver = order.getDriver();
        if (driver != null) {
            driver.setAvailable(true);
        }
        persist(() -> {
            orderDAO.update(order);
            if (driver != null) {
                driverDAO.update(driver);
            }
        });
    }

    // 9. Listeaza comenzile unui user
    public List<Order> getOrdersByUser(User user) {
        AuditService.getInstance().log("getOrdersByUser");
        List<Order> result = new ArrayList<>();
        for (Order o : orders) {
            if (o.getUser().getId() == user.getId()) {
                result.add(o);
            }
        }
        return result;
    }

    // 10. Listeaza review-urile unui restaurant
    public List<Review> getReviewsByRestaurant(Restaurant restaurant) {
        AuditService.getInstance().log("getReviewsByRestaurant");
        return restaurant.getReviews();
    }

    // 11. Listeaza toate review-urile
    public List<Review> getAllReviews() {
        AuditService.getInstance().log("getAllReviews");
        return reviews;
    }

    // 12. Returneaza toate restaurantele sortate alfabetic
    public TreeSet<Restaurant> getAllRestaurants() {
        AuditService.getInstance().log("getAllRestaurants");
        return restaurants;
    }

    // 13. Listeaza toti utilizatorii si soferii
    public List<Person> getAllPersons() {
        AuditService.getInstance().log("getAllPersons");
        return persons;
    }

    // Helperi de adaugare (in-memory + persistenta prin DAO)
    public void addRestaurant(Restaurant restaurant) {
        if (getRestaurantById(restaurant.getId()) == null) {
            restaurants.add(restaurant);
        }
        persist(() -> restaurantDAO.create(restaurant));
    }

    public void addMenuItem(Restaurant restaurant, MenuItem item) {
        restaurant.addMenuItem(item);
        persist(() -> MenuItemDAO.getInstance().create(item, restaurant.getId()));
    }

    public void addDriver(Driver driver) {
        if (getDriverById(driver.getId()) == null) {
            drivers.add(driver);
            persons.add(driver);
        }
        persist(() -> driverDAO.create(driver));
    }

    public void addUser(User user) {
        if (getUserById(user.getId()) == null) {
            users.add(user);
            persons.add(user);
        }
        persist(() -> userDAO.create(user));
    }

    public void addReview(Review review) {
        if (getReviewById(review.getId()) == null) {
            reviews.add(review);
            Restaurant canonical = getRestaurantById(review.getRestaurant().getId());
            if (canonical != null) {
                canonical.addReview(review);
            } else {
                review.getRestaurant().addReview(review);
            }
        }
        persist(() -> reviewDAO.create(review));
    }

    // Cautari dupa id (folosite si pentru a evita dublarea in colectii)
    public User getUserById(int id) {
        for (User u : users) {
            if (u.getId() == id) {
                return u;
            }
        }
        return null;
    }

    public Driver getDriverById(int id) {
        for (Driver d : drivers) {
            if (d.getId() == id) {
                return d;
            }
        }
        return null;
    }

    public Restaurant getRestaurantById(int id) {
        for (Restaurant r : restaurants) {
            if (r.getId() == id) {
                return r;
            }
        }
        return null;
    }

    public Review getReviewById(int id) {
        for (Review r : reviews) {
            if (r.getId() == id) {
                return r;
            }
        }
        return null;
    }

    public List<Order> getAllOrders() {
        return new ArrayList<>(orders);
    }

    public List<User> getAllUsers() {
        return new ArrayList<>(users);
    }

    public List<Driver> getAllDrivers() {
        return new ArrayList<>(drivers);
    }

    // Inchide conexiunea la baza de date (apelat la oprirea aplicatiei)
    public void close() {
        try {
            DatabaseConnection.getInstance().close();
        } catch (SQLException e) {
            System.err.println("Eroare la inchiderea conexiunii: " + e.getMessage());
        }
    }

    // Helper care transforma SQLException (checked) intr-o eroare de runtime,
    // pentru a pastra simpla semnatura metodelor publice ale serviciului
    private interface DbAction {
        void run() throws SQLException;
    }

    private void persist(DbAction action) {
        try {
            action.run();
        } catch (SQLException e) {
            throw new IllegalStateException("Eroare la persistenta: " + e.getMessage(), e);
        }
    }

}
