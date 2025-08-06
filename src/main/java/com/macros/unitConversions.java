package com.macros;

import java.util.HashSet;
import java.util.Set;

public class unitConversions {

    // Unit Validation
    private static final Set<String> validUnits = new HashSet();

    static {
        validUnits.add("g");
        validUnits.add("oz");
        validUnits.add("lb");
        validUnits.add("cup");
        validUnits.add("ml");
        validUnits.add("l");
        validUnits.add("tsp");
        validUnits.add("tbsp");
        validUnits.add("floz");
        validUnits.add("unit");
    }

    public static boolean isValidUnit(String unit) {
        return validUnits.contains(unit.toLowerCase());
    }
    // Conversion methods for common units
    public static double gramsToOunces(double grams) {
        return grams / 28.35;
    }

    public static double ouncesToGrams(double ounces) {
        return ounces * 28.35;
    }

    public static double cupsToFluidOunces(double cups) {
        return cups * 8;
    }

    public static double fluidOuncesToCups(double fluidOunces) {
        return fluidOunces / 8;
    }
    
}
