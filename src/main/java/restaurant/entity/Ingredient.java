package restaurant.entity;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import static java.time.Instant.now;

public class Ingredient {
    private Integer id;
    private String name;
    private CategoryEnum category;
    private Double price;
    private List<StockMovement> stockMovementList;

    public Ingredient() {
    }

    public Ingredient(Integer id, String name, CategoryEnum category, Double price, List<StockMovement> stockMovementList) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.price = price;
        this.stockMovementList = stockMovementList;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public CategoryEnum getCategory() {
        return category;
    }

    public void setCategory(CategoryEnum category) {
        this.category = category;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Ingredient that = (Ingredient) o;
        return Objects.equals(id, that.id) && Objects.equals(name, that.name) && category == that.category && Objects.equals(price, that.price);
    }

    public List<StockMovement> getStockMovementList() {
        return stockMovementList;
    }

    public void setStockMovementList(List<StockMovement> stockMovementList) {
        this.stockMovementList = stockMovementList;
    }

    public StockValue getStockValueAt(Instant t) {
        if (stockMovementList == null || stockMovementList.isEmpty()) {
            StockValue sv = new StockValue();
            sv.setQuantity(0.0);
            sv.setUnit(Unit.KG);
            return sv;
        }
        Map<Unit, List<StockMovement>> unitSet = stockMovementList.stream()
                .collect(Collectors.groupingBy(m -> m.getValue().getUnit()));

        if (unitSet.size() > 1) {
            throw new RuntimeException("Multiple units not handled");
        }

        List<StockMovement> filtered = stockMovementList.stream()
                .filter(m -> !m.getCreationDatetime().isAfter(t))
                .toList();

        double in = filtered.stream()
                .filter(m -> m.getType() == MovementTypeEnum.IN)
                .mapToDouble(m -> m.getValue().getQuantity())
                .sum();

        double out = filtered.stream()
                .filter(m -> m.getType() == MovementTypeEnum.OUT)
                .mapToDouble(m -> m.getValue().getQuantity())
                .sum();

        StockValue sv = new StockValue();
        sv.setQuantity(in - out);
        sv.setUnit(unitSet.keySet().iterator().next());

        return sv;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, category, price);
    }

    @Override
    public String toString() {
        return "Ingredient{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", category=" + category +
                ", price=" + price +
                ", actualStock=" + getStockValueAt(now()) +
                '}';
    }
}