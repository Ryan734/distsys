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