import java.time.*;

class Logger {
    private static volatile Logger instance;
    private Logger() { }

    public static Logger getInstance() {
        if (instance == null) {
            synchronized (Logger.class) {
                if (instance == null) instance = new Logger();
            }
        }
        return instance;
    }

    public void info(String msg) {
        System.out.println("[" + LocalTime.now() + "] [INFO] " + msg);
    }

    public void warn(String msg) {
        System.out.println("[" + LocalTime.now() + "] [WARN] " + msg);
    }
}

class Product {
    final String sku;
    final String name;
    final double unitPrice;

    Product(String sku, String name, double unitPrice) {
        this.sku = sku;
        this.name = name;
        this.unitPrice = unitPrice;
    }
}

class OrderItem {
    final Product product;
    final int quantity;

    OrderItem(Product product, int quantity) {
        if (quantity <= 0) throw new IllegalArgumentException("Quantity must be > 0");
        this.product = product;
        this.quantity = quantity;
    }

    double lineTotal() {
        return product.unitPrice * quantity;
    }

    @Override
    public String toString() {
        return product.name + " x" + quantity + " = " + String.format("%.2f", lineTotal());
    }
}