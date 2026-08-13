package dev.excsi.urlshortener.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "country_analytics")
public class CountryBucketEntity {

    @EmbeddedId
    private CountryBucketCompositePrimaryKey id;

    @Column(name = "click_count", nullable = false)
    private int clicks;

    protected CountryBucketEntity() {}

    public CountryBucketEntity(CountryBucketCompositePrimaryKey id, int clicks) {
        this.id = id;
        this.clicks = clicks;
    }

    public CountryBucketCompositePrimaryKey getId() {
        return id;
    }

    public int getClicks() {
        return clicks;
    }
}
