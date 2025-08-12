package com.macros;

import java.util.List;

public class Macros {
    private double totalFat;
    private double totalCarbs;
    private double totalProtein;

    public void mealTotals(List<Meal.TotalMeal> meals) {
        totalFat = 0;
        totalCarbs = 0;
        totalProtein = 0;

        for (Meal.TotalMeal meal : meals) {
            for (Meal.MealItem item : meal.getItems()) {
                Food food = item.getFood();
                double amount = item.getAmount();
                String unit = item.getUnit();
                double servingSize = food.getServingSize();
                String servingUnit = food.getServingUnit();

                double convertAmount = UnitConversions.convert(amount, unit, servingUnit);

                double multiplier = convertAmount / servingSize;

                totalFat += food.getFat() * multiplier;
                totalCarbs += food.getCarbs() * multiplier;
                totalProtein += food.getProtein() * multiplier;
            }
        }
    }

    public double getTotalFat() {
        return totalFat;
    }

    public double getTotalCarbs() {
        return totalCarbs;
    }

    public double getTotalProtein() {
        return totalProtein;
    }
}
