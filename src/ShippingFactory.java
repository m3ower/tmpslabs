interface ShippingFactory {
    ShippingLabel createLabel(String destination);
    PackageBox createBox();
}

class DHLFactory implements ShippingFactory {
    public ShippingLabel createLabel(String destination) { return new DHLLabel(destination); }
    public PackageBox createBox() { return new DHLBox(); }
}

class FedExFactory implements ShippingFactory {
    public ShippingLabel createLabel(String destination) { return new FedExLabel(destination); }
    public PackageBox createBox() { return new FedExBox(); }
}