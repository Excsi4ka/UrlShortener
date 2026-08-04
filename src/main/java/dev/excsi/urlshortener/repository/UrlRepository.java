package dev.excsi.urlshortener.repository;

import dev.excsi.urlshortener.entity.UrlEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UrlRepository extends JpaRepository<UrlEntity, String> {

    @Modifying
    @Query("""
        update UrlEntity url
        set url.totalClicks = url.totalClicks + :clicks
        where url.shortUrl = :shortUrl
    """)
    void incrementTotalClickCount(@Param("shortUrl") String shortUrl, @Param("clicks") int clicks);
}
