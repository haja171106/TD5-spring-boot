package mg.haja;

import restaurant.*;

import java.sql.SQLException;

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

        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            if (db != null) {
                db.close();
            }
        }
    }
}
