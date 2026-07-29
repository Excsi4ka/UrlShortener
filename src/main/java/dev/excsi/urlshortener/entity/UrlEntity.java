package dev.excsi.urlshortener.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "urls")
public class UrlEntity {

    @Id
    @Column(name = "short_url", nullable = false, length = 7)
    private String shortUrl;

    @Column(name = "long_url", nullable = false, length = 2048)
    private String longUrl;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    private LocalDateTime dateCreated;

    protected UrlEntity() {}

    public UrlEntity(String shortCode, String longUrl) {
        this.shortUrl = shortCode;
        this.longUrl = longUrl;
    }

    public String getShortCode() {
        return shortUrl;
    }

    public String getLongUrl() {
        return longUrl;
    }

    public LocalDateTime getDateCreated() {
        return dateCreated;
    }
}

