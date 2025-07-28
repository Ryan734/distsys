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
