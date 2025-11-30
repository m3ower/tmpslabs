import java.time.*;
import java.util.*;

/**
 * BEHAVIORAL PATTERN: Strategy
 *
 * Purpose: Defines a family of algorithms, encapsulates each one, and makes
 * them interchangeable. Strategy lets the algorithm vary independently from
 * clients that use it.
 *
 * Use case: Different discount calculation strategies for orders
 */

// Strategy interface
interface DiscountStrategy {
    double calculateDiscount(Order order);
    String getStrategyName();
    String getDescription();
}

// Concrete Strategy 1: No Discount
class NoDiscountStrategy implements DiscountStrategy {
    @Override
    public double calculateDiscount(Order order) {
        return 0.0;
    }

    @Override
    public String getStrategyName() {
        return "No Discount";
    }

    @Override
    public String getDescription() {
        return "Standard pricing - no discounts applied";
    }
}

// Concrete Strategy 2: Percentage Discount
class PercentageDiscountStrategy implements DiscountStrategy {
    private final double percentage;

    public PercentageDiscountStrategy(double percentage) {
        if (percentage < 0 || percentage > 100) {
            throw new IllegalArgumentException("Percentage must be between 0 and 100");
        }
        this.percentage = percentage;
    }

    @Override
    public double calculateDiscount(Order order) {
        double subtotal = order.subtotal();
        double discount = subtotal * (percentage / 100.0);
        Logger.getInstance().info("💰 Percentage discount (" + percentage + "%): $" +
                String.format("%.2f", discount));
        return discount;
    }

    @Override
    public String getStrategyName() {
        return "Percentage Discount";
    }

    @Override
    public String getDescription() {
        return percentage + "% off entire order";
    }
}

// Concrete Strategy 3: Fixed Amount Discount
class FixedAmountDiscountStrategy implements DiscountStrategy {
    private final double amount;

    public FixedAmountDiscountStrategy(double amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Discount amount cannot be negative");
        }
        this.amount = amount;
    }

    @Override
    public double calculateDiscount(Order order) {
        double subtotal = order.subtotal();
        double discount = Math.min(amount, subtotal); // Don't exceed order total
        Logger.getInstance().info("💰 Fixed discount: $" + String.format("%.2f", discount));
        return discount;
    }

    @Override
    public String getStrategyName() {
        return "Fixed Amount Discount";
    }

    @Override
    public String getDescription() {
        return "$" + String.format("%.2f", amount) + " off";
    }
}

// Concrete Strategy 4: Bulk Order Discount
class BulkOrderDiscountStrategy implements DiscountStrategy {
    private final int quantityThreshold;
    private final double discountPercentage;

    public BulkOrderDiscountStrategy(int quantityThreshold, double discountPercentage) {
        this.quantityThreshold = quantityThreshold;
        this.discountPercentage = discountPercentage;
    }

    @Override
    public double calculateDiscount(Order order) {
        int totalQuantity = order.items.stream()
                .mapToInt(item -> item.quantity)
                .sum();

        if (totalQuantity >= quantityThreshold) {
            double discount = order.subtotal() * (discountPercentage / 100.0);
            Logger.getInstance().info("💰 Bulk order discount (qty: " + totalQuantity +
                    " >= " + quantityThreshold + "): $" + String.format("%.2f", discount));
            return discount;
        }

        Logger.getInstance().info("💰 Bulk discount not applicable (qty: " +
                totalQuantity + " < " + quantityThreshold + ")");
        return 0.0;
    }

    @Override
    public String getStrategyName() {
        return "Bulk Order Discount";
    }

    @Override
    public String getDescription() {
        return discountPercentage + "% off for orders with " +
                quantityThreshold + "+ items";
    }
}

// Concrete Strategy 5: Seasonal Discount
class SeasonalDiscountStrategy implements DiscountStrategy {
    private final Month seasonMonth;
    private final double discountPercentage;

    public SeasonalDiscountStrategy(Month seasonMonth, double discountPercentage) {
        this.seasonMonth = seasonMonth;
        this.discountPercentage = discountPercentage;
    }

    @Override
    public double calculateDiscount(Order order) {
        Month currentMonth = LocalDateTime.now().getMonth();

        if (currentMonth == seasonMonth) {
            double discount = order.subtotal() * (discountPercentage / 100.0);
            Logger.getInstance().info("💰 Seasonal discount for " + seasonMonth +
                    ": $" + String.format("%.2f", discount));
            return discount;
        }

        Logger.getInstance().info("💰 Seasonal discount not applicable " +
                "(current: " + currentMonth + ", seasonal: " + seasonMonth + ")");
        return 0.0;
    }

    @Override
    public String getStrategyName() {
        return "Seasonal Discount";
    }

    @Override
    public String getDescription() {
        return discountPercentage + "% off during " + seasonMonth;
    }
}

// Concrete Strategy 6: Tiered Discount (based on order value)
class TieredDiscountStrategy implements DiscountStrategy {
    private final TreeMap<Double, Double> tiers = new TreeMap<>();

    public TieredDiscountStrategy() {
        // Default tiers: spend threshold -> discount percentage
        tiers.put(50.0, 5.0);    // Spend $50+ -> 5% off
        tiers.put(100.0, 10.0);  // Spend $100+ -> 10% off
        tiers.put(200.0, 15.0);  // Spend $200+ -> 15% off
        tiers.put(500.0, 20.0);  // Spend $500+ -> 20% off
    }

