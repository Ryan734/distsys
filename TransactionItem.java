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
