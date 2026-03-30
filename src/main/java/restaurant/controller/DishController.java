package restaurant.controller;

import restaurant.model.Dish;
import restaurant.model.Ingredient;
import restaurant.repository.DishRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/dishes")
public class DishController {

    private final DishRepository dishRepository;

    public DishController(DishRepository dishRepository) {
        this.dishRepository = dishRepository;
    }

    @GetMapping
    public List<Dish> getAllDishes() {
        return dishRepository.findAll();
    }

    @PutMapping("/{id}/ingredients")
    public ResponseEntity<?> updateDishIngredients(
            @PathVariable Integer id,
            @RequestBody(required = false) List<Ingredient> ingredients) {

        if (ingredients == null) {
            return ResponseEntity.status(400)
                    .body("Request body with list of ingredients is required.");
        }

        Dish dish = dishRepository.findById(id);
        if (dish == null) {
            return ResponseEntity.status(404)
                    .body("Dish.id=" + id + " is not found");
        }

        Dish updated = dishRepository.updateIngredients(id, ingredients);
        return ResponseEntity.ok(updated);
    }
}