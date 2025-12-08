package com.macros;
public class Food {
    private Integer id;
    private final String foodName;
    private final double servingSize;
    private final String servingUnit;
    private final double fat;
    private final double carbs;
    private final double protein;

    public Food(String foodName, double servingSize, String servingUnit, double fat, double carbs, double protein) {
        this(null, foodName, servingSize, servingUnit, fat, carbs, protein);
    }

    public Food(Integer id, String foodName, double servingSize, String servingUnit, double fat, double carbs, double protein) {
        this.id = id;
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

    public Integer getId() {
        return id;
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
