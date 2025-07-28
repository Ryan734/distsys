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
