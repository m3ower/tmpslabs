# Gurev Andreea - SOLID Principles Lab 0 Report

A simple Java application demonstrating three SOLID principles through a coffee shop ordering system with an interactive menu.

---

## Objectives

The main objective of this project is to demonstrate practical implementation of three SOLID principles in a real-world scenario. The coffee shop system allows customers to place orders and apply various discounts, showcasing clean code architecture and design patterns.

---

## Used Principles

### 1. **Single Responsibility Principle (SRP)**

**How it's used:** The `Order` class has only one responsibility - to store and manage order data. It doesn't handle business logic, calculations, or output formatting.

```java
class Order {
    private String customerName;
    private String coffeeType;
    private double basePrice;

    public Order(String customerName, String coffeeType, double basePrice) {
        this.customerName = customerName;
        this.coffeeType = coffeeType;
        this.basePrice = basePrice;
    }
    // Only getters - no business logic
}
```

### 2. **Open/Closed Principle (OCP)**

**How it's used:** The discount system is open for extension but closed for modification. New discount types can be added without changing existing code by implementing the `DiscountStrategy` interface.

```java
interface DiscountStrategy {
    double applyDiscount(double price);
    String getDiscountName();
}

class StudentDiscount implements DiscountStrategy {
    @Override
    public double applyDiscount(double price) {
        return price * 0.85; // 15% off
    }
    
    @Override
    public String getDiscountName() {
        return "Student Discount (15% off)";
    }
}

class LoyaltyDiscount implements DiscountStrategy {
    @Override
    public double applyDiscount(double price) {
        return price * 0.80; // 20% off
    }
    
    @Override
    public String getDiscountName() {
        return "Loyalty Member Discount (20% off)";
    }
}
```

### 3. **Dependency Inversion Principle (DIP)**

**How it's used:** The `OrderProcessor` class depends on the `DiscountStrategy` abstraction (interface) rather than concrete discount implementations. This allows flexible discount strategies to be injected at runtime.

```java
class OrderProcessor {
    private DiscountStrategy discountStrategy;
    
    public OrderProcessor(DiscountStrategy discountStrategy) {
        this.discountStrategy = discountStrategy;
    }
    
    public void processOrder(Order order) {
        double finalPrice = discountStrategy.applyDiscount(order.getBasePrice());
        // Print receipt...
    }
}
```

---

## Implementation

The implementation consists of a simple coffee shop system where customers can order coffee and apply discounts. The system uses three main components: an `Order` class that stores order information following SRP, a `DiscountStrategy` interface with multiple implementations demonstrating OCP, and an `OrderProcessor` class that depends on abstractions rather than concrete classes following DIP. The interactive menu guides users through selecting coffee, choosing discounts, and displays a formatted receipt.

**Main Flow:**

```java
public static void main(String[] args) {
    Scanner scanner = new Scanner(System.in);
    
    System.out.print("Enter customer name: ");
    String customerName = scanner.nextLine();
    
    Order order = new Order(customerName, coffeeType, basePrice);
    
    DiscountStrategy discount = new StudentDiscount(); 
    
    OrderProcessor processor = new OrderProcessor(discount);
    processor.processOrder(order);
}
```

**Key Components:**

- **Order Class** - Pure data container with single responsibility
- **DiscountStrategy Interface** - Defines contract for discount implementations
- **Concrete Discount Classes** - Multiple implementations (NoDiscount, StudentDiscount, LoyaltyDiscount, SeniorDiscount)
- **OrderProcessor** - Processes orders using injected discount strategy
- **Main Class** - Interactive menu for user input


---

## Conclusions / Results

### Sample Output:

```
Welcome to SOLID Coffee Shop!
Enter customer name: John

Select your coffee:
1. Espresso - $3.00
2. Cappuccino - $4.50
3. Latte - $4.00
Choice: 2

Select discount type:
1. No Discount
2. Student Discount (15% off)
3. Loyalty Member (20% off)
4. Senior Discount (10% off)
Choice: 2

========== ORDER RECEIPT ==========
Customer: John
Coffee: Cappuccino
Base Price: $4.50
Discount Applied: Student Discount (15% off)
Final Price: $3.82
===================================
```

### Key Takeaways:

**SRP** ensures each class has a clear, single purpose making the code maintainable and easy to understand

**OCP** allows adding new discount types without breaking existing code - we added 4 different discounts effortlessly

**DIP** makes the system flexible and testable by depending on interfaces rather than concrete implementations

The project successfully demonstrates how SOLID principles lead to clean, maintainable, and extensible code architecture in a practical real-world scenario.