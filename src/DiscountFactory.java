class DiscountFactory {
    public static Discount createDiscount(int choice) {
        switch (choice) {
            case 1:
                return new NoDiscount();
            case 2:
                return new StudentDiscount();
            case 3:
                return new SeniorDiscount();
            case 4:
                return new LoyaltyDiscount();
            case 5:
                return new PregnantWomenDiscount();
            default:
                return new NoDiscount();
        }
    }

    public static void displayDiscountMenu() {
        System.out.println("\nSelect discount type:");
        System.out.println("1. No Discount");
        System.out.println("2. Student Discount (15% off)");
        System.out.println("3. Senior Discount (10% off)");
        System.out.println("4. Loyalty Member (20% off)");
        System.out.println("5. Pregnant Women Discount (12% off)");
        System.out.print("Choice: ");
    }
}