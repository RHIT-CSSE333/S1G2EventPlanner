package eventplanner.models;

public class User {
    private int Id;
    private String Email;
    private String PhoneNo;
    private String FirstName;
    private String Minit;
    private String LastName;
    private String DOB;

    public User (int Id, String Email, String PhoneNo, String FirstName, String Minit, String LastName, String DOB) {
        this.Id = Id;
        this.Email = Email;
        this.PhoneNo = PhoneNo;
        this.FirstName = FirstName;
        this.Minit = Minit;
        this.LastName = LastName;
        this.DOB = DOB;
    }

    public int getId() {
        return Id;
    }

    public String getEmail() {
        return Email;
    }

    public String getPhoneNo() {
        return PhoneNo;
    }

    public String getFullName() {
        if(Minit != null && !Minit.isEmpty()) {
            return FirstName + " " + Minit + " " + LastName;
        }
        return FirstName + " " + LastName;
    }

    public String getFirstName() {
        return FirstName;
    }

    public String getMinit() {
        return Minit;
    }

    public String getLastName() {
        return LastName;
    }

    public String getDOB() {
        return DOB;
    }
}


