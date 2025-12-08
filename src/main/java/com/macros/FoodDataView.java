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
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.util.Pair;

public class FoodDataView {

    public static VBox create(DBManager dbManager, ArrayList<Food> foods, Macros macros, Scene scene, VBox mainLayout, Label macroLabel) {
        Label title = new Label("Food Data Management");

        ListView<String> foodsList = new ListView<>();
        for (Food f : foods) foodsList.getItems().add(f.getName());
        foodsList.setPrefHeight(360);
        VBox.setVgrow(foodsList, Priority.ALWAYS);

        Button addBtn = new Button("Add Food");
        Button removeBtn = new Button("Remove Food");
        Button backBtn = new Button("Back");

        HBox buttonRow = new HBox(10, addBtn, removeBtn);
        buttonRow.setPadding(new Insets(10));
        buttonRow.setAlignment(javafx.geometry.Pos.CENTER);

        VBox layout = new VBox(12, title, foodsList, buttonRow, backBtn);
        layout.setPadding(new Insets(20));
        layout.setAlignment(javafx.geometry.Pos.CENTER);

        // Add food dialog
        addBtn.setOnAction(e -> {
            Dialog<Pair<String, String>> dialog = new Dialog<>();
            dialog.setTitle("Add Food");
            dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

            GridPane grid = new GridPane();
            grid.setHgap(10);
            grid.setVgap(10);

            TextField nameField = new TextField();
            TextField servingSizeField = new TextField("1");
            TextField unitField = new TextField("unit");
            TextField fatField = new TextField("0");
            TextField carbsField = new TextField("0");
            TextField proteinField = new TextField("0");

            grid.add(new Label("Name:"), 0, 0);
            grid.add(nameField, 1, 0);
            grid.add(new Label("Serving size:"), 0, 1);
            grid.add(servingSizeField, 1, 1);
            grid.add(new Label("Unit:"), 0, 2);
            grid.add(unitField, 1, 2);
            grid.add(new Label("Fat (g):"), 0, 3);
            grid.add(fatField, 1, 3);
            grid.add(new Label("Carbs (g):"), 0, 4);
            grid.add(carbsField, 1, 4);
            grid.add(new Label("Protein (g):"), 0, 5);
            grid.add(proteinField, 1, 5);

            dialog.getDialogPane().setContent(grid);
            dialog.setResultConverter(bt -> bt == ButtonType.OK ? new Pair<>(nameField.getText(), "") : null);
            dialog.showAndWait().ifPresent(result -> {
                String name = nameField.getText().trim();
                if (name.isEmpty()) return;
                double servingSize;
                double fat, carbs, protein;
                String unit = unitField.getText().trim();
                try {
                    servingSize = Utility.parseAmount(servingSizeField.getText());
                    fat = Utility.parseAmount(fatField.getText());
                    carbs = Utility.parseAmount(carbsField.getText());
                    protein = Utility.parseAmount(proteinField.getText());
                } catch (NumberFormatException ex) {
                    ex.printStackTrace();
                    return;
                }

                if (!UnitConversions.isValidUnit(unit)) {
                    unit = "unit";
                }

                Food food = new Food(name, servingSize, unit, fat, carbs, protein);
                try {
                    dbManager.addFood(food);
                    foods.add(food);
                    foodsList.getItems().add(food.getName());
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            });
        });

        // Remove selected food from DB and lists
        removeBtn.setOnAction(e -> {
            String sel = foodsList.getSelectionModel().getSelectedItem();
            if (sel == null) return;
            Food selected = foods.stream().filter(f -> f.getName().equals(sel)).findFirst().orElse(null);
            if (selected == null) return;

            try {
                dbManager.removeFood(selected);
            } catch (SQLException ex) {
                ex.printStackTrace();
                return;
            }

            foods.remove(selected);
            foodsList.getItems().remove(sel);

            // Remove any dailyMeals that reference this food (single-item meals)
            for (int i = Utility.dailyMeals.size()-1; i >= 0; i--) {
                Meal.TotalMeal tm = Utility.dailyMeals.get(i);
                if (tm.getItems().size() == 1) {
                    Meal.MealItem mi = tm.getItems().get(0);
                    if (mi.getFood().getName().equals(sel)) {
                        Utility.dailyMeals.remove(i);
                    }
                }
            }

            // Recalculate macros and update label
            macros.mealTotals(Utility.dailyMeals);
            String updatedMacroText = String.format("Fat: %.1fg | Carbs: %.1fg | Protein: %.1fg", macros.getTotalFat(), macros.getTotalCarbs(), macros.getTotalProtein());
            if (macroLabel != null) macroLabel.setText(updatedMacroText);
        });

        backBtn.setOnAction(e -> scene.setRoot(mainLayout));

        return layout;
    }
}
