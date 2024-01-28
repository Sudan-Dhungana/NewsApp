package com.infinix.NewsApp;

import java.util.ArrayList;

public class NewsModel {

    private final ArrayList<Articles> articles;

    public NewsModel(ArrayList<Articles> articles) {
        this.articles = articles;
    }

    public ArrayList<Articles> getArticles() {
        return articles;
    }

}
