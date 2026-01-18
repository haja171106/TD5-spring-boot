package restaurant;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Dish {
    private int id;
    private String name;
    private DishTypeEnum dishType;
    private Double price;
    private List<DishIngredient> dishIngredients = new ArrayList<>();

    public Dish() {}

    public Double getDishCost() {
        if (dishIngredients == null || dishIngredients.isEmpty()) {
            return 0.0;
        }

        double total = 0.0;

        for (DishIngredient di : dishIngredients) {
            if (di.getIngredient() == null) {
                continue;
            }

            Double ingredientPrice = di.getIngredient().getPrice();
            Double quantity = di.getQuantityRequired();

            if (ingredientPrice == null || quantity == null) {
                continue;
            }

            total += ingredientPrice * quantity;
        }

        return total;
    }

    public Double getGrossMargin() {
        if (price == null) {
            throw new IllegalStateException("Dish price is NULL, cannot calculate gross margin");
        }
        return price - getDishCost();
    }


    public Dish(int id, String name, DishTypeEnum dishType, Double price, List<DishIngredient> dishIngredients) {
        this.id = id;
        this.name = name;
        this.dishType = dishType;
        this.price = price;
        this.dishIngredients = dishIngredients;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public DishTypeEnum getDishType() {
        return dishType;
    }

    public void setDishType(DishTypeEnum dishType) {
        this.dishType = dishType;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public List<DishIngredient> getDishIngredients() {
        return dishIngredients;
    }

    public void setDishIngredients(List<DishIngredient> dishIngredients) {
        this.dishIngredients = dishIngredients;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Dish dish = (Dish) o;
        return id == dish.id && Objects.equals(name, dish.name) && dishType == dish.dishType && Objects.equals(price, dish.price) && Objects.equals(dishIngredients, dish.dishIngredients);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, dishType, price, dishIngredients);
    }

    @Override
    public String toString() {
        return "Dish{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", dishType=" + dishType +
                ", price=" + price +
                ", dishIngredients=" + dishIngredients +
                '}';
    }
}
