package mg.haja;

import restaurant.*;

import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) {

        DBConnection db = null;

        try {
            db = new DBConnection();
            DataRetriever retriever = new DataRetriever(db.getConnection());

            // ===== 1️⃣ findDishById =====
            Dish dish = retriever.findDishById(1);
            if (dish != null) {
                System.out.println("Plat trouvé : " + dish.getName());
            } else {
                System.out.println("Aucun plat trouvé.");
            }

            // ===== 2️⃣ findIngredients (pagination simple) =====
            int page = 1;
            int size = 5;

            List<Ingredient> ingredients = retriever.findIngredients(page, size);
            System.out.println("\nIngrédients (page " + page + ") :");
            for (Ingredient ingredient : ingredients) {
                System.out.println("- " + ingredient.getName()
                        + " | prix = " + ingredient.getPrice());
            }

            // ===== 3️⃣ createIngredients =====
            List<Ingredient> newIngredients = new ArrayList<>();
            // Exemple (décommente si besoin)
            // newIngredients.add(new Ingredient(0, "Farine", 2400.0, dish));

            List<Ingredient> savedIngredients =
                    retriever.createIngredients(newIngredients);

            System.out.println("\nIngrédients créés avec succès :");
            for (Ingredient ing : savedIngredients) {
                System.out.println(ing.getName() + " | prix = " + ing.getPrice());
            }

            // ===== 4️⃣ saveDish =====
            dish.setIngredients(savedIngredients);
            Dish savedDish = retriever.saveDish(dish);

            System.out.println("\nPlat sauvegardé avec succès :");
            System.out.println("ID : " + savedDish.getId());
            System.out.println("Nom : " + savedDish.getName());

            // ===== 5️⃣ findDishsByIngredientName =====
            System.out.println("\nPlats contenant 'Fromage' :");
            List<Dish> dishes = retriever.findDishsByIngredientName("Fromage");

            for (Dish d : dishes) {
                System.out.println("Plat : " + d.getName());
                for (Ingredient i : d.getIngredients()) {
                    System.out.println("  - " + i.getName());
                }
            }

            // ===== 6️⃣ findIngredientsByCriteria (NOUVELLE MÉTHODE) =====
            System.out.println("\nIngrédients filtrés par critères :");

            List<Ingredient> filteredIngredients =
                    retriever.findIngredientsByCriteria(
                            "Tomate",
                            CategoryEnum.VEGETABLE,
                            "Salade",
                            1,
                            5
                    );

            for (Ingredient i : filteredIngredients) {
                System.out.println(
                        "- " + i.getName()
                                + " | prix = " + i.getPrice()
                                + " | plat = " + (i.getDish() != null ? i.getDish().getName() : "Aucun")
                );
            }

        } catch (RuntimeException e) {
            System.out.println("Erreur : " + e.getMessage());
        } finally {
            if (db != null) {
                db.close();
            }
        }
    }
}
