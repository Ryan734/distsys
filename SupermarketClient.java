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
