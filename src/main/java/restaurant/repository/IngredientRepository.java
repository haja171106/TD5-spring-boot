package restaurant.repository;

import restaurant.model.*;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Repository
public class IngredientRepository {

    private final DataSource dataSource;

    public IngredientRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public List<Ingredient> findAll() {
        List<Ingredient> ingredients = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT id, name, price, category FROM ingredient")) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                ingredients.add(mapIngredient(rs));
            }
            return ingredients;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public Ingredient findById(Integer id) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT id, name, price, category FROM ingredient WHERE id = ?")) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return mapIngredient(rs);
            }
            return null;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public StockValue getStockValueAt(int ingredientId, Instant at, Unit unit) {
        String sql = """
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
              AND unit = ?::unit
            """;
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, ingredientId);
            ps.setTimestamp(2, Timestamp.from(at));
            ps.setString(3, unit.name());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                StockValue sv = new StockValue();
                sv.setQuantity(rs.getDouble("stock_quantity"));
                sv.setUnit(unit);
                return sv;
            }
            StockValue zero = new StockValue();
            zero.setQuantity(0.0);
            zero.setUnit(unit);
            return zero;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private Ingredient mapIngredient(ResultSet rs) throws SQLException {
        return new Ingredient(
                rs.getInt("id"),
                rs.getString("name"),
                CategoryEnum.valueOf(rs.getString("category")),
                rs.getDouble("price"),
                List.of()
        );
    }
}