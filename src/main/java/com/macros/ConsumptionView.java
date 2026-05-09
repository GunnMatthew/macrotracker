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

public class ConsumptionView {
    public static VBox create(DBManager dbManager, ArrayList<Food> foods, Macros macros, Scene scene, VBox mainLayout, Label macroLabel) {
        // Left List (Avail foods)
        Label leftTitle = new Label("Food to Add");
        ListView<String> availableFoods = new ListView<>();
        for (Food f : foods) availableFoods.getItems().add(f.getName());
        availableFoods.setPrefSize(300, 360);
        VBox.setVgrow(availableFoods, Priority.ALWAYS);

        VBox leftCol = new VBox(6, leftTitle, availableFoods);
        leftCol.setPadding(new Insets(8));
        leftCol.setAlignment(javafx.geometry.Pos.TOP_CENTER);

        // Right List (consumed Items)
        Label rightTitle = new Label("Food Consumed");
        ListView<String> consumedList = new ListView<>();
        consumedList.setPrefSize(300, 360);
        VBox.setVgrow(consumedList, Priority.ALWAYS);

        // Populate consumedList from dailyMeals
        for (Meal.TotalMeal tm : Utility.dailyMeals) {
            for (Meal.MealItem mi : tm.getItems()) {
                if (mi == null || mi.getFood() == null) continue;
                consumedList.getItems().add(String.format("%s - %s %s", mi.getFood().getName(), mi.getAmount(), mi.getUnit()));
            }
        }

        VBox rightCol = new VBox(6, rightTitle, consumedList);
        rightCol.setPadding(new Insets(8));
        rightCol.setAlignment(javafx.geometry.Pos.TOP_CENTER);

        // Page management buttons
        Button addBtn = new Button("Add -->");
        Button removeBtn = new Button("Remove <--");
        Button resetBtn = new Button("Reset Macros");
        Button backBtn = new Button("Back");

        addBtn.setMaxWidth(Double.MAX_VALUE);
        removeBtn.setMaxWidth(Double.MAX_VALUE);
        resetBtn.setMaxWidth(Double.MAX_VALUE);
        backBtn.setMaxWidth(Double.MAX_VALUE);

        VBox centerCol = new VBox(12, addBtn, removeBtn, resetBtn, backBtn);
        centerCol.setPadding(new Insets(8));
        centerCol.setAlignment(javafx.geometry.Pos.CENTER);

        HBox mainRow = new HBox(18, leftCol, centerCol, rightCol);
        mainRow.setPadding(new Insets(10));

        // Text labels
        Label header = new Label("Consumption Management");
        Label staticDaily = new Label("Daily Macros:");
        Label macroCopy = new Label(macroLabel.getText());

        VBox container = new VBox(10, header, mainRow, staticDaily, macroCopy);
        container.setPadding(new Insets(12));
        container.setAlignment(javafx.geometry.Pos.TOP_CENTER);

        // Enable/disable buttons based on selections
        addBtn.setDisable(true);
        removeBtn.setDisable(true);
        availableFoods.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newV) -> addBtn.setDisable(newV == null));
        consumedList.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newV) -> removeBtn.setDisable(newV == null));

        // Add consumed item handler
        addBtn.setOnAction(ev -> {
            String selName = availableFoods.getSelectionModel().getSelectedItem();
            if (selName == null) return;
            Food selected = foods.stream().filter(f -> f.getName().equals(selName)).findFirst().orElse(null);
            if (selected == null) return;

            Dialog<Pair<String, String>> dialog = new Dialog<>();
            dialog.setTitle("Add Consumed Food");
            dialog.setHeaderText("Enter amount and unit for: " + selected.getName());
            dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

            GridPane grid = new GridPane();
            grid.setHgap(8);
            grid.setVgap(8);
            TextField amountField = new TextField("1");
            TextField unitField = new TextField(selected.getServingUnit());
            grid.add(new Label("Amount:"), 0, 0);
            grid.add(amountField, 1, 0);
            grid.add(new Label("Unit:"), 0, 1);
            grid.add(unitField, 1, 1);
            dialog.getDialogPane().setContent(grid);

            dialog.setResultConverter(bt -> bt == ButtonType.OK ? new Pair<>(amountField.getText(), unitField.getText()) : null);
            dialog.showAndWait().ifPresent(p -> {
                String amtS = p.getKey().trim();
                String unit = p.getValue().trim();
                double amount;
                try { amount = Utility.parseAmount(amtS); } catch (NumberFormatException ex) { return; }
                if (!UnitConversions.isValidUnit(unit)) unit = "unit";

                try { dbManager.addConsumedFood(selected.getName(), amount, unit); } catch (SQLException ex) { ex.printStackTrace(); return; }

                Meal.TotalMeal meal = new Meal.TotalMeal("Single Food: " + selected.getName());
                meal.addItem(selected, amount, unit);
                Utility.dailyMeals.add(meal);

                consumedList.getItems().add(String.format("%s - %s %s", selected.getName(), amount, unit));

                macros.mealTotals(Utility.dailyMeals);
                String updated = String.format("Fat: %.1fg | Carbs: %.1fg | Protein: %.1fg", macros.getTotalFat(), macros.getTotalCarbs(), macros.getTotalProtein());
                macroLabel.setText(updated);
                macroCopy.setText(updated);
            });
        });

        // Remove consumed item handler
        removeBtn.setOnAction(ev -> {
            String sel = consumedList.getSelectionModel().getSelectedItem();
            if (sel == null) return;
            String[] parts = sel.split(" - ", 2);
            if (parts.length < 2) return;
            String name = parts[0].trim();
            String rest = parts[1].trim();
            String[] restParts = rest.split(" ");
            if (restParts.length < 1) return;
            String unit = restParts[restParts.length-1];
            double amount;
            try {
                String amtToken = rest.substring(0, rest.lastIndexOf(' ')).trim();
                amount = Utility.parseAmount(amtToken);
            } catch (Exception ex) {
                try { amount = Utility.parseAmount(restParts[0]); } catch (Exception ex2) { return; }
            }

            try { dbManager.removeConsumedFood(name, amount, unit); } catch (SQLException ex) { ex.printStackTrace(); return; }

            // remove matching entry from Main.dailyMeals
            for (int i = Utility.dailyMeals.size()-1; i >= 0; i--) {
                Meal.TotalMeal tm = Utility.dailyMeals.get(i);
                if (tm.getItems().isEmpty()) continue;
                Meal.MealItem mi = tm.getItems().get(0);
                if (mi.getFood() != null && mi.getFood().getName().equalsIgnoreCase(name) && mi.getAmount() == amount && mi.getUnit().equalsIgnoreCase(unit)) {
                    Utility.dailyMeals.remove(i);
                    break;
                }
            }

            consumedList.getItems().remove(sel);
            macros.mealTotals(Utility.dailyMeals);
            String updated = String.format("Fat: %.1fg | Carbs: %.1fg | Protein: %.1fg", macros.getTotalFat(), macros.getTotalCarbs(), macros.getTotalProtein());
            macroLabel.setText(updated);
            macroCopy.setText(updated);
        });

        // Reset handler
        resetBtn.setOnAction(ev -> {
            Utility.resetConsumedFood(dbManager);
            Utility.dailyMeals.clear();
            consumedList.getItems().clear();
            macros.mealTotals(Utility.dailyMeals);
            String updated = String.format("Fat: %.1fg | Carbs: %.1fg | Protein: %.1fg", macros.getTotalFat(), macros.getTotalCarbs(), macros.getTotalProtein());
            macroLabel.setText(updated);
            macroCopy.setText(updated);
        });

        // Back menu handler
        backBtn.setOnAction(ev -> {
            macros.mealTotals(Utility.dailyMeals);
            String updated = String.format("Fat: %.1fg | Carbs: %.1fg | Protein: %.1fg", macros.getTotalFat(), macros.getTotalCarbs(), macros.getTotalProtein());
            macroLabel.setText(updated);
            scene.setRoot(mainLayout);
        });

        return container;
    }
}