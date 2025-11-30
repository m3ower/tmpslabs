import java.util.*;

/**
 * BEHAVIORAL PATTERN: Chain of Responsibility
 *
 * Purpose: Creates a chain of validation handlers where each handler
 * processes the order validation request or passes it to the next handler.
 *
 * Use case: Multi-step order validation before checkout
 */

// Abstract Handler
abstract class OrderValidationHandler {
    protected OrderValidationHandler nextHandler;
    protected Logger logger = Logger.getInstance();

    public OrderValidationHandler setNext(OrderValidationHandler handler) {
        this.nextHandler = handler;
        return handler;
    }

    public abstract ValidationResult validate(Order order, OrderValidationContext context);

    protected ValidationResult passToNext(Order order, OrderValidationContext context) {
        if (nextHandler != null) {
            return nextHandler.validate(order, context);
        }
        return new ValidationResult(true, "All validations passed");
    }
}

// Context object to pass through the chain
class OrderValidationContext {
    private final InventoryManager inventoryManager;
    private final Map<String, Object> data = new HashMap<>();

    public OrderValidationContext(InventoryManager inventoryManager) {
        this.inventoryManager = inventoryManager;
    }

    public InventoryManager getInventoryManager() {
        return inventoryManager;
    }

    public void put(String key, Object value) {
        data.put(key, value);
    }

    public Object get(String key) {
        return data.get(key);
    }
}

// Result object
class ValidationResult {
    private final boolean valid;
    private final String message;
    private final List<String> warnings;

    public ValidationResult(boolean valid, String message) {
        this.valid = valid;
        this.message = message;
        this.warnings = new ArrayList<>();
    }

    public boolean isValid() {
        return valid;
    }

    public String getMessage() {
        return message;
    }

    public void addWarning(String warning) {
        warnings.add(warning);
    }

    public List<String> getWarnings() {
        return warnings;
    }
}

// Concrete Handler 1: Validate order basics
class BasicOrderValidationHandler extends OrderValidationHandler {
    @Override
    public ValidationResult validate(Order order, OrderValidationContext context) {
        logger.info("🔍 Chain Step 1: Validating basic order information...");

        if (order.items == null || order.items.isEmpty()) {
            return new ValidationResult(false, "Order must contain at least one item");
        }

        if (order.customerName == null || order.customerName.isBlank()) {
            return new ValidationResult(false, "Customer name is required");
        }

        if (order.shippingAddress == null || order.shippingAddress.isBlank()) {
            return new ValidationResult(false, "Shipping address is required");
        }

        logger.info("✓ Basic validation passed");
        return passToNext(order, context);
    }
}

// Concrete Handler 2: Validate inventory
class InventoryValidationHandler extends OrderValidationHandler {
    @Override
    public ValidationResult validate(Order order, OrderValidationContext context) {
        logger.info("🔍 Chain Step 2: Validating inventory availability...");

        InventoryManager inventory = context.getInventoryManager();
        ValidationResult result = new ValidationResult(true, "Inventory check passed");

        for (OrderItem item : order.items) {
            int available = inventory.getAvailableQuantity(item.product.sku);

            if (available < item.quantity) {
                return new ValidationResult(false,
                        "Insufficient stock for " + item.product.name +
                                " (need " + item.quantity + ", have " + available + ")");
            }

            // Add warning if stock is low
            if (available < item.quantity * 2) {
                result.addWarning("Low stock alert for " + item.product.name +
                        " (only " + available + " remaining)");
            }
        }

        logger.info("✓ Inventory validation passed");
        return passToNext(order, context);
    }
}

// Concrete Handler 3: Validate order value
class OrderValueValidationHandler extends OrderValidationHandler {
    private static final double MIN_ORDER_VALUE = 5.0;
    private static final double MAX_ORDER_VALUE = 10000.0;

    @Override
    public ValidationResult validate(Order order, OrderValidationContext context) {
        logger.info("🔍 Chain Step 3: Validating order value...");

        double total = order.total();

        if (total < MIN_ORDER_VALUE) {
            return new ValidationResult(false,
                    "Order total ($" + String.format("%.2f", total) +
                            ") is below minimum of $" + String.format("%.2f", MIN_ORDER_VALUE));
        }

        if (total > MAX_ORDER_VALUE) {
            return new ValidationResult(false,
                    "Order total ($" + String.format("%.2f", total) +
                            ") exceeds maximum of $" + String.format("%.2f", MAX_ORDER_VALUE));
        }

        ValidationResult result = new ValidationResult(true, "Order value validation passed");

        // Add warning for high-value orders
        if (total > 500.0) {
            result.addWarning("High-value order detected ($" +
                    String.format("%.2f", total) + ") - additional verification may be required");
        }

        logger.info("✓ Order value validation passed");
        return passToNext(order, context);
    }
}

// Concrete Handler 4: Validate product availability
class ProductAvailabilityHandler extends OrderValidationHandler {
    private final Set<String> discontinuedProducts = new HashSet<>(
            Arrays.asList("SKU-999", "SKU-888")
    );

    @Override
    public ValidationResult validate(Order order, OrderValidationContext context) {
        logger.info("🔍 Chain Step 4: Validating product availability...");

        ValidationResult result = new ValidationResult(true, "Product availability check passed");

        for (OrderItem item : order.items) {
            if (discontinuedProducts.contains(item.product.sku)) {
                return new ValidationResult(false,
                        "Product " + item.product.name + " has been discontinued");
            }

            // Check for quantity limits
            if (item.quantity > 50) {
                result.addWarning("Large quantity order for " + item.product.name +
                        " (" + item.quantity + " units) - bulk order processing may apply");
            }
        }

        logger.info("✓ Product availability validation passed");
        return passToNext(order, context);
    }
}

// Validation Chain Builder
class OrderValidationChainBuilder {
    public static OrderValidationHandler buildStandardChain(InventoryManager inventory) {
        OrderValidationHandler basicHandler = new BasicOrderValidationHandler();
        OrderValidationHandler inventoryHandler = new InventoryValidationHandler();
        OrderValidationHandler valueHandler = new OrderValueValidationHandler();
        OrderValidationHandler productHandler = new ProductAvailabilityHandler();

        basicHandler
                .setNext(inventoryHandler)
                .setNext(valueHandler)
                .setNext(productHandler);

        Logger.getInstance().info("📋 Validation chain built: Basic → Inventory → Value → Product");
        return basicHandler;
    }

    public static OrderValidationHandler buildQuickChain(InventoryManager inventory) {
        OrderValidationHandler basicHandler = new BasicOrderValidationHandler();
        OrderValidationHandler inventoryHandler = new InventoryValidationHandler();

        basicHandler.setNext(inventoryHandler);

        Logger.getInstance().info("📋 Quick validation chain built: Basic → Inventory");
        return basicHandler;
    }
}