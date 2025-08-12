package com.macros;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Scanner;

public class Main {
    private static DBManager dbManager;
    private static Scanner scanner = new Scanner(System.in);
    public static ArrayList<Meal.TotalMeal> dailyMeals = new ArrayList<>();

    public static void main(String[] args) {
        try {
            dbManager = new DBManager("macrotracker.db");
            dbManager.createTable();
            loadDailyMealsFromDB();
        } catch (SQLException e) {
            System.out.println("Database Error: " + e.getMessage());
            return;
        }

        while (true) { 
            optionMenu();
        }
    }

    //Main Menu
    private static void optionMenu() {
        Macros macros = new Macros();
        macros.mealTotals(dailyMeals);

        System.out.println("\nMacro Tracker\n");
        System.out.println("1: Daily Consumption Management Menu\n");
        System.out.println("2: Food Data Management Menu\n");
        System.out.println("0: Quit\n");
        System.out.printf("Daily Count - Fat: %sg | Carbs: %sg | Protein: %sg |%n\n", macros.getTotalFat(), macros.getTotalCarbs(), macros.getTotalProtein());

        String choice = scanner.nextLine();

        switch (choice) {
            case "1" -> consumedMenu();
            case "2" -> dataManagementMenu();
            case "0" -> {
                System.out.println("Exiting...");
                System.exit(0);
            }
            default -> System.out.println("Invalid Option");
        }
    }

    // Consumed Food Sub-Menu
    private static void consumedMenu() {
        while (true) {
            Macros macros = new Macros();

            System.out.println("\nConsumed Food Menu\n");
            System.out.println("1: Add Consumed food\n");
            System.out.println("2: List stored food (currently used to view foods that can be added)\n");
            System.out.println("8: Reset daily macro tracker\n");
            System.out.println("9: Return to main menu.\n");
            System.out.println("0: Quit\n");
            System.out.printf("Daily Count - Fat: %sg | Carbs: %sg | Protein: %sg |%n\n", macros.getTotalFat(), macros.getTotalCarbs(), macros.getTotalProtein());

            String choice = scanner.nextLine();

            switch (choice) {
            case "1" -> addToDailyConsumption();
            case "2" -> listFood();
            case "8" -> resetConsumedFood();
            case "9" -> {
                return;
                }
            case "0" -> {
                System.out.println("Exiting...");
                System.exit(0);
                }
            default -> System.out.println("Invalid Option");
            }
        }
    }

    // Data Management Sub-Menu
    private static void dataManagementMenu() {
        while (true) {
            System.out.println("\nFood Data Management Menu\n");
            System.out.println("1: Add Food\n");
            System.out.println("2: Remove food\n");
            System.out.println("3: List foods\n");
            System.out.println("9: Return to main menu.\n");
            System.out.println("0: Quit\n");

            String choice = scanner.nextLine();

            switch (choice) {
            case "1" -> addFood();
            case "2" -> removeFood();
            case "3" -> listFood();
            case "9" -> {
                return;
                }
            case "0" -> {
                System.out.println("Exiting...");
                System.exit(0);
                }
            default -> System.out.println("Invalid Option");
            }
        }
    }

    // Adds food to the food list
    private static void addFood() {
        System.out.println("Enter food name:");
        String foodName = scanner.nextLine();

        System.out.println("Enter serving size:");
        double servingSize = parseAmount(scanner.nextLine());

        String servingUnit;
        while (true) {
            System.out.println("Enter the unit of measurement for serving size (G, Oz, lb, Cup, mL, L, tsp, tbsp, FlOz, unit): ");
            servingUnit = scanner.nextLine().toLowerCase();
            if (UnitConversions.isValidUnit(servingUnit)) {
                break;
            } else {
                System.out.println("Invalid unit. Please enter a valid unit.");
            }
        }

        System.out.println("Enter Fat (g):");
        double fat = parseAmount(scanner.nextLine());

        System.out.println("Enter Carbohydrates (g):");
        double carbs = parseAmount(scanner.nextLine());

        System.out.println("Enter Protein (g):");
        double protein = parseAmount(scanner.nextLine());

        Food food = new Food(foodName, servingSize, servingUnit, fat, carbs, protein);

        try {
            dbManager.addFood(food);
            System.out.println(foodName + " added to the list");
        } catch (SQLException e) {
            System.out.println("Database Error: " + e.getMessage());
        }
    }

