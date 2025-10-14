class Order {
    private String customerName;
    private String coffeeType;
    private double basePrice;

    public Order(String customerName, String coffeeType, double basePrice) {
        this.customerName = customerName;
        this.coffeeType = coffeeType;
        this.basePrice = basePrice;
    }

    public String getCustomerName() {
        return customerName;
    }

    public String getCoffeeType() {
        return coffeeType;
    }

    public double getBasePrice() {
        return basePrice;
    }
}