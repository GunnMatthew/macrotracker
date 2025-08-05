package com.macros;
public class Food {
    private String foodName;
    private double servingSize;
    private String servingUnit;
    private double fat;
    private double carbs;
    private double protein;

    public Food(String foodName, double servingSize, String servingUnit, double fat, double carbs, double protein) {
        this.foodName = foodName;
        this.servingSize = servingSize;
        this.servingUnit = servingUnit;
        this.fat = fat;
        this.carbs = carbs;
        this.protein = protein;
    }

    public String getName() {
        return foodName;
    }

    public double getServingSize() {
        return servingSize;
    }

    public String getServingUnit() {
        return servingUnit;
    }

    public double getFat() {
        return fat;
    }

    public double getCarbs() {
        return carbs;
    }

    public double getProtein() {
        return protein;
    }

    
    @Override
    public String toString() {
        return String.format("%s (Serving Size: %s%s) - Fat: %.1fg, Carbs: %.1fg, Protein: %.1fg", foodName, servingSize, servingUnit, fat, carbs, protein);
    }
}
