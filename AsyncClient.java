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
