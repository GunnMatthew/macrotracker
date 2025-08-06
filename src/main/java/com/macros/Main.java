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

    public static void main(String[] args) {
        // Load our food json file if it exists
        loadFromFile();

        while (true) { 
            optionMenu();
            String choice = scanner.nextLine();

            switch(choice) {
                case "1":
                    addFood();
                    break;
                case "2":
                    listFood();
                    break;
                case "0":
                    saveToFile();
                    System.out.println("Exiting...");
                    return;
                default:
                    System.out.println("Invalid Option");

            }
        }
    }

    private static void optionMenu() {
        System.out.println("\nMacro Tracker\n");
        System.out.println("1. Add food\n");
        System.out.println("2. List foods\n");
        System.out.println("0. Save and quit");
    }

    private static void addFood() {
        System.out.println("Enter the food name: ");
        String foodName = scanner.nextLine();

        System.out.println("\nEnter Serving Size (Just a number, no units): ");
        double servingSize = Double.parseDouble(scanner.nextLine());

        System.out.println("Enter the unit of measurement for serving size (G, Oz, lb, Cup, mL, L, tsp, tbsp, FlOz, unit): ");
        String servingUnit = scanner.nextLine().toLowerCase();
        while (true) {
            if (unitConversions.isValidUnit(servingUnit)) {
                break;
            } else {
                System.out.println("Invalid unit.  Please enter a valid unit (G, Oz, lb, Cup, mL, L, tsp, tbsp, FlOz): ");
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
        System.out.println("\nFood added to list.");
    }

    private static void saveToFile() {
        try (FileWriter writer = new FileWriter("foods.json")) {
            Gson gson = new Gson();
            gson.toJson(foodList, writer);
        } catch (IOException e) {
            System.out.println("Error saving foods: " + e.getMessage());
        }
    }

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

    public static void listFood() {
        if (foodList.isEmpty()) {
            System.out.println("\nFood list is currently empty.\nTry adding food.");
            return;
        }

        System.out.println("\n--------------------------------------");
        for (Food food : foodList) {
            System.out.println(food);
        }
        System.out.println("--------------------------------------");
    }

}
