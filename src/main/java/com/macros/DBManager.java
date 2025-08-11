package com.macros;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

public class DBManager {
    private Connection connection;

    public DBManager(String dbFile) throws SQLException {
        connection = DriverManager.getConnection("jdbc:sqlite:" + dbFile);
    }

    public void createTable() throws SQLException {
        //Table for foods
        String foods = """
                CREATE TABLE IF NOT EXISTS foods (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    name TEXT UNIQUE,
                    servingSize REAL,
                    servingUnit TEXT,
                    fat REAL,
                    carbs REAL,
                    protein REAL
                );
                """;

        //Table to track foods consumed
        String consumedFoods = """
            CREATE TABLE IF NOT EXISTS consumedFoods (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT,
                amount REAL,
                unit TEXT
            );
            """;

        try (Statement statement = connection.createStatement()) {
            statement.execute(foods);
            statement.execute(consumedFoods);
        }
    }

    public void close() throws SQLException {
        if (connection != null) connection.close();
    }

    //Need to add methods for add, get, etc.
    //Add food to database
    public void addFood(Food food) throws SQLException {
        String sql = "INSERT INTO foods (name, servingSize, servingUnit, fat, carbs, protein) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, food.getName());
            pstmt.setDouble(2, food.getServingSize());
            pstmt.setString(3, food.getServingUnit());
            pstmt.setDouble(4, food.getFat());
            pstmt.setDouble(5, food.getCarbs());
            pstmt.setDouble(6, food.getProtein());
            pstmt.executeUpdate();
        }
    }

    //Remove food from database
    public void removeFood(Food food) throws SQLException {
        String sql = "DELETE FROM foods WHERE name = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, food.getName());
            pstmt.executeUpdate();
        }
    }

    //Add consumed foods for tracking macros
    public void addConsumedFood(String foodName, double amount, String unit) throws SQLException {
        String sql = "INSERT INTO consumedFoods (name, amount, unit) VALUES (?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, foodName);
            pstmt.setDouble(2, amount);
            pstmt.setString(3, unit);
            pstmt.executeUpdate();
        }
    }

    //Manual resetting of consumed foods ***Replace later with automatic process at midnight
    public void resetConsumedFoods() throws SQLException {
        String sql = "DELETE FROM consumedFoods";
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate(sql);
        }
    }

    public ArrayList<Food> getAllFoods() throws SQLException {
        ArrayList<Food> foods = new ArrayList<>();
        String sql = "SELECT name, servingSize, servingUnit, fat, carbs, protein FROM foods";
        
        try (Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery(sql)) {
            while (rs.next()) {
                String name = rs.getString("name");
                double servingSize = rs.getDouble("servingSize");
                String servingUnit = rs.getString("servingUnit");
                double fat = rs.getDouble("fat");
                double carbs = rs.getDouble("carbs");
                double protein = rs.getDouble("protein");
                foods.add(new Food(name, servingSize, servingUnit, fat, carbs, protein));
            }
        }
        return foods;
    }

    public ArrayList<Object[]> getAllConsumedFoods() throws SQLException {
        ArrayList<Object[]> consumedFoods = new ArrayList<>();
        String sql = "SELECT name, amount, unit FROM consumedFoods";
        
        try (Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery(sql)) {
            while (rs.next()) {
                consumedFoods.add(new Object[] {
                    rs.getString("name"),
                    rs.getDouble("amount"),
                    rs.getString("unit")
                });
            }
        }
        return consumedFoods;
    }

}
