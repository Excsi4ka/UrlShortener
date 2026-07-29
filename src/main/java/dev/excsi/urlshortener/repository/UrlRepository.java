package dev.excsi.urlshortener.repository;

import dev.excsi.urlshortener.entity.UrlEntity;
import org.springframework.data.repository.CrudRepository;

public interface UrlRepository extends CrudRepository<UrlEntity, String> {

}
