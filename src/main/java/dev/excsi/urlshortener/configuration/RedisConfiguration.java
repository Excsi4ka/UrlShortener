package dev.excsi.urlshortener.configuration;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.cache.RedisCacheManager.RedisCacheManagerBuilder;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

@Configuration
@EnableCaching
public class RedisConfiguration {

    @Bean
    public RedisCacheManager getCacheManager(RedisConnectionFactory connectionFactory) {
        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
                .disableCachingNullValues()
                .entryTtl(Duration.ofMinutes(10))
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(StringRedisSerializer.UTF_8))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(StringRedisSerializer.UTF_8));

        return RedisCacheManagerBuilder.fromConnectionFactory(connectionFactory)
                .withCacheConfiguration("redirects", config) //redirect cache bucket config
                .build();
    }
}
