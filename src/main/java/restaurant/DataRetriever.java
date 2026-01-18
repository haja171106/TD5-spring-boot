package restaurant;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DataRetriever {

    public Dish findDishById(Integer id) {
        String sql = """
            SELECT d.id AS dish_id,
                   d.name AS dish_name,
                   d.price AS dish_price,
                   d.dish_type,
                   i.id AS ingredient_id,
                   i.name AS ingredient_name,
                   i.category,
                   i.price AS ingredient_price,
                   di.quantity
            FROM dish d
            JOIN dish_ingredient di ON di.dish_id = d.id
            JOIN ingredient i ON i.id = di.ingredient_id
            WHERE d.id = ?
        """;

        try (Connection conn = new DBConnection().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();

            Dish dish = null;
            List<DishIngredient> dishIngredients = new ArrayList<>();

            while (rs.next()) {
                if (dish == null) {
                    dish = new Dish();
                    dish.setId(rs.getInt("dish_id"));
                    dish.setName(rs.getString("dish_name"));
                    dish.setPrice(rs.getObject("dish_price") == null
                            ? null
                            : rs.getDouble("dish_price"));
                    dish.setDishType(
                            DishTypeEnum.valueOf(rs.getString("dish_type"))
                    );
                }

                if (rs.getObject("ingredient_id") != null) {
                    Ingredient ingredient = new Ingredient();
                    ingredient.setId(rs.getInt("ingredient_id"));
                    ingredient.setName(rs.getString("ingredient_name"));
                    ingredient.setCategory(
                            CategoryEnum.valueOf(rs.getString("category"))
                    );
                    ingredient.setPrice(rs.getDouble("ingredient_price"));

                    DishIngredient di = new DishIngredient();
                    di.setDish(dish);
                    di.setIngredient(ingredient);
                    di.setQuantityRequired(
                            rs.getObject("quantity") == null
                                    ? null
                                    : rs.getDouble("quantity")
                    );

                    dishIngredients.add(di);
                }
            }

            if (dish == null) {
                throw new RuntimeException("Dish not found: " + id);
            }

            dish.setDishIngredients(dishIngredients);
            return dish;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public Dish saveDish(Dish dish) {
        String upsertDishSql = """
            INSERT INTO dish (id, name, price, dish_type)
            VALUES (?, ?, ?, ?::dish_type)
            ON CONFLICT (id) DO UPDATE
            SET name = EXCLUDED.name,
                price = EXCLUDED.price,
                dish_type = EXCLUDED.dish_type
            RETURNING id
        """;

        try (Connection conn = new DBConnection().getConnection()) {
            conn.setAutoCommit(false);

            Integer dishId;

            try (PreparedStatement ps = conn.prepareStatement(upsertDishSql)) {
                ps.setObject(1, dish.getId());
                ps.setString(2, dish.getName());
                ps.setObject(3, dish.getPrice());
                ps.setString(4, dish.getDishType().name());

                ResultSet rs = ps.executeQuery();
                rs.next();
                dishId = rs.getInt(1);
            }

            deleteDishIngredients(conn, dishId);
            insertDishIngredients(conn, dishId, dish.getDishIngredients());

            conn.commit();
            return findDishById(dishId);

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public List<Ingredient> createIngredients(List<Ingredient> ingredients) {
        if (ingredients == null || ingredients.isEmpty()) {
            return List.of();
        }

        String sql = """
            INSERT INTO ingredient (name, category, price)
            VALUES (?, ?::ingredient_category, ?)
            RETURNING id
        """;

        try (Connection conn = new DBConnection().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            conn.setAutoCommit(false);

            for (Ingredient ingredient : ingredients) {
                ps.setString(1, ingredient.getName());
                ps.setString(2, ingredient.getCategory().name());
                ps.setDouble(3, ingredient.getPrice());

                ResultSet rs = ps.executeQuery();
                rs.next();
                ingredient.setId(rs.getInt(1));
            }

            conn.commit();
            return ingredients;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void deleteDishIngredients(Connection conn, Integer dishId)
            throws SQLException {

        try (PreparedStatement ps = conn.prepareStatement(
                "DELETE FROM dish_ingredient WHERE dish_id = ?")) {
            ps.setInt(1, dishId);
            ps.executeUpdate();
        }
    }

    private void insertDishIngredients(
            Connection conn,
            Integer dishId,
            List<DishIngredient> ingredients
    ) throws SQLException {

        if (ingredients == null || ingredients.isEmpty()) {
            return;
        }

        String sql = """
            INSERT INTO dish_ingredient (dish_id, ingredient_id, quantity)
            VALUES (?, ?, ?)
        """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (DishIngredient di : ingredients) {
                ps.setInt(1, dishId);
                ps.setInt(2, di.getIngredient().getId());
                ps.setObject(3, di.getQuantityRequired());
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }
}
