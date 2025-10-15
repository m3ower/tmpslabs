class OrderProcessor {
    private Discount discount; // Depends on abstraction, not concrete class

    public OrderProcessor(Discount discount) {
        this.discount = discount;
    }

    public void processOrder(Order order) {
        double finalPrice = discount.applyDiscount(order.getBasePrice());

        System.out.println("\n========== ORDER RECEIPT ==========");
        System.out.println("Customer: " + order.getCustomerName());
        System.out.println("Coffee: " + order.getCoffeeType());
        System.out.println("Base Price: $" + String.format("%.2f", order.getBasePrice()));
        System.out.println("Discount Applied: " + discount.getDiscountName());
        System.out.println("Final Price: $" + String.format("%.2f", finalPrice));
        System.out.println("===================================\n");
    }
}