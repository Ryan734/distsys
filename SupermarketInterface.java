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