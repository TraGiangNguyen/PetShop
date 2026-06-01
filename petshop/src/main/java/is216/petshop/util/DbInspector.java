package is216.petshop.util;

import is216.petshop.dao.StockDAO;
import java.util.ArrayList;
import java.util.List;

public class DbInspector {
    public static void main(String[] args) {
        StockDAO dao = new StockDAO();
        dao.ensureTablesExist();
        
        List<StockDAO.RestockItem> items = new ArrayList<>();
        // Let's add a test item. We need a valid product ID.
        // Let's get the products first.
        var products = dao.getAllProducts();
        if (products.isEmpty()) {
            System.out.println("[ERROR] No products found to test restock.");
            return;
        }
        
        var firstProduct = products.get(0);
        System.out.println("Testing restock for product: " + firstProduct.getName() + " (ID: " + firstProduct.getId() + ")");
        
        items.add(new StockDAO.RestockItem(firstProduct.getId(), firstProduct.getName(), 5, 20000.0));
        
        boolean ok = dao.createRestockOrder(1, 100000, items);
        if (ok) {
            System.out.println("[SUCCESS] Restock order created successfully!");
        } else {
            System.out.println("[ERROR] Failed to create restock order.");
        }
    }
}
