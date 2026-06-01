package is216.petshop.Product;

public class Product {
    private int Id;
    private String name;
    private String category;
    private long price;
    private int stock;
    private String imageUrl;
    
    // New DB fields
    private String unit;        // DONVITINH
    private double tax;         // THUE
    private String barcode;     // MAVACH
    private String origin;      // XUATXU
    private String suitableFor; // PHUHOP
    private int activeBuy;      // COTHEMUA (1 or 0)
    private int activeSell;     // COTHEBAN (1 or 0)
    private int activePos;      // POS (1 or 0)

    // Constructor đầy đủ
    public Product(){
        this.name = "";
        this.category = "";
        this.price = 0;
        this.stock = 0;
        this.imageUrl = "";
        this.unit = "Cái";
        this.tax = 0.0;
        this.barcode = "";
        this.origin = "";
        this.suitableFor = "";
        this.activeBuy = 1;
        this.activeSell = 1;
        this.activePos = 1;
    }
    public Product(String name, String category, long price, int stock, String imageUrl) {
        this.name = name;
        this.category = category;
        this.price = price;
        this.stock = stock;
        this.imageUrl = imageUrl;
        this.unit = "Cái";
        this.tax = 0.0;
        this.barcode = "";
        this.origin = "";
        this.suitableFor = "";
        this.activeBuy = 1;
        this.activeSell = 1;
        this.activePos = 1;
    }

    // Các hàm Getters để lấy dữ liệu ra
    public int getId() { return Id; } 
    public String getName() { return name; }
    public String getCategory() { return category; }
    public long getPrice() { return price; }
    public int getStock() { return stock; }
    public String getImageUrl() { return imageUrl; }
    
    public String getUnit() { return unit; }
    public double getTax() { return tax; }
    public String getBarcode() { return barcode; }
    public String getOrigin() { return origin; }
    public String getSuitableFor() { return suitableFor; }
    public int getActiveBuy() { return activeBuy; }
    public int getActiveSell() { return activeSell; }
    public int getActivePos() { return activePos; }

    public void setId(int s){ Id = s;    }
    public void setName(String s){ name = s;    }
    public void setCategory(String s){ category = s;    }
    public void setPrice(long s){ price = s;    }
    public void setStock(int s){ stock = s;    }
    public void setImageUrl(String s) { imageUrl = s; }
    
    public void setUnit(String s) { unit = s; }
    public void setTax(double s) { tax = s; }
    public void setBarcode(String s) { barcode = s; }
    public void setOrigin(String s) { origin = s; }
    public void setSuitableFor(String s) { suitableFor = s; }
    public void setActiveBuy(int s) { activeBuy = s; }
    public void setActiveSell(int s) { activeSell = s; }
    public void setActivePos(int s) { activePos = s; }
    
}