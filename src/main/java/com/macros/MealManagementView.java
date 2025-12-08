package com.macros;

import java.sql.SQLException;
import java.util.ArrayList;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public class MealManagementView {

    public static VBox create(DBManager dbManager, ArrayList<Food> foods, Macros macros, Scene scene, VBox mainLayout) {
        ListView<String> foodsList = new ListView<>();
        for (Food f : foods) foodsList.getItems().add(f.getName());
        foodsList.setPrefHeight(220);
        foodsList.setPrefWidth(600);
        VBox.setVgrow(foodsList, Priority.ALWAYS);

        ListView<String> mealItemsView = new ListView<>();
        mealItemsView.setPrefHeight(200);
        mealItemsView.setPrefWidth(600);
        VBox.setVgrow(mealItemsView, Priority.ALWAYS);

        TextField amountField = new TextField("1");
        TextField unitField = new TextField();
        Button addItemBtn = new Button("Add Item");
        Button removeItemBtn = new Button("Remove Item");
        Button saveMealBtn = new Button("Save Meal");
        Button backMealBtn = new Button("Back");

        addItemBtn.setDisable(true);
        foodsList.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newV) -> addItemBtn.setDisable(newV == null));

        HBox entryRow = new HBox(10, new Label("Amount:"), amountField, new Label("Unit:"), unitField, addItemBtn);
        entryRow.setAlignment(javafx.geometry.Pos.CENTER);

        HBox bottomBtns = new HBox(10, saveMealBtn, removeItemBtn, backMealBtn);
        bottomBtns.setAlignment(javafx.geometry.Pos.CENTER);
        VBox mealLayout = new VBox(10, new Label("Meal Management"), foodsList, entryRow, new Label("Current Items:"), mealItemsView, bottomBtns);
        mealLayout.setPadding(new Insets(10));
        mealLayout.setAlignment(javafx.geometry.Pos.CENTER);

        final Meal.TotalMeal currentMeal = new Meal.TotalMeal("Untitled Meal");

        // Add item button
        addItemBtn.setOnAction(ev -> {
            String selName = foodsList.getSelectionModel().getSelectedItem();
            if (selName == null) return;
            Food selected = foods.stream().filter(f -> f.getName().equals(selName)).findFirst().orElse(null);
            if (selected == null) return;

            double amount = 1;
            try { amount = Utility.parseAmount(amountField.getText()); } catch (NumberFormatException ex) {}
            String unit = unitField.getText().isBlank() ? selected.getServingUnit() : unitField.getText();

            currentMeal.addItem(selected, amount, unit);
            mealItemsView.getItems().add(String.format("%s - %s %s", selected.getName(), amount, unit));
        });

        removeItemBtn.setDisable(true);
        mealItemsView.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newV) -> {
            removeItemBtn.setDisable(newV == null);
        });

        // Remove item button
        removeItemBtn.setOnAction(ev -> {
            int sel = mealItemsView.getSelectionModel().getSelectedIndex();
            if (sel >= 0) {
                currentMeal.getItems().remove(sel);
                mealItemsView.getItems().remove(sel);
            }
        });

        // save button
        saveMealBtn.setOnAction(ev -> {
            Dialog<String> d = new Dialog<>();
            d.setTitle("Save Meal");
            d.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
            TextField nameField = new TextField(currentMeal.getName());
            d.getDialogPane().setContent(new VBox(8, new Label("Meal name:"), nameField));
            d.setResultConverter(bt -> bt == ButtonType.OK ? nameField.getText() : null);
            d.showAndWait().ifPresent(name -> {
                if (name == null || name.isBlank()) return;

                Meal.TotalMeal newMeal = new Meal.TotalMeal(name);

                for (Meal.MealItem mi : currentMeal.getItems()) {
                    if (mi == null) continue;
                    Food itemFood = mi.getFood();
                    double itemAmount = mi.getAmount();
                    String itemUnit = mi.getUnit();
                    if (itemFood == null) continue;
                    newMeal.addItem(itemFood, itemAmount, itemUnit);
                }

                try {
                    dbManager.addMeal(newMeal);
                    Macros m = new Macros();

                    m.mealTotals(java.util.List.of(newMeal));

                    double totalFat = m.getTotalFat();
                    double totalCarbs = m.getTotalCarbs();
                    double totalProtein = m.getTotalProtein();

                    Food mealFood = new Food(name, 1.0, "unit", totalFat, totalCarbs, totalProtein);
                    try {
                        dbManager.addFood(mealFood);
                        foods.add(mealFood);
                        foodsList.getItems().add(mealFood.getName());
                    } catch (SQLException addFoodEx) {
                        String msg = addFoodEx.getMessage();
                        if (msg == null || !msg.toLowerCase().contains("unique")) {
                            addFoodEx.printStackTrace();
                        }
                    }

                    currentMeal.getItems().clear();
                    mealItemsView.getItems().clear();
                    System.out.println("Meal saved: " + name);
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            });
        });

        // back button
        backMealBtn.setOnAction(ev -> scene.setRoot(mainLayout));

        return mealLayout;
    }
}
