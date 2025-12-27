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
}