    // Removes food from the food list
    private static void removeFood() {
        try {
            ArrayList<Food> foods = dbManager.getAllFoods();
            if (foods.isEmpty()) {
                System.out.println("\nThere is nothing to remove!");
                return;
            }

            System.out.println("Enter the name of the food to remove: ");
            String foodName = scanner.nextLine();

            boolean isRemoved = false;

            for (Food food : foods) {
                if (food.getName().equalsIgnoreCase(foodName)) {
                    dbManager.removeFood(food);
                    isRemoved = true;
                    System.out.println(String.format("\n%s removed from food list.", foodName));
                    break;
                }
            }

            if (!isRemoved) {
                System.out.println(String.format("\n%s not found in the food list.", foodName));
            }
        } catch (SQLException e) {
            System.out.println("Database Error: " + e.getMessage());
        }
    }


    // Lists all food items in the food list
    public static void listFood() {
        try {
            ArrayList<Food> foods = dbManager.getAllFoods();
            if (foods.isEmpty()) {
                System.out.println("\nFood list is currently empty. Try adding food.");
                return;
            }

            System.out.println("\n--------------------------------------");
            for (Food food : foods) {
                System.out.println(food);
            }
            System.out.println("--------------------------------------");
        } catch (SQLException e) {
            System.out.println("Database Error: " + e.getMessage());
        }
    }

    //Add consumed foods to calculate total macros
    private static void addToDailyConsumption() {
        System.out.println("Enter name of consumed food: ");
        String foodName = scanner.nextLine();

        Food food = null;

        try {
            ArrayList<Food> foods = dbManager.getAllFoods();
            for (Food f : foods) {
                if (f.getName().equalsIgnoreCase(foodName)) {
                    food = f;
                    break;
                }
            }
        } catch (SQLException e) {
            System.out.println("Database Error: " + e.getMessage());
            return;
        }

        if (food == null) {
            System.out.println("Food not found.");
            return;
        }

        System.out.println("Enter amount eaten (just # value, unit will be next): ");
        double amount = parseAmount(scanner.nextLine());

        System.out.println("Enter unit (G, Oz, lb, Cup, mL, L, tsp, tbsp, FlOz, unit): ");
        String unit = scanner.nextLine();

        try {
            dbManager.addConsumedFood(food.getName(), amount, unit);
        } catch (SQLException e) {
            System.out.println("Database Error: " + e.getMessage());
        }

        Meal.TotalMeal meal = new Meal.TotalMeal("Single Food: " + food.getName());
        meal.addItem(food, amount, unit);
        dailyMeals.add(meal);

        System.out.println(food.getName() + " added to daily consumption.");
    }
    
    //Fetch consumed foods from database
    private static void loadDailyMealsFromDB() {
        try {
            ArrayList<Object[]> consumed = dbManager.getAllConsumedFoods();
            for (Object[] entry : consumed) {
                String foodName = (String) entry[0];
                double amount = (Double) entry[1];
                String unit = (String) entry[2];

                Food food = null;

                ArrayList<Food> foods = dbManager.getAllFoods();
                for (Food f : foods) {
                    if (f.getName().equalsIgnoreCase(foodName)) {
                        food = f;
                        break;
                    }
                }

                if (food != null) {
                    Meal.TotalMeal meal = new Meal.TotalMeal("Single food: " + food.getName());
                    meal.addItem(food, amount, unit);
                    dailyMeals.add(meal);
                }
            }
        } catch (SQLException e) {
            System.out.println("Database Error: " + e.getMessage());
        }
    }

    //Method to handle exception for dbManager.resetConsumedFoods
    private static void resetConsumedFood() {
        try {
                dbManager.resetConsumedFoods();
                dailyMeals.clear();
                System.out.println("Daily macros have been reset.");
            } catch (SQLException e) {
                System.out.println("Database Error: " + e.getMessage());
            }
    }

    //Allows the use of fractions in user input.  QOL
    private static double parseAmount(String input) {
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
