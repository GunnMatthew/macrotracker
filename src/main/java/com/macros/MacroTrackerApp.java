package com.macros;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class MacroTrackerApp extends Application {
    private DBManager dbManager;

    @Override
    public void start(Stage primaryStage) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM dd, yyyy");
        
        // Initialize database manager and create necessary tables if they don't exist
        try {
            dbManager = new DBManager("macrotracker.db");
            dbManager.createTable();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Load foods first (needed to compute macros from stored consumed items)
        final ArrayList<Food> foods;
        try {
            foods = dbManager.getAllFoods();
        } catch (SQLException e) {
            e.printStackTrace();
            return;
        }

        // Check date, save history and reset only if date changed since last open.
        try {
            LocalDate today = LocalDate.now();
            LocalDate lastOpened = dbManager.getLastOpenedDate();

            // Load consumed items from DB so we can compute accurate macros for history
            ArrayList<Meal.TotalMeal> consumedMeals = new ArrayList<>();
            ArrayList<Object[]> consumedRows = dbManager.getAllConsumedFoods();
            for (Object[] entry : consumedRows) {
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

                    // avoid adding obvious duplicates
                    boolean exists = false;
                    for (Meal.TotalMeal existing : consumedMeals) {
                        if (existing.getItems().size() == meal.getItems().size()) {
                            exists = true;
                            break;
                        }
                    }
                    if (!exists) consumedMeals.add(meal);
                }
            }

            if (lastOpened != null && lastOpened.isBefore(today)) {
                // Date changed: compute macros based on consumedRows and save to history
                Macros yesterdayMacros = new Macros();
                yesterdayMacros.mealTotals(consumedMeals);
                dbManager.saveMacroHistory(lastOpened, yesterdayMacros.getTotalFat(), yesterdayMacros.getTotalCarbs(), yesterdayMacros.getTotalProtein());

                // clear stored consumed items for the new day
                dbManager.resetConsumedFoods();
                Utility.dailyMeals.clear();
            } else {
                // No date change (or first run) - populate in-memory list from DB
                Utility.dailyMeals.clear();
                Utility.dailyMeals.addAll(consumedMeals);
            }

            // Update last opened date to today
            dbManager.setLastOpenedDate(today);

        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Handle retrieving macro data
        ArrayList<Meal.TotalMeal> dailyMeals = Utility.dailyMeals;
        Macros macros = new Macros();
        macros.mealTotals(dailyMeals);
        String macroText = String.format("Fat: %.1fg | Carbs: %.1fg | Protein: %.1fg", macros.getTotalFat(), macros.getTotalCarbs(), macros.getTotalProtein());

        Label title = new Label("Macro Tracker");
        Label macroLabel = new Label(macroText);
        Label staticDaily = new Label("Daily Macros:");
        Label date = new Label(LocalDate.now().format(formatter));

        Button dailyConsumptionBtn = new Button("Consuption Mgmt.");
        Button foodDataBtn = new Button("Food Data Mgmt.");
        Button mealMgmtBtn = new Button("Meal Mgmt.");
        Button quitBtn = new Button("Quit");

        Region spacer = new Region();
        spacer.setMinHeight(10);

        //Management buttons layout
        HBox buttonRow = new HBox(20, dailyConsumptionBtn, foodDataBtn, mealMgmtBtn);
        buttonRow.setAlignment(javafx.geometry.Pos.CENTER);
        buttonRow.setPadding(new Insets(10));
        buttonRow.setPadding(new Insets(10));
        
        VBox layout = new VBox(20, title, buttonRow, quitBtn, spacer, date, staticDaily, macroLabel);
        layout.setPadding(new Insets(20));
        layout.setAlignment(javafx.geometry.Pos.CENTER);

        // Create and display the main application scene
        Scene scene = new Scene(layout, 800, 450);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Macro Tracker");
        primaryStage.show();

        // Consumption Button Configuration (Consumption Management)
        dailyConsumptionBtn.setStyle("-fx-font-size: 12px;");
        dailyConsumptionBtn.setPrefWidth(150);
        dailyConsumptionBtn.setOnAction(e -> {
            VBox consumption = ConsumptionView.create(dbManager, foods, macros, scene, layout, macroLabel);
            scene.setRoot(consumption);
        });

        //Food Button Configuration
        foodDataBtn.setStyle("-fx-font-size: 12px;");
        foodDataBtn.setPrefWidth(150);
        foodDataBtn.setOnAction(e -> {
            VBox foodLayout = FoodDataView.create(dbManager, foods, macros, scene, layout, macroLabel);
            scene.setRoot(foodLayout);
        });

        // Meal Management Button Configuration
        mealMgmtBtn.setStyle("-fx-font-size: 12px;");
        mealMgmtBtn.setPrefWidth(150);
        mealMgmtBtn.setOnAction(e -> {
            VBox mealLayout = MealManagementView.create(dbManager, foods, macros, scene, layout);
            scene.setRoot(mealLayout);
        });

        quitBtn.setOnAction(e -> primaryStage.close());
    }
        
    public static void main(String[] args) {
        launch(args);
    }
    
}
