package dev.excsi.urlshortener.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;

@Embeddable
public class ClickBucketCompositePrimaryKey implements Serializable {

    @Column(name = "short_url", length = 7, nullable = false)
    private String shortUrl;

    @Column(name = "bucket_date", nullable = false)
    private LocalDate bucketDate;

    protected ClickBucketCompositePrimaryKey() {}

    public ClickBucketCompositePrimaryKey(String shortUrl, LocalDate bucketStart) {
        this.shortUrl = shortUrl;
        this.bucketDate = bucketStart;
    }

    public LocalDate getBucketDate() {
        return bucketDate;
    }

    public String getShortUrl() {
        return shortUrl;
    }

    @Override
    public int hashCode() {
        return Objects.hash(shortUrl, bucketDate);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ClickBucketCompositePrimaryKey key) {
            return key.getBucketDate().equals(bucketDate)
                    && key.getShortUrl().equals(shortUrl);
        }
        return false;
    }
}
