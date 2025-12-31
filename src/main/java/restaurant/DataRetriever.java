package restaurant;

import java.sql.*;
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
            SELECT d.id, d.name, d.dish_type,
                   i.id AS ingredient_id, i.name AS ingredient_name, i.price
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
                            rs.getInt("id"),
                            rs.getString("name"),
                            DishTypeEnum.valueOf(rs.getString("dish_type")),
                            ingredients
                    );
                }

                if (rs.getInt("ingredient_id") != 0) {
                    ingredients.add(new Ingredient(
                            rs.getInt("ingredient_id"),
                            rs.getString("ingredient_name"),
                            rs.getDouble("price"),
                            dish
                    ));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return dish;
    }
    public List<Ingredient> findIngredients(int page, int size) {

        List<Ingredient> ingredients = new ArrayList<>();
        int offset = (page - 1) * size;

        String sql = """
            SELECT id, name, price
            FROM ingredient
            ORDER BY id
            LIMIT ? OFFSET ?
        """;

        try (PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setInt(1, size);
            ps.setInt(2, offset);

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                ingredients.add(new Ingredient(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getDouble("price"),
                        null
                ));
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return ingredients;
    }
    public List<Ingredient> createIngredients(List<Ingredient> newIngredients) {

        String checkSql = "SELECT COUNT(*) FROM ingredient WHERE name = ?";
        String insertSql = """
            INSERT INTO ingredient(name, price, id_dish)
            VALUES (?, ?, ?)
            RETURNING id
        """;

        List<Ingredient> saved = new ArrayList<>();

        try {
            connection.setAutoCommit(false);

            try (
                    PreparedStatement check = connection.prepareStatement(checkSql);
                    PreparedStatement insert = connection.prepareStatement(insertSql)
            ) {
                for (Ingredient ing : newIngredients) {

                    check.setString(1, ing.getName());
                    ResultSet rs = check.executeQuery();
                    rs.next();

                    if (rs.getInt(1) > 0) {
                        throw new RuntimeException("Ingrédient déjà existant : " + ing.getName());
                    }

                    insert.setString(1, ing.getName());
                    insert.setDouble(2, ing.getPrice());
                    insert.setInt(3, ing.getDish().getId());

                    ResultSet inserted = insert.executeQuery();
                    inserted.next();
                    ing.setId(inserted.getInt(1));

                    saved.add(ing);
                }

                connection.commit();
                return saved;
            }

        } catch (Exception e) {
            try { connection.rollback(); } catch (SQLException ignored) {}
            throw new RuntimeException(e);
        } finally {
            try { connection.setAutoCommit(true); } catch (SQLException ignored) {}
        }
    }
    public Dish saveDish(Dish dish) {

        String insertDishSql =
                "INSERT INTO dish(name, dish_type) VALUES (?, ?::dish_type) RETURNING id";

        String updateDishSql =
                "UPDATE dish SET name = ?, dish_type = ?::dish_type WHERE id = ?";

        String clearIngredients =
                "UPDATE ingredient SET id_dish = NULL WHERE id_dish = ?";

        String bindIngredient =
                "UPDATE ingredient SET id_dish = ? WHERE id = ?";

        try {
            connection.setAutoCommit(false);

            if (dish.getId() == 0) {
                try (PreparedStatement ps = connection.prepareStatement(insertDishSql)) {
                    ps.setString(1, dish.getName());
                    ps.setString(2, dish.getDishType().name());
                    ResultSet rs = ps.executeQuery();
                    rs.next();
                    dish.setId(rs.getInt(1));
                }
            } else {
                try (PreparedStatement ps = connection.prepareStatement(updateDishSql)) {
                    ps.setString(1, dish.getName());
                    ps.setString(2, dish.getDishType().name());
                    ps.setInt(3, dish.getId());
                    ps.executeUpdate();
                }
            }

            try (PreparedStatement ps = connection.prepareStatement(clearIngredients)) {
                ps.setInt(1, dish.getId());
                ps.executeUpdate();
            }

            try (PreparedStatement ps = connection.prepareStatement(bindIngredient)) {
                for (Ingredient i : dish.getIngredients()) {
                    ps.setInt(1, dish.getId());
                    ps.setInt(2, i.getId());
                    ps.executeUpdate();
                }
            }

            connection.commit();
            return dish;

        } catch (Exception e) {
            try { connection.rollback(); } catch (SQLException ignored) {}
            throw new RuntimeException(e);
        } finally {
            try { connection.setAutoCommit(true); } catch (SQLException ignored) {}
        }
    }
    public List<Dish> findDishsByIngredientName(String ingredientName) {

        List<Dish> dishes = new ArrayList<>();

        String sql = """
            SELECT d.id, d.name, d.dish_type,
                   i.id AS ingredient_id, i.name AS ingredient_name, i.price
            FROM dish d
            JOIN ingredient i ON d.id = i.id_dish
            WHERE i.name ILIKE ?
        """;

        try (PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setString(1, "%" + ingredientName + "%");
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {

                Dish dish = null;
                for (Dish d : dishes) {
                    if (d.getId() == rs.getInt("id")) {
                        dish = d;
                        break;
                    }
                }

                if (dish == null) {
                    dish = new Dish(
                            rs.getInt("id"),
                            rs.getString("name"),
                            DishTypeEnum.valueOf(rs.getString("dish_type")),
                            new ArrayList<>()
                    );
                    dishes.add(dish);
                }

                dish.getIngredients().add(
                        new Ingredient(
                                rs.getInt("ingredient_id"),
                                rs.getString("ingredient_name"),
                                rs.getDouble("price"),
                                dish
                        )
                );
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return dishes;
    }
}
