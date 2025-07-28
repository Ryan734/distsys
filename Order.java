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
