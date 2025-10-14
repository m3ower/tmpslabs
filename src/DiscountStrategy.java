interface DiscountStrategy {
    double applyDiscount(double price);
    String getDiscountName();
}

class NoDiscount implements DiscountStrategy {
    @Override
    public double applyDiscount(double price) {
        return price;
    }

    @Override
    public String getDiscountName() {
        return "No Discount";
    }
}

class StudentDiscount implements DiscountStrategy {
    @Override
    public double applyDiscount(double price) {
        return price * 0.85; // 15% off
    }

    @Override
    public String getDiscountName() {
        return "Student Discount (15% off)";
    }
}

class LoyaltyDiscount implements DiscountStrategy {
    @Override
    public double applyDiscount(double price) {
        return price * 0.80; // 20% off
    }

    @Override
    public String getDiscountName() {
        return "Loyalty Member Discount (20% off)";
    }
}

class SeniorDiscount implements DiscountStrategy {
    @Override
    public double applyDiscount(double price) {
        return price * 0.90; // 10% off
    }

    @Override
    public String getDiscountName() {
        return "Senior Discount (10% off)";
    }
}