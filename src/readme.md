# Gurev Andreea - Laboratory Work 2 Report



---

## Table of Contents
1. [Introduction](#introduction)
2. [Theory and Motivation](#theory-and-motivation)
3. [Implementation & Explanation](#implementation--explanation)
4. [Pattern Integration](#pattern-integration)
5. [Results and Screenshots](#results-and-screenshots)
6. [Conclusions](#conclusions)

---

## Introduction

### Project Overview
This laboratory work extends the E-Commerce Order Management System from Laboratory Work 1 by implementing three structural design patterns. The system now supports:
- **Dynamic order enhancements** (gift wrapping, insurance, express processing)
- **Legacy system integration** (inventory management)
- **Simplified checkout workflow** (coordinating multiple subsystems)

### Patterns from Previous Lab (Creational)
- ✅ **Singleton**: Logger for centralized logging
- ✅ **Builder**: Order.Builder for complex object construction
- ✅ **Factory Method**: PaymentProcessorCreator for payment processors
- ✅ **Abstract Factory**: ShippingFactory for shipping materials

### New Patterns (Structural)
- **Decorator**: Dynamic order enhancements
- **Adapter**: Legacy inventory system integration
- **Facade**: Simplified checkout process

---

## Theory and Motivation

### What are Structural Design Patterns?

Structural design patterns deal with object composition and class structures. They help ensure that when one part of a system changes, the entire structure doesn't need to change. These patterns focus on how classes and objects can be composed to form larger structures.

### The Three Implemented Patterns

#### 1. Decorator Pattern 🎨

**Intent**: Attach additional responsibilities to an object dynamically. Decorators provide a flexible alternative to subclassing for extending functionality.

**Problem**: In our e-commerce system, customers can add various optional services to their orders:
- Gift wrapping
- Shipping insurance
- Express processing
- Priority support

Creating a subclass for every possible combination would lead to **class explosion**:
```
Order
├── OrderWithGiftWrap
├── OrderWithInsurance
├── OrderWithGiftWrapAndInsurance
├── OrderWithGiftWrapAndExpress
├── OrderWithInsuranceAndExpress
└── OrderWithGiftWrapAndInsuranceAndExpress
... (2^4 = 16 classes for 4 features!)
```

**Solution**: Use decorators that can be wrapped around orders dynamically.

**Benefits**:
- Flexible: Add/remove features at runtime
- Open/Closed Principle: Add new features without modifying existing code
- Single Responsibility: Each decorator handles one feature
- Transparent: Client code doesn't need to know about decorators

---

#### 2. Adapter Pattern 🔌

**Intent**: Convert the interface of a class into another interface clients expect. Adapter lets classes work together that couldn't otherwise because of incompatible interfaces.

**Problem**: Our modern e-commerce system needs to integrate with a legacy inventory management system that:
- Uses different method names (`queryStockLevel` vs `getAvailableQuantity`)
- Uses different parameter types
- Cannot be modified (third-party or legacy code)

**Solution**: Create an adapter that translates between the modern interface and the legacy system.

**Benefits**:
- Reuse existing code without modification
- Single Responsibility: Adapter only handles interface conversion
- Integration without tight coupling
- Easy to swap implementations

---

#### 3. Facade Pattern 🏛️

**Intent**: Provide a unified interface to a set of interfaces in a subsystem. Facade defines a higher-level interface that makes the subsystem easier to use.

**Problem**: The checkout process involves coordinating multiple complex subsystems:
```java
// WITHOUT FACADE - Client must handle:
1. Validate inventory
2. Reserve stock
3. Process payment
4. Handle payment rollback on failure
5. Create shipping labels
6. Create package boxes
7. Initialize order tracking
8. Update tracking status
9. Send order confirmation email
10. Send payment receipt email
11. Send shipping notification email
12. Release inventory on errors
13. Handle all exceptions
... ~50+ lines of complex coordination code!
```

**Solution**: Create a facade that provides a simple `processCheckout()` method that handles all complexity internally.

**Benefits**:
- Simplified interface for complex subsystems
- Reduced dependencies between client and subsystems
- Easier to maintain and test
- Centralized coordination logic

---

## Implementation & Explanation


#### Key Implementation

**Component Interface**:
```java
interface OrderComponent {
    double calculateTotal();
    String getDescription();
    Order getBaseOrder();
}
```

**Concrete Component**:
```java
class BasicOrderComponent implements OrderComponent {
    private final Order order;
    
    public BasicOrderComponent(Order order) {
        this.order = order;
    }
    
    @Override
    public double calculateTotal() {
        return order.total();
    }
    
    @Override
    public String getDescription() {
        return "Standard Order";
    }
    
    @Override
    public Order getBaseOrder() {
        return order;
    }
}
```

**Abstract Decorator**:
```java
abstract class OrderEnhancementDecorator implements OrderComponent {
    protected OrderComponent wrappedOrder;
    
    public OrderEnhancementDecorator(OrderComponent order) {
        this.wrappedOrder = order;
    }
    
    // Default implementations delegate to wrapped object
    @Override
    public double calculateTotal() {
        return wrappedOrder.calculateTotal();
    }
    
    @Override
    public String getDescription() {
        return wrappedOrder.getDescription();
    }
}
```

**Concrete Decorator Example - Gift Wrapping**:
```java
class GiftWrapDecorator extends OrderEnhancementDecorator {
    private static final double GIFT_WRAP_FEE = 5.99;
    
    public GiftWrapDecorator(OrderComponent order) {
        super(order);
    }
    
    @Override
    public double calculateTotal() {
        // Add gift wrap fee to wrapped order's total
        return wrappedOrder.calculateTotal() + GIFT_WRAP_FEE;
    }
    
    @Override
    public String getDescription() {
        // Append gift wrap to description
        return wrappedOrder.getDescription() + 
               " + Gift Wrapping ($" + String.format("%.2f", GIFT_WRAP_FEE) + ")";
    }
}
```

#### Usage in Main Application

```java
// Build base order
Order order = new Order.Builder()
        .orderId("ORD-001")
        .customer("John Doe")
        .shipTo("123 Main St")
        .addItem(product, 2)
        .build();

// Wrap with decorators dynamically based on user choices
OrderComponent enhancedOrder = new BasicOrderComponent(order);

if (addGiftWrap) {
    enhancedOrder = new GiftWrapDecorator(enhancedOrder);
}
if (addInsurance) {
    enhancedOrder = new InsuranceDecorator(enhancedOrder);
}
if (addExpress) {
    enhancedOrder = new ExpressProcessingDecorator(enhancedOrder);
}

// Final total includes all enhancements
double finalTotal = enhancedOrder.calculateTotal();
String description = enhancedOrder.getDescription();
// Description: "Standard Order + Gift Wrapping ($5.99) + Shipping Insurance ($4.80)"
```

#### Why This Works

1. **Composition over Inheritance**: Each decorator wraps another OrderComponent
2. **Transparency**: Client treats decorated and undecorated objects the same way
3. **Flexibility**: Can add/remove decorators at runtime
4. **Extensibility**: New decorators can be added without changing existing code

---

### Pattern 2: Adapter Pattern (Inventory Integration)

**Location**: `src/InventoryIntegration.java`

#### Architecture

```
InventoryManager (target interface)
         ↑
         │ implements
         │
InventoryAdapter ────uses───> LegacyInventorySystem (adaptee)
```

#### Key Implementation

**Target Interface** (what our system expects):
```java
interface InventoryManager {
    boolean checkStock(String sku, int quantity);
    void reserveStock(String sku, int quantity);
    void releaseStock(String sku, int quantity);
    int getAvailableQuantity(String sku);
}
```

**Adaptee** (legacy system with incompatible interface):
```java
class LegacyInventorySystem {
    private final Map<String, Integer> stockLevels = new HashMap<>();
    
    // Legacy method names
    public int queryStockLevel(String productCode) {
        return stockLevels.getOrDefault(productCode, 0);
    }
    
    public boolean decrementStock(String productCode, int amount) {
        int current = queryStockLevel(productCode);
        if (current >= amount) {
            stockLevels.put(productCode, current - amount);
            return true;
        }
        return false;
    }
    
    public void incrementStock(String productCode, int amount) {
        int current = queryStockLevel(productCode);
        stockLevels.put(productCode, current + amount);
    }
}
```

**Adapter** (converts interfaces):
```java
class InventoryAdapter implements InventoryManager {
    private final LegacyInventorySystem legacySystem;
    
    public InventoryAdapter(LegacyInventorySystem legacySystem) {
        this.legacySystem = legacySystem;
        Logger.getInstance().info("Inventory Adapter initialized");
    }
    
    @Override
    public boolean checkStock(String sku, int quantity) {
        // Adapt modern method to legacy method
        int available = legacySystem.queryStockLevel(sku);
        return available >= quantity;
    }
    
    @Override
    public void reserveStock(String sku, int quantity) {
        // Adapt reserve operation to decrement
        if (!legacySystem.decrementStock(sku, quantity)) {
            throw new IllegalStateException("Insufficient stock");
        }
    }
    
    @Override
    public void releaseStock(String sku, int quantity) {
        // Adapt release operation to increment
        legacySystem.incrementStock(sku, quantity);
    }
    
    @Override
    public int getAvailableQuantity(String sku) {
        // Direct delegation with name translation
        return legacySystem.queryStockLevel(sku);
    }
}
```

#### Usage in Main Application

```java
// Initialize legacy system (cannot modify this code)
LegacyInventorySystem legacyInventory = new LegacyInventorySystem();

// Create adapter
InventoryAdapter inventoryAdapter = new InventoryAdapter(legacyInventory);

// Now can use modern interface with legacy system
if (inventoryAdapter.checkStock("SKU-1", 5)) {
    inventoryAdapter.reserveStock("SKU-1", 5);
    System.out.println("Stock reserved");
}

// Get stock levels through adapter
int available = inventoryAdapter.getAvailableQuantity("SKU-2");
System.out.println("Available: " + available);
```

#### Why This Works

1. **Interface Translation**: Adapter converts method calls between interfaces
2. **No Legacy Modification**: Legacy system remains unchanged
3. **Transparent Integration**: Rest of system uses modern interface
4. **Easy to Replace**: Can swap legacy system without affecting clients

---

### Pattern 3: Facade Pattern (Checkout Workflow)

**Location**: `src/CheckoutFacade.java`

#### Architecture

```
Client
  │
  └──> CheckoutFacade.processCheckout()
            │
            ├──> InventoryManager (stock validation)
            ├──> PaymentProcessor (payment processing)
            ├──> ShippingFactory (label & box creation)
            ├──> OrderTrackingSystem (tracking)
            └──> NotificationService (emails)
```

#### Subsystems

**Notification Service**:
```java
class NotificationService {
    public void sendOrderConfirmation(Order order) {
        // Send confirmation email
    }
    
    public void sendShippingNotification(Order order, ShippingLabel label) {
        // Send shipping notification
    }
    
    public void sendPaymentReceipt(Order order, double amount) {
        // Send payment receipt
    }
}
```

**Order Tracking System**:
```java
class OrderTrackingSystem {
    private final Map<String, String> orderStatuses = new HashMap<>();
    
    public void createTracking(String orderId) {
        orderStatuses.put(orderId, "CREATED");
    }
    
    public void updateStatus(String orderId, String status) {
        orderStatuses.put(orderId, status);
    }
}
```

#### Facade Implementation

```java
class CheckoutFacade {
    // Subsystems
    private final InventoryManager inventoryManager;
    private final NotificationService notificationService;
    private final OrderTrackingSystem trackingSystem;
    
    public CheckoutFacade(InventoryManager inventoryManager) {
        this.inventoryManager = inventoryManager;
        this.notificationService = new NotificationService();
        this.trackingSystem = new OrderTrackingSystem();
    }
    
    /**
     * MAIN FACADE METHOD
     * Replaces ~50+ lines of complex coordination code
     */
    public CheckoutResult processCheckout(
            Order order,
            OrderComponent enhancedOrder,
            PaymentProcessorCreator paymentCreator,
            ShippingFactory shippingFactory) {
        
        CheckoutResult result = new CheckoutResult(order.orderId);
        
        try {
            // Step 1: Validate inventory
            if (!validateInventory(order)) {
                result.setFailure("Insufficient inventory");
                return result;
            }
            
            // Step 2: Reserve stock
            reserveInventory(order);
            
            // Step 3: Process payment
            double finalAmount = enhancedOrder.calculateTotal();
            if (!processPayment(paymentCreator, order, finalAmount)) {
                releaseInventory(order); // Rollback
                result.setFailure("Payment failed");
                return result;
            }
            
            // Step 4: Create shipping materials
            ShippingLabel label = shippingFactory.createLabel(order.shippingAddress);
            PackageBox box = shippingFactory.createBox();
            result.setShippingDetails(label, box);
            
            // Step 5: Create tracking
            trackingSystem.createTracking(order.orderId);
            trackingSystem.updateStatus(order.orderId, "PAYMENT_CONFIRMED");
            
            // Step 6: Send notifications
            notificationService.sendOrderConfirmation(order);
            notificationService.sendPaymentReceipt(order, finalAmount);
            notificationService.sendShippingNotification(order, label);
            
            // Step 7: Finalize
            trackingSystem.updateStatus(order.orderId, "READY_TO_SHIP");
            result.setSuccess(enhancedOrder.getDescription());
            
        } catch (Exception e) {
            // Handle errors and attempt rollback
            releaseInventory(order);
            result.setFailure(e.getMessage());
        }
        
        return result;
    }
    
    // Private helper methods hide complexity
    private boolean validateInventory(Order order) { /* ... */ }
    private void reserveInventory(Order order) { /* ... */ }
    private void releaseInventory(Order order) { /* ... */ }
    private boolean processPayment(...) { /* ... */ }
}
```

#### Usage in Main Application

**WITHOUT Facade** (what client would need to do):
```java
// Client has to handle everything manually (~50+ lines)
StockValidator validator = new StockValidator(inventory);
if (!validator.validateOrder(order)) {
    System.out.println("Insufficient stock");
    return;
}

validator.reserveOrderStock(order);

PaymentProcessor payment = paymentCreator.createProcessor();
if (!payment.process(order)) {
    // Rollback inventory
    for (OrderItem item : order.items) {
        inventory.releaseStock(item.product.sku, item.quantity);
    }
    return;
}

ShippingLabel label = shippingFactory.createLabel(order.shippingAddress);
PackageBox box = shippingFactory.createBox();

OrderTrackingSystem tracking = new OrderTrackingSystem();
tracking.createTracking(order.orderId);
tracking.updateStatus(order.orderId, "PAYMENT_CONFIRMED");

NotificationService notifications = new NotificationService();
notifications.sendOrderConfirmation(order);
notifications.sendPaymentReceipt(order, order.total());
notifications.sendShippingNotification(order, label);

tracking.updateStatus(order.orderId, "READY_TO_SHIP");
// ... error handling, rollback logic, etc.
```

**WITH Facade** (simple single call):
```java
// Client uses simple facade interface (1 line!)
CheckoutResult result = checkoutFacade.processCheckout(
    order,
    enhancedOrder,
    paymentCreator,
    shippingFactory
);

// Check result
if (result.isSuccess()) {
    result.printSummary();
} else {
    System.out.println("Checkout failed: " + result.getMessage());
}
```

#### Why This Works

1. **Simplified Interface**: One method instead of many subsystem calls
2. **Error Handling**: Facade handles all errors and rollbacks internally
3. **Coordination**: Facade knows the correct order of operations
4. **Maintainability**: Changes to workflow only affect facade
5. **Testability**: Can test facade as a unit

---

## Pattern Integration

### How Patterns Work Together

The beauty of design patterns is how they complement each other. In our system:

```
User Action: Place Order
      │
      ├─> BUILDER (Lab 1): Construct Order object
      │     └─> Order.Builder.build()
      │
      ├─> DECORATOR (Lab 2): Add enhancements
      │     ├─> BasicOrderComponent(order)
      │     ├─> GiftWrapDecorator(...)
      │     ├─> InsuranceDecorator(...)
      │     └─> ExpressProcessingDecorator(...)
      │
      ├─> FACTORY METHOD (Lab 1): Create payment processor
      │     └─> PaymentProcessorCreator.createProcessor()
      │
      ├─> ABSTRACT FACTORY (Lab 1): Create shipping materials
      │     ├─> ShippingFactory.createLabel()
      │     └─> ShippingFactory.createBox()
      │
      ├─> ADAPTER (Lab 2): Check/reserve inventory
      │     └─> InventoryAdapter → LegacyInventorySystem
      │
      └─> FACADE (Lab 2): Coordinate everything
            └─> CheckoutFacade.processCheckout()
                  ├─> Uses ADAPTER for inventory
                  ├─> Uses FACTORY METHOD for payment
                  ├─> Uses ABSTRACT FACTORY for shipping
                  └─> Coordinates all subsystems
```

### Pattern Synergy Examples

**1. Facade + Adapter**:
```java
class CheckoutFacade {
    private final InventoryManager inventoryManager; // Could be adapter
    
    public CheckoutResult processCheckout(...) {
        // Facade uses adapter internally
        if (!inventoryManager.checkStock(...)) {
            return failure;
        }
        inventoryManager.reserveStock(...);
    }
}
```

**2. Decorator + Builder**:
```java
// Build order
Order order = new Order.Builder()
    .orderId("ORD-001")
    .customer("John")
    .build();

// Decorate order
OrderComponent enhanced = new BasicOrderComponent(order);
enhanced = new GiftWrapDecorator(enhanced);
enhanced = new ExpressProcessingDecorator(enhanced);
```

**3. All Patterns Together in Checkout**:
```java
// User places order...

// 1. BUILDER: Create order
Order order = new Order.Builder()
    .customer(name)
    .shipTo(address)
    .addItem(product, qty)
    .build();

// 2. DECORATOR: Add features
OrderComponent enhanced = new BasicOrderComponent(order);
if (giftWrap) enhanced = new GiftWrapDecorator(enhanced);
if (insurance) enhanced = new InsuranceDecorator(enhanced);

// 3. FACTORY METHOD: Create payment processor
PaymentProcessorCreator paymentCreator = 
    new CreditCardProcessorCreator();

// 4. ABSTRACT FACTORY: Create shipping materials
ShippingFactory shipFactory = new DHLFactory();

// 5. FACADE uses ADAPTER internally
CheckoutResult result = checkoutFacade.processCheckout(
    order,        // from BUILDER
    enhanced,     // from DECORATOR
    paymentCreator, // from FACTORY METHOD
    shipFactory     // from ABSTRACT FACTORY
);
// Facade coordinates:
// - ADAPTER for inventory
// - FACTORY METHOD for payment
// - ABSTRACT FACTORY for shipping
// - All notifications and tracking
```

---

## Results


### Sample Execution

```
==================== MAIN MENU ====================
Cart: (empty) | Customer: (not set) | Address: (not set)
Payment: PAYPAL | Shipping: DHL
---------------------------------------------------
 1) View product catalog
 2) Add item to cart
 3) View/modify cart
 4) Set customer name
 5) Set shipping address
 6) Set discount
 7) Set notes
 8) Choose payment method
 9) Choose carrier
10) Manage order enhancements (DECORATOR) 🎨
11) Check inventory status (ADAPTER) 🔌
12) Review draft order
13) CHECKOUT (FACADE) 🏛️
 0) Exit
===================================================
```

### Decorator Pattern in Action

```
=== ORDER ENHANCEMENTS (Decorator Pattern) ===
Add optional features to your order:
 1) ☐ Gift Wrapping (+$5.99)
 2) ☐ Shipping Insurance (+2% of order)
 3) ☐ Express Processing (+$9.99)
 4) ☐ Priority Support (+$3.99)
 5) Clear all enhancements
 0) Back
Toggle enhancement: 1

[timestamp] [INFO] Applied: Gift Wrapping

=== ORDER ENHANCEMENTS (Decorator Pattern) ===
 1) ✅ Gift Wrapping (+$5.99)
 2) ☐ Shipping Insurance (+2% of order)
...
```

### Adapter Pattern in Action

```
=== INVENTORY CHECK (via Adapter Pattern) ===
[timestamp] [INFO] Checking inventory through adapted legacy system...

=== LEGACY INVENTORY REPORT ===
SKU-1: 50 units
SKU-2: 30 units
SKU-3: 100 units
SKU-4: 45 units
SKU-5: 25 units
================================

Checking availability for current cart:
✅ All cart items are in stock!
```

### Facade Pattern in Action

```
🏛️  Using FACADE PATTERN to process checkout...

[timestamp] [INFO] === STARTING CHECKOUT FACADE ===
[timestamp] [INFO] Step 1/7: Validating inventory...
[timestamp] [INFO] Validating stock for order ORD-20250108-143027
[timestamp] [INFO] Stock validation passed
[timestamp] [INFO] Step 2/7: Reserving stock...
[LEGACY SYSTEM] Decremented SKU-1 by 2
[timestamp] [INFO] Reserved 2 units of SKU-1
[timestamp] [INFO] Step 3/7: Processing payment...
[timestamp] [INFO] Using processor: PayPal
[timestamp] [INFO] Processing PayPal payment of $61.78
[timestamp] [INFO] Step 4/7: Preparing shipping...
[timestamp] [INFO] Step 5/7: Creating tracking...
[timestamp] [INFO] 📦 Tracking created for order ORD-20250108-143027
[timestamp] [INFO] 📦 Order ORD-20250108-143027 status: PAYMENT_CONFIRMED
[timestamp] [INFO] Step 6/7: Sending notifications...
[timestamp] [INFO] 📧 Sending order confirmation email to John Doe
[timestamp] [INFO] 📧 Sending payment receipt to John Doe
[timestamp] [INFO] 📧 Sending shipping notification to John Doe
[timestamp] [INFO] Step 7/7: Finalizing order...
[timestamp] [INFO] 📦 Order ORD-20250108-143027 status: READY_TO_SHIP
[timestamp] [INFO] === CHECKOUT COMPLETE ===

============================================================
CHECKOUT RESULT - Order #ORD-20250108-143027
============================================================
Status: ✅ SUCCESS
Message: Order processed successfully with: Standard Order + Gift Wrapping ($5.99) + Express Processing ($9.99)
Amount Paid: $61.78

Shipping Details:
  Label: DHL Label → 123 Main Street, City, State
  Box: DHL Standard Box 40x30x20cm
============================================================
```

### Complete Order Flow Example

```
1. Add items to cart
   - Added: Mechanical Keyboard x1 ($89.50)

2. Set customer: John Doe

3. Set shipping address: 123 Main Street

4. Manage enhancements (DECORATOR):
   - ✅ Gift Wrapping (+$5.99)
   - ✅ Express Processing (+$9.99)

5. Check inventory (ADAPTER):
   - All items available

6. Review draft order:
   Subtotal: $89.50
   Base Total: $89.50
   
   --- Order Enhancements ---
   🎁 Gift Wrapping: +$5.99
   ⚡ Express Processing: +$9.99
   
   FINAL TOTAL: $105.48

7. Checkout (FACADE):
   - Validates inventory (via ADAPTER)
   - Reserves stock (via ADAPTER)
   - Processes payment (via FACTORY METHOD)
   - Creates shipping (via ABSTRACT FACTORY)
   - Sends notifications
   - Updates tracking
   
   ✅ Order successful!
```

---


### Final Thoughts

This laboratory work demonstrates that design patterns are powerful tools for creating maintainable, extensible, and flexible software. The structural patterns successfully addressed real problems:
- Decorator eliminated class explosion
- Adapter enabled legacy integration
- Facade simplified complex workflows

Most importantly, the patterns work together harmoniously, creating a cohesive system that is greater than the sum of its parts.

---

## References

1. Gamma, E., Helm, R., Johnson, R., & Vlissides, J. (1994). *Design Patterns: Elements of Reusable Object-Oriented Software*. Addison-Wesley.

2. Freeman, E., & Freeman, E. (2004). *Head First Design Patterns*. O'Reilly Media.

3. Martin, R. C. (2017). *Clean Architecture: A Craftsman's Guide to Software Structure and Design*. Prentice Hall.

4. Bloch, J. (2018). *Effective Java* (3rd ed.). Addison-Wesley Professional.

5. Refactoring.Guru. *Design Patterns*. https://refactoring.guru/design-patterns

---
