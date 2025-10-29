import java.util.*;
import java.time.*;
import java.time.format.DateTimeFormatter;

public class Main {
    // I/O + logging
    private static final Scanner SC = new Scanner(System.in);
    private static final Logger LOG = Logger.getInstance();

    // Catalog (immutable)
    private static final Map<Integer, Product> CATALOG = new LinkedHashMap<>();
    static {
        CATALOG.put(1, new Product("SKU-1", "Wireless Mouse", 19.99));
        CATALOG.put(2, new Product("SKU-2", "Mechanical Keyboard", 89.50));
        CATALOG.put(3, new Product("SKU-3", "Desk Pad", 24.00));
        CATALOG.put(4, new Product("SKU-4", "USB-C Hub 6-in-1", 39.95));
        CATALOG.put(5, new Product("SKU-5", "Noise-canceling Headset", 129.00));
    }

    // State (kept simple & explicit)
    private static final List<OrderItem> CART = new ArrayList<>();
    private static String customerName = "";
    private static String shippingAddress = "";
    private static double discount = 0.0;
    private static String notes = "";
    private static String paymentChoice = "paypal"; // card | paypal
    private static String carrierChoice = "dhl";    // dhl | fedex

    public static void main(String[] args) {
        LOG.info("Welcome! (Singleton, Builder, Factory Method, Abstract Factory demo)\n");
        boolean running = true;
        while (running) {
            printMainMenu();
            switch (prompt("Select an option: ").trim()) {
                case "1": showCatalog(); break;
                case "2": addItemFlow(); break;
                case "3": cartManager(); break;
                case "4": setCustomer(); break;
                case "5": setAddress(); break;
                case "6": setDiscount(); break;
                case "7": setNotes(); break;
                case "8": choosePayment(); break;
                case "9": chooseCarrier(); break;
                case "10": reviewDraft(); break;
                case "11": checkout(); break;
                case "0": running = false; break;
                default: System.out.println("Invalid option. Try again.");
            }
        }
        LOG.info("Goodbye!");
    }

    // ---------- MENUS ----------

    private static void printMainMenu() {
        System.out.println("\n==================== MAIN MENU ====================");
        System.out.println("Cart: " + (CART.isEmpty() ? "(empty)" : CART.size() + " item(s)") +
                " | Customer: " + mark(customerName) +
                " | Address: " + mark(shippingAddress) +
                " | Pay: " + paymentChoice.toUpperCase() +
                " | Ship: " + carrierChoice.toUpperCase());
        System.out.println("---------------------------------------------------");
        System.out.println(" 1) View product catalog");
        System.out.println(" 2) Add item to cart");
        System.out.println(" 3) View/modify cart");
        System.out.println(" 4) Set customer name");
        System.out.println(" 5) Set shipping address");
        System.out.println(" 6) Set discount");
        System.out.println(" 7) Set notes");
        System.out.println(" 8) Choose payment method");
        System.out.println(" 9) Choose carrier");
        System.out.println("10) Review draft order");
        System.out.println("11) CHECKOUT");
        System.out.println(" 0) Exit");
        System.out.println("===================================================");
    }

    private static void showCatalog() {
        System.out.println("\n-- Product Catalog --");
        CATALOG.forEach((id, p) -> System.out.println(" " + id + ") " + p));
    }

    private static void addItemFlow() {
        showCatalog();
        int id = promptInt("Enter product number: ");
        Product p = CATALOG.get(id);
        if (p == null) { System.out.println("No such product."); return; }
        int qty = promptInt("Quantity: ");
        if (qty <= 0) { System.out.println("Quantity must be > 0."); return; }
        CART.add(new OrderItem(p, qty));
        LOG.info("Added: " + p.name + " x" + qty);
    }

    private static void cartManager() {
        while (true) {
            printCart();
            System.out.println("Cart options:");
            System.out.println(" 1) Update quantity");
            System.out.println(" 2) Remove item");
            System.out.println(" 3) Clear cart");
            System.out.println(" 0) Back");
            String c = prompt("Choose: ").trim();
            switch (c) {
                case "1": updateQty(); break;
                case "2": removeItem(); break;
                case "3": CART.clear(); LOG.info("Cart cleared."); break;
                case "0": return;
                default: System.out.println("Invalid option.");
            }
        }
    }

    // ---------- STATE SETUP ----------

    private static void setCustomer() {
        customerName = promptNonEmpty("Customer full name: ");
        LOG.info("Customer set.");
    }

    private static void setAddress() {
        shippingAddress = promptNonEmpty("Shipping address: ");
        LOG.info("Address set.");
    }

    private static void setDiscount() {
        double d = promptDouble("Discount amount (>= 0): ");
        if (d < 0) { System.out.println("Discount cannot be negative."); return; }
        discount = d;
        LOG.info("Discount set to $" + String.format("%.2f", discount));
    }

    private static void setNotes() {
        notes = prompt("Notes (optional): ");
        LOG.info("Notes updated.");
    }

    private static void choosePayment() {
        System.out.println("Payment methods: 1) Card  2) PayPal");
        String c = prompt("Choose (1/2): ").trim();
        paymentChoice = "1".equals(c) ? "card" : "paypal";
        LOG.info("Payment: " + paymentChoice.toUpperCase());
    }

    private static void chooseCarrier() {
        System.out.println("Carriers: 1) DHL  2) FedEx");
        String c = prompt("Choose (1/2): ").trim();
        carrierChoice = "2".equals(c) ? "fedex" : "dhl";
        LOG.info("Carrier: " + carrierChoice.toUpperCase());
    }

    // ---------- REVIEW + CHECKOUT ----------

