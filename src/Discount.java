abstract class Discount {
    protected double discountRate;
    protected String discountName;

    public double applyDiscount(double price) {
        return price * (1 - discountRate);
    }

    public String getDiscountName() {
        return discountName;
    }
}

class NoDiscount extends Discount {
    public NoDiscount() {
        this.discountRate = 0.0;
        this.discountName = "No Discount";
    }
}

class StudentDiscount extends Discount {
    public StudentDiscount() {
        this.discountRate = 0.15;
        this.discountName = "Student Discount (15% off)";
    }
}

class SeniorDiscount extends Discount {
    public SeniorDiscount() {
        this.discountRate = 0.10;
        this.discountName = "Senior Discount (10% off)";
    }
}

class LoyaltyDiscount extends Discount {
    public LoyaltyDiscount() {
        this.discountRate = 0.20;
        this.discountName = "Loyalty Member Discount (20% off)";
    }
}

class PregnantWomenDiscount extends Discount {
    public PregnantWomenDiscount() {
        this.discountRate = 0.12;
        this.discountName = "Pregnant Women Discount (12% off)";
    }
}