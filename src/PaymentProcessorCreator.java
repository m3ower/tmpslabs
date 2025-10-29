abstract class PaymentProcessorCreator {
    // The Factory Method
    protected abstract PaymentProcessor createProcessor();

    // Template operation using the product created by the factory method
    public boolean pay(Order order) {
        PaymentProcessor p = createProcessor();
        Logger.getInstance().info("Using processor: " + p.name());
        return p.process(order);
    }
}

class CreditCardProcessorCreator extends PaymentProcessorCreator {
    @Override
    protected PaymentProcessor createProcessor() { return new CreditCardProcessor(); }
}

class PaypalProcessorCreator extends PaymentProcessorCreator {
    @Override
    protected PaymentProcessor createProcessor() { return new PaypalProcessor(); }
}

// ========== Shipping ==========
// Products created by the abstract factory
interface ShippingLabel { String render(); }
interface PackageBox   { String spec();    }

// Concrete products (DHL)
class DHLLabel implements ShippingLabel {
    private final String to;
    DHLLabel(String to) { this.to = to; }
    public String render() { return "DHL Label → " + to; }
}

class DHLBox implements PackageBox {
    public String spec() { return "DHL Standard Box 40x30x20cm"; }
}

// Concrete products (FedEx)
class FedExLabel implements ShippingLabel {
    private final String to;
    FedExLabel(String to) { this.to = to; }
    public String render() { return "FedEx Label → " + to; }
}

class FedExBox implements PackageBox {
    public String spec() { return "FedEx Pak 35x28x5cm"; }
}