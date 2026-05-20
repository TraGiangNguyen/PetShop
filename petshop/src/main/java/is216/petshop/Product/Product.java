package is216.petshop.Product;

public class Product {
    private int Id;
    private String name;
    private String category;
    private long price;
    private int stock;
    private String imageUrl;

    // Constructor đầy đủ
    public Product(){
       this.name = "";
        this.category = "";
        this.price = 0;
        this.stock = 0;
        this.imageUrl = "";
    }
    public Product(String name, String category, long price, int stock, String imageUrl) {
        this.name = name;
        this.category = category;
        this.price = price;
        this.stock = stock;
        this.imageUrl = imageUrl;
    }

    // Các hàm Getters để lấy dữ liệu ra
        public int getId() { return Id; } 
    public String getName() { return name; }
    public String getCategory() { return category; }
    public long getPrice() { return price; }
    public int getStock() { return stock; }
    public String getImageUrl() { return imageUrl; }
    public void setId(int s){ Id = s;    }
    public void setName(String s){ name = s;    }
    public void setCategory(String s){ category = s;    }
    public void setPrice(long s){ price = s;    }
    public void setStock(int s){ stock = s;    }
    
}