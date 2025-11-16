import java.util.*;

class NotificationService {
    private final Logger logger = Logger.getInstance();

    public void sendOrderConfirmation(Order order) {
        logger.info("📧 Sending order confirmation email to " + order.customerName);
        System.out.println("   Email sent: Order #" + order.orderId + " confirmed");
    }

    public void sendShippingNotification(Order order, ShippingLabel label) {
        logger.info("📧 Sending shipping notification to " + order.customerName);
        System.out.println("   Email sent: Your package is being prepared");
    }

    public void sendPaymentReceipt(Order order, double amount) {
        logger.info("📧 Sending payment receipt to " + order.customerName);
        System.out.println("   Email sent: Payment of $" + String.format("%.2f", amount) + " received");
    }
}

class OrderTrackingSystem {
    private final Map<String, String> orderStatuses = new HashMap<>();
    private final Logger logger = Logger.getInstance();

    public void createTracking(String orderId) {
        orderStatuses.put(orderId, "CREATED");
        logger.info("📦 Tracking created for order " + orderId);
    }

    public void updateStatus(String orderId, String status) {
        orderStatuses.put(orderId, status);
        logger.info("📦 Order " + orderId + " status: " + status);
    }

    public String getStatus(String orderId) {
        return orderStatuses.getOrDefault(orderId, "UNKNOWN");
    }
}

class CheckoutFacade {
    private final InventoryManager inventoryManager;
    private final NotificationService notificationService;
    private final OrderTrackingSystem trackingSystem;
    private final Logger logger;

    public CheckoutFacade(InventoryManager inventoryManager) {
        this.inventoryManager = inventoryManager;
        this.notificationService = new NotificationService();
        this.trackingSystem = new OrderTrackingSystem();
        this.logger = Logger.getInstance();
    }

    public CheckoutResult processCheckout(
            Order order,
            OrderComponent enhancedOrder,
            PaymentProcessorCreator paymentCreator,
            ShippingFactory shippingFactory) {

        logger.info("=== STARTING CHECKOUT FACADE ===");
        CheckoutResult result = new CheckoutResult(order.orderId);

        try {
            logger.info("Step 1/7: Validating inventory...");
            if (!validateInventory(order)) {
                result.setFailure("Insufficient inventory");
                return result;
            }

            logger.info("Step 2/7: Reserving stock...");
            reserveInventory(order);

            logger.info("Step 3/7: Processing payment...");
            double finalAmount = enhancedOrder.calculateTotal();
            if (!processPayment(paymentCreator, order, finalAmount)) {
                releaseInventory(order);
                result.setFailure("Payment failed");
                return result;
            }
            result.setAmountPaid(finalAmount);

            logger.info("Step 4/7: Preparing shipping...");
            ShippingLabel label = shippingFactory.createLabel(order.shippingAddress);
            PackageBox box = shippingFactory.createBox();
            result.setShippingDetails(label, box);

            logger.info("Step 5/7: Creating tracking...");
            trackingSystem.createTracking(order.orderId);
            trackingSystem.updateStatus(order.orderId, "PAYMENT_CONFIRMED");

            logger.info("Step 6/7: Sending notifications...");
            notificationService.sendOrderConfirmation(order);
            notificationService.sendPaymentReceipt(order, finalAmount);
            notificationService.sendShippingNotification(order, label);

            logger.info("Step 7/7: Finalizing order...");
            trackingSystem.updateStatus(order.orderId, "READY_TO_SHIP");

            result.setSuccess(enhancedOrder.getDescription());
            logger.info("=== CHECKOUT COMPLETE ===");

        } catch (Exception e) {
            logger.warn("Checkout failed: " + e.getMessage());
            try {
                releaseInventory(order);
            } catch (Exception rollbackError) {
                logger.warn("Rollback failed: " + rollbackError.getMessage());
            }
            result.setFailure(e.getMessage());
        }

        return result;
    }

    public boolean checkInventoryAvailability(List<OrderItem> items) {
        for (OrderItem item : items) {
            int available = inventoryManager.getAvailableQuantity(item.product.sku);
            if (available < item.quantity) {
                logger.warn("Low stock: " + item.product.name +
                        " (need " + item.quantity + ", have " + available + ")");
                return false;
            }
        }
        return true;
    }


    public void displayInventoryStatus() {
        if (inventoryManager instanceof InventoryAdapter) {
            ((InventoryAdapter) inventoryManager).showInventoryReport();
        }
    }

    private boolean validateInventory(Order order) {
        StockValidator validator = new StockValidator(inventoryManager);
        return validator.validateOrder(order);
    }

    private void reserveInventory(Order order) {
        StockValidator validator = new StockValidator(inventoryManager);
        validator.reserveOrderStock(order);
    }

    private void releaseInventory(Order order) {
        for (OrderItem item : order.items) {
            inventoryManager.releaseStock(item.product.sku, item.quantity);
        }
    }

    private boolean processPayment(PaymentProcessorCreator creator, Order order, double amount) {
        try {
            return creator.pay(order);
        } catch (Exception e) {
            logger.warn("Payment processing error: " + e.getMessage());
            return false;
        }
    }
}

class CheckoutResult {
    private final String orderId;
    private boolean success;
    private String message;
    private double amountPaid;
    private ShippingLabel shippingLabel;
    private PackageBox packageBox;

    public CheckoutResult(String orderId) {
        this.orderId = orderId;
        this.success = false;
    }

    public void setSuccess(String enhancements) {
        this.success = true;
        this.message = "Order processed successfully with: " + enhancements;
    }

    public void setFailure(String reason) {
        this.success = false;
        this.message = "Checkout failed: " + reason;
    }

    public void setAmountPaid(double amount) {
        this.amountPaid = amount;
    }

    public void setShippingDetails(ShippingLabel label, PackageBox box) {
        this.shippingLabel = label;
        this.packageBox = box;
    }

    public boolean isSuccess() {
        return success;
    }

    public void printSummary() {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("CHECKOUT RESULT - Order #" + orderId);
        System.out.println("=".repeat(60));
        System.out.println("Status: " + (success ? "✅ SUCCESS" : "❌ FAILED"));
        System.out.println("Message: " + message);

        if (success) {
            System.out.println("Amount Paid: $" + String.format("%.2f", amountPaid));
            System.out.println("\nShipping Details:");
            System.out.println("  Label: " + shippingLabel.render());
            System.out.println("  Box: " + packageBox.spec());
        }

        System.out.println("=".repeat(60) + "\n");
    }
}