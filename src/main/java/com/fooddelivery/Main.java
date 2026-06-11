package com.fooddelivery;

import com.fooddelivery.model.*;
import com.fooddelivery.service.FoodDeliveryService;

import java.util.Arrays;
import java.util.List;

public class Main {
    public static void main(String[] args) {

        // La construire, serviciul incarca automat datele existente din baza de date
        FoodDeliveryService service = new FoodDeliveryService();

        try {
            // Am ales sa folosesc userii pentru ca se presupune ca aplicatia are mereu macar un user,
            // tabelul user nefiind unul din care se sterge de obicei. Daca users e gol se presupune ca e prima rulare, chiar daca poate nu e.
            // Problema asta se poate rezolva daca fac un tabel separat care tine minte doar daca a mai fost sau nu rulat programul
            if (service.getAllUsers().isEmpty()) {
                System.out.println("=== Prima rulare: populez datele initiale si le persist in DB ===");
                seedData(service);
            } else {
                System.out.println("=== Date incarcate din baza de date (rulare ulterioara) ===");
            }

            // Referinte obtinute din serviciu, indiferent daca au fost semanate acum sau incarcate din DB
            User u1 = service.getUserById(1);
            Driver d1 = service.getDriverById(1);
            Restaurant r1 = service.getRestaurantById(1);
            Restaurant r2 = service.getRestaurantById(2);
            Category pizza = r1.getCategory();

            System.out.println("\n=== Toate restaurantele (sortat alfabetic) ===");
            for (Restaurant r : service.getAllRestaurants()) {
                System.out.println("  " + r);
            }

            // 1. Cauta restaurante dupa categorie
            System.out.println("\n=== Restaurante " + pizza.getName() + " ===");
            for (Restaurant r : service.getRestaurantsByCategory(pizza)) {
                System.out.println("  " + r);
            }

            // 2. Listeaza meniul unui restaurant
            System.out.println("\n=== Meniu " + r1.getName() + " ===");
            for (MenuItem item : service.getMenu(r1)) {
                System.out.println("  " + item);
            }

            // 3. Plaseaza o comanda
            List<OrderItem> items1 = Arrays.asList(
                    new OrderItem(r1.getMenu().get(0), 2),
                    new OrderItem(r1.getMenu().get(1), 1));
            Order order1 = service.placeOrder(nextOrderId(service), u1, r1, u1.getAddress(), items1);
            System.out.println("\n=== Comanda plasata (persistata in DB) ===");
            System.out.println("  " + order1);

            // 4. Calculeaza totalul unei comenzi
            System.out.println("  Total: " + service.calculateTotal(order1) + " RON");

            // 5. Listeaza soferii disponibili
            System.out.println("\n=== Soferi disponibili ===");
            for (Driver d : service.getAvailableDrivers()) {
                System.out.println("  " + d);
            }

            // 6. Atribuie un sofer unei comenzi
            service.assignDriver(order1, d1);
            System.out.println("\n=== Dupa atribuire sofer (persistata in DB) ===");
            System.out.println("  " + order1);

            // 7. Cauta comenzi dupa status
            System.out.println("\n=== Comenzi ASSIGNED ===");
            for (Order o : service.getOrdersByStatus(OrderStatus.ASSIGNED)) {
                System.out.println("  " + o);
            }

            // 8. Marcheaza comanda ca livrata
            service.markDelivered(order1);
            System.out.println("\n=== Dupa livrare (persistata in DB) ===");
            System.out.println("  " + order1);
            System.out.println("  Sofer disponibil din nou: " + d1.isAvailable());

            // 9. Listeaza comenzile unui user
            service.placeOrder(nextOrderId(service), u1, r2, u1.getAddress(),
                    Arrays.asList(new OrderItem(r2.getMenu().get(0), 1)));
            System.out.println("\n=== Comenzile lui " + u1.getName() + " ===");
            for (Order o : service.getOrdersByUser(u1)) {
                System.out.println("  " + o);
            }

            // 10. Listeaza review-urile unui restaurant
            System.out.println("\n=== Review-uri " + r1.getName() + " ===");
            System.out.println("  Medie: " + String.format("%.1f", r1.getAverageRating()) + "/5");
            for (Review rev : service.getReviewsByRestaurant(r1)) {
                System.out.println("  " + rev);
            }

            // 11. Listeaza toate review-urile
            System.out.println("\n=== Toate review-urile ===");
            for (Review r : service.getAllReviews()) {
                System.out.println("  " + r);
            }

            // 12. Listeaza restaurante sortate dupa rating
            System.out.println("\n=== Restaurante sortate dupa rating ===");
            service.getAllRestaurants().stream()
                    .sorted(Restaurant.BY_RATING)
                    .forEach(r -> System.out.println("  " + r));

            // 13. Listeaza toti utilizatorii si soferii
            System.out.println("\n=== Toti utilizatorii si soferii ===");
            for (Person p : service.getAllPersons()) {
                System.out.println("  " + p);
            }

            System.out.println("  Toate operatiile de mai sus au fost salvate in food_delivery.db.");
            System.out.println("  Actiunile au fost inregistrate in audit.csv");

        } finally {
            // Inchide conexiunea la baza de date la oprirea aplicatiei
            service.close();
        }
    }

