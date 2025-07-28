import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Main supermarket server implementation
 */
public class SupermarketServer extends UnicastRemoteObject implements SupermarketInterface {
    private static final long serialVersionUID = 1L;
    
    // Data storage
    private Map<String, Product> products;
    private Map<String, Transaction> transactions;
    private Map<String, Customer> customers;
    
    // Remote service references
    private BankInterface bankService;
    private SupplierInterface supplierService;
    
    // Constructor
    public SupermarketServer() throws RemoteException {
        super();
        this.products = new ConcurrentHashMap<>();
        this.transactions = new ConcurrentHashMap<>();
        this.customers = new ConcurrentHashMap<>();
        
        initializeProducts();
        connectToExternalServices();
    }
    
    /**
     * Initialize sample products
     */
    private void initializeProducts() {
        products.put("P001", new Product("P001", "Apples", 0.50, 100, 20));
        products.put("P002", new Product("P002", "Bread", 1.20, 50, 10));
        products.put("P003", new Product("P003", "Milk", 0.90, 75, 15));
        products.put("P004", new Product("P004", "Cheese", 3.50, 30, 5));
        products.put("P005", new Product("P005", "Soap", 2.00, 40, 10));
        
        // Set special offers
        products.get("P001").setSpecialOffer("3-for-2");
        products.get("P005").setSpecialOffer("extra-100-points");
        products.get("P005").setLoyaltyPoints(100);
    }
    
    /**
     * Connect to external RMI services
     */
    private void connectToExternalServices() {
        try {
            Registry registry = LocateRegistry.getRegistry("localhost", 1099);
            bankService = (BankInterface) registry.lookup("BankService");
            supplierService = (SupplierInterface) registry.lookup("SupplierService");
            System.out.println("Connected to external services");
        } catch (Exception e) {
            System.err.println("Error connecting to external services: " + e.getMessage());
        }
    }
    
    @Override
    public List<Product> getAvailableProducts() throws RemoteException {
        return new ArrayList<>(products.values());
    }
    
    @Override
    public boolean updateStock(String productId, int quantity) throws RemoteException {
        Product product = products.get(productId);
        if (product != null) {
            synchronized (product) {
                int newStock = product.getStockLevel() - quantity;
                if (newStock >= 0) {
                    product.setStockLevel(newStock);
                    
                    // Check if reorder is needed
                    if (newStock <= product.getReorderThreshold()) {
                        reorderProduct(productId);
                    }
                    return true;
                }
            }
        }
        return false;
    }
    
    /**
     * Automatic reorder when stock is low
     */
    private void reorderProduct(String productId) {
        try {
            if (supplierService != null) {
                int reorderQuantity = 50; // Default reorder quantity
                boolean ordered = supplierService.placeOrder(productId, reorderQuantity);
                if (ordered) {
                    System.out.println("Reorder placed for product: " + productId);
                }
            }
        } catch (RemoteException e) {
            System.err.println("Error placing reorder: " + e.getMessage());
        }
    }
    
    @Override
    public String createTransaction(String customerId) throws RemoteException {
        String transactionId = UUID.randomUUID().toString();
        Transaction transaction = new Transaction(transactionId, customerId);
        transactions.put(transactionId, transaction);
        return transactionId;
    }
    
    @Override
    public boolean addToBasket(String transactionId, String productId, int quantity) 
            throws RemoteException {
        Transaction transaction = transactions.get(transactionId);
        Product product = products.get(productId);
        
        if (transaction != null && product != null) {
            if (product.getStockLevel() >= quantity) {
                transaction.addItem(new TransactionItem(product, quantity));
                return true;
            }
        }
        return false;
    }
    
    @Override
    public double calculateTotal(String transactionId) throws RemoteException {
        Transaction transaction = transactions.get(transactionId);
        if (transaction != null) {
            return transaction.calculateTotal();
        }
        return 0.0;
    }
    
    @Override
    public boolean processPayment(String transactionId, PaymentDetails payment) 
            throws RemoteException {
        Transaction transaction = transactions.get(transactionId);
        if (transaction == null) {
            return false;
        }
        
        double totalAmount = transaction.calculateTotal();
        
        // Apply loyalty points if requested
        if (payment.isUseLoyaltyPoints()) {
            double pointsValue = payment.getLoyaltyPointsToRedeem() / 100.0;
            totalAmount -= pointsValue;
        }
        
        // Process payment through bank
        if (bankService != null) {
            boolean paymentSuccess = bankService.processPayment(
                payment.getAccountNumber(), totalAmount);
            
            if (paymentSuccess) {
                // Update stock levels
                for (TransactionItem item : transaction.getItems()) {
                    updateStock(item.getProduct().getProductId(), item.getQuantity());
                }
                
                // Award loyalty points
                int pointsEarned = (int) (totalAmount * 10); // 10 points per pound
                for (TransactionItem item : transaction.getItems()) {
                    pointsEarned += item.getProduct().getLoyaltyPoints() * item.getQuantity();
                }
                
                Customer customer = customers.get(payment.getCustomerId());
                if (customer != null) {
                    customer.addLoyaltyPoints(pointsEarned);
                }
                
                transaction.setCompleted(true);
                return true;
            }
        }
        
        return false;
    }
    
    @Override
    public int getLoyaltyPoints(String customerId) throws RemoteException {
        Customer customer = customers.get(customerId);
        return customer != null ? customer.getLoyaltyPoints() : 0;
    }
    
    @Override
    public boolean redeemLoyaltyPoints(String customerId, int points) throws RemoteException {
        Customer customer = customers.get(customerId);
        if (customer != null && customer.getLoyaltyPoints() >= points) {
            customer.deductLoyaltyPoints(points);
            return true;
        }
        return false;
    }
    
    /**
     * Main method to start the server
     */
    public static void main(String[] args) {
        try {
            // Create and bind the server
            SupermarketServer server = new SupermarketServer();
            Registry registry = LocateRegistry.createRegistry(1099);
            registry.rebind("SupermarketService", server);
            
            System.out.println("Supermarket Server is running on port 1099");
            System.out.println("Waiting for client connections...");
            
        } catch (Exception e) {
            System.err.println("Server error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
