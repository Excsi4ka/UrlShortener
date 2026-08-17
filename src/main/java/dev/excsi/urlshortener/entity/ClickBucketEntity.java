package dev.excsi.urlshortener.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "click_analytics")
public class ClickBucketEntity {

    @EmbeddedId
    private ClickBucketCompositePrimaryKey id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "short_url", nullable = false, insertable = false, updatable = false)
    private UrlEntity url;

    @Column(name = "click_count", nullable = false)
    private int clicks;

    protected ClickBucketEntity() {}

    public ClickBucketEntity(ClickBucketCompositePrimaryKey id, int clicks) {
        this.id = id;
        this.clicks = clicks;
    }

    public ClickBucketCompositePrimaryKey getId() {
        return id;
    }

    public UrlEntity getUrl() {
        return url;
    }

    public int getClicks() {
        return clicks;
    }
}
