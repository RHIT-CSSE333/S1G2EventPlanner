package eventplanner.models;

public class Venue {
    private int id;
    private String name;
    private int maxCapacity;
    private PricingType pricingType;
    private int price;
    private String state;
    private String city;
    private String streetAddress;
    private int zipCode;   // TODO: Change to String in database

    private enum PricingType {
        Hourly,
        Daily
    }

    public Venue(int id, String name, int maxCapacity, int pricingType, int price, String state, String city, String streetAddress, int zipCode) {
        this.id = id;
        this.name = name;
        this.maxCapacity = maxCapacity;
        this.pricingType = fromInt(pricingType);
        this.price = price;
        this.state = state;
        this.city = city;
        this.streetAddress = streetAddress;
        this.zipCode = zipCode;
    }
    
    public static PricingType fromInt(int pricingType) {
        switch (pricingType) {
            case 0:
                return PricingType.Hourly;
            case 1:
                return PricingType.Daily;
            default:
                throw new IllegalArgumentException("Invalid pricing type: " + pricingType);
        }
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getMaxCapacity() {
        return maxCapacity;
    }

    public PricingType getPricingType() {
        return pricingType;
    }

    public int getPrice() {
        return price;
    }

    public String getState() {
        return state;
    }

    public String getCity() {
        return city;
    }

    public String getStreetAddress() {
        return streetAddress;
    }

    public int getZipCode() {
        return zipCode;
    }

    public String getAddress() {
        return streetAddress + ", " + city + ", " + state + " " + zipCode;
    }
}
