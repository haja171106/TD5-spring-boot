package restaurant.repository;

import restaurant.model.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public class DishRepository {

    private final JdbcTemplate jdbc;

    public DishRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<Dish> findAll() {
        List<Dish> dishes = jdbc.query(
                "SELECT id, name, selling_price, dish_type FROM dish",
                (rs, row) -> {
                    Dish dish = new Dish();
                    dish.setId(rs.getInt("id"));
                    dish.setName(rs.getString("name"));
                    dish.setPrice(rs.getDouble("selling_price"));
                    dish.setDishType(DishTypeEnum.valueOf(rs.getString("dish_type")));
                    return dish;
                }
        );
        dishes.forEach(d -> d.setDishIngredients(findIngredientsByDishId(d.getId())));
        return dishes;
    }

    public Dish findById(Integer id) {
        List<Dish> results = jdbc.query(
                "SELECT id, name, selling_price, dish_type FROM dish WHERE id = ?",
                (rs, row) -> {
                    Dish dish = new Dish();
                    dish.setId(rs.getInt("id"));
                    dish.setName(rs.getString("name"));
                    dish.setPrice(rs.getDouble("selling_price"));
                    dish.setDishType(DishTypeEnum.valueOf(rs.getString("dish_type")));
                    return dish;
                },
                id
        );
        if (results.isEmpty()) return null;
        Dish dish = results.get(0);
        dish.setDishIngredients(findIngredientsByDishId(id));
        return dish;
    }

    private List<DishIngredient> findIngredientsByDishId(Integer idDish) {
        return jdbc.query("""
            SELECT i.id, i.name, i.price, i.category, di.required_quantity, di.unit
            FROM ingredient i
            JOIN dish_ingredient di ON di.id_ingredient = i.id
            WHERE di.id_dish = ?
            """,
                (rs, row) -> {
                    Ingredient ingredient = new Ingredient();
                    ingredient.setId(rs.getInt("id"));
                    ingredient.setName(rs.getString("name"));
                    ingredient.setPrice(rs.getDouble("price"));
                    ingredient.setCategory(CategoryEnum.valueOf(rs.getString("category")));

                    DishIngredient di = new DishIngredient();
                    di.setIngredient(ingredient);
                    di.setQuantity(rs.getDouble("required_quantity"));
                    di.setUnit(Unit.valueOf(rs.getString("unit")));
                    return di;
                },
                idDish
        );
    }

    @Transactional
    public Dish updateIngredients(Integer dishId, List<Ingredient> requestIngredients) {
        List<Integer> validIds = requestIngredients.stream()
                .map(Ingredient::getId)
                .filter(rid -> !jdbc.query(
                        "SELECT id FROM ingredient WHERE id = ?",
                        (rs, r) -> rs.getInt("id"), rid).isEmpty()
                ).toList();

        jdbc.update("DELETE FROM dish_ingredient WHERE id_dish = ?", dishId);

        for (Integer ingId : validIds) {
            jdbc.update("""
                INSERT INTO dish_ingredient (id_ingredient, id_dish, required_quantity, unit)
                SELECT ?, ?, di_old.required_quantity, di_old.unit
                FROM dish_ingredient di_old
                WHERE di_old.id_ingredient = ? LIMIT 1
                ON CONFLICT DO NOTHING
                """,
                    ingId, dishId, ingId
            );
            jdbc.update("""
                INSERT INTO dish_ingredient (id_ingredient, id_dish, required_quantity, unit)
                VALUES (?, ?, 1, 'KG')
                ON CONFLICT DO NOTHING
                """,
                    ingId, dishId
            );
        }
        return findById(dishId);
    }
}