package eventplanner.models;

public class Vendor {
    private int id;
    private String name;

    public Vendor (int id, String name) {
        this.name = name;
        this.id = id;
    }

    public int getID() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

}
