package dev.excsi.urlshortener.repository;

import dev.excsi.urlshortener.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<UserEntity, Long> {

    Optional<UserEntity> findByProviderAndProviderUserId(String provider, String providerUserId);
}
