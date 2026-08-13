package dev.excsi.urlshortener.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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

    @Column(name = "total_clicks", nullable = false)
    private int totalClicks = 0;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private UserEntity owner;

    protected UrlEntity() {}

    public UrlEntity(String shortCode, String longUrl, UserEntity owner) {
        this.shortUrl = shortCode;
        this.longUrl = longUrl;
        this.owner = owner;
    }

    public String getShortUrl() {
        return shortUrl;
    }

    public String getLongUrl() {
        return longUrl;
    }

    public LocalDateTime getDateCreated() {
        return dateCreated;
    }

    public int getTotalClicks() {
        return totalClicks;
    }

    public Long getOwnerId() {
        return owner.getId();
    }
}
