package restaurant.repository;

import restaurant.model.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import restaurant.model.StockValue;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;

@Repository
public class IngredientRepository {

    private final JdbcTemplate jdbc;

    public IngredientRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<Ingredient> findAll() {
        return jdbc.query(
                "SELECT id, name, price, category FROM ingredient",
                (rs, row) -> {
                    Ingredient ingredient = new Ingredient();
                    ingredient.setId(rs.getInt("id"));
                    ingredient.setName(rs.getString("name"));
                    ingredient.setPrice(rs.getDouble("price"));

                    String category = rs.getString("category");
                    if (category != null) {
                        ingredient.setCategory(CategoryEnum.valueOf(category));
                    }

                    ingredient.setStockMovementList(List.of());
                    return ingredient;
                }
        );
    }

    public Ingredient findById(Integer id) {
        List<Ingredient> results = jdbc.query(
                "SELECT id, name, price, category FROM ingredient WHERE id = ?",
                (rs, row) -> new Ingredient(
                        rs.getInt("id"),
                        rs.getString("name"),
                        CategoryEnum.valueOf(rs.getString("category")),
                        rs.getDouble("price"),
                        List.of()
                ),
                id
        );
        if (results.isEmpty()) return null;
        return results.get(0);
    }

    public StockValue getStockValueAt(int ingredientId, Instant at, Unit unit) {
        List<StockValue> results = jdbc.query("""
        SELECT
            COALESCE(SUM(
                CASE
                    WHEN type = 'IN'  THEN quantity
                    WHEN type = 'OUT' THEN -quantity
                    ELSE 0
                END
            ), 0) AS stock_quantity
        FROM stock_movement
        WHERE id_ingredient = ?
          AND creation_datetime <= ?
          AND unit = CAST(? AS unit)
        """,
                (rs, row) -> {
                    StockValue sv = new StockValue();
                    sv.setQuantity(rs.getDouble("stock_quantity"));
                    sv.setUnit(unit);
                    return sv;
                },
                ingredientId,
                Timestamp.from(at),
                unit.name()
        );
        if (results.isEmpty()) {
            StockValue zero = new StockValue();
            zero.setQuantity(0.0);
            zero.setUnit(unit);
            return zero;
        }
        return results.get(0);
    }
}