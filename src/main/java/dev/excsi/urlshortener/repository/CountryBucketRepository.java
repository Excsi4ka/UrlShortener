package dev.excsi.urlshortener.repository;

import dev.excsi.urlshortener.entity.CountryBucketCompositePrimaryKey;
import dev.excsi.urlshortener.entity.CountryBucketEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CountryBucketRepository extends JpaRepository<CountryBucketEntity, CountryBucketCompositePrimaryKey> {

    @Query("""
        select bucket from CountryBucketEntity bucket
        where bucket.id.shortUrl = :shortUrl
        order by bucket.clicks desc
    """)
    List<CountryBucketEntity> getBuckets(@Param("shortUrl") String shortUrl);

    @Modifying
    @Query(value = """
        insert into country_analytics (short_url, country_code, click_count)
        values (:shortUrl, :countryCode, :clicks)
        on conflict (short_url, country_code)
        do update set click_count = country_analytics.click_count + :clicks
    """, nativeQuery = true)
    void incrementClicks(@Param("shortUrl") String shortUrl, @Param("countryCode") String countryCode, @Param("clicks") int clicks);
}
