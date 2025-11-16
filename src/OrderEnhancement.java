interface OrderComponent {
    double calculateTotal();
    String getDescription();
    Order getBaseOrder();
}

class BasicOrderComponent implements OrderComponent {
    private final Order order;

    public BasicOrderComponent(Order order) {
        this.order = order;
    }

    @Override
    public double calculateTotal() {
        return order.total();
    }

    @Override
    public String getDescription() {
        return "Standard Order";
    }

    @Override
    public Order getBaseOrder() {
        return order;
    }
}

abstract class OrderEnhancementDecorator implements OrderComponent {
    protected OrderComponent wrappedOrder;

    public OrderEnhancementDecorator(OrderComponent order) {
        this.wrappedOrder = order;
    }

    @Override
    public double calculateTotal() {
        return wrappedOrder.calculateTotal();
    }

    @Override
    public String getDescription() {
        return wrappedOrder.getDescription();
    }

    @Override
    public Order getBaseOrder() {
        return wrappedOrder.getBaseOrder();
    }
}

class GiftWrapDecorator extends OrderEnhancementDecorator {
    private static final double GIFT_WRAP_FEE = 5.99;

    public GiftWrapDecorator(OrderComponent order) {
        super(order);
    }

    @Override
    public double calculateTotal() {
        return wrappedOrder.calculateTotal() + GIFT_WRAP_FEE;
    }

    @Override
    public String getDescription() {
        return wrappedOrder.getDescription() + " + Gift Wrapping ($" +
                String.format("%.2f", GIFT_WRAP_FEE) + ")";
    }
}

class InsuranceDecorator extends OrderEnhancementDecorator {
    private static final double INSURANCE_RATE = 0.02; // 2% of order value

    public InsuranceDecorator(OrderComponent order) {
        super(order);
    }

    @Override
    public double calculateTotal() {
        double insuranceFee = wrappedOrder.calculateTotal() * INSURANCE_RATE;
        return wrappedOrder.calculateTotal() + insuranceFee;
    }

    @Override
    public String getDescription() {
        double insuranceFee = wrappedOrder.getBaseOrder().total() * INSURANCE_RATE;
        return wrappedOrder.getDescription() + " + Shipping Insurance ($" +
                String.format("%.2f", insuranceFee) + ")";
    }
}

class ExpressProcessingDecorator extends OrderEnhancementDecorator {
    private static final double EXPRESS_FEE = 9.99;

    public ExpressProcessingDecorator(OrderComponent order) {
        super(order);
    }

    @Override
    public double calculateTotal() {
        return wrappedOrder.calculateTotal() + EXPRESS_FEE;
    }

    @Override
    public String getDescription() {
        return wrappedOrder.getDescription() + " + Express Processing ($" +
                String.format("%.2f", EXPRESS_FEE) + ")";
    }
}

class PrioritySupportDecorator extends OrderEnhancementDecorator {
    private static final double SUPPORT_FEE = 3.99;

    public PrioritySupportDecorator(OrderComponent order) {
        super(order);
    }

    @Override
    public double calculateTotal() {
        return wrappedOrder.calculateTotal() + SUPPORT_FEE;
    }

    @Override
    public String getDescription() {
        return wrappedOrder.getDescription() + " + Priority Support ($" +
                String.format("%.2f", SUPPORT_FEE) + ")";
    }
}