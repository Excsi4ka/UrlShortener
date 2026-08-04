package dev.excsi.urlshortener.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "click_analytics")
public class ClickBucketEntity {

    @EmbeddedId
    private ClickBucketCompositePrimaryKey id;

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

    public int getClicks() {
        return clicks;
    }
}
