package is216.petshop.util;

public class TestInsert {
    public static void main(String[] args) {
        try {
            new is216.petshop.Booking.BookingDAO().ensureTablesExist();
            System.out.println("✅ Tables ensured successfully!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
