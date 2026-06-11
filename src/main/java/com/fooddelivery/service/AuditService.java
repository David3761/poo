package com.fooddelivery.service;

import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;

public class AuditService {

    private static AuditService instance;
    private static final String CSV_FILE = "audit.csv";

    private AuditService() {}

    public static AuditService getInstance() {
        if (instance == null) {
            instance = new AuditService();
        }
        return instance;
    }

    public void log(String actionName) {
        try (FileWriter fw = new FileWriter(CSV_FILE, true)) {
            fw.write(actionName + "," + LocalDateTime.now() + "\n");
        } catch (IOException e) {
            System.err.println("Eroare audit: " + e.getMessage());
        }
    }
}
