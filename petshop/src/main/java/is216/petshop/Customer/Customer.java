package is216.petshop.Customer;

import java.util.Date;

public class Customer {
    private int id;
    private String name;
    private String phone;
    private String email;
    private String address;
    private String partnerType; // KHACH_HANG / NHA_CUNG_CAP / CA_HAI
    private int loyaltyPoints;
    private Date joinDate;
    private String type; // Đồng/Bạc/Vàng

    public Customer() {}

    public Customer(int id, String name, String phone, String email, String address, String partnerType, int loyaltyPoints, Date joinDate, String type) {
        this.id = id;
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.address = address;
        this.partnerType = partnerType;
        this.loyaltyPoints = loyaltyPoints;
        this.joinDate = joinDate;
        this.type = type;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getPartnerType() { return partnerType; }
    public void setPartnerType(String partnerType) { this.partnerType = partnerType; }

    public int getLoyaltyPoints() { return loyaltyPoints; }
    public void setLoyaltyPoints(int loyaltyPoints) { this.loyaltyPoints = loyaltyPoints; }

    public Date getJoinDate() { return joinDate; }
    public void setJoinDate(Date joinDate) { this.joinDate = joinDate; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
}
