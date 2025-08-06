package com.macros;

import java.util.ArrayList;
import java.util.List;

public class Meal {
    
    public static class MealItem {
        private Food food;
        private double amount;
        private String unit;

        public MealItem(Food food, double amount, String unit) {
            this.food = food;
            this.amount = amount;
            this.unit = unit;
        }

        public Food getFood() {
            return food;
        }

        public double getAmount() {
            return amount;
        }

        public String getUnit() {
            return unit;
        }
    }

    public static class TotalMeal {
        private String name;
        private List<MealItem> items = new ArrayList<>();

        public TotalMeal (String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public List<MealItem> getItems() {
            return items;
        }

        public void addItem(Food food, double amount, String unit) {
            items.add(new MealItem(food, amount, unit));
        }
    }
}
