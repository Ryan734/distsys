import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Remote interface for bank operations
 */
public interface BankInterface extends Remote {
    boolean processPayment(String accountNumber, double amount) throws RemoteException;
    double checkBalance(String accountNumber) throws RemoteException;
}
