import java.util.*;
import java.util.concurrent.*;

/**
 * Asynchronous supermarket system using message queues
 */
public class AsyncSupermarketSystem {
    private MessageQueueServer messageQueue;
    private Map<String, Product> products;
    private Map<String, Double> bankAccounts;
    private ExecutorService executorService;
    private volatile boolean running;
    
    // Service availability flags for testing disconnection
    private volatile boolean bankServiceAvailable = true;
    private volatile boolean supplierServiceAvailable = true;
    
    public AsyncSupermarketSystem() {
        this.messageQueue = new MessageQueueServer();
        this.products = new ConcurrentHashMap<>();
        this.bankAccounts = new ConcurrentHashMap<>();
        this.executorService = Executors.newFixedThreadPool(10);
        this.running = true;
        
        initializeData();
        setupMessageHandlers();
    }
    
    /**
     * Initialize sample data
     */
    private void initializeData() {
        // Initialize products
        products.put("P001", new Product("P001", "Apples", 0.50, 100, 20));
        products.put("P002", new Product("P002", "Bread", 1.20, 50, 10));
        products.put("P003", new Product("P003", "Milk", 0.90, 75, 15));
        
        // Initialize bank accounts
        bankAccounts.put("ACC001", 1000.00);
        bankAccounts.put("ACC002", 500.00);
        bankAccounts.put("ACC003", 2000.00);
    }
    
    /**
     * Setup message handlers for different services
     */
    private void setupMessageHandlers() {
        // Payment request handler
        executorService.execute(() -> handlePaymentRequests());
        
        // Stock update handler
        executorService.execute(() -> handleStockUpdates());
        
        // Reorder request handler
        executorService.execute(() -> handleReorderRequests());
        
        // Transaction log subscriber
        messageQueue.subscribe("transaction-logs", message -> {
            System.out.println("[AUDIT] Transaction log: " + message.getContent());
        });
        
        // Loyalty update subscriber
        messageQueue.subscribe("loyalty-updates", message -> {
            System.out.println("[LOYALTY] Points update: " + message.getContent());
        });
    }
    
