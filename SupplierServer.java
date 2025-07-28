import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;

/**
 * Supplier server implementation for product ordering
 */
public class SupplierServer extends UnicastRemoteObject implements SupplierInterface {
    private static final long serialVersionUID = 1L;
    
    private Map<String, Product> catalog;
    private List<Order> orders;
    
    public SupplierServer() throws RemoteException {
        super();
        this.catalog = new HashMap<>();
        this.orders = new ArrayList<>();
        initializeCatalog();
    }
    
    /**
     * Initialize product catalog
     */
    private void initializeCatalog() {
        catalog.put("P001", new Product("P001", "Apples", 0.30, 1000, 0));
        catalog.put("P002", new Product("P002", "Bread", 0.80, 500, 0));
        catalog.put("P003", new Product("P003", "Milk", 0.60, 750, 0));
        catalog.put("P004", new Product("P004", "Cheese", 2.50, 300, 0));
        catalog.put("P005", new Product("P005", "Soap", 1.50, 400, 0));
    }
    
    @Override
    public boolean placeOrder(String productId, int quantity) throws RemoteException {
        Product product = catalog.get(productId);
        
        if (product != null && product.getStockLevel() >= quantity) {
            Order order = new Order(UUID.randomUUID().toString(), productId, quantity);
            orders.add(order);
            
            // Update supplier stock
            product.setStockLevel(product.getStockLevel() - quantity);
            
            System.out.println("Order received: " + quantity + " units of " + product.getName());
            return true;
        }
        
        return false;
    }
    
    @Override
    public List<Product> getProductCatalog() throws RemoteException {
        return new ArrayList<>(catalog.values());
    }
    
    @Override
    public boolean checkAvailability(String productId, int quantity) throws RemoteException {
        Product product = catalog.get(productId);
        return product != null && product.getStockLevel() >= quantity;
    }
    
    public static void main(String[] args) {
        try {
            SupplierServer server = new SupplierServer();
            Registry registry = LocateRegistry.getRegistry(1099);
            registry.rebind("SupplierService", server);
            
            System.out.println("Supplier Server is running");
            System.out.println("Ready to process orders...");
            
        } catch (Exception e) {
            System.err.println("Supplier Server error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}