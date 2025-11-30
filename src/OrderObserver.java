import java.util.*;
import java.time.*;
import java.time.format.DateTimeFormatter;

/**
 * BEHAVIORAL PATTERN: Observer
 *
 * Purpose: Defines a one-to-many dependency between objects so that when
 * one object changes state, all its dependents are notified automatically.
 *
 * Use case: Notify multiple systems/services when order status changes
 */

// Subject interface
interface OrderSubject {
    void attach(OrderObserver observer);
    void detach(OrderObserver observer);
    void notifyObservers(OrderEvent event);
}

// Observer interface
interface OrderObserver {
    void update(OrderEvent event);
    String getObserverName();
}

// Event object containing order state change information
class OrderEvent {
    private final String orderId;
    private final OrderStatus oldStatus;
    private final OrderStatus newStatus;
    private final LocalDateTime timestamp;
    private final Map<String, Object> metadata;

    public OrderEvent(String orderId, OrderStatus oldStatus, OrderStatus newStatus) {
        this.orderId = orderId;
        this.oldStatus = oldStatus;
        this.newStatus = newStatus;
        this.timestamp = LocalDateTime.now();
        this.metadata = new HashMap<>();
    }

    public String getOrderId() { return orderId; }
    public OrderStatus getOldStatus() { return oldStatus; }
    public OrderStatus getNewStatus() { return newStatus; }
    public LocalDateTime getTimestamp() { return timestamp; }

    public void addMetadata(String key, Object value) {
        metadata.put(key, value);
    }

    public Object getMetadata(String key) {
        return metadata.get(key);
    }

    @Override
    public String toString() {
        return String.format("OrderEvent[%s: %s → %s at %s]",
                orderId, oldStatus, newStatus,
                timestamp.format(DateTimeFormatter.ofPattern("HH:mm:ss")));
    }
}

// Order Status Enum
enum OrderStatus {
    CREATED("Order Created"),
    VALIDATED("Order Validated"),
    PAYMENT_PENDING("Payment Pending"),
    PAYMENT_CONFIRMED("Payment Confirmed"),
    PROCESSING("Processing"),
    READY_TO_SHIP("Ready to Ship"),
    SHIPPED("Shipped"),
    DELIVERED("Delivered"),
    CANCELLED("Cancelled"),
    FAILED("Failed");

    private final String description;

    OrderStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}

// Concrete Subject: Observable Order Tracker
class ObservableOrderTracker implements OrderSubject {
    private final List<OrderObserver> observers = new ArrayList<>();
    private final Map<String, OrderStatus> orderStatuses = new HashMap<>();
    private final Logger logger = Logger.getInstance();

    @Override
    public void attach(OrderObserver observer) {
        if (!observers.contains(observer)) {
            observers.add(observer);
            logger.info("👁️ Observer attached: " + observer.getObserverName());
        }
    }

    @Override
    public void detach(OrderObserver observer) {
        if (observers.remove(observer)) {
            logger.info("👁️ Observer detached: " + observer.getObserverName());
        }
    }

    @Override
    public void notifyObservers(OrderEvent event) {
        logger.info("📢 Notifying " + observers.size() + " observers about: " + event);
        for (OrderObserver observer : observers) {
            try {
                observer.update(event);
            } catch (Exception e) {
                logger.warn("Failed to notify observer " + observer.getObserverName() +
                        ": " + e.getMessage());
            }
        }
    }

    public void updateOrderStatus(String orderId, OrderStatus newStatus) {
        OrderStatus oldStatus = orderStatuses.getOrDefault(orderId, OrderStatus.CREATED);
        orderStatuses.put(orderId, newStatus);

        OrderEvent event = new OrderEvent(orderId, oldStatus, newStatus);
        notifyObservers(event);
    }

    public void updateOrderStatus(String orderId, OrderStatus newStatus,
                                  Map<String, Object> metadata) {
        OrderStatus oldStatus = orderStatuses.getOrDefault(orderId, OrderStatus.CREATED);
        orderStatuses.put(orderId, newStatus);

        OrderEvent event = new OrderEvent(orderId, oldStatus, newStatus);
        metadata.forEach(event::addMetadata);
        notifyObservers(event);
    }

    public OrderStatus getOrderStatus(String orderId) {
        return orderStatuses.getOrDefault(orderId, OrderStatus.CREATED);
    }

    public int getObserverCount() {
        return observers.size();
    }
}

// Concrete Observer 1: Email Notification System
class EmailNotificationObserver implements OrderObserver {
    private final Logger logger = Logger.getInstance();

