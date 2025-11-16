interface InventoryManager {
    boolean checkStock(String sku, int quantity);
    void reserveStock(String sku, int quantity);
    void releaseStock(String sku, int quantity);
    int getAvailableQuantity(String sku);
}

class LegacyInventorySystem {
    private final java.util.Map<String, Integer> stockLevels = new java.util.HashMap<>();

    public LegacyInventorySystem() {
        stockLevels.put("SKU-1", 50);
        stockLevels.put("SKU-2", 30);
        stockLevels.put("SKU-3", 100);
        stockLevels.put("SKU-4", 45);
        stockLevels.put("SKU-5", 25);
    }

    public int queryStockLevel(String productCode) {
        return stockLevels.getOrDefault(productCode, 0);
    }

    public boolean decrementStock(String productCode, int amount) {
        int current = queryStockLevel(productCode);
        if (current >= amount) {
            stockLevels.put(productCode, current - amount);
            System.out.println("[LEGACY SYSTEM] Decremented " + productCode + " by " + amount);
            return true;
        }
        return false;
    }

    public void incrementStock(String productCode, int amount) {
        int current = queryStockLevel(productCode);
        stockLevels.put(productCode, current + amount);
        System.out.println("[LEGACY SYSTEM] Incremented " + productCode + " by " + amount);
    }

    public void printInventoryReport() {
        System.out.println("\n=== LEGACY INVENTORY REPORT ===");
        stockLevels.forEach((sku, qty) ->
                System.out.println(sku + ": " + qty + " units"));
        System.out.println("================================\n");
    }
}

class InventoryAdapter implements InventoryManager {
    private final LegacyInventorySystem legacySystem;

    public InventoryAdapter(LegacyInventorySystem legacySystem) {
        this.legacySystem = legacySystem;
        Logger.getInstance().info("Inventory Adapter initialized with legacy system");
    }

    @Override
    public boolean checkStock(String sku, int quantity) {
        int available = legacySystem.queryStockLevel(sku);
        return available >= quantity;
    }

    @Override
    public void reserveStock(String sku, int quantity) {
        if (!legacySystem.decrementStock(sku, quantity)) {
            Logger.getInstance().warn("Failed to reserve stock for " + sku);
            throw new IllegalStateException("Insufficient stock for " + sku);
        }
        Logger.getInstance().info("Reserved " + quantity + " units of " + sku);
    }

    @Override
    public void releaseStock(String sku, int quantity) {
        legacySystem.incrementStock(sku, quantity);
        Logger.getInstance().info("Released " + quantity + " units of " + sku);
    }

    @Override
    public int getAvailableQuantity(String sku) {
        return legacySystem.queryStockLevel(sku);
    }

    public void showInventoryReport() {
        legacySystem.printInventoryReport();
    }
}

class StockValidator {
    private final InventoryManager inventory;

    public StockValidator(InventoryManager inventory) {
        this.inventory = inventory;
    }

    public boolean validateOrder(Order order) {
        Logger.getInstance().info("Validating stock for order " + order.orderId);

        for (OrderItem item : order.items) {
            String sku = item.product.sku;
            int needed = item.quantity;
            int available = inventory.getAvailableQuantity(sku);

            if (available < needed) {
                Logger.getInstance().warn("Insufficient stock: " + sku +
                        " (need " + needed + ", have " + available + ")");
                return false;
            }
        }

        Logger.getInstance().info("Stock validation passed");
        return true;
    }

    public void reserveOrderStock(Order order) {
        for (OrderItem item : order.items) {
            inventory.reserveStock(item.product.sku, item.quantity);
        }
    }
}