package restaurant;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DataRetriever {

    private final Connection connection;

    public DataRetriever(Connection connection) {
        this.connection = connection;
    }

    public Dish findDishById(Integer id) {

        Dish dish = null;
        List<Ingredient> ingredients = new ArrayList<>();

        String sql = """
            SELECT 
                d.id AS dish_id,
                d.name AS dish_name,
                d.dish_type,
                i.id AS ingredient_id,
                i.name AS ingredient_name,
                i.price
            FROM dish d
            LEFT JOIN ingredient i ON d.id = i.id_dish
            WHERE d.id = ?
        """;

        try (PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                if (dish == null) {
                    dish = new Dish(
                            rs.getInt("dish_id"),
                            rs.getString("dish_name"),
                            DishTypeEnum.valueOf(rs.getString("dish_type")),
                            ingredients
                    );
                }
                int ingredientId = rs.getInt("ingredient_id");
                if (!rs.wasNull()) {
                    Ingredient ingredient = new Ingredient(
                            ingredientId,
                            rs.getString("ingredient_name"),
                            rs.getDouble("price"),
                            dish
                    );
                    ingredients.add(ingredient);
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return dish;
    }
    public List<Ingredient> findIngredients(int page, int size) {

        List<Ingredient> ingredients = new ArrayList<>();

        String sql = """
            SELECT 
                i.id,
                i.name,
                i.price,
                d.id AS dish_id,
                d.name AS dish_name,
                d.dish_type
            FROM ingredient i
            LEFT JOIN dish d ON i.id_dish = d.id
            ORDER BY i.id
            LIMIT ? OFFSET ?
        """;

        int offset = (page - 1) * size;

        try (PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setInt(1, size);
            ps.setInt(2, offset);

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {

                Dish dish = null;
                if (rs.getInt("dish_id") != 0) {
                    dish = new Dish(
                            rs.getInt("dish_id"),
                            rs.getString("dish_name"),
                            DishTypeEnum.valueOf(rs.getString("dish_type")),
                            new ArrayList<>()
                    );
                }

                Ingredient ingredient = new Ingredient(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getDouble("price"),
                        dish
                );

                ingredients.add(ingredient);
            }

        } catch (SQLException e) {
           throw new RuntimeException(e);
        }

        return ingredients;
    }
    public List<Ingredient> createIngredients(List<Ingredient> newIngredients) {

        String checkSql = """
            SELECT COUNT(*) FROM ingredient
            WHERE name = ? AND id_dish = ?
        """;

        String insertSql = """
            INSERT INTO ingredient (name, price, id_dish)
            VALUES (?, ?, ?)
            RETURNING id
        """;

        List<Ingredient> savedIngredients = new ArrayList<>();

        try {
            connection.setAutoCommit(false);

            try (
                    PreparedStatement checkStmt = connection.prepareStatement(checkSql);
                    PreparedStatement insertStmt = connection.prepareStatement(insertSql)
            ) {

                for (Ingredient ingredient : newIngredients) {
                    checkStmt.setString(1, ingredient.getName());
                    checkStmt.setInt(2, ingredient.getDish().getId());

                    ResultSet rs = checkStmt.executeQuery();
                    rs.next();

                    if (rs.getInt(1) > 0) {
                        throw new RuntimeException(
                                "L'ingrédient existe déjà : " + ingredient.getName()
                        );
                    }

                    insertStmt.setString(1, ingredient.getName());
                    insertStmt.setDouble(2, ingredient.getPrice());
                    insertStmt.setInt(3, ingredient.getDish().getId());

                    ResultSet inserted = insertStmt.executeQuery();
                    inserted.next();

                    ingredient.setId(inserted.getInt("id"));
                    savedIngredients.add(ingredient);
                }

                connection.commit();
                return savedIngredients;
            }

        } catch (Exception e) {
            try {
                connection.rollback();
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
            throw new RuntimeException(e);

        } finally {
            try {
                connection.setAutoCommit(true);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }

}
