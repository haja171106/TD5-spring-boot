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

            Dish dish = retriever.findDishById(1);

            if (dish == null) {
                System.out.println("Aucun plat trouvé.");
                return;
            }

            System.out.println("Plat trouvé : " + dish.getName());
            System.out.println("Ingrédients du plat :");
            for (Ingredient i : dish.getIngredients()) {
                System.out.println(" - " + i.getName());
            }
            int page = 1;
            int size = 5;

            List<Ingredient> ingredients = retriever.findIngredients(page, size);

            System.out.println("\nIngrédients (page " + page + ") :");
            for (Ingredient ingredient : ingredients) {
                System.out.println(
                        "- " + ingredient.getName() +
                                " | prix = " + ingredient.getPrice()
                );
            }
            List<Ingredient> newIngredients = new ArrayList<>();
   //         newIngredients.add(new Ingredient(0, "Oignon", 800.0, dish));

            List<Ingredient> savedIngredients =
                    retriever.createIngredients(newIngredients);

            System.out.println("\nIngrédients créés avec succès :");
            for (Ingredient ing : savedIngredients) {
                System.out.println(
                        "- " + ing.getName() +
                                " | prix = " + ing.getPrice()
                );
            }
            dish.setIngredients(savedIngredients);
            Dish savedDish = retriever.saveDish(dish);

            System.out.println("\nPlat sauvegardé avec succès :");
            System.out.println("ID : " + savedDish.getId());
            System.out.println("Nom : " + savedDish.getName());

            List<Dish> dishes =
                    retriever.findDishsByIngredientName("Tomate");

            System.out.println("\nPlats contenant 'tomate' :");
            for (Dish d : dishes) {
                System.out.println("Plat : " + d.getName());
                for (Ingredient i : d.getIngredients()) {
                    System.out.println("  - " + i.getName());
                }
            }

        } catch (RuntimeException e) {
            System.out.println("❌ Erreur : " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (db != null) {
                db.close();
            }
        }
    }
}