    // Populeaza datele initiale prin serviciu (care le si persista in DB)
    private static void seedData(FoodDeliveryService service) {
        Category pizza = new Category(1, "Pizza");
        Category sushi = new Category(2, "Sushi");

        User u1 = new User(1, "Ion Popescu", "ion@email.com", "0721000001", "Str. Lalelelor 5");
        User u2 = new User(2, "Maria Ionescu", "maria@email.com", "0721000002", "Bd. Libertatii 12");
        service.addUser(u1);
        service.addUser(u2);

        Driver d1 = new Driver(1, "Gheorghe Popa", "ghe@email.com", "0731000001", "B-01-ABC");
        Driver d2 = new Driver(2, "Vasile Marin", "vas@email.com", "0731000002", "B-02-XYZ");
        service.addDriver(d1);
        service.addDriver(d2);

        Restaurant r1 = new Restaurant(1, "Andiamo", "Str. Victoriei 10", pizza);
        Restaurant r2 = new Restaurant(2, "Zen Sushi", "Calea Floreasca 55", sushi);
        Restaurant r3 = new Restaurant(3, "Bella Napoli", "Bd. Unirii 3", pizza);
        service.addRestaurant(r1);
        service.addRestaurant(r2);
        service.addRestaurant(r3);

        service.addMenuItem(r1, new MenuItem(1, "Margherita", 28.0, "Sos, mozzarella, busuioc"));
        service.addMenuItem(r1, new MenuItem(2, "Quattro Stagioni", 35.0, "Ciuperci, ardei, masline, sunca"));
        service.addMenuItem(r2, new MenuItem(3, "Salmon Roll", 42.0, "8 bucati"));
        service.addMenuItem(r2, new MenuItem(4, "Tuna Nigiri", 38.0, "6 bucati"));
        service.addMenuItem(r3, new MenuItem(5, "Prosciutto", 39.0, "Sunca, rucola, parmezan"));

        service.addReview(new Review(1, u1, r1, 5, "Pizza excelenta, livrare rapida!"));
        service.addReview(new Review(2, u2, r1, 4, "Buna, dar putin sarata."));
        service.addReview(new Review(3, u1, r2, 3, "Sushi ok, dar portii mici."));
        service.addReview(new Review(4, u2, r3, 5, "Cea mai buna pizza din oras!"));
    }

    // Genereaza un id de comanda care nu intra in coliziune cu cele deja existente
    private static int nextOrderId(FoodDeliveryService service) {
        int max = 0;
        for (Order o : service.getAllOrders()) {
            max = Math.max(max, o.getId());
        }
        return max + 1;
    }
}