    public TieredDiscountStrategy addTier(double threshold, double percentage) {
        tiers.put(threshold, percentage);
        return this;
    }

    @Override
    public double calculateDiscount(Order order) {
        double subtotal = order.subtotal();

        // Find the highest tier that applies
        Map.Entry<Double, Double> applicableTier = tiers.floorEntry(subtotal);

        if (applicableTier != null) {
            double percentage = applicableTier.getValue();
            double discount = subtotal * (percentage / 100.0);
            Logger.getInstance().info("💰 Tiered discount (tier: $" +
                    applicableTier.getKey() + " -> " + percentage + "%): $" +
                    String.format("%.2f", discount));
            return discount;
        }

        Logger.getInstance().info("💰 No tier discount applicable (subtotal: $" +
                String.format("%.2f", subtotal) + ")");
        return 0.0;
    }

    @Override
    public String getStrategyName() {
        return "Tiered Discount";
    }

    @Override
    public String getDescription() {
        StringBuilder desc = new StringBuilder("Tiered discounts: ");
        tiers.forEach((threshold, percentage) ->
                desc.append("$").append(String.format("%.0f", threshold))
                        .append("+ → ").append(String.format("%.0f", percentage))
                        .append("% off; "));
        return desc.toString();
    }
}

// Concrete Strategy 7: First-Time Customer Discount
class FirstTimeCustomerStrategy implements DiscountStrategy {
    private final Set<String> existingCustomers;
    private final double discountPercentage;

    public FirstTimeCustomerStrategy(double discountPercentage) {
        this.existingCustomers = new HashSet<>();
        this.discountPercentage = discountPercentage;
    }

    public void addExistingCustomer(String customerName) {
        existingCustomers.add(customerName.toLowerCase());
    }

    @Override
    public double calculateDiscount(Order order) {
        String customer = order.customerName.toLowerCase();

        if (!existingCustomers.contains(customer)) {
            double discount = order.subtotal() * (discountPercentage / 100.0);
            Logger.getInstance().info("💰 First-time customer discount for " +
                    order.customerName + ": $" + String.format("%.2f", discount));
            existingCustomers.add(customer); // Mark as existing customer
            return discount;
        }

        Logger.getInstance().info("💰 Customer " + order.customerName +
                " is not eligible for first-time discount");
        return 0.0;
    }

    @Override
    public String getStrategyName() {
        return "First-Time Customer Discount";
    }

    @Override
    public String getDescription() {
        return discountPercentage + "% off for first-time customers";
    }
}

// Context class that uses the strategy
class DiscountCalculator {
    private DiscountStrategy strategy;
    private final Logger logger = Logger.getInstance();

    public DiscountCalculator(DiscountStrategy strategy) {
        this.strategy = strategy;
        logger.info("🎯 Discount strategy set: " + strategy.getStrategyName());
    }

    public void setStrategy(DiscountStrategy strategy) {
        this.strategy = strategy;
        logger.info("🎯 Discount strategy changed to: " + strategy.getStrategyName());
    }

    public double calculateDiscount(Order order) {
        logger.info("🎯 Calculating discount using: " + strategy.getStrategyName());
        return strategy.calculateDiscount(order);
    }

    public String getStrategyInfo() {
        return strategy.getStrategyName() + " - " + strategy.getDescription();
    }
}

// Strategy Manager for easy strategy selection
class DiscountStrategyManager {
    private final Map<String, DiscountStrategy> strategies = new LinkedHashMap<>();
    private final Logger logger = Logger.getInstance();

    public DiscountStrategyManager() {
        // Register default strategies
        registerStrategy("none", new NoDiscountStrategy());
        registerStrategy("percent10", new PercentageDiscountStrategy(10));
        registerStrategy("percent15", new PercentageDiscountStrategy(15));
        registerStrategy("percent20", new PercentageDiscountStrategy(20));
        registerStrategy("fixed5", new FixedAmountDiscountStrategy(5.0));
        registerStrategy("fixed10", new FixedAmountDiscountStrategy(10.0));
        registerStrategy("bulk", new BulkOrderDiscountStrategy(10, 15));
        registerStrategy("seasonal", new SeasonalDiscountStrategy(Month.DECEMBER, 20));
        registerStrategy("tiered", new TieredDiscountStrategy());
        registerStrategy("firsttime", new FirstTimeCustomerStrategy(15));
    }

    public void registerStrategy(String key, DiscountStrategy strategy) {
        strategies.put(key, strategy);
        logger.info("Registered discount strategy: " + key + " - " + strategy.getStrategyName());
    }

    public DiscountStrategy getStrategy(String key) {
        return strategies.get(key);
    }

    public void listStrategies() {
        System.out.println("\n=== AVAILABLE DISCOUNT STRATEGIES ===");
        int i = 1;
        for (Map.Entry<String, DiscountStrategy> entry : strategies.entrySet()) {
            DiscountStrategy strategy = entry.getValue();
            System.out.println(i++ + ") " + entry.getKey() + " - " +
                    strategy.getStrategyName() + "\n   " + strategy.getDescription());
        }
        System.out.println("=====================================\n");
    }

    public Set<String> getStrategyKeys() {
        return strategies.keySet();
    }
}