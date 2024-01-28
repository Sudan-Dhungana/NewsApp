package com.infinix.NewsApp;

public class CategoryRVModel {

    private final String category;
    private final String categoryImageUrl;

    public CategoryRVModel(String category, String categoryImageUrl) {
        this.category = category;
        this.categoryImageUrl = categoryImageUrl;
    }

    public String getCategory() {
        return category;
    }

    public String getCategoryImageUrl() {
        return categoryImageUrl;
    }

}
