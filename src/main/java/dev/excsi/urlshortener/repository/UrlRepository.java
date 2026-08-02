package dev.excsi.urlshortener.repository;

import dev.excsi.urlshortener.entity.UrlEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UrlRepository extends JpaRepository<UrlEntity, String> {

}
