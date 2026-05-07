/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package is216.petshop.model;

/**
 *
 * @author Admin
 */
public class Product {
        private int id;
        private String name;
        private String type;
        private double price;
        private int stock;

    public Product(int id, String name, String type, double price, int stock) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.price = price;
        this.stock = stock;
    }

    public Product(String name, String type, double price) {
        this.name = name;
        this.type = type;
        this.price = price;
    }
    
    public int getId() { return id; }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public double getPrice() {
        return price;
    }
    
    public int getStock() { 
        return stock; 
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setPrice(double price) {
        this.price = price;
    }
    
    public void setStock(int stock) { 
        this.stock = stock; 
    }
}
