package com.macros;

import java.sql.*;
import java.util.ArrayList;

public class DBManager {
    private Connection connection;

    public DBManager(String dbFile) throws SQLException {
        connection = DriverManager.getConnection("jdbc:sqlite:" + dbFile);
    }

    public void createTable() throws SQLException {
        String sql = """
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

        try (Statement statement = connection.createStatement()) {
            statement.execute(sql);
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

    public ArrayList<Food> getAllFoods() throws SQLException {
        ArrayList<Food> foods = new ArrayList<>();
        String sql = "SELECT name, serving_size, serving_unit, fat, carbs, protein FROM foods";
        
        try (Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery(sql)) {
            while (rs.next()) {
                String name = rs.getString("name");
                double servingSize = rs.getDouble("serving_size");
                String servingUnit = rs.getString("serving_unit");
                double fat = rs.getDouble("fat");
                double carbs = rs.getDouble("carbs");
                double protein = rs.getDouble("protein");
                foods.add(new Food(name, servingSize, servingUnit, fat, carbs, protein));
            }
        }
        return foods;
    }

}
