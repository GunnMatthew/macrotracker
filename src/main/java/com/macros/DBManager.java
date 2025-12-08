package com.macros;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;

public class DBManager {
    private Connection connection;

    public DBManager(String dbFile) throws SQLException {
        connection = DriverManager.getConnection("jdbc:sqlite:" + dbFile);
    }

    public void createTable() throws SQLException {
        // Maybe look into a more organized naming convention to prevent mixing up where I'm putting/pulling data.
        // Table for foods
        String foods = """
                CREATE TABLE IF NOT EXISTS foods (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    name TEXT UNIQUE,
                    servingSize REAL,
                    servingUnit TEXT,
                    fat REAL,
                    carbs REAL,
                    protein REAL
                );
                """;

        // Table to track foods consumed
        String consumedFoods = """
            CREATE TABLE IF NOT EXISTS consumedFoods (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT,
                amount REAL,
                unit TEXT
            );
            """;

        // Table to store assembled meals
        String meals = """
            CREATE TABLE IF NOT EXISTS meals (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT UNIQUE
            );
            """;

        // Table to store meal items (reference food id)
        String mealItems = """
            CREATE TABLE IF NOT EXISTS mealItems (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                meal_id INTEGER,
                food_id INTEGER,
                amount REAL,
                unit TEXT,
                FOREIGN KEY(meal_id) REFERENCES meals(id),
                FOREIGN KEY(food_id) REFERENCES foods(id)
            );
            """;

        // Table for session data (For tracking date/time.  Utilize in future for accounts)
        String sessionData = """
            CREATE TABLE IF NOT EXISTS sessionData (
                id INTEGER PRIMARY KEY CHECK (id = 1),
                last_opened_date TEXT
            );
            """;

        // Table for storing macro data for analytical purposes
        String macroData = """
            CREATE TABLE IF NOT EXISTS macroData (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                date TEXT,
                fat REAL,
                carbs REAL,
                protein REAL
            );
            """;

        try (Statement statement = connection.createStatement()) {
            statement.execute(foods);
            statement.execute(consumedFoods);
            statement.execute(meals);
            statement.execute(mealItems);
            statement.execute(sessionData);
            statement.execute(macroData);
        }
    }

