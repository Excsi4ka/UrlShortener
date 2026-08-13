package dev.excsi.urlshortener.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class DeviceBucketCompositePrimaryKey implements Serializable {

    @Column(name = "short_url", length = 7, nullable = false)
    private String shortUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "device_type", length = 20, nullable = false)
    private DeviceType deviceType;

    protected DeviceBucketCompositePrimaryKey() {}

    public DeviceBucketCompositePrimaryKey(String shortUrl, DeviceType deviceType) {
        this.shortUrl = shortUrl;
        this.deviceType = deviceType;
    }

    public String getShortUrl() {
        return shortUrl;
    }

    public DeviceType getDeviceType() {
        return deviceType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(shortUrl, deviceType);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof DeviceBucketCompositePrimaryKey key) {
            return key.getShortUrl().equals(shortUrl)
                    && key.getDeviceType() == deviceType;
        }
        return false;
    }
}
