package com.eatlah.eatlah.models;

public class FoodItem {

    private String _id;
    private String name;
    private String price;
    private String image_path;
    private String description;

    FoodItem(String _id, String name, String price, String image_path, String description) {
        this.image_path = image_path;
        this._id = _id;
        this.price = price;
        this.description = description;
        this.name = name;
    }

    FoodItem() {}

    public String getImage_path() {
        return image_path;
    }

    public String get_id() {
        return _id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getPrice() {
        return price;
    }

    public void setImage_path(String image_path) {
        this.image_path = image_path;
    }

    public void set_id(String _id) {
        this._id = _id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setPrice(String price) {
        this.price = price;
    }
}
