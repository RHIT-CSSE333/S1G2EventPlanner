package eventplanner.models;

public class Event {
    private int id;
    private String name;
    private String startTime;
    private String endTime;
    private int price;
    private String venueName;
    private String venueAddress;
    private int maxCapacity;
    private String registrationDeadline;

    public Event(int id, String name, String startTime, String endTime, int price,
                 String venueName, String venueAddress, int maxCapacity, String registrationDeadline) {
        this.id = id;
        this.name = name;
        this.startTime = startTime;
        this.endTime = endTime;
        this.price = price;
        this.venueName = venueName;
        this.venueAddress = venueAddress;
        this.maxCapacity = maxCapacity;
        this.registrationDeadline = registrationDeadline;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getStartTime() {
        return startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public int getPrice() {
        return price;
    }

    public String getVenueName() {
        return venueName;
    }

    public String getVenueAddress() {
        return venueAddress;
    }

    public int getMaxCapacity() {
        return maxCapacity;
    }

    public String getRegistrationDeadline() {
        return registrationDeadline;
    }

    @Override
    public String toString() {
        return "Event(" + id + ", " + name + ", " + startTime + ", " + endTime + ", " + price
                    + ", " + venueName + ", " + venueAddress + ", " + maxCapacity + ", " + registrationDeadline;
    }
}
