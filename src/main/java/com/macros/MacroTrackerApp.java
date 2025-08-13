package com.macros;

import java.sql.SQLException;
import java.util.ArrayList;

import javafx.application.Application;
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
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Pair;

public class MacroTrackerApp extends Application {
    //Database initialization
    private DBManager dbManager;

    @Override
    public void start(Stage primaryStage) {
        try {
            dbManager = new DBManager("macrotracker.db");
        } catch (SQLException e) {
            e.printStackTrace();
        }

        final ArrayList<Food> foods;
        try {
            foods = dbManager.getAllFoods();
        } catch (SQLException e) {
            e.printStackTrace();
            return;
        }

        try {
            Main.dailyMeals.clear();
            ArrayList<Object[]> consumed = dbManager.getAllConsumedFoods();
            
            for (Object[] entry : consumed) {
                String foodName = (String) entry[0];
                double amount = (Double) entry[1];
                String unit = (String) entry[2];

                Food food = null;

                for (Food f : foods) {
                    if (f.getName().equalsIgnoreCase(foodName)) {
                        food = f;
                        break;
                    }
                }

                if (food != null) {
                    Meal.TotalMeal meal = new Meal.TotalMeal("Single Food: " + food.getName());
                    meal.addItem(food, amount, unit);
                    Main.dailyMeals.add(meal);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        //Handles retrieving macro data
        ArrayList<Meal.TotalMeal> dailyMeals = Main.dailyMeals;
        Macros macros = new Macros();
        macros.mealTotals(dailyMeals);
        String macroText = String.format("Fat: %.1fg | Carbs: %.1fg | Protein: %.1fg", macros.getTotalFat(), macros.getTotalCarbs(), macros.getTotalProtein());

        //Labels
        Label title = new Label("Macro Tracker");
        Label macroLabel = new Label(macroText);
        Label staticDaily = new Label("Daily Macros:");

        //Buttons
        Button dailyConsumptionBtn = new Button("Consuption Mgmt.");
        Button foodDataBtn = new Button("Food Data Mgmt.");
        Button mealMgmtBtn = new Button("Meal Mgmt.");
        Button quitBtn = new Button("Quit");

        //Spacer
        Region spacer = new Region();
        spacer.setMinHeight(10);

        //Management buttons horizontally layed out
        HBox buttonRow = new HBox(20, dailyConsumptionBtn, foodDataBtn, mealMgmtBtn);
        buttonRow.setAlignment(javafx.geometry.Pos.CENTER);
        buttonRow.setPadding(new Insets(10));
        buttonRow.setPadding(new Insets(10));
        
        VBox layout = new VBox(20, title, buttonRow, quitBtn, spacer, staticDaily, macroLabel);
        layout.setPadding(new Insets(20));
        layout.setAlignment(javafx.geometry.Pos.CENTER);

        Scene scene = new Scene(layout, 600, 300);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Macro Tracker");
        primaryStage.show();

        //Consumption Button Configuration
        dailyConsumptionBtn.setStyle("-fx-font-size: 12px;");
        dailyConsumptionBtn.setPrefWidth(150);
        dailyConsumptionBtn.setOnAction(e -> {
            
            ListView<String> foodListView = new ListView<>();

            for (Food food : foods) {
                foodListView.getItems().add(food.getName());
            }

            foodListView.setPrefHeight(120);

            Label consumptionLabel = new Label("Consumption Management");
            Label staticDailyCopy = new Label("Daily Macros:");
            Label macroLabelCopy = new Label(macroLabel.getText());

            Button backBtn = new Button("Back");
            Button addFoodBtn = new Button("Add food");
            Button resetMacrosBtn = new Button("Reset Macros");

            HBox consumeBtnLayout = new HBox(20, addFoodBtn, resetMacrosBtn);
            consumeBtnLayout.setAlignment(javafx.geometry.Pos.CENTER);
            consumeBtnLayout.setPadding(new Insets(10));

            VBox consumptionLayout = new VBox(20, consumptionLabel, foodListView, consumeBtnLayout, staticDailyCopy, macroLabelCopy, backBtn);
            consumptionLayout.setAlignment(javafx.geometry.Pos.CENTER);
            consumptionLayout.setPadding(new Insets(20));

            //Disable addFoodBtn until a food item is selected
            addFoodBtn.setDisable(true);
            foodListView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
                addFoodBtn.setDisable(newVal == null);
            });

            scene.setRoot(consumptionLayout);

            //Add Food Button
            addFoodBtn.setOnAction(ev -> {
                String selectedFoodName = foodListView.getSelectionModel().getSelectedItem();

                if (selectedFoodName != null) {
                    Food selectedFood = foods.stream().filter(f -> f.getName().equals(selectedFoodName)).findFirst().orElse(null);

                    if (selectedFood != null) {
                        Dialog<Pair<String, String>> dialog = new Dialog<>();
                        dialog.setTitle("Add Food");
                        dialog.setHeaderText("Enter Amount and unit for: " + selectedFood.getName());

                        ButtonType addButtonType = new ButtonType("Add", ButtonType.OK.getButtonData());
                        dialog.getDialogPane().getButtonTypes().addAll(addButtonType, ButtonType.CANCEL);

                        GridPane grid = new GridPane();
                        grid.setHgap(10);
                        grid.setVgap(10);

                        TextField amountField = new TextField("1");
                        TextField unitField = new TextField(selectedFood.getServingUnit());

                        grid.add(new Label("Amount:"), 0, 0);
                        grid.add(amountField, 1, 0);
                        grid.add(new Label("Unit:"), 0, 1);
                        grid.add(unitField, 1, 1);

                        dialog.getDialogPane().setContent(grid);
                        dialog.setResultConverter(dialogButton -> {
                            if (dialogButton == addButtonType) {
                                return new Pair<>(amountField.getText(), unitField.getText());
                            }
                            return null;
                        });

                        dialog.showAndWait().ifPresent(result -> {
                            try {
                                double amount = Double.parseDouble(result.getKey());
                                String unit = result.getValue();

                                try {
                                    dbManager.addConsumedFood(selectedFood.getName(), amount, unit);
                                } catch (SQLException ex) {
                                    ex.printStackTrace();
                                }

                                Meal.TotalMeal meal = new Meal.TotalMeal("Single Food: " + selectedFood.getName());
                                meal.addItem(selectedFood, amount, unit);
                                Main.dailyMeals.add(meal);

                                macros.mealTotals(Main.dailyMeals);

                                String updatedMacroText = String.format("Fat: %.1fg | Carbs: %.1fg | Protein: %.1fg", macros.getTotalFat(), macros.getTotalCarbs(), macros.getTotalProtein());

                                macroLabel.setText(updatedMacroText);
                                macroLabelCopy.setText(updatedMacroText);
                            } catch (NumberFormatException ex) {
                                ex.printStackTrace();
                            }
                        });
                    }
                }

                System.out.println("Add Food button pressed.");
            });

            //Reset Macro Button
            resetMacrosBtn.setOnAction( ev -> {
                Main.resetConsumedFood(dbManager);
                macros.mealTotals(Main.dailyMeals);

                String updatedMacroText = String.format("Fat: %.1fg | Carbs: %.1fg | Protein: %.1fg", macros.getTotalFat(), macros.getTotalCarbs(), macros.getTotalProtein());
                 macroLabel.setText(updatedMacroText);
                macroLabelCopy.setText(updatedMacroText);
            });

            //Consumption Menu Back button
            backBtn.setOnAction(ev -> {
                macros.mealTotals(Main.dailyMeals);
                String updatedMacroText = String.format("Fat: %.1fg | Carbs: %.1fg | Protein: %.1fg", macros.getTotalFat(), macros.getTotalCarbs(), macros.getTotalProtein());
                macroLabel.setText(updatedMacroText);
                scene.setRoot(layout);
            });

            System.out.println("Consumption Management Button Clicked");
        });

        //Food Button Configuration

        foodDataBtn.setStyle("-fx-font-size: 12px;");
        foodDataBtn.setPrefWidth(150);
        foodDataBtn.setOnAction(e -> {
            //Currently a palceholder for actual button logic
            System.out.println("Food Data Management Button Clicked");
        });

        quitBtn.setOnAction(e -> primaryStage.close());
    }
        
    public static void main(String[] args) {
        launch(args);
    }
    
}
