package com.infinix.NewsApp;

public class Articles {
    private final String title;
    private final String description;
    private final String urlToImage;
    private final String url;
    private final String content;
    private final String publishedAt;

    public Articles(String title, String description, String urlToImage, String url, String content, String publishedAt) {
        this.title = title;
        this.description = description;
        this.urlToImage = urlToImage;
        this.url = url;
        this.content = content;
        this.publishedAt = publishedAt;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getUrlToImage() {
        return urlToImage;
    }

    public String getUrl() {
        return url;
    }

    public String getContent() {
        return content;
    }

    public String getPublishedAt() {
        return publishedAt;
    }
}
