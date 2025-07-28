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
