package com.macros;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Scanner;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class Main {
    private static ArrayList<Food> foodList = new ArrayList<>();
    private static Scanner scanner = new Scanner(System.in);
    private static ArrayList<Meal.TotalMeal> mealList = new ArrayList<>();

    public static void main(String[] args) {
        // Load food and meals json file if it exists
        loadFromFile();
        loadMealsFromFile();

        while (true) { 
            optionMenu();
            String choice = scanner.nextLine();
            appLoop(choice);
        }
    }

    private static void optionMenu() {
        Macros macros = new Macros();

        System.out.println("\nMacro Tracker\n");
        System.out.println("1: Add food\n");
        System.out.println("2: List foods\n");
        System.out.println("3: Remove food\n");
        System.out.println("4: Edit existing food (not implemented yet)\n");
        System.out.println("5: Add meal (not implemented yet)\n");
        System.out.println("6: List meals\n");
        System.out.println("7: Remove meal (not implemented yet)\n");
        System.out.println("8: Add to daily consumption (not implemented yet)\n");
        System.out.println("9: Reset daily consumption (not implemented yet)\n");
        System.out.printf("Daily Count - Fat: %sg | Carbs: %sg | Protein: %sg |%n\n", macros.getTotalFat(), macros.getTotalCarbs(), macros.getTotalProtein());
        System.out.println("0: Save and quit");
    }

    public static void appLoop(String choice) {
        switch (choice) {
            case "1" -> addFood();
            case "2" -> listFood();
            case "3" -> removeFood();
            case "5" -> addMeal();
            case "6" -> listMeals();
            case "0" -> {
                saveToFile();
                System.out.println("Exiting...");
                System.exit(0);
            }
            default -> System.out.println("Invalid Option");
        }
    }

    // Adds food to the food list
    private static void addFood() {
        System.out.println("Enter the food name: ");
        String foodName = scanner.nextLine();

        for (Food food : foodList) {
            if (food.getName().equalsIgnoreCase(foodName)) {
                System.out.println(String.format("%s already exists in the list.", foodName));
                return;
            }
        }

        double servingSize = 0;

        while (true) {
            System.out.println("\nEnter Serving Size (Just a number, no units): ");
            String input = scanner.nextLine();
        try {
            servingSize = Double.parseDouble(input);
            break;
        } catch (NumberFormatException e) {
            System.out.println("\nInvalid number. Please enter a numeric value.");
        }
        }

        String servingUnit;
        while (true) {
            System.out.println("Enter the unit of measurement for serving size (G, Oz, lb, Cup, mL, L, tsp, tbsp, FlOz, unit): ");
            servingUnit = scanner.nextLine().toLowerCase();

            if (unitConversions.isValidUnit(servingUnit)) {
                break;
            } else {
                System.out.println("Invalid unit. Please enter a valid unit (G, Oz, lb, Cup, mL, L, tsp, tbsp, FlOz): ");
                return;
            }
        }
        
        System.out.println("\nEnter Fat (g): ");
        double fat = Double.parseDouble(scanner.nextLine());

        System.out.println("\nEnter Carbohydrates (g): ");
        double carbs = Double.parseDouble(scanner.nextLine());

        System.out.println("\nEnter Protein (g): ");
        double protein = Double.parseDouble(scanner.nextLine());

        Food food = new Food(foodName, servingSize, servingUnit, fat, carbs, protein);
        foodList.add(food);
        System.out.println(foodName + " added to the list");
    }

    // Removes food from the food list

    private static void removeFood() {
        
        if (foodList.isEmpty()) {
            System.out.println("\nThere is nothing to remove!");
            return;
        }

        System.out.println("Enter the name of the food to remove: ");
        String foodName = scanner.nextLine();

        boolean isRemoved = false;
        for (int i = 0; i < foodList.size(); i++) {
          if (foodList.get(i).getName().equalsIgnoreCase(foodName)) { 
            foodList.remove(i);
            isRemoved = true;
            System.out.println(String.format("\n%s removed from list.", foodName));
            break;
          }
        }

        if (!isRemoved) {
            System.out.println(String.format("\n%s not found in the list.", foodName));
        }
    }

    // Saves food list to a JSON file
    private static void saveToFile() {
        try (FileWriter writer = new FileWriter("foods.json")) {
            Gson gson = new Gson();
            gson.toJson(foodList, writer);
        } catch (IOException e) {
            System.out.println("Error saving foods: " + e.getMessage());
        }
    }

    // Loads food list from a JSON file
    private static void loadFromFile() {
        File file = new File("foods.json");
        if (!file.exists()) return;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            Gson gson = new Gson();
            Type foodListType = new TypeToken<ArrayList<Food>>(){}.getType();
            foodList = gson.fromJson(reader, foodListType);
        } catch (IOException e) {
            System.out.println("\nError loading foods: " + e.getMessage());
        }
    }


    // Lists all food items in the food list
    public static void listFood() {
        if (foodList.isEmpty()) {
            System.out.println("\nFood list is currently empty. Try adding food.");
            return;
        }

        System.out.println("\n--------------------------------------");
        for (Food food : foodList) {
            System.out.println(food);
        }
        System.out.println("--------------------------------------");
    }

    // Adds a meal to the meal list
    public static void addMeal() {
        System.out.println("Enter a meal name: ");
        String mealName = scanner.nextLine();

        Meal.TotalMeal meal = new Meal.TotalMeal(mealName);

        while (true) { 
            System.out.println("Enter food name to add ingrediant to meal, or type 'exit' to finish.");
            String foodName = scanner.nextLine();

            if (foodName.equalsIgnoreCase("exit")) break;

            Food food = null;

            for (Food f : foodList) {
                if (f.getName().equalsIgnoreCase(foodName)) {
                    food = f;
                    break;
                }
            }

            if (food == null) {
                System.out.println(foodName +" not found");
            }

            System.out.println("Enter amount of " + foodName + " used: ");
            double amount = Double.parseDouble(scanner.nextLine());

            System.out.println("Enter unit for " + foodName + " (G, Oz, lb, Cup, mL, L, tsp, tbsp, floz, unit): ");
            String unit = scanner.nextLine();

            meal.addItem(food, amount, unit);
        }

        mealList.add(meal);
        mealToFile();
        System.out.println(meal + " added to meals list.");
    }

    // Lists all meals in the meal list
    public static void listMeals() {
        if (mealList.isEmpty()) {
            System.out.println("\nMeal list is currently empty. Try adding meals.");
            return;
        }

        System.out.println("\n--------------------------------------");
        for (Meal.TotalMeal meal : mealList) {
            System.out.println(meal.getName());
        System.out.println("--------------------------------------");
        }
    }

    // Removes a meal from the meal list
    public static void removeMeal() {
        if (mealList.isEmpty()) {
            System.out.println("\nThere is nothing to remove!");
            return;
        }

        System.out.println("Enter the name of the meal to remove: ");
        String mealName = scanner.nextLine();

        boolean isRemoved = false;
        for (int i = 0; i < mealList.size(); i++) {
            if (mealList.get(i).getName().equalsIgnoreCase(mealName)) {
                mealList.remove(i);
                isRemoved = true;
                System.out.println(String.format("\n%s removed from meals list.", mealName));
                break;
            }
        }
        
        if (!isRemoved) {
            System.out.println(String.format("\n%s not found in the meals list.", mealName));
        }
    }

    // Saves meal list to a JSON file
    private static void mealToFile() {
        try (FileWriter writer = new FileWriter("meals.json")) {
            Gson gson = new Gson();
            gson.toJson(mealList, writer);
        } catch (IOException e) {
            System.out.println("Error saving meals: " + e.getMessage());
        }
    }

    // Loads meal list from a JSON file
    private static void loadMealsFromFile() {
        File file = new File("meals.json");

        if (!file.exists()) return;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            Gson gson = new Gson();
            Type mealListType = new TypeToken<ArrayList<Meal.TotalMeal>>(){}.getType();
            mealList = gson.fromJson(reader, mealListType);
        } catch (IOException e) {
            System.out.println("\nError loading meals: " + e.getMessage());
        }
    }

}
