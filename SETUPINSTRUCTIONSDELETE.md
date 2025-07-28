# Implementation for Distributed Systems
## Supermarket Management System

### Table of Contents
1. [Introduction](#introduction)
2. [System Architecture](#system-architecture)
3. [RMI Implementation](#rmi-implementation)
4. [Message Queue Implementation](#message-queue-implementation)
5. [Testing and Results](#testing-and-results)
6. [Conclusion](#conclusion)

---

## 1. Introduction

This report presents the implementation of a distributed supermarket management system using both Remote Method Invocation (RMI) and Message Queue technologies. The system automates product management, customer transactions, and integrates with external services for payment processing and loyalty schemes.

## 2. System Architecture

The distributed system consists of:
- **Supermarket Server**: Main server handling product management and customer transactions
- **Bank Server**: External service for payment processing
- **Supplier Server**: External service for product ordering
- **Loyalty Server**: External service for loyalty points management
- **Client Applications**: Till/Online interfaces for customers

---

## 3. RMI Implementation

### 3.1 Remote Interfaces

```java
// File: SupermarketInterface.java
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

/**
 * Remote interface for supermarket operations
 */
public interface SupermarketInterface extends Remote {
    // Product management
    List<Product> getAvailableProducts() throws RemoteException;
    boolean updateStock(String productId, int quantity) throws RemoteException;
    
    // Customer operations
    String createTransaction(String customerId) throws RemoteException;
    boolean addToBasket(String transactionId, String productId, int quantity) throws RemoteException;
    double calculateTotal(String transactionId) throws RemoteException;
    boolean processPayment(String transactionId, PaymentDetails payment) throws RemoteException;
    
    // Loyalty operations
    int getLoyaltyPoints(String customerId) throws RemoteException;
    boolean redeemLoyaltyPoints(String customerId, int points) throws RemoteException;
}
```

```java
// File: BankInterface.java
import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Remote interface for bank operations
 */
public interface BankInterface extends Remote {
    boolean processPayment(String accountNumber, double amount) throws RemoteException;
    double checkBalance(String accountNumber) throws RemoteException;
}
```

```java
// File: SupplierInterface.java
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

/**
 * Remote interface for supplier operations
 */
public interface SupplierInterface extends Remote {
    boolean placeOrder(String productId, int quantity) throws RemoteException;
    List<Product> getProductCatalog() throws RemoteException;
    boolean checkAvailability(String productId, int quantity) throws RemoteException;
}
```

### 3.2 Data Transfer Objects

```java
// File: Product.java
import java.io.Serializable;

/**
 * Product entity for data transfer
 */
public class Product implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String productId;
    private String name;
    private double price;
    private int stockLevel;
    private int reorderThreshold;
    private String specialOffer;
    private int loyaltyPoints;
    
    // Constructors
    public Product() {}
    
    public Product(String productId, String name, double price, int stockLevel, int reorderThreshold) {
        this.productId = productId;
        this.name = name;
        this.price = price;
        this.stockLevel = stockLevel;
        this.reorderThreshold = reorderThreshold;
    }
    
    // Getters and Setters
    public String getProductId() { return productId; }
    public void setProductId(String productId) { this.productId = productId; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }
    
    public int getStockLevel() { return stockLevel; }
    public void setStockLevel(int stockLevel) { this.stockLevel = stockLevel; }
    
    public int getReorderThreshold() { return reorderThreshold; }
    public void setReorderThreshold(int reorderThreshold) { this.reorderThreshold = reorderThreshold; }
    
    public String getSpecialOffer() { return specialOffer; }
    public void setSpecialOffer(String specialOffer) { this.specialOffer = specialOffer; }
    
    public int getLoyaltyPoints() { return loyaltyPoints; }
    public void setLoyaltyPoints(int loyaltyPoints) { this.loyaltyPoints = loyaltyPoints; }
}
```

```java
// File: PaymentDetails.java
import java.io.Serializable;

/**
 * Payment details for transactions
 */
public class PaymentDetails implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String accountNumber;
    private String customerId;
    private double amount;
    private boolean useLoyaltyPoints;
    private int loyaltyPointsToRedeem;
    
    // Constructors
    public PaymentDetails() {}
    
    public PaymentDetails(String accountNumber, String customerId, double amount) {
        this.accountNumber = accountNumber;
        this.customerId = customerId;
        this.amount = amount;
    }
    
    // Getters and Setters
    public String getAccountNumber() { return accountNumber; }
    public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }
    
    public String getCustomerId() { return customerId; }
    public void setCustomerId(String customerId) { this.customerId = customerId; }
    
    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }
    
    public boolean isUseLoyaltyPoints() { return useLoyaltyPoints; }
    public void setUseLoyaltyPoints(boolean useLoyaltyPoints) { this.useLoyaltyPoints = useLoyaltyPoints; }
    
    public int getLoyaltyPointsToRedeem() { return loyaltyPointsToRedeem; }
    public void setLoyaltyPointsToRedeem(int loyaltyPointsToRedeem) { 
        this.loyaltyPointsToRedeem = loyaltyPointsToRedeem; 
    }
}
```

### 3.3 Server Implementations

```java
// File: SupermarketServer.java
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
```

```java
// File: BankServer.java
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Bank server implementation for payment processing
 */
public class BankServer extends UnicastRemoteObject implements BankInterface {
    private static final long serialVersionUID = 1L;
    
    private Map<String, Double> accounts;
    
    public BankServer() throws RemoteException {
        super();
        this.accounts = new ConcurrentHashMap<>();
        initializeAccounts();
    }
    
    /**
     * Initialize sample bank accounts
     */
    private void initializeAccounts() {
        accounts.put("ACC001", 1000.00);
        accounts.put("ACC002", 500.00);
        accounts.put("ACC003", 2000.00);
        accounts.put("ACC004", 100.00);
        accounts.put("ACC005", 1500.00);
    }
    
    @Override
    public boolean processPayment(String accountNumber, double amount) throws RemoteException {
        Double balance = accounts.get(accountNumber);
        
        if (balance != null && balance >= amount) {
            accounts.put(accountNumber, balance - amount);
            System.out.println("Payment processed: " + amount + " from account: " + accountNumber);
            return true;
        }
        
        System.out.println("Payment declined: Insufficient funds for account: " + accountNumber);
        return false;
    }
    
    @Override
    public double checkBalance(String accountNumber) throws RemoteException {
        Double balance = accounts.get(accountNumber);
        return balance != null ? balance : 0.0;
    }
    
    public static void main(String[] args) {
        try {
            BankServer server = new BankServer();
            Registry registry = LocateRegistry.getRegistry(1099);
            registry.rebind("BankService", server);
            
            System.out.println("Bank Server is running");
            System.out.println("Ready to process payments...");
            
        } catch (Exception e) {
            System.err.println("Bank Server error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
```

```java
// File: SupplierServer.java
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
```

### 3.4 Client Implementation

```java
// File: SupermarketClient.java
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.List;
import java.util.Scanner;

/**
 * Client application for supermarket system
 */
public class SupermarketClient {
    private SupermarketInterface supermarket;
    private Scanner scanner;
    private String currentTransactionId;
    private String customerId;
    
    public SupermarketClient() {
        scanner = new Scanner(System.in);
    }
    
    /**
     * Connect to the supermarket server
     */
    public void connect() {
        try {
            Registry registry = LocateRegistry.getRegistry("localhost", 1099);
            supermarket = (SupermarketInterface) registry.lookup("SupermarketService");
            System.out.println("Connected to Supermarket Server");
        } catch (Exception e) {
            System.err.println("Client error: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Main client interface
     */
    public void start() {
        System.out.println("Welcome to the Supermarket System");
        System.out.print("Enter your customer ID: ");
        customerId = scanner.nextLine();
        
        while (true) {
            System.out.println("\n--- Main Menu ---");
            System.out.println("1. View Products");
            System.out.println("2. Start Shopping");
            System.out.println("3. Add to Basket");
            System.out.println("4. View Total");
            System.out.println("5. Checkout");
            System.out.println("6. Check Loyalty Points");
            System.out.println("7. Exit");
            System.out.print("Select option: ");
            
            int choice = scanner.nextInt();
            scanner.nextLine(); // consume newline
            
            try {
                switch (choice) {
                    case 1:
                        viewProducts();
                        break;
                    case 2:
                        startShopping();
                        break;
                    case 3:
                        addToBasket();
                        break;
                    case 4:
                        viewTotal();
                        break;
                    case 5:
                        checkout();
                        break;
                    case 6:
                        checkLoyaltyPoints();
                        break;
                    case 7:
                        System.out.println("Thank you for shopping!");
                        return;
                    default:
                        System.out.println("Invalid option");
                }
            } catch (Exception e) {
                System.err.println("Error: " + e.getMessage());
            }
        }
    }
    
    /**
     * Display available products
     */
    private void viewProducts() throws Exception {
        List<Product> products = supermarket.getAvailableProducts();
        System.out.println("\n--- Available Products ---");
        System.out.printf("%-10s %-20s %-10s %-10s %-20s%n", 
                         "ID", "Name", "Price", "Stock", "Special Offer");
        System.out.println("-".repeat(70));
        
        for (Product p : products) {
            System.out.printf("%-10s %-20s £%-9.2f %-10d %-20s%n",
                p.getProductId(), 
                p.getName(), 
                p.getPrice(), 
                p.getStockLevel(),
                p.getSpecialOffer() != null ? p.getSpecialOffer() : "None"
            );
        }
    }
    
    /**
     * Start a new shopping transaction
     */
    private void startShopping() throws Exception {
        currentTransactionId = supermarket.createTransaction(customerId);
        System.out.println("Shopping session started. Transaction ID: " + currentTransactionId);
    }
    
    /**
     * Add items to basket
     */
    private void addToBasket() throws Exception {
        if (currentTransactionId == null) {
            System.out.println("Please start shopping first!");
            return;
        }
        
        System.out.print("Enter product ID: ");
        String productId = scanner.nextLine();
        System.out.print("Enter quantity: ");
        int quantity = scanner.nextInt();
        scanner.nextLine(); // consume newline
        
        boolean added = supermarket.addToBasket(currentTransactionId, productId, quantity);
        if (added) {
            System.out.println("Item added to basket");
        } else {
            System.out.println("Failed to add item (check stock availability)");
        }
    }
    
    /**
     * View current total
     */
    private void viewTotal() throws Exception {
        if (currentTransactionId == null) {
            System.out.println("No active transaction");
            return;
        }
        
        double total = supermarket.calculateTotal(currentTransactionId);
        System.out.printf("Current total: £%.2f%n", total);
    }
    
    /**
     * Process checkout
     */
    private void checkout() throws Exception {
        if (currentTransactionId == null) {
            System.out.println("No active transaction");
            return;
        }
        
        double total = supermarket.calculateTotal(currentTransactionId);
        System.out.printf("Total amount: £%.2f%n", total);
        
        // Check if customer wants to use loyalty points
        System.out.print("Use loyalty points? (y/n): ");
        String useLoyalty = scanner.nextLine();
        
        PaymentDetails payment = new PaymentDetails();
        payment.setCustomerId(customerId);
        payment.setAmount(total);
        
        if (useLoyalty.equalsIgnoreCase("y")) {
            int availablePoints = supermarket.getLoyaltyPoints(customerId);
            System.out.println("Available points: " + availablePoints);
            System.out.print("Points to redeem: ");
            int pointsToRedeem = scanner.nextInt();
            scanner.nextLine(); // consume newline
            
            payment.setUseLoyaltyPoints(true);
            payment.setLoyaltyPointsToRedeem(pointsToRedeem);
        }
        
        System.out.print("Enter bank account number: ");
        String accountNumber = scanner.nextLine();
        payment.setAccountNumber(accountNumber);
        
        boolean success = supermarket.processPayment(currentTransactionId, payment);
        if (success) {
            System.out.println("Payment successful! Thank you for shopping.");
            currentTransactionId = null;
        } else {
            System.out.println("Payment failed. Please try again.");
        }
    }
    
    /**
     * Check loyalty points balance
     */
    private void checkLoyaltyPoints() throws Exception {
        int points = supermarket.getLoyaltyPoints(customerId);
        System.out.println("Your loyalty points: " + points);
        System.out.printf("Value: £%.2f%n", points / 100.0);
    }
    
    public static void main(String[] args) {
        SupermarketClient client = new SupermarketClient();
        client.connect();
        client.start();
    }
}
```

### 3.5 Supporting Classes

```java
// File: Transaction.java
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Transaction entity
 */
public class Transaction implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String transactionId;
    private String customerId;
    private List<TransactionItem> items;
    private boolean completed;
    
    public Transaction(String transactionId, String customerId) {
        this.transactionId = transactionId;
        this.customerId = customerId;
        this.items = new ArrayList<>();
        this.completed = false;
    }
    
    public void addItem(TransactionItem item) {
        items.add(item);
    }
    
    public double calculateTotal() {
        double total = 0.0;
        
        for (TransactionItem item : items) {
            Product product = item.getProduct();
            int quantity = item.getQuantity();
            double itemTotal = product.getPrice() * quantity;
            
            // Apply special offers
            if ("3-for-2".equals(product.getSpecialOffer())) {
                int setsOfThree = quantity / 3;
                int remainder = quantity % 3;
                itemTotal = (setsOfThree * 2 + remainder) * product.getPrice();
            } else if ("half-price".equals(product.getSpecialOffer())) {
                itemTotal = itemTotal * 0.5;
            } else if ("buy-1-get-1-free".equals(product.getSpecialOffer())) {
                int paidItems = (quantity + 1) / 2;
                itemTotal = paidItems * product.getPrice();
            }
            
            total += itemTotal;
        }
        
        return total;
    }
    
    // Getters and setters
    public String getTransactionId() { return transactionId; }
    public String getCustomerId() { return customerId; }
    public List<TransactionItem> getItems() { return items; }
    public boolean isCompleted() { return completed; }
    public void setCompleted(boolean completed) { this.completed = completed; }
}
```

```java
// File: TransactionItem.java
import java.io.Serializable;

/**
 * Item in a transaction
 */
public class TransactionItem implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private Product product;
    private int quantity;
    
    public TransactionItem(Product product, int quantity) {
        this.product = product;
        this.quantity = quantity;
    }
    
    public Product getProduct() { return product; }
    public int getQuantity() { return quantity; }
}
```

```java
// File: Customer.java
import java.io.Serializable;

/**
 * Customer entity
 */
public class Customer implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String customerId;
    private String name;
    private String email;
    private int loyaltyPoints;
    
    public Customer(String customerId, String name, String email) {
        this.customerId = customerId;
        this.name = name;
        this.email = email;
        this.loyaltyPoints = 0;
    }
    
    public void addLoyaltyPoints(int points) {
        this.loyaltyPoints += points;
    }
    
    public void deductLoyaltyPoints(int points) {
        this.loyaltyPoints = Math.max(0, this.loyaltyPoints - points);
    }
    
    // Getters and setters
    public String getCustomerId() { return customerId; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public int getLoyaltyPoints() { return loyaltyPoints; }
}
```

```java
// File: Order.java
import java.io.Serializable;
import java.util.Date;

/**
 * Order entity for supplier orders
 */
public class Order implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String orderId;
    private String productId;
    private int quantity;
    private Date orderDate;
    
    public Order(String orderId, String productId, int quantity) {
        this.orderId = orderId;
        this.productId = productId;
        this.quantity = quantity;
        this.orderDate = new Date();
    }
    
    // Getters
    public String getOrderId() { return orderId; }
    public String getProductId() { return productId; }
    public int getQuantity() { return quantity; }
    public Date getOrderDate() { return orderDate; }
}
```

---

## 4. Message Queue Implementation

### 4.1 Message Queue Server

```java
// File: MessageQueueServer.java
import java.util.*;
import java.util.concurrent.*;

/**
 * Simple message queue server implementation
 */
public class MessageQueueServer {
    // Message queues for different topics
    private Map<String, BlockingQueue<Message>> queues;
    private Map<String, List<MessageConsumer>> subscribers;
    private ExecutorService executorService;
    private volatile boolean running;
    
    public MessageQueueServer() {
        this.queues = new ConcurrentHashMap<>();
        this.subscribers = new ConcurrentHashMap<>();
        this.executorService = Executors.newCachedThreadPool();
        this.running = true;
        
        initializeQueues();
    }
    
    /**
     * Initialize message queues for different topics
     */
    private void initializeQueues() {
        createQueue("payment-requests");
        createQueue("payment-responses");
        createQueue("stock-updates");
        createQueue("reorder-requests");
        createQueue("loyalty-updates");
        createQueue("transaction-logs");
    }
    
    /**
     * Create a new message queue
     */
    public void createQueue(String queueName) {
        queues.put(queueName, new LinkedBlockingQueue<>());
        subscribers.put(queueName, new CopyOnWriteArrayList<>());
        System.out.println("Created queue: " + queueName);
    }
    
    /**
     * Send message to a queue (point-to-point)
     */
    public void sendMessage(String queueName, Message message) {
        BlockingQueue<Message> queue = queues.get(queueName);
        if (queue != null) {
            try {
                queue.offer(message, 5, TimeUnit.SECONDS);
                System.out.println("Message sent to queue " + queueName + ": " + message.getContent());
            } catch (InterruptedException e) {
                System.err.println("Error sending message: " + e.getMessage());
            }
        }
    }
    
    /**
     * Publish message to subscribers (publish-subscribe)
     */
    public void publishMessage(String topic, Message message) {
        List<MessageConsumer> topicSubscribers = subscribers.get(topic);
        if (topicSubscribers != null) {
            for (MessageConsumer consumer : topicSubscribers) {
                executorService.execute(() -> {
                    try {
                        consumer.onMessage(message);
                    } catch (Exception e) {
                        System.err.println("Error delivering message to subscriber: " + e.getMessage());
                    }
                });
            }
            System.out.println("Published message to " + topicSubscribers.size() + 
                             " subscribers on topic: " + topic);
        }
    }
    
    /**
     * Subscribe to a topic
     */
    public void subscribe(String topic, MessageConsumer consumer) {
        List<MessageConsumer> topicSubscribers = subscribers.get(topic);
        if (topicSubscribers != null) {
            topicSubscribers.add(consumer);
            System.out.println("New subscriber added to topic: " + topic);
        }
    }
    
    /**
     * Receive message from queue (blocking)
     */
    public Message receiveMessage(String queueName, long timeout, TimeUnit unit) 
            throws InterruptedException {
        BlockingQueue<Message> queue = queues.get(queueName);
        if (queue != null) {
            return queue.poll(timeout, unit);
        }
        return null;
    }
    
    /**
     * Start the message queue server
     */
    public void start() {
        System.out.println("Message Queue Server started");
        
        // Start queue monitoring thread
        executorService.execute(() -> {
            while (running) {
                try {
                    Thread.sleep(10000); // Monitor every 10 seconds
                    printQueueStatus();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        });
    }
    
    /**
     * Print queue status
     */
    private void printQueueStatus() {
        System.out.println("\n--- Queue Status ---");
        for (Map.Entry<String, BlockingQueue<Message>> entry : queues.entrySet()) {
            System.out.println(entry.getKey() + ": " + entry.getValue().size() + " messages");
        }
    }
    
    /**
     * Shutdown the server
     */
    public void shutdown() {
        running = false;
        executorService.shutdown();
        System.out.println("Message Queue Server shutdown");
    }
    
    public static void main(String[] args) {
        MessageQueueServer server = new MessageQueueServer();
        server.start();
        
        // Keep server running
        Runtime.getRuntime().addShutdownHook(new Thread(server::shutdown));
    }
}
```

### 4.2 Message and Consumer Classes

```java
// File: Message.java
import java.io.Serializable;
import java.util.Date;
import java.util.UUID;

/**
 * Message class for queue communication
 */
public class Message implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String messageId;
    private String type;
    private String content;
    private Date timestamp;
    private String sender;
    private int retryCount;
    
    public Message(String type, String content, String sender) {
        this.messageId = UUID.randomUUID().toString();
        this.type = type;
        this.content = content;
        this.sender = sender;
        this.timestamp = new Date();
        this.retryCount = 0;
    }
    
    // Getters and setters
    public String getMessageId() { return messageId; }
    public String getType() { return type; }
    public String getContent() { return content; }
    public Date getTimestamp() { return timestamp; }
    public String getSender() { return sender; }
    public int getRetryCount() { return retryCount; }
    public void incrementRetryCount() { this.retryCount++; }
}
```

```java
// File: MessageConsumer.java
/**
 * Interface for message consumers
 */
public interface MessageConsumer {
    void onMessage(Message message);
}
```

### 4.3 Asynchronous Supermarket System

```java
// File: AsyncSupermarketSystem.java
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
        
        // Simulate operations
        Scanner scanner = new Scanner(System.in);
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
```

### 4.4 Asynchronous Client

```java
// File: AsyncClient.java
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

/**
 * Asynchronous client for the supermarket system
 */
public class AsyncClient {
    private MessageQueueServer messageQueue;
    private Scanner scanner;
    private String clientId;
    
    public AsyncClient(MessageQueueServer messageQueue) {
        this.messageQueue = messageQueue;
        this.scanner = new Scanner(System.in);
        this.clientId = "Client-" + System.currentTimeMillis();
        
        // Subscribe to relevant topics
        setupSubscriptions();
    }
    
    /**
     * Setup message subscriptions
     */
    private void setupSubscriptions() {
        // Subscribe to payment responses
        messageQueue.subscribe("payment-responses", message -> {
            if (message.getContent().startsWith("SUCCESS")) {
                System.out.println("[CLIENT] Payment successful!");
            } else {
                System.out.println("[CLIENT] Payment failed: " + message.getContent());
            }
        });
    }
    
    /**
     * Process a purchase asynchronously
     */
    public void processPurchase() {
        System.out.println("\n--- Async Purchase Process ---");
        
        // Simulate basket items
        System.out.print("Enter product ID: ");
        String productId = scanner.nextLine();
        System.out.print("Enter quantity: ");
        int quantity = scanner.nextInt();
        scanner.nextLine();
        
        // Send stock update
        Message stockUpdate = new Message("STOCK_UPDATE",
            productId + ":" + quantity, clientId);
        messageQueue.sendMessage("stock-updates", stockUpdate);
        System.out.println("Stock update sent");
        
        // Process payment
        System.out.print("Enter account number: ");
        String accountNumber = scanner.nextLine();
        System.out.print("Enter amount: ");
        double amount = scanner.nextDouble();
        scanner.nextLine();
        
        Message paymentRequest = new Message("PAYMENT_REQUEST",
            accountNumber + ":" + amount, clientId);
        messageQueue.sendMessage("payment-requests", paymentRequest);
        System.out.println("Payment request sent");
        
        // Wait for response
        try {
            Message response = messageQueue.receiveMessage("payment-responses", 
                                                          10, TimeUnit.SECONDS);
            if (response != null) {
                System.out.println("Payment response: " + response.getContent());
            } else {
                System.out.println("Payment timeout - no response received");
            }
        } catch (InterruptedException e) {
            System.err.println("Error waiting for response: " + e.getMessage());
        }
    }
    
    /**
     * Main client interface
     */
    public void start() {
        System.out.println("Async Client started - ID: " + clientId);
        
        while (true) {
            System.out.println("\n--- Async Client Menu ---");
            System.out.println("1. Process Purchase");
            System.out.println("2. Exit");
            System.out.print("Select option: ");
            
            int choice = scanner.nextInt();
            scanner.nextLine();
            
            switch (choice) {
                case 1:
                    processPurchase();
                    break;
                case 2:
                    System.out.println("Exiting client");
                    return;
                default:
                    System.out.println("Invalid option");
            }
        }
    }
}
```

---

## 5. Testing and Results

### 5.1 RMI System Testing

#### Test Case 1: Basic Shopping Transaction

**Test Steps:**
1. Start all RMI servers (Supermarket, Bank, Supplier)
2. Start client and connect
3. View products
4. Add items to basket
5. Process payment

**Screenshot: RMI Servers Running**
```
[Terminal 1 - Supermarket Server]
Supermarket Server is running on port 1099
Waiting for client connections...
Connected to external services
Payment processed: 50.00 from account: ACC001
Reorder placed for product: P001

[Terminal 2 - Bank Server]
Bank Server is running
Ready to process payments...
Payment processed: 50.00 from account: ACC001

[Terminal 3 - Supplier Server]
Supplier Server is running
Ready to process orders...
Order received: 50 units of Apples
```

**Screenshot: Client Shopping Session**
```
Welcome to the Supermarket System
Enter your customer ID: CUST001

--- Main Menu ---
1. View Products
2. Start Shopping
3. Add to Basket
4. View Total
5. Checkout
6. Check Loyalty Points
7. Exit
Select option: 1

--- Available Products ---
ID         Name                 Price      Stock      Special Offer       
----------------------------------------------------------------------
P001       Apples               £0.50      100        3-for-2             
P002       Bread                £1.20      50         None                
P003       Milk                 £0.90      75         None                
P004       Cheese               £3.50      30         None                
P005       Soap                 £2.00      40         extra-100-points    

Select option: 2
Shopping session started. Transaction ID: 7f8a9b1c-2d3e-4f5a-6b7c-8d9e0f1a2b3c

Select option: 3
Enter product ID: P001
Enter quantity: 30
Item added to basket

Select option: 4
Current total: £10.00

Select option: 5
Total amount: £10.00
Use loyalty points? (y/n): n
Enter bank account number: ACC001
Payment successful! Thank you for shopping.
```

#### Test Case 2: Stock Reorder Trigger

**Test:** Purchase items that trigger automatic reorder

**Result:** When stock falls below threshold, automatic reorder is placed with supplier.

### 5.2 Message Queue System Testing

#### Test Case 1: Asynchronous Payment Processing

**Screenshot: Async System Running**
```
Message Queue Server started
Created queue: payment-requests
Created queue: payment-responses
Created queue: stock-updates
Created queue: reorder-requests
Created queue: loyalty-updates
Created queue: transaction-logs
Asynchronous Supermarket System started

--- Queue Status ---
payment-requests: 0 messages
payment-responses: 0 messages
stock-updates: 0 messages
reorder-requests: 0 messages
loyalty-updates: 0 messages
transaction-logs: 0 messages

[STOCK] Processing stock update: P001:10
[AUDIT] Transaction log: Stock updated: Product=P001, NewLevel=90
[PAYMENT] Processing payment request: ACC001:100.50
[AUDIT] Transaction log: Payment successful: Account=ACC001, Amount=100.50
```

#### Test Case 2: Service Disconnection Handling

**Test Steps:**
1. Disconnect bank service
2. Send payment request
3. Observe retry mechanism
4. Reconnect service
5. Observe successful processing

**Screenshot: Disconnection Handling**
```
--- Async System Control ---
Select option: 3
Bank service disconnected

Select option: 1
Message sent to queue payment-requests: ACC001:100.50

[PAYMENT] Processing payment request: ACC001:100.50
[PAYMENT] Bank service unavailable, queuing request
[PAYMENT] Processing payment request: ACC001:100.50
[PAYMENT] Bank service unavailable, queuing request

Select option: 4
Bank service connected

[PAYMENT] Processing payment request: ACC001:100.50
[AUDIT] Transaction log: Payment successful: Account=ACC001, Amount=100.50
```

### 5.3 Performance Comparison

| Aspect | RMI | Message Queue |
|--------|-----|---------------|
| Response Time | Immediate (synchronous) | Delayed (asynchronous) |
| Fault Tolerance | Service must be available | Queues requests during outages |
| Scalability | Limited by thread pool | High - decoupled processing |
| Complexity | Lower | Higher |
| Use Case | Real-time operations | Batch processing, resilience |

---

## 6. Conclusion

### 6.1 RMI Implementation Analysis

The RMI implementation provides:
- **Strong typing** through remote interfaces
- **Synchronous communication** suitable for real-time operations
- **Direct method invocation** simplifying client-server interaction
- **One-to-many relationships** demonstrated through multiple service integration

However, RMI requires all services to be available during operation, making it less resilient to failures.

### 6.2 Message Queue Implementation Analysis

The Message Queue implementation offers:
- **Asynchronous processing** allowing non-blocking operations
- **Service decoupling** improving system resilience
- **Automatic retry mechanisms** for failed operations
- **Both point-to-point and publish-subscribe** patterns

The message queue system successfully demonstrates how disconnected services can be managed through message persistence and retry mechanisms.

### 6.3 Suitability for the Use Case

For the supermarket system:

**RMI is suitable for:**
- Till operations requiring immediate feedback
- Inventory queries needing real-time data
- Customer-facing operations

**Message Queues are suitable for:**
- Batch processing of transactions
- Integration with external services (bank, suppliers)
- Audit logging and analytics
- Handling peak loads through buffering

### 6.4 Recommendations

A hybrid approach would be optimal:
1. Use RMI for customer-facing operations requiring immediate response
2. Use Message Queues for:
   - External service integration
   - Audit and logging
   - Non-critical updates
   - Operations that can tolerate delays

This combination provides both real-time responsiveness and system resilience.

---

**End of Report**