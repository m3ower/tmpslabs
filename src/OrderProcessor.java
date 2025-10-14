class OrderProcessor {
    private DiscountStrategy discountStrategy;

    public OrderProcessor(DiscountStrategy discountStrategy) {
        this.discountStrategy = discountStrategy;
    }

    public void processOrder(Order order) {
        double finalPrice = discountStrategy.applyDiscount(order.getBasePrice());

        System.out.println("\n========== ORDER RECEIPT ==========");
        System.out.println("Customer: " + order.getCustomerName());
        System.out.println("Coffee: " + order.getCoffeeType());
        System.out.println("Base Price: $" + String.format("%.2f", order.getBasePrice()));
        System.out.println("Discount Applied: " + discountStrategy.getDiscountName());
        System.out.println("Final Price: $" + String.format("%.2f", finalPrice));
        System.out.println("===================================\n");
    }
}