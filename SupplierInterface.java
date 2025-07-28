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
