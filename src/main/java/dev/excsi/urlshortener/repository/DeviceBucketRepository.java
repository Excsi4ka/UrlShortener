package dev.excsi.urlshortener.repository;

import dev.excsi.urlshortener.entity.DeviceBucketCompositePrimaryKey;
import dev.excsi.urlshortener.entity.DeviceBucketEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface DeviceBucketRepository extends JpaRepository<DeviceBucketEntity, DeviceBucketCompositePrimaryKey> {

    @Query("""
        select bucket from DeviceBucketEntity bucket
        where bucket.id.shortUrl = :shortUrl
        order by bucket.clicks desc
    """)
    List<DeviceBucketEntity> getBuckets(@Param("shortUrl") String shortUrl);

    @Modifying
    @Query(value = """
        insert into device_analytics (short_url, device_type, click_count)
        values (:shortUrl, :deviceType, :clicks)
        on conflict (short_url, device_type)
        do update set click_count = device_analytics.click_count + :clicks
    """, nativeQuery = true)
    void incrementClicks(@Param("shortUrl") String shortUrl, @Param("deviceType") String deviceType, @Param("clicks") int clicks);
}
