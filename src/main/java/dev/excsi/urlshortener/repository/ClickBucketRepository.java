package dev.excsi.urlshortener.repository;

import dev.excsi.urlshortener.entity.ClickBucketEntity;
import dev.excsi.urlshortener.entity.ClickBucketCompositePrimaryKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface ClickBucketRepository extends JpaRepository<ClickBucketEntity, ClickBucketCompositePrimaryKey> {

    @Query(value = """
        select bucket from ClickBucketEntity bucket
        where bucket.id.shortUrl = :shortUrl
        and bucket.id.bucketDate >= :date
    """)
    List<ClickBucketEntity> getBucketsSince(@Param("shortUrl") String shortUrl, @Param("date") LocalDate date);

    @Modifying
    @Query(value = """
        insert into click_analytics (short_url, bucket_date, click_count)
        values (:shortUrl, :bucketDate, :clicks)
        on conflict (short_url, bucket_date)
        do update set click_count = click_analytics.click_count + :clicks
    """, nativeQuery = true)
    void incrementClicks(@Param("shortUrl") String shortUrl, @Param("bucketDate") LocalDate bucketDate, @Param("clicks") int clicks);

    @Modifying
    @Query(value = """
        delete from ClickBucketEntity bucket
        where bucket.id.bucketDate <= :date
    """)
    void deleteOlderThan(@Param("date") LocalDate date);
}
