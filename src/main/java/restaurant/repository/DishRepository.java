package restaurant.repository;

import restaurant.entity.*;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Repository
public class DishRepository {

    private final DataSource dataSource;

    public DishRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public List<Dish> findAll() {
        List<Dish> dishes = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT id, name, selling_price, dish_type FROM dish")) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Dish dish = mapDish(rs);
                dish.setDishIngredients(findIngredientsByDishId(conn, dish.getId()));
                dishes.add(dish);
            }
            return dishes;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public Dish findById(Integer id) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT id, name, selling_price, dish_type FROM dish WHERE id = ?")) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Dish dish = mapDish(rs);
                dish.setDishIngredients(findIngredientsByDishId(conn, id));
                return dish;
            }
            return null;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public Dish updateIngredients(Integer dishId, List<Ingredient> requestIngredients) {
        try (Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(false);

            try (PreparedStatement ps = conn.prepareStatement(
                    "DELETE FROM dish_ingredient WHERE id_dish = ?")) {
                ps.setInt(1, dishId);
                ps.executeUpdate();
            }

            for (Ingredient requested : requestIngredients) {
                if (requested.getId() == null) continue;

                boolean exists = false;
                try (PreparedStatement check = conn.prepareStatement(
                        "SELECT id FROM ingredient WHERE id = ?")) {
                    check.setInt(1, requested.getId());
                    ResultSet rs = check.executeQuery();
                    exists = rs.next();
                }

                if (exists) {
                    try (PreparedStatement insert = conn.prepareStatement("""
                            INSERT INTO dish_ingredient (id_ingredient, id_dish, required_quantity, unit)
                            VALUES (?, ?, 1, 'KG')
                            ON CONFLICT DO NOTHING
                            """)) {
                        insert.setInt(1, requested.getId());
                        insert.setInt(2, dishId);
                        insert.executeUpdate();
                    }
                }

            }

            conn.commit();
            return findById(dishId);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private List<DishIngredient> findIngredientsByDishId(Connection conn, Integer idDish)
            throws SQLException {
        List<DishIngredient> list = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement("""
                SELECT i.id, i.name, i.price, i.category, di.required_quantity, di.unit
                FROM ingredient i
                JOIN dish_ingredient di ON di.id_ingredient = i.id
                WHERE di.id_dish = ?
                """)) {
            ps.setInt(1, idDish);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Ingredient ingredient = new Ingredient();
                ingredient.setId(rs.getInt("id"));
                ingredient.setName(rs.getString("name"));
                ingredient.setPrice(rs.getDouble("price"));
                ingredient.setCategory(CategoryEnum.valueOf(rs.getString("category")));

                DishIngredient di = new DishIngredient();
                di.setIngredient(ingredient);
                di.setQuantity(rs.getDouble("required_quantity"));
                di.setUnit(Unit.valueOf(rs.getString("unit")));
                list.add(di);
            }
        }
        return list;
    }

    private Dish mapDish(ResultSet rs) throws SQLException {
        Dish dish = new Dish();
        dish.setId(rs.getInt("id"));
        dish.setName(rs.getString("name"));
        dish.setPrice(rs.getDouble("selling_price"));
        dish.setDishType(DishTypeEnum.valueOf(rs.getString("dish_type")));
        return dish;
    }

    public List<DishIngredient> findIngredientsByDishIdWithFilters(
            Integer dishId,
            String ingredientName,
            Double ingredientPriceAround) {

        StringBuilder sql = new StringBuilder("""
            SELECT i.id, i.name, i.price, i.category, di.required_quantity, di.unit
            FROM ingredient i
            JOIN dish_ingredient di ON di.id_ingredient = i.id
            WHERE di.id_dish = ?
            """);

        List<Object> params = new ArrayList<>();
        params.add(dishId);

        if (ingredientName != null) {
            sql.append(" AND i.name ILIKE ?");
            params.add("%" + ingredientName + "%");
        }

        if (ingredientPriceAround != null) {
            sql.append(" AND i.price BETWEEN ? AND ?");
            params.add(ingredientPriceAround - 50);
            params.add(ingredientPriceAround + 50);
        }

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }

            List<DishIngredient> list = new ArrayList<>();
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Ingredient ingredient = new Ingredient();
                ingredient.setId(rs.getInt("id"));
                ingredient.setName(rs.getString("name"));
                ingredient.setPrice(rs.getDouble("price"));
                ingredient.setCategory(CategoryEnum.valueOf(rs.getString("category")));

                DishIngredient di = new DishIngredient();
                di.setIngredient(ingredient);
                di.setQuantity(rs.getDouble("required_quantity"));
                di.setUnit(Unit.valueOf(rs.getString("unit")));
                list.add(di);
            }
            return list;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}