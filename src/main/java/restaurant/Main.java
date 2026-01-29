package restaurant;

import java.util.List;

public class Main {
    public static void main(String[] args) {
        DataRetriever dataRetriever = new DataRetriever();
        Dish saladeVerte = dataRetriever.findDishById(1);
        System.out.println(saladeVerte);

        Dish poulet = dataRetriever.findDishById(2);
        System.out.println(poulet);

        Dish rizLegume = dataRetriever.findDishById(3);
        rizLegume.setPrice(100.0);
        Dish newRizLegume = dataRetriever.saveDish(rizLegume);
        System.out.println(newRizLegume); // Should not throw exception


//        Dish rizLegumeAgain = dataRetriever.findDishById(3);
//        rizLegumeAgain.setPrice(null);
//        Dish savedNewRizLegume = dataRetriever.saveDish(rizLegume);
//        System.out.println(savedNewRizLegume); // Should throw exception

        DishOrder saladeOrder = new DishOrder();
        saladeOrder.setDish(saladeVerte);
        saladeOrder.setQuantity(2);

        DishOrder pouletOrder = new DishOrder();
        pouletOrder.setDish(poulet);
        pouletOrder.setQuantity(1);

        Order order = new Order();
        order.setDishOrderList(List.of(saladeOrder, pouletOrder));

        Order savedOrder = dataRetriever.saveOrder(order);

        System.out.println("Commande sauvegardée avec succès");
        System.out.println("Référence : " + savedOrder.getReference());
        System.out.println("Total HT : " + savedOrder.getTotalAmountHT());
        System.out.println("Total TTC : " + savedOrder.getTotalAmountTTC());
    }
}
