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

    // STRUCTURAL PATTERNS: Initialize subsystems
    private static final LegacyInventorySystem legacyInventory = new LegacyInventorySystem();
    private static final InventoryAdapter inventoryAdapter = new InventoryAdapter(legacyInventory);
    private static final CheckoutFacade checkoutFacade = new CheckoutFacade(inventoryAdapter);

    // State (kept simple & explicit)
    private static final List<OrderItem> CART = new ArrayList<>();
    private static String customerName = "";
    private static String shippingAddress = "";
    private static double discount = 0.0;
    private static String notes = "";
    private static String paymentChoice = "paypal"; // card | paypal
    private static String carrierChoice = "dhl";    // dhl | fedex

    // NEW: Order enhancements (Decorator Pattern)
    private static boolean addGiftWrap = false;
    private static boolean addInsurance = false;
    private static boolean addExpress = false;
    private static boolean addPrioritySupport = false;

    public static void main(String[] args) {
        LOG.info("Welcome to E-Commerce System!");
        LOG.info("Demonstrating: Singleton, Builder, Factory Method, Abstract Factory,");
        LOG.info("               Decorator, Adapter, Facade patterns\n");

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
                case "10": manageEnhancements(); break;  // NEW: Decorator Pattern
                case "11": checkInventory(); break;      // NEW: Adapter Pattern
                case "12": reviewDraft(); break;
                case "13": checkout(); break;            // UPDATED: Uses Facade
                case "0": running = false; break;
                default: System.out.println("Invalid option. Try again.");
            }
        }
        LOG.info("Goodbye!");
    }

    // ---------- MENUS ----------

    private static void printMainMenu() {
        System.out.println("\n" + "=".repeat(70));
        System.out.println("                      E-COMMERCE SYSTEM MENU");
        System.out.println("=".repeat(70));

        System.out.println("Cart: " + (CART.isEmpty() ? "(empty)" : CART.size() + " item(s)") +
                " | Customer: " + mark(customerName) +
                " | Address: " + mark(shippingAddress));
        System.out.println("Payment: " + paymentChoice.toUpperCase() +
                " | Shipping: " + carrierChoice.toUpperCase());

        // Show active enhancements (Decorator)
        List<String> enhancements = new ArrayList<>();
        if (addGiftWrap) enhancements.add("Gift");
        if (addInsurance) enhancements.add("Insurance");
        if (addExpress) enhancements.add("Express");
        if (addPrioritySupport) enhancements.add("Support");
        if (!enhancements.isEmpty()) {
            System.out.println("Enhancements: " + String.join(", ", enhancements));
        }

        System.out.println("-".repeat(70));
        System.out.println(" 1) View product catalog");
        System.out.println(" 2) Add item to cart");
        System.out.println(" 3) View/modify cart");
        System.out.println(" 4) Set customer name");
        System.out.println(" 5) Set shipping address");
        System.out.println(" 6) Set discount");
        System.out.println(" 7) Set notes");
        System.out.println(" 8) Choose payment method");
        System.out.println(" 9) Choose carrier");
        System.out.println("10) Manage order enhancements (DECORATOR)");
        System.out.println("11) Check inventory status (ADAPTER)");
        System.out.println("12) Review draft order");
        System.out.println("13) CHECKOUT (FACADE)");
        System.out.println(" 0) Exit");
        System.out.println("=".repeat(70));
    }

    private static void showCatalog() {
        System.out.println("\n-- Product Catalog --");
        CATALOG.forEach((id, p) -> {
            int stock = inventoryAdapter.getAvailableQuantity(p.sku);
            System.out.println(" " + id + ") " + p + " [Stock: " + stock + "]");
        });
    }

    private static void addItemFlow() {
        showCatalog();
        int id = promptInt("Enter product number: ");
        Product p = CATALOG.get(id);
        if (p == null) { System.out.println("No such product."); return; }

        // Check stock availability (Adapter Pattern)
        int available = inventoryAdapter.getAvailableQuantity(p.sku);
        System.out.println("Available stock: " + available);

        int qty = promptInt("Quantity: ");
        if (qty <= 0) { System.out.println("Quantity must be > 0."); return; }

        if (qty > available) {
            System.out.println("Insufficient stock! Only " + available + " available.");
            return;
        }

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

    // ---------- NEW: DECORATOR PATTERN - Order Enhancements ----------

    private static void manageEnhancements() {
        while (true) {
            System.out.println("\n=== ORDER ENHANCEMENTS (Decorator Pattern) ===");
            System.out.println("Add optional features to your order:");
            System.out.println(" 1) " + (addGiftWrap ? "✅" : "☐") + " Gift Wrapping (+$5.99)");
            System.out.println(" 2) " + (addInsurance ? "✅" : "☐") + " Shipping Insurance (+2% of order)");
            System.out.println(" 3) " + (addExpress ? "✅" : "☐") + " Express Processing (+$9.99)");
            System.out.println(" 4) " + (addPrioritySupport ? "✅" : "☐") + " Priority Support (+$3.99)");
            System.out.println(" 5) Clear all enhancements");
            System.out.println(" 0) Back");

            String c = prompt("Toggle enhancement: ").trim();
            switch (c) {
                case "1": addGiftWrap = !addGiftWrap; break;
                case "2": addInsurance = !addInsurance; break;
                case "3": addExpress = !addExpress; break;
                case "4": addPrioritySupport = !addPrioritySupport; break;
                case "5":
                    addGiftWrap = false;
                    addInsurance = false;
                    addExpress = false;
                    addPrioritySupport = false;
                    LOG.info("All enhancements cleared");
                    break;
                case "0": return;
                default: System.out.println("Invalid option.");
            }
        }
    }

    // ---------- NEW: ADAPTER PATTERN - Inventory Check ----------

    private static void checkInventory() {
        System.out.println("\n=== INVENTORY CHECK (via Adapter Pattern) ===");
        LOG.info("Checking inventory through adapted legacy system...");

        inventoryAdapter.showInventoryReport();

        if (!CART.isEmpty()) {
            System.out.println("Checking availability for current cart:");
            boolean allAvailable = checkoutFacade.checkInventoryAvailability(CART);
            if (allAvailable) {
                System.out.println("All cart items are in stock!");
            } else {
                System.out.println("Some items have insufficient stock!");
            }
        }

        prompt("\nPress Enter to continue...");
    }

    // ---------- REVIEW + CHECKOUT ----------

    private static void reviewDraft() {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("              DRAFT ORDER REVIEW");
        System.out.println("=".repeat(60));

        printCart();
        double subtotal = cartSubtotal();
        System.out.println("Subtotal: $" + fmt(subtotal));
        System.out.println("Discount: $" + fmt(discount));
        System.out.println("Base Total: $" + fmt(Math.max(0.0, subtotal - discount)));

        if (hasAnyEnhancements()) {
            System.out.println("\n--- Order Enhancements ---");
            if (addGiftWrap) System.out.println(" Gift Wrapping: +$5.99");
            if (addInsurance) {
                double fee = subtotal * 0.02;
                System.out.println(" Shipping Insurance (2%): +$" + fmt(fee));
            }
            if (addExpress) System.out.println("  ⚡ Express Processing: +$9.99");
            if (addPrioritySupport) System.out.println("Priority Support: +$3.99");

            double enhancedTotal = calculateEnhancedTotal(subtotal - discount);
            System.out.println("\nFINAL TOTAL (with enhancements): $" + fmt(enhancedTotal));
        }

        System.out.println("\n--- Order Details ---");
        System.out.println("Customer: " + mark(customerName));
        System.out.println("Address:  " + mark(shippingAddress));
        System.out.println("Payment:  " + paymentChoice.toUpperCase());
        System.out.println("Carrier:  " + carrierChoice.toUpperCase());
        System.out.println("Notes:    " + (notes == null ? "" : notes));
        System.out.println("=".repeat(60));

        missingHints();
    }

    private static void checkout() {
        // Preconditions
        if (CART.isEmpty()) { System.out.println("Cart is empty."); return; }
        if (blank(customerName)) { System.out.println("Set customer name first."); return; }
        if (blank(shippingAddress)) { System.out.println("Set shipping address first."); return; }

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

        OrderComponent enhancedOrder = new BasicOrderComponent(order);

        if (addGiftWrap) {
            enhancedOrder = new GiftWrapDecorator(enhancedOrder);
            LOG.info("Applied: Gift Wrapping");
        }
        if (addInsurance) {
            enhancedOrder = new InsuranceDecorator(enhancedOrder);
            LOG.info("Applied: Shipping Insurance");
        }
        if (addExpress) {
            enhancedOrder = new ExpressProcessingDecorator(enhancedOrder);
            LOG.info("Applied: Express Processing");
        }
        if (addPrioritySupport) {
            enhancedOrder = new PrioritySupportDecorator(enhancedOrder);
            LOG.info("Applied: Priority Support");
        }

        System.out.println("\n" + "=".repeat(60));
        System.out.println("              FINAL ORDER SUMMARY");
        System.out.println("=".repeat(60));
        System.out.println(order);
        System.out.println("Enhancement Details: " + enhancedOrder.getDescription());
        System.out.println("FINAL TOTAL: $" + String.format("%.2f", enhancedOrder.calculateTotal()));
        System.out.println("=".repeat(60));

        String confirm = prompt("\nProceed with checkout? (yes/no): ").trim().toLowerCase();
        if (!confirm.equals("yes") && !confirm.equals("y")) {
            System.out.println("Checkout cancelled.");
            return;
        }

        PaymentProcessorCreator paymentCreator = "card".equalsIgnoreCase(paymentChoice)
                ? new CreditCardProcessorCreator()
                : new PaypalProcessorCreator();

        ShippingFactory shipFactory = "fedex".equalsIgnoreCase(carrierChoice)
                ? new FedExFactory()
                : new DHLFactory();

        LOG.info("\nUsing FACADE PATTERN to process checkout...\n");
        CheckoutResult result = checkoutFacade.processCheckout(
                order,
                enhancedOrder,
                paymentCreator,
                shipFactory
        );

        result.printSummary();

        if (result.isSuccess()) {
            System.out.println("\n📦 Updated inventory:");
            inventoryAdapter.showInventoryReport();

            resetState();
        }
    }

    // ---------- CART OPS ----------

    private static void printCart() {
        System.out.println("\n-- Shopping Cart --");
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
        addGiftWrap = false;
        addInsurance = false;
        addExpress = false;
        addPrioritySupport = false;
        LOG.info("State reset for a new order.");
    }

    private static void missingHints() {
        List<String> misses = new ArrayList<>();
        if (CART.isEmpty()) misses.add("add items");
        if (blank(customerName)) misses.add("set customer");
        if (blank(shippingAddress)) misses.add("set address");
        if (!misses.isEmpty()) {
            System.out.println("\n⚠️  Next steps: " + String.join(", ", misses) + ".");
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

    private static boolean hasAnyEnhancements() {
        return addGiftWrap || addInsurance || addExpress || addPrioritySupport;
    }

    private static double calculateEnhancedTotal(double baseTotal) {
        double total = baseTotal;
        if (addGiftWrap) total += 5.99;
        if (addInsurance) total += baseTotal * 0.02;
        if (addExpress) total += 9.99;
        if (addPrioritySupport) total += 3.99;
        return total;
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