    private static void reviewDraft() {
        System.out.println("\n=== DRAFT ORDER REVIEW ===");
        printCart();
        double subtotal = cartSubtotal();
        System.out.println("Subtotal: $" + fmt(subtotal));
        System.out.println("Discount: $" + fmt(discount));
        System.out.println("Total:    $" + fmt(Math.max(0.0, subtotal - discount)));
        System.out.println("Customer: " + mark(customerName));
        System.out.println("Address:  " + mark(shippingAddress));
        System.out.println("Payment:  " + paymentChoice.toUpperCase());
        System.out.println("Carrier:  " + carrierChoice.toUpperCase());
        System.out.println("Notes:    " + (notes == null ? "" : notes));
        System.out.println("===========================================");
        missingHints();
    }

    private static void checkout() {
        // Preconditions
        if (CART.isEmpty()) { System.out.println("Cart is empty."); return; }
        if (blank(customerName)) { System.out.println("Set customer name first."); return; }
        if (blank(shippingAddress)) { System.out.println("Set shipping address first."); return; }

        // Build a fresh Order via Builder so we never double-add items
        Order.Builder b = new Order.Builder()
                .orderId(genOrderId())
                .customer(customerName)
                .shipTo(shippingAddress)
                .discount(discount)
                .notes(notes == null ? "" : notes);

        for (OrderItem it : CART) {
            b.addItem(it.product, it.quantity);
        }

        Order order;
        try {
            order = b.build();
        } catch (Exception ex) {
            System.out.println("Could not build order: " + ex.getMessage());
            return;
        }

        // Show final summary
        System.out.println("\n=== ORDER SUMMARY ===");
        System.out.println(order);

        // Pay via Factory Method
        PaymentProcessorCreator creator = "card".equalsIgnoreCase(paymentChoice)
                ? new CreditCardProcessorCreator()
                : new PaypalProcessorCreator();

        boolean paid = creator.pay(order);
        if (!paid) { LOG.warn("Payment failed."); return; }
        LOG.info("Payment successful.");

        // Shipping via Abstract Factory
        ShippingFactory shipFactory = "fedex".equalsIgnoreCase(carrierChoice)
                ? new FedExFactory()
                : new DHLFactory();

        ShippingLabel label = shipFactory.createLabel(order.shippingAddress);
        PackageBox box = shipFactory.createBox();

        System.out.println("=== SHIPPING PREP ===");
        System.out.println("Label: " + label.render());
        System.out.println("Box:   " + box.spec());

        LOG.info("Order complete.");
        resetState();
    }

    // ---------- CART OPS ----------

    private static void printCart() {
        System.out.println("\n-- Cart --");
        if (CART.isEmpty()) {
            System.out.println("(empty)");
            return;
        }
        int i = 1;
        for (OrderItem it : CART) {
            System.out.println(" " + (i++) + ") " + it);
        }
    }

    private static void updateQty() {
        if (CART.isEmpty()) { System.out.println("Cart is empty."); return; }
        int idx = promptInt("Item number: ");
        if (idx < 1 || idx > CART.size()) { System.out.println("Invalid item."); return; }
        int q = promptInt("New quantity (>=1): ");
        if (q < 1) { System.out.println("Must be >= 1."); return; }
        OrderItem old = CART.get(idx - 1);
        CART.set(idx - 1, new OrderItem(old.product, q));
        LOG.info("Updated qty → " + old.product.name + " x" + q);
    }

    private static void removeItem() {
        if (CART.isEmpty()) { System.out.println("Cart is empty."); return; }
        int idx = promptInt("Item number to remove: ");
        if (idx < 1 || idx > CART.size()) { System.out.println("Invalid item."); return; }
        OrderItem removed = CART.remove(idx - 1);
        LOG.info("Removed: " + removed.product.name);
    }

    private static double cartSubtotal() {
        double sum = 0.0;
        for (OrderItem it : CART) sum += it.lineTotal();
        return sum;
    }

    private static void resetState() {
        CART.clear();
        customerName = "";
        shippingAddress = "";
        discount = 0.0;
        notes = "";
        paymentChoice = "paypal";
        carrierChoice = "dhl";
        LOG.info("State reset for a new order.");
    }

    private static void missingHints() {
        List<String> misses = new ArrayList<>();
        if (CART.isEmpty()) misses.add("add items");
        if (blank(customerName)) misses.add("set customer");
        if (blank(shippingAddress)) misses.add("set address");
        if (!misses.isEmpty()) {
            System.out.println("Next steps: " + String.join(", ", misses) + ".");
        }
    }

    // ---------- UTIL ----------

    private static String genOrderId() {
        return "ORD-" + DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss").format(LocalDateTime.now());
    }

    private static String mark(String s) {
        return blank(s) ? "(not set)" : s;
    }

    private static boolean blank(String s) {
        return s == null || s.isBlank();
    }

    private static String fmt(double v) {
        return String.format("%.2f", v);
    }

    private static String prompt(String msg) {
        System.out.print(msg);
        return SC.nextLine();
    }

    private static String promptNonEmpty(String msg) {
        while (true) {
            String s = prompt(msg);
            if (!blank(s)) return s.trim();
            System.out.println("Please enter a non-empty value.");
        }
    }

    private static int promptInt(String msg) {
        while (true) {
            String s = prompt(msg);
            try { return Integer.parseInt(s.trim()); }
            catch (Exception e) { System.out.println("Enter a valid integer."); }
        }
    }

    private static double promptDouble(String msg) {
        while (true) {
            String s = prompt(msg);
            try { return Double.parseDouble(s.trim()); }
            catch (Exception e) { System.out.println("Enter a valid number."); }
        }
    }
}
