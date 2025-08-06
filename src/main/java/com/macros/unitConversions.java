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

    // Conversion logic, can add more as necessary
    public static double convert(double amount, String fromUnit, String toUnit) {
        fromUnit = fromUnit.toLowerCase();
        toUnit = toUnit.toLowerCase();

        if (fromUnit.equals(toUnit)) {
            return amount;
        }
        
        if (fromUnit.equals("g") && toUnit.equals("oz")) {
            return amount / 28.35;
        }

        if (fromUnit.equals("oz") && toUnit.equals("g")) {
            return amount * 28.35;
        }

        if (fromUnit.equals("cup") && toUnit.equals("floz")) {
            return amount * 8;
        }

        if (fromUnit.equals("floz") && toUnit.equals("cup")) {
            return amount / 8;
        }

        if (fromUnit.equals("oz") && toUnit.equals("lb")) {
            return amount / 16;
        }

        if (fromUnit.equals("lb") && toUnit.equals("oz")) {
            return amount * 16;
        }

        if (fromUnit.equals("ml") && toUnit.equals("l")) {
            return amount / 1000;
        }

        if (fromUnit.equals("l") && toUnit.equals("ml")) {
            return amount * 1000;
        }

        if (fromUnit.equals("tsp") && toUnit.equals("tbsp")) {
            return amount / 3;
        }

        if (fromUnit.equals("tbsp") && toUnit.equals("tsp")) {
            return amount * 3;
        }

        if (fromUnit.equals("g") && toUnit.equals("lb")) {
            return amount / 453.592;
        }

        if (fromUnit.equals("lb") && toUnit.equals("g")) {
            return amount * 453.592;
        }

        throw new UnsupportedOperationException("Conversion from " + fromUnit + " to " + toUnit + " is not supported.");
    }
}
