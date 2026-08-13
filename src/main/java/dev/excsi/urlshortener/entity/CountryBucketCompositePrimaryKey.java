package dev.excsi.urlshortener.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class CountryBucketCompositePrimaryKey implements Serializable {

    @Column(name = "short_url", length = 7, nullable = false)
    private String shortUrl;

    @Column(name = "country_code", length = 2, nullable = false)
    private String countryCode;

    protected CountryBucketCompositePrimaryKey() {}

    public CountryBucketCompositePrimaryKey(String shortUrl, String countryCode) {
        this.shortUrl = shortUrl;
        this.countryCode = countryCode;
    }

    public String getShortUrl() {
        return shortUrl;
    }

    public String getCountryCode() {
        return countryCode;
    }

    @Override
    public int hashCode() {
        return Objects.hash(shortUrl, countryCode);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof CountryBucketCompositePrimaryKey key) {
            return key.getShortUrl().equals(shortUrl)
                    && key.getCountryCode().equals(countryCode);
        }
        return false;
    }
}
