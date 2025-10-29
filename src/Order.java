import java.util.*;

class Order {
    final String orderId;
    final String customerName;
    final String shippingAddress;
    final List<OrderItem> items;
    final double discount;
    final String notes;

    private Order(Builder b) {
        if (b.orderId == null || b.orderId.isBlank()) throw new IllegalArgumentException("orderId required");
        if (b.customerName == null || b.customerName.isBlank()) throw new IllegalArgumentException("customerName required");
        if (b.shippingAddress == null || b.shippingAddress.isBlank()) throw new IllegalArgumentException("shippingAddress required");
        if (b.items.isEmpty()) throw new IllegalArgumentException("At least one item required");
        this.orderId = b.orderId;
        this.customerName = b.customerName;
        this.shippingAddress = b.shippingAddress;
        this.items = List.copyOf(b.items);
        this.discount = b.discount;
        this.notes = b.notes;
    }

    public double subtotal() { return items.stream().mapToDouble(OrderItem::lineTotal).sum(); }
    public double total() { return Math.max(0.0, subtotal() - discount); }

    static class Builder {
        private String orderId;
        private String customerName;
        private String shippingAddress;
        private final List<OrderItem> items = new ArrayList<>();
        private double discount = 0.0;
        private String notes = "";

        public Builder orderId(String orderId) { this.orderId = orderId; return this; }
        public Builder customer(String name) { this.customerName = name; return this; }
        public Builder shipTo(String address) { this.shippingAddress = address; return this; }
        public Builder addItem(Product p, int qty) { this.items.add(new OrderItem(p, qty)); return this; }
        public Builder discount(double amount) { this.discount = amount; return this; }
        public Builder notes(String notes) { this.notes = notes; return this; }
        public boolean hasItems() { return !items.isEmpty(); }

        public Order build() { return new Order(this); }
    }

    @Override public String toString() {
        StringBuilder sb = new StringBuilder("Order " + orderId + " for " + customerName + "\n");
        for (OrderItem it : items) sb.append("  - ").append(it).append("\n");
        sb.append("Subtotal: ").append(String.format("%.2f", subtotal())).append("\n");
        sb.append("Discount: ").append(String.format("%.2f", discount)).append("\n");
        sb.append("Total:    ").append(String.format("%.2f", total())).append("\n");
        sb.append("Ship To:  ").append(shippingAddress).append("\n");
        if (notes != null && !notes.isBlank()) sb.append("Notes:    ").append(notes).append("\n");
        return sb.toString();
    }
}

// ========== Payments ==========
interface PaymentProcessor {
    boolean process(Order order);
    String name();
}

class CreditCardProcessor implements PaymentProcessor {
    public boolean process(Order order) {
        Logger.getInstance().info("Charging credit card for $" + String.format("%.2f", order.total()));
        return true;
    }
    public String name() { return "CreditCard"; }
}

class PaypalProcessor implements PaymentProcessor {
    public boolean process(Order order) {
        Logger.getInstance().info("Processing PayPal payment of $" + String.format("%.2f", order.total()));
        return true;
    }
    public String name() { return "PayPal"; }
}
