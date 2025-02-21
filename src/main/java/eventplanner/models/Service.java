package eventplanner.models;

public class Service {
    private int id;
    private String name;
    private String description; 
    private double price;
    private int vendorId;
    private String vendorName;

    public Service(int id, String name, String description, double price, int vendorId, String vendorName) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.vendorId = vendorId;
        this.vendorName = vendorName;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public double getPrice() {
        return price;
    }

    public int getVendorId() {
        return vendorId;
    }

    public String getVendorName() {
        return vendorName;
    }
}
