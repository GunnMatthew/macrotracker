package com.macros;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

import com.google.gson.Gson;

public class Main {
    private static ArrayList<Food> foodList = new ArrayList<>();
    private static Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
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
        System.out.println("Macro Tracker\n");
        System.out.println("1. Add food\n");
        System.out.println("2. List foods\n");
        System.out.println("0. Save and quit");
    }

    private static void addFood() {
        System.out.println("Enter the food name: ");
        String foodName = scanner.nextLine();

        System.out.println("\nEnter Serving Size (Just a number, no units): ");
        double servingSize = Double.parseDouble(scanner.nextLine());

        System.out.println("Enter the unit of measurement for serving size (cup, oz, etc.): ");
        String servingUnit = scanner.nextLine();
        
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

    public static void listFood() {
        if (foodList.isEmpty()) {
            System.out.println("\nFood list is currently empty.\nTry adding food.");
            return;
        }

        for (Food food : foodList) {
            System.out.println(food);
        }
    }

}
