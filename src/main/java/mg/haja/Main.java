package mg.haja;

import restaurant.*;

import java.util.List;

public class Main {
    public static void main(String[] args) {

        DBConnection db = null;

        try {
            db = new DBConnection();

            DataRetriever retriever = new DataRetriever(db.getConnection());

            Dish dish = retriever.findDishById(1);

            if (dish != null) {
                System.out.println("Plat trouvé : " + dish.getName());
            } else {
                System.out.println("Aucun plat trouvé.");
            }

            int page = 1;
            int size = 5;

            List<Ingredient> ingredients = retriever.findIngredients(page, size);

            System.out.println("\nIngrédients (page " + page + ") :");
            for (Ingredient ingredient : ingredients) {
                System.out.println("- " + ingredient.getName() + " | prix = " + ingredient.getPrice());
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            if (db != null) {
                db.close();
            }
        }
    }
}
