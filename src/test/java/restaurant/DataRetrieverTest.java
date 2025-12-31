package restaurant;

import org.junit.jupiter.api.*;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class DataRetrieverTest {

    private static DBConnection db;
    private static DataRetriever retriever;

    @BeforeAll
    public static void setup() {
        db = new DBConnection();  // Assure-toi que la DB est prête et que dotenv fonctionne
        retriever = new DataRetriever(db.getConnection());
    }

    @AfterAll
    public static void teardown() {
        db.close();
    }

    @Test
    @Order(1)
    public void testFindDishById_existing() {
        Dish dish = retriever.findDishById(1);
        assertNotNull(dish, "Dish should not be null");
        assertEquals("Salade fraîche", dish.getName());
        assertEquals(2, dish.getIngredients().size()); // Selon ton jeu de données
    }

    @Test
    @Order(2)
    public void testFindDishById_nonExisting() {
        Dish dish = retriever.findDishById(999);
        assertNull(dish, "Dish should be null for non-existing ID");
    }

    @Test
    @Order(3)
    public void testFindIngredients_pagination_page2_size2() {
        List<Ingredient> ingredients = retriever.findIngredients(2, 2);
        assertEquals(2, ingredients.size());
        assertEquals("Poulet", ingredients.get(0).getName());
        assertEquals("Chocolat", ingredients.get(1).getName());
    }

    @Test
    @Order(4)
    public void testFindIngredients_pagination_page3_size5() {
        List<Ingredient> ingredients = retriever.findIngredients(3, 5);
        assertTrue(ingredients.isEmpty(), "Page 3 should return empty list");
    }

    @Test
    @Order(5)
    public void testFindDishsByIngredientName() {
        List<Dish> dishes = retriever.findDishsByIngredientName("eur");
        assertEquals(1, dishes.size());
        assertEquals("Gâteau au chocolat", dishes.get(0).getName());
    }

    @Test
    @Order(6)
    public void testFindIngredientsByCriteria_vegetables() {
        List<Ingredient> ingredients = retriever.findIngredientsByCriteria(
                null,
                CategoryEnum.VEGETABLE,
                null,
                1,
                10
        );
        assertTrue(ingredients.stream().allMatch(i -> i.getDish() != null));
        assertTrue(ingredients.stream().allMatch(i -> i.getName().equals("Laitue") || i.getName().equals("Tomate")));
    }

    @Test
    @Order(7)
    public void testFindIngredientsByCriteria_noMatch() {
        List<Ingredient> ingredients = retriever.findIngredientsByCriteria(
                "cho",
                null,
                "Sal",
                1,
                10
        );
        assertTrue(ingredients.isEmpty());
    }

    @Test
    @Order(8)
    public void testCreateIngredients_success() {
        Dish dish = retriever.findDishById(1);
        List<Ingredient> newIngredients = List.of(
                new Ingredient(0, "Fromage", 1200.0, dish),
                new Ingredient(0, "Oignon", 500.0, dish)
        );
        List<Ingredient> savedIngredients = retriever.createIngredients(newIngredients);
        assertEquals(2, savedIngredients.size());
    }

    @Test
    @Order(9)
    public void testCreateIngredients_duplicateFails() {
        Dish dish = retriever.findDishById(1);
        List<Ingredient> newIngredients = List.of(
                new Ingredient(0, "Carotte", 2000.0, dish),
                new Ingredient(0, "Laitue", 2000.0, dish) // déjà existante
        );
        assertThrows(RuntimeException.class, () -> retriever.createIngredients(newIngredients));
    }

    @Test
    @Order(10)
    public void testSaveDish_newDish() {
        Dish dish = new Dish(0, "Soupe de légumes", DishTypeEnum.START, List.of(
                new Ingredient(0, "Oignon", 500.0, null)
        ));
        Dish savedDish = retriever.saveDish(dish);
        assertTrue(savedDish.getId() > 0);
        assertEquals(1, savedDish.getIngredients().size());
    }

    @Test
    @Order(11)
    public void testSaveDish_updateDish() {
        Dish dish = retriever.findDishById(1);
        dish.setName("Salade de fromage");
        dish.setIngredients(List.of(new Ingredient(0, "Fromage", 1200.0, dish)));
        Dish updatedDish = retriever.saveDish(dish);
        assertEquals("Salade de fromage", updatedDish.getName());
        assertEquals(1, updatedDish.getIngredients().size());
    }
}
