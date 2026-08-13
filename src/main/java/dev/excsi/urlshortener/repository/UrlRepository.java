package dev.excsi.urlshortener.repository;

import dev.excsi.urlshortener.entity.UrlEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UrlRepository extends JpaRepository<UrlEntity, String> {

    @Modifying
    @Query("""
        update UrlEntity url
        set url.totalClicks = url.totalClicks + :clicks
        where url.shortUrl = :shortUrl
    """)
    int incrementTotalClickCount(@Param("shortUrl") String shortUrl, @Param("clicks") int clicks);

    @Query("""
        select url from UserEntity user
        join user.urls url
        where user.id = :ownerId
        order by url.dateCreated desc
    """)
    List<UrlEntity> findAllByOwnerId(@Param("ownerId") Long ownerId);

    @Query("""
        select url from UserEntity user
        join user.urls url
        where user.id = :ownerId
        and url.shortUrl = :shortUrl
    """)
    Optional<UrlEntity> findOwnedAnalyticsUrl(@Param("shortUrl") String shortUrl, @Param("ownerId") Long ownerId);
}