    /**
     * Handle payment requests asynchronously
     */
    private void handlePaymentRequests() {
        while (running) {
            try {
                Message request = messageQueue.receiveMessage("payment-requests", 1, TimeUnit.SECONDS);
                
                if (request != null) {
                    System.out.println("[PAYMENT] Processing payment request: " + request.getContent());
                    
                    // Simulate bank service availability check
                    if (!bankServiceAvailable) {
                        System.out.println("[PAYMENT] Bank service unavailable, queuing request");
                        // Retry later
                        request.incrementRetryCount();
                        if (request.getRetryCount() < 3) {
                            Thread.sleep(2000); // Wait before retry
                            messageQueue.sendMessage("payment-requests", request);
                        } else {
                            // Send failure response
                            Message response = new Message("PAYMENT_RESPONSE", 
                                "FAILED:Service unavailable", "BankService");
                            messageQueue.sendMessage("payment-responses", response);
                        }
                        continue;
                    }
                    
                    // Process payment
                    String[] parts = request.getContent().split(":");
                    String accountNumber = parts[0];
                    double amount = Double.parseDouble(parts[1]);
                    
                    boolean success = processPayment(accountNumber, amount);
                    
                    // Send response
                    Message response = new Message("PAYMENT_RESPONSE", 
                        success ? "SUCCESS" : "FAILED:Insufficient funds", "BankService");
                    messageQueue.sendMessage("payment-responses", response);
                    
                    // Log transaction
                    Message logMessage = new Message("TRANSACTION_LOG",
                        String.format("Payment %s: Account=%s, Amount=%.2f",
                                    success ? "successful" : "failed", accountNumber, amount),
                        "BankService");
                    messageQueue.publishMessage("transaction-logs", logMessage);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }
    
    /**
     * Handle stock updates asynchronously
     */
    private void handleStockUpdates() {
        while (running) {
            try {
                Message update = messageQueue.receiveMessage("stock-updates", 1, TimeUnit.SECONDS);
                
                if (update != null) {
                    System.out.println("[STOCK] Processing stock update: " + update.getContent());
                    
                    String[] parts = update.getContent().split(":");
                    String productId = parts[0];
                    int quantity = Integer.parseInt(parts[1]);
                    
                    Product product = products.get(productId);
                    if (product != null) {
                        int newStock = product.getStockLevel() - quantity;
                        product.setStockLevel(Math.max(0, newStock));
                        
                        // Check if reorder needed
                        if (newStock <= product.getReorderThreshold()) {
                            Message reorderRequest = new Message("REORDER_REQUEST",
                                productId + ":50", "SupermarketSystem");
                            messageQueue.sendMessage("reorder-requests", reorderRequest);
                        }
                        
                        // Log update
                        Message logMessage = new Message("STOCK_LOG",
                            String.format("Stock updated: Product=%s, NewLevel=%d", 
                                        productId, newStock),
                            "SupermarketSystem");
                        messageQueue.publishMessage("transaction-logs", logMessage);
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }
    
    /**
     * Handle reorder requests asynchronously
     */
    private void handleReorderRequests() {
        while (running) {
            try {
                Message request = messageQueue.receiveMessage("reorder-requests", 1, TimeUnit.SECONDS);
                
                if (request != null) {
                    System.out.println("[SUPPLIER] Processing reorder request: " + request.getContent());
                    
                    // Check supplier availability
                    if (!supplierServiceAvailable) {
                        System.out.println("[SUPPLIER] Supplier service unavailable, queuing request");
                        request.incrementRetryCount();
                        if (request.getRetryCount() < 5) {
                            Thread.sleep(5000); // Wait longer for supplier
                            messageQueue.sendMessage("reorder-requests", request);
                        }
                        continue;
                    }
                    
                    // Process reorder
                    String[] parts = request.getContent().split(":");
                    String productId = parts[0];
                    int quantity = Integer.parseInt(parts[1]);
                    
                    // Simulate order processing
                    Thread.sleep(1000);
                    
                    // Update stock
                    Product product = products.get(productId);
                    if (product != null) {
                        product.setStockLevel(product.getStockLevel() + quantity);
                        System.out.println("[SUPPLIER] Reorder completed for " + productId);
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }
    
    /**
     * Process payment
     */
    private boolean processPayment(String accountNumber, double amount) {
        Double balance = bankAccounts.get(accountNumber);
        if (balance != null && balance >= amount) {
            bankAccounts.put(accountNumber, balance - amount);
            return true;
        }
        return false;
    }
    
    /**
     * Simulate service disconnection
     */
    public void simulateServiceDisconnection(String service, boolean available) {
        switch (service) {
            case "bank":
                bankServiceAvailable = available;
                System.out.println("Bank service " + (available ? "connected" : "disconnected"));
                break;
            case "supplier":
                supplierServiceAvailable = available;
                System.out.println("Supplier service " + (available ? "connected" : "disconnected"));
                break;
        }
    }
    
    /**
     * Start the asynchronous system
     */
    public void start() {
        messageQueue.start();
        System.out.println("Asynchronous Supermarket System started");
    }
    
    /**
     * Shutdown the system
     */
    public void shutdown() {
        running = false;
        executorService.shutdown();
        messageQueue.shutdown();
    }
    
    /**
     * Get message queue for client use
     */
    public MessageQueueServer getMessageQueue() {
        return messageQueue;
    }
    
    public static void main(String[] args) {
        AsyncSupermarketSystem system = new AsyncSupermarketSystem();
        system.start();
        
        try (// Simulate operations
        Scanner scanner = new Scanner(System.in)) {
            while (true) {
                System.out.println("\n--- Async System Control ---");
                System.out.println("1. Send payment request");
                System.out.println("2. Update stock");
                System.out.println("3. Disconnect bank service");
                System.out.println("4. Reconnect bank service");
                System.out.println("5. Disconnect supplier service");
                System.out.println("6. Reconnect supplier service");
                System.out.println("7. Exit");
                System.out.print("Select option: ");
                
                int choice = scanner.nextInt();
                scanner.nextLine();
                
                switch (choice) {
                    case 1:
                        Message paymentRequest = new Message("PAYMENT_REQUEST",
                            "ACC001:100.50", "ClientApp");
                        system.getMessageQueue().sendMessage("payment-requests", paymentRequest);
                        break;
                        
                    case 2:
                        Message stockUpdate = new Message("STOCK_UPDATE",
                            "P001:10", "ClientApp");
                        system.getMessageQueue().sendMessage("stock-updates", stockUpdate);
                        break;
                        
                    case 3:
                        system.simulateServiceDisconnection("bank", false);
                        break;
                        
                    case 4:
                        system.simulateServiceDisconnection("bank", true);
                        break;
                        
                    case 5:
                        system.simulateServiceDisconnection("supplier", false);
                        break;
                        
                    case 6:
                        system.simulateServiceDisconnection("supplier", true);
                        break;
                        
                    case 7:
                        system.shutdown();
                        return;
                }
            }
        }
    }
}
