package eventplanner.models;

public class Event {
    private int id;
    private String name;
    private String startTime;
    private String endTime;
    private double price;
    private int venueId;
    private String venueName;
    private String venueAddress;
    private int maxCapacity;
    private String registrationDeadline;
    private boolean isPublic;
    private boolean paymentStatus;

    public Event(int id, String name, String startTime, String endTime, double price, int venueId,
                 String venueName, String venueAddress, int maxCapacity, String registrationDeadline,
                 boolean isPublic, boolean paymentStatus) {
        this.id = id;
        this.name = name;
        this.startTime = startTime;
        this.endTime = endTime;
        this.price = price;
        this.venueId = venueId;
        this.venueName = venueName;
        this.venueAddress = venueAddress;
        this.maxCapacity = maxCapacity;
        this.registrationDeadline = registrationDeadline;
        this.isPublic = isPublic;
        this.paymentStatus = paymentStatus;
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

    public double getPrice() {
        return price;
    }

    public int getVenueId() {
        return venueId;
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

    public boolean isPaymentCompleted() {
        return paymentStatus;
    }

    public boolean isPublic() {
        return isPublic;
    }

    public void setPublic(boolean aPublic) {
        isPublic = aPublic;
    }

    public boolean isPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(boolean paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    @Override
    public String toString() {
        return "Event(" + id + ", " + name + ", " + startTime + ", " + endTime + ", " + price
                    + ", " + venueName + ", " + venueAddress + ", " + maxCapacity + ", " + registrationDeadline;
    }
}