    public void close() throws SQLException {
        if (connection != null) connection.close();
    }
    //Add food to database
    public void addFood(Food food) throws SQLException {
        String sql = "INSERT INTO foods (name, servingSize, servingUnit, fat, carbs, protein) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, food.getName());
            pstmt.setDouble(2, food.getServingSize());
            pstmt.setString(3, food.getServingUnit());
            pstmt.setDouble(4, food.getFat());
            pstmt.setDouble(5, food.getCarbs());
            pstmt.setDouble(6, food.getProtein());
            pstmt.executeUpdate();
        }
    }

    //Remove food from database
    public void removeFood(Food food) throws SQLException {
        String sql = "DELETE FROM foods WHERE name = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, food.getName());
            pstmt.executeUpdate();
        }
    }

    //Add consumed foods for tracking macros
    public void addConsumedFood(String foodName, double amount, String unit) throws SQLException {
        String sql = "INSERT INTO consumedFoods (name, amount, unit) VALUES (?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, foodName);
            pstmt.setDouble(2, amount);
            pstmt.setString(3, unit);
            pstmt.executeUpdate();
        }
    }

    //Manual resetting of consumed foods ***Replace later with automatic process at midnight
    public void resetConsumedFoods() throws SQLException {
        String sql = "DELETE FROM consumedFoods";
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate(sql);
        }
    }

    public ArrayList<Food> getAllFoods() throws SQLException {
        ArrayList<Food> foods = new ArrayList<>();
        String sql = "SELECT id, name, servingSize, servingUnit, fat, carbs, protein FROM foods";
        
        try (Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery(sql)) {
            while (rs.next()) {
                Integer id = rs.getInt("id");
                String name = rs.getString("name");
                double servingSize = rs.getDouble("servingSize");
                String servingUnit = rs.getString("servingUnit");
                double fat = rs.getDouble("fat");
                double carbs = rs.getDouble("carbs");
                double protein = rs.getDouble("protein");
                foods.add(new Food(id, name, servingSize, servingUnit, fat, carbs, protein));
            }
        }
        return foods;
    }

    public void addMeal(Meal.TotalMeal meal) throws SQLException {
        // If meal name exists, replace it else, create new meal
        Integer mealId = null;

        String findMeal = "SELECT id FROM meals WHERE lower(name) = lower(?) LIMIT 1";
        try (PreparedStatement findStmt = connection.prepareStatement(findMeal)) {
            findStmt.setString(1, meal.getName());
            try (ResultSet r = findStmt.executeQuery()) {
                if (r.next()) mealId = r.getInt("id");
            }
        }

        if (mealId == null) {
            String insertMeal = "INSERT INTO meals (name) VALUES (?)";
            try (PreparedStatement mealStmt = connection.prepareStatement(insertMeal, Statement.RETURN_GENERATED_KEYS)) {
                mealStmt.setString(1, meal.getName());
                mealStmt.executeUpdate();
                try (ResultSet gen = mealStmt.getGeneratedKeys()) {
                    if (!gen.next()) throw new SQLException("failed to create meal");
                    mealId = gen.getInt(1);
                }
            }
        } else {
            try (PreparedStatement delItems = connection.prepareStatement("DELETE FROM mealItems WHERE meal_id = ?")) {
                delItems.setInt(1, mealId);
                delItems.executeUpdate();
            }
        }

        // Create the meal by inserting items
        String insertItem = "INSERT INTO mealItems (meal_id, food_id, amount, unit) VALUES (?, ?, ?, ?)";
        try (PreparedStatement itemStmt = connection.prepareStatement(insertItem)) {
            for (Meal.MealItem mi : meal.getItems()) {
                Integer foodId = mi.getFood().getId();
                if (foodId == null) {
                    String find = "SELECT id FROM foods WHERE lower(name) = lower(?) LIMIT 1";
                    try (PreparedStatement findStmt = connection.prepareStatement(find)) {
                        findStmt.setString(1, mi.getFood().getName());
                        try (ResultSet r = findStmt.executeQuery()) {
                            if (r.next()) foodId = r.getInt("id");
                        }
                    }
                }

                if (foodId == null) {
                    throw new SQLException("Failed to resolve food id for: " + mi.getFood().getName());
                }

                itemStmt.setInt(1, mealId);
                itemStmt.setInt(2, foodId);
                itemStmt.setDouble(3, mi.getAmount());
                itemStmt.setString(4, mi.getUnit());
                itemStmt.addBatch();
            }
            itemStmt.executeBatch();
        }
    }

    // Load all meals
    public ArrayList<Meal.TotalMeal> getAllMeals() throws SQLException {
        ArrayList<Meal.TotalMeal> meals = new ArrayList<>();

        ArrayList<Food> foods = getAllFoods();

        String mealSql = "SELECT id, name FROM meals";
        try (Statement stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery(mealSql)) {
            while (rs.next()) {
                int mealId = rs.getInt("id");
                String mealName = rs.getString("name");
                Meal.TotalMeal tm = new Meal.TotalMeal(mealName);

                String itemsSql = "SELECT food_id, amount, unit FROM mealItems WHERE meal_id = ?";
                try (PreparedStatement it = connection.prepareStatement(itemsSql)) {
                    it.setInt(1, mealId);
                    try (ResultSet ir = it.executeQuery()) {
                        while (ir.next()) {
                            int foodId = ir.getInt("food_id");
                            double amount = ir.getDouble("amount");
                            String unit = ir.getString("unit");

                            Food resolved = null;
                            for (Food f : foods) {
                                if (f.getId() != null && f.getId() == foodId) {
                                    resolved = f;
                                    break;
                                }
                            }

                            if (resolved != null) tm.addItem(resolved, amount, unit);
                        }
                    }
                }

                meals.add(tm);
            }
        }

        return meals;
    }

    // Remove meal and ingredients
    public void removeMeal(String mealName) throws SQLException {
        String find = "SELECT id FROM meals WHERE name = ?";
        try (PreparedStatement f = connection.prepareStatement(find)) {
            f.setString(1, mealName);
            try (ResultSet r = f.executeQuery()) {
                if (!r.next()) return; // nothing to do
                int mealId = r.getInt("id");

                try (PreparedStatement delItems = connection.prepareStatement("DELETE FROM mealItems WHERE meal_id = ?")) {
                    delItems.setInt(1, mealId);
                    delItems.executeUpdate();
                }

                try (PreparedStatement delMeal = connection.prepareStatement("DELETE FROM meals WHERE id = ?")) {
                    delMeal.setInt(1, mealId);
                    delMeal.executeUpdate();
                }
            }
        }
    }

    public ArrayList<Object[]> getAllConsumedFoods() throws SQLException {
        ArrayList<Object[]> consumedFoods = new ArrayList<>();
        String sql = "SELECT name, amount, unit FROM consumedFoods";
        
        try (Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery(sql)) {
            while (rs.next()) {
                consumedFoods.add(new Object[] {
                    rs.getString("name"),
                    rs.getDouble("amount"),
                    rs.getString("unit")
                });
            }
        }
        return consumedFoods;
    }

    // Remove a food entry.  Make sense now that meals go to food, if you make a mistake you wanna remove it
    public void removeConsumedFood(String name, double amount, String unit) throws SQLException {
        String sql = "DELETE FROM consumedFoods WHERE id = (SELECT id FROM consumedFoods WHERE name = ? AND amount = ? AND unit = ? LIMIT 1)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, name);
            pstmt.setDouble(2, amount);
            pstmt.setString(3, unit);
            pstmt.executeUpdate();
        }
    }

    // Check last opened date for macro resetting
    public LocalDate getLastOpenedDate() throws SQLException {
        String sql = "SELECT last_opened_date FROM sessionData WHERE id = 1";
        try (Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery(sql)) {
            if (rs.next()) {
                String dateStr = rs.getString("last_opened_date");
                if (dateStr != null && !dateStr.isEmpty()) {
                    return LocalDate.parse(dateStr);
                }
            }
        }
        return null;
    }

    // Set/update last opened date
    public void setLastOpenedDate(LocalDate date) throws SQLException {
        String checkSql = "SELECT COUNT(*) FROM sessionData WHERE id = 1";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(checkSql)) {
            rs.next();
            int count = rs.getInt(1);
            if (count == 0) {
                String insertSql = "INSERT INTO sessionData (id, last_opened_date) VALUES (1, ?)";
                try (PreparedStatement pstmt = connection.prepareStatement(insertSql)) {
                    pstmt.setString(1, date.toString());
                    pstmt.executeUpdate();
                }
            } else {
                String updateSql = "UPDATE sessionData SET last_opened_date = ? WHERE id = 1";
                try (PreparedStatement pstmt = connection.prepareStatement(updateSql)) {
                    pstmt.setString(1, date.toString());
                    pstmt.executeUpdate();
                }
            }
        }
    }

    // Save daily macros for history tracking - NOT USED YET.  ADDED FOR FUTURE
    public void saveMacroHistory(LocalDate date, double fat, double carbs, double protein) throws SQLException {
        String checkSql = "SELECT COUNT(*) FROM macroData WHERE date = ?";
        try (PreparedStatement checkStmt = connection.prepareStatement(checkSql)) {
            checkStmt.setString(1, date.toString());
            try (ResultSet rs = checkStmt.executeQuery()) {
                rs.next();
                int count = rs.getInt(1);
                
                if (count == 0) {
                    String insertSql = "INSERT INTO macroData (date, fat, carbs, protein) VALUES (?, ?, ?, ?)";
                    try (PreparedStatement insertStmt = connection.prepareStatement(insertSql)) {
                        insertStmt.setString(1, date.toString());
                        insertStmt.setDouble(2, fat);
                        insertStmt.setDouble(3, carbs);
                        insertStmt.setDouble(4, protein);
                        insertStmt.executeUpdate();
                    }
                } else {
                    String updateSql = "UPDATE macroData SET fat = ?, carbs = ?, protein = ? WHERE date = ?";
                    try (PreparedStatement updateStmt = connection.prepareStatement(updateSql)) {
                        updateStmt.setDouble(1, fat);
                        updateStmt.setDouble(2, carbs);
                        updateStmt.setDouble(3, protein);
                        updateStmt.setString(4, date.toString());
                        updateStmt.executeUpdate();
                    }
                }
            }
        }
    }

    // Get average macros for the past N days - NOT USED YET.  ADDED FOR FUTURE
    public double[] getAverageMacrosLastNDays(int days) throws SQLException {
        String sql = "SELECT AVG(fat) as avgFat, AVG(carbs) as avgCarbs, AVG(protein) as avgProtein FROM macroData WHERE date >= date('now', '-' || ? || ' days')";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, days);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    double avgFat = rs.getDouble("avgFat");
                    double avgCarbs = rs.getDouble("avgCarbs");
                    double avgProtein = rs.getDouble("avgProtein");
                    return new double[] { avgFat, avgCarbs, avgProtein };
                }
            }
        }
        return new double[] { 0, 0, 0 };
    }

    // Get macros for a specific date - NOT USED YET.  ADDED FOR FUTURE
    public double[] getMacrosForDate(LocalDate date) throws SQLException {
        String sql = "SELECT fat, carbs, protein FROM macroData WHERE date = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, date.toString());
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    double fat = rs.getDouble("fat");
                    double carbs = rs.getDouble("carbs");
                    double protein = rs.getDouble("protein");
                    return new double[] { fat, carbs, protein };
                }
            }
        }
        return null;
    }

    

}
