import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Bank server implementation for payment processing
 */
public class BankServer extends UnicastRemoteObject implements BankInterface {
    private static final long serialVersionUID = 1L;
    
    private Map<String, Double> accounts;
    
    public BankServer() throws RemoteException {
        super();
        this.accounts = new ConcurrentHashMap<>();
        initializeAccounts();
    }
    
    /**
     * Initialize sample bank accounts
     */
    private void initializeAccounts() {
        accounts.put("ACC001", 1000.00);
        accounts.put("ACC002", 500.00);
        accounts.put("ACC003", 2000.00);
        accounts.put("ACC004", 100.00);
        accounts.put("ACC005", 1500.00);
    }
    
    @Override
    public boolean processPayment(String accountNumber, double amount) throws RemoteException {
        Double balance = accounts.get(accountNumber);
        
        if (balance != null && balance >= amount) {
            accounts.put(accountNumber, balance - amount);
            System.out.println("Payment processed: " + amount + " from account: " + accountNumber);
            return true;
        }
        
        System.out.println("Payment declined: Insufficient funds for account: " + accountNumber);
        return false;
    }
    
    @Override
    public double checkBalance(String accountNumber) throws RemoteException {
        Double balance = accounts.get(accountNumber);
        return balance != null ? balance : 0.0;
    }
    
    public static void main(String[] args) {
        try {
            BankServer server = new BankServer();
            Registry registry = LocateRegistry.getRegistry(1099);
            registry.rebind("BankService", server);
            
            System.out.println("Bank Server is running");
            System.out.println("Ready to process payments...");
            
        } catch (Exception e) {
            System.err.println("Bank Server error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