    @Override
    public void update(OrderEvent event) {
        logger.info("📧 EMAIL: Sending notification for order " + event.getOrderId());

        switch (event.getNewStatus()) {
            case PAYMENT_CONFIRMED:
                sendEmail("Payment Confirmation",
                        "Your payment has been confirmed for order " + event.getOrderId());
                break;
            case SHIPPED:
                String trackingNumber = (String) event.getMetadata("trackingNumber");
                sendEmail("Order Shipped",
                        "Order " + event.getOrderId() + " has been shipped. " +
                                (trackingNumber != null ? "Tracking: " + trackingNumber : ""));
                break;
            case DELIVERED:
                sendEmail("Order Delivered",
                        "Order " + event.getOrderId() + " has been delivered!");
                break;
            case CANCELLED:
                sendEmail("Order Cancelled",
                        "Order " + event.getOrderId() + " has been cancelled.");
                break;
            default:
                // Other status changes don't trigger emails
                break;
        }
    }

    private void sendEmail(String subject, String body) {
        System.out.println("   ✉️  Email: [" + subject + "] " + body);
    }

    @Override
    public String getObserverName() {
        return "EmailNotificationObserver";
    }
}

// Concrete Observer 2: SMS Notification System
class SMSNotificationObserver implements OrderObserver {
    private final Logger logger = Logger.getInstance();
    private final Set<OrderStatus> smsEnabledStatuses = EnumSet.of(
            OrderStatus.PAYMENT_CONFIRMED,
            OrderStatus.SHIPPED,
            OrderStatus.DELIVERED
    );

    @Override
    public void update(OrderEvent event) {
        if (smsEnabledStatuses.contains(event.getNewStatus())) {
            logger.info("📱 SMS: Sending notification for order " + event.getOrderId());
            sendSMS("Order " + event.getOrderId() + ": " +
                    event.getNewStatus().getDescription());
        }
    }

    private void sendSMS(String message) {
        System.out.println("   📲 SMS: " + message);
    }

    @Override
    public String getObserverName() {
        return "SMSNotificationObserver";
    }
}

// Concrete Observer 3: Analytics System
class AnalyticsObserver implements OrderObserver {
    private final Logger logger = Logger.getInstance();
    private final Map<OrderStatus, Integer> statusCounts = new HashMap<>();

    @Override
    public void update(OrderEvent event) {
        logger.info("📊 ANALYTICS: Recording status change for order " + event.getOrderId());

        OrderStatus status = event.getNewStatus();
        statusCounts.put(status, statusCounts.getOrDefault(status, 0) + 1);

        System.out.println("   📈 Analytics: Status '" + status.getDescription() +
                "' count: " + statusCounts.get(status));

        // Record metadata if available
        if (event.getMetadata("totalAmount") != null) {
            System.out.println("   💰 Order value: $" + event.getMetadata("totalAmount"));
        }
    }

    public void printReport() {
        System.out.println("\n=== ANALYTICS REPORT ===");
        statusCounts.forEach((status, count) ->
                System.out.println(status.getDescription() + ": " + count + " orders"));
        System.out.println("========================\n");
    }

    @Override
    public String getObserverName() {
        return "AnalyticsObserver";
    }
}

// Concrete Observer 4: Inventory Update System
class InventoryUpdateObserver implements OrderObserver {
    private final Logger logger = Logger.getInstance();
    private final InventoryManager inventoryManager;

    public InventoryUpdateObserver(InventoryManager inventoryManager) {
        this.inventoryManager = inventoryManager;
    }

    @Override
    public void update(OrderEvent event) {
        logger.info("📦 INVENTORY: Processing status change for order " + event.getOrderId());

        // Handle inventory updates based on status transitions
        if (event.getNewStatus() == OrderStatus.CANCELLED &&
                (event.getOldStatus() == OrderStatus.PAYMENT_CONFIRMED ||
                        event.getOldStatus() == OrderStatus.PROCESSING)) {

            System.out.println("   ↩️  Inventory: Order cancelled, stock will be released");
            // In real implementation, would restore inventory here
        } else if (event.getNewStatus() == OrderStatus.SHIPPED) {
            System.out.println("   ✓ Inventory: Stock committed for shipped order");
        }
    }

    @Override
    public String getObserverName() {
        return "InventoryUpdateObserver";
    }
}

// Concrete Observer 5: Audit Log System
class AuditLogObserver implements OrderObserver {
    private final Logger logger = Logger.getInstance();
    private final List<String> auditLog = new ArrayList<>();

    @Override
    public void update(OrderEvent event) {
        String logEntry = String.format("[%s] Order %s: %s → %s",
                event.getTimestamp().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                event.getOrderId(),
                event.getOldStatus(),
                event.getNewStatus());

        auditLog.add(logEntry);
        logger.info("📝 AUDIT: " + logEntry);
        System.out.println("   📋 Audit log entry created");
    }

    public void printAuditLog() {
        System.out.println("\n=== AUDIT LOG ===");
        auditLog.forEach(System.out::println);
        System.out.println("=================\n");
    }

    @Override
    public String getObserverName() {
        return "AuditLogObserver";
    }
}