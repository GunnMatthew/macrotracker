package com.macros;

import java.sql.SQLException;
import java.util.ArrayList;

public class Utility {
    // Was originally Main.java, cleaned up as I didn't need the console based program anymore.  Class name likely temporary, find a better fit for
    // resetConsumedFood, parseAmount and dailyMeals storage.
    public static ArrayList<Meal.TotalMeal> dailyMeals = new ArrayList<>();

    // Method to handle exception for dbManager.resetConsumedFoods
    public static void resetConsumedFood(DBManager dbManager) {
        try {
                dbManager.resetConsumedFoods();
                dailyMeals.clear();
                System.out.println("Daily macros have been reset.");
            } catch (SQLException e) {
                System.out.println("Database Error: " + e.getMessage());
            }
    }

    // Allows the use of fractions in user input. QOL
    public static double parseAmount(String input) {
        input = input.trim();

        if (input.contains("/")) {
            String[] parts = input.split("/");
            if (parts.length == 2) {
                try {
                    double numerator = Double.parseDouble(parts[0].trim());
                    double denominator = Double.parseDouble(parts[1].trim());
                    return numerator / denominator;
                } catch (NumberFormatException e) {
                    System.out.println("Invalid fraction format. Please enter a valid number.");
                    return 0;
                }
            }
        }
        try {
            return Double.parseDouble(input);
        } catch (NumberFormatException e) {
            System.out.println("Invalid number format. Please enter a valid number.");
            return 0;
        }
    }
    
}
