package mg.haja;

import restaurant.*;

import java.sql.Connection;

public class Main {

    public static void main(String[] args) {

        DBConnection db = new DBConnection();
        Connection connection = null;

        try {
            connection = db.getConnection();
            DataRetriever retriever = new DataRetriever(connection);

            Dish dish = retriever.findDishById(1);

            System.out.println("Plat trouvé : " + dish.getName());
            System.out.println("Type : " + dish.getDishType());

            System.out.println("Ingrédients :");
            for (DishIngredient di : dish.getDishIngredients()) {
                System.out.println(
                        "- " + di.getIngredient().getName()
                                + " | quantité = " + di.getQuantityRequired()
                );
            }

            dish.setName("Salade fraîche");
            retriever.saveDish(dish);

            System.out.println("Plat mis à jour avec succès");

        } catch (RuntimeException e) {
            throw new RuntimeException(e);
        } finally {
            db.closeConnection(connection);
        }
    }
}
