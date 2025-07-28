# Denis Pack - RMI System Setup and Running Instructions

## Prerequisites
- Java Development Kit (JDK) 8 or higher installed
- Command line/terminal access
- All Java files saved in the same directory

## Step 1: Compile All Java Files

Open a terminal/command prompt in your project directory and run:

```bash
javac *.java
```

If successful, you should see `.class` files generated for each `.java` file.

### Troubleshooting Compilation:
If you get compilation errors, compile in this order please:
```bash
# First compile interfaces and data classes
javac SupermarketInterface.java BankInterface.java SupplierInterface.java
javac Product.java PaymentDetails.java Customer.java Order.java
javac Transaction.java TransactionItem.java

# Then compile servers
javac BankServer.java SupplierServer.java SupermarketServer.java

# Finally compile client
javac SupermarketClient.java
```

## Step 2: Start RMI Registry

Open a **new terminal window** in the same directory and run the following:

```bash
rmiregistry
```

**Windows:**
```cmd
start rmiregistry
```

**Very Important:** 
- Keep this terminal window open - it's running the RMI registry
- You should see no output (blank window) - this is normal.
- The registry runs on port 1099 by default

## Step 3: Start the Servers

You need **three seperate terminal windows** for the three servers. Navigate to your project directory in each.

### Terminal 1 - Start Bank Server:
```bash
java BankServer
```

Expected output:
```
Bank Server is running
Ready to process payments...
```

### Terminal 2 - Start Supplier Server
```bash
java SupplierServer
```

Expected output:
```
Supplier Server is running
Ready to process orders...
```

### Terminal 3 - Start Supermarket Server:
```bash
java SupermarketServer
```

Expected output:
```
Connected to external services
Supermarket Server is running on port 1099
Waiting for client connections...
```

**Note:** The Supermarket Server must be started last as it connects to the Bank and Supplier services.

## Step 5: Run the Client

Open a **fourth terminal window**, navigate to your project directory, and run:

```bash
java SupermarketClient
```

Expected output:
```
Connected to Supermarket Server
Welcome to the Supermarket System
Enter your customer ID: 
```

## Complete Terminal Layout

You should now have 5 terminal windows open:
1. **RMI Registry** - blank window
2. **Bank Server** - showing "Bank Server is running"
3. **Supplier Server** - showing "Supplier Server is running"
4. **Supermarket Server** - showing connection status
5. **Client** - interactive menu

## Testing the System

### In the Client Terminal:

1. **Enter a customer ID** when prompted (e.g., "CUST001")

2. **View Products** - Select option 1
   ```
   --- Main Menu ---
   1. View Products
   ...
   Select option: 1
   ```

3. **Start Shopping** - Select option 2
   ```
   Select option: 2
   Shopping session started. Transaction ID: [UUID will appear here]
   ```

4. **Add Items** - Select option 3
   ```
   Select option: 3
   Enter product ID: P001
   Enter quantity: 10
   ```

5. **Checkout** - Select option 5
   ```
   Select option: 5
   Use loyalty points? (y/n): n
   Enter bank account number: ACC001
   ```

## Monitoring Server Activity

Pay attention to the server terminals for activity:

- **Bank Server** will show payment processing
- **Supplier Server** will show reorder requests when the stock is low
- **Supermarket Server** will show various operations

## Stopping the System

To properly shut down down the system:

1. Exit the client (option 7)
2. Press `Ctrl+C` in each server terminal
3. Press `Ctrl+C` in the RMI registry terminal

## Common Issues and Solutions

### Issue 1: "Connection refused" error
**Solution:** Make sure RMI registry is running before starting servers

### Issue 2: "AlreadyBoundException"
**Solution:** The service is already registered. Either restart all services or use `rebind` (which the code already does)

### Issue 3: "ClassNotFoundException"
**Solution:** Ensure all classes are compiled and you're running from the correct directory

### Issue 4: Port 1099 already in use
**Solution:** 
```bash
# On Linux/Mac
lsof -i :1099
kill -9 [PID]

# On Windows
netstat -ano | findstr :1099
taskkill /PID [PID] /F
```

### Issue 5: Cannot find service in registry
**Solution:** Ensure services are started in correct order: Bank → Supplier → Supermarket → Client

## Alternative: Running with Scripts

### Create a startup script:

**Linux/Mac (`start-rmi.sh`):**
```bash
#!/bin/bash
echo "Starting RMI Registry..."
rmiregistry &
REG_PID=$!
sleep 2

echo "Starting Bank Server..."
java BankServer &
sleep 2

echo "Starting Supplier Server..."
java SupplierServer &
sleep 2

echo "Starting Supermarket Server..."
java SupermarketServer &
sleep 2

echo "All servers started. Starting client..."
java SupermarketClient

# Cleanup on exit
kill $REG_PID
```

Make it executable:
```bash
chmod +x start-rmi.sh
./start-rmi.sh
```

**Windows (`start-rmi.bat`):**
```batch
@echo off
echo Starting RMI Registry...
start "RMI Registry" rmiregistry
timeout /t 2

echo Starting Bank Server...
start "Bank Server" java BankServer
timeout /t 2

echo Starting Supplier Server...
start "Supplier Server" java SupplierServer
timeout /t 2

echo Starting Supermarket Server...
start "Supermarket Server" java SupermarketServer
timeout /t 2

echo All servers started. Starting client...
java SupermarketClient
```

## Testing Checklist

- [ ] All Java files compiled successfully
- [ ] RMI Registry started
- [ ] Bank Server running
- [ ] Supplier Server running
- [ ] Supermarket Server connected to external services
- [ ] Client connected successfully
- [ ] Can view products
- [ ] Can add items to basket
- [ ] Can process payment
- [ ] Automatic reorder triggered when stock low
- [ ] All servers showing appropriate log messages

## Additional Notes

1. **Customer IDs for testing:**
   - The system accepts any customer ID
   - Use "CUST001", "CUST002", etc. for testing

2. **Bank Account Numbers for testing:**
   - ACC001 (balance: 1000.00)
   - ACC002 (balance: 500.00)
   - ACC003 (balance: 2000.00)

3. **Product IDs for testing:**
   - P001 - Apples (3-for-2 offer)
   - P002 - Bread
   - P003 - Milk
   - P004 - Cheese
   - P005 - Soap (100 bonus loyalty points)