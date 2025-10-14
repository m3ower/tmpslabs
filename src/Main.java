import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("Welcome to SOLID Coffee Shop!");

        System.out.print("Enter customer name: ");
        String customerName = scanner.nextLine();

        System.out.println("\nSelect your coffee:");
        System.out.println("1. Espresso - $3.00");
        System.out.println("2. Cappuccino - $4.50");
        System.out.println("3. Latte - $4.00");
        System.out.print("Choice: ");
        int coffeeChoice = scanner.nextInt();

        String coffeeType = "";
        double basePrice = 0;

        switch (coffeeChoice) {
            case 1:
                coffeeType = "Espresso";
                basePrice = 3.00;
                break;
            case 2:
                coffeeType = "Cappuccino";
                basePrice = 4.50;
                break;
            case 3:
                coffeeType = "Latte";
                basePrice = 4.00;
                break;
            default:
                coffeeType = "Latte";
                basePrice = 4.00;
        }

        Order order = new Order(customerName, coffeeType, basePrice);

        System.out.println("\nSelect discount type:");
        System.out.println("1. No Discount");
        System.out.println("2. Student Discount (15% off)");
        System.out.println("3. Loyalty Member (20% off)");
        System.out.println("4. Senior Discount (10% off)");
        System.out.print("Choice: ");
        int discountChoice = scanner.nextInt();

        DiscountStrategy discount;
        switch (discountChoice) {
            case 2:
                discount = new StudentDiscount();
                break;
            case 3:
                discount = new LoyaltyDiscount();
                break;
            case 4:
                discount = new SeniorDiscount();
                break;
            default:
                discount = new NoDiscount();
        }

        OrderProcessor processor = new OrderProcessor(discount);
        processor.processOrder(order);

        scanner.close();
    }
}