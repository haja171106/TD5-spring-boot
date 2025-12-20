package mg.haja;

import restaurant.DBConnection;
import java.sql.SQLException;

public class Main {
    public static void main(String[] args) {
        String user = "mini_dish_db_manager";
        String password = "123456";
        String url = "jdbc:postgresql://localhost:5432/mini_dish_db";

        try {
            DBConnection db = new DBConnection(url,user, password);
            System.out.println("Connecté à la base de données");
            db.close();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}