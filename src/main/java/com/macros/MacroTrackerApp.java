package com.macros;

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
        Button quitBtn = new Button("Quit");

        //Spacer
        Region spacer = new Region();
        spacer.setMinHeight(10);

        //Management buttons horizontally layed out
        HBox buttonRow = new HBox(20, dailyConsumptionBtn, foodDataBtn);
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
            //Currently a palceholder for actual button logic
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
