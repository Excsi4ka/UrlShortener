package dev.excsi.urlshortener.exception;

public class UrlNotFoundException extends RuntimeException {

    private final String shortUrl;

    public UrlNotFoundException(String shortUrl) {
        this.shortUrl = shortUrl;
    }

    public String getShortUrl() {
        return shortUrl;
    }
}